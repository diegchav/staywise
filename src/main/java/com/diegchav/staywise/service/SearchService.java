package com.diegchav.staywise.service;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.diegchav.staywise.domain.criteria.SearchCriteria;
import com.diegchav.staywise.api.dto.SearchResponse;
import com.diegchav.staywise.domain.document.HotelDocument;
import com.diegchav.staywise.mapper.SearchMapper;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    private static final Map<String, String> SORT_FIELD_MAPPING = Map.of(
            "name", "name.keyword"
    );

    private final ElasticsearchOperations operations;

    public SearchService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public Page<SearchResponse> search(
            String q,
            String city,
            String country,
            Double minRating,
            Double maxRating,
            Pageable pageable
    ) {
        var criteria = new SearchCriteria(
                q,
                city,
                country,
                minRating,
                maxRating
        );

        return search(criteria, pageable);
    }

    public Page<SearchResponse> search(SearchCriteria criteria, Pageable pageable) {
        Query query = buildQuery(criteria);
        NativeQuery nativeQuery = buildNativeQuery(query, pageable);
        return execute(nativeQuery, pageable);
    }

    private Query buildQuery(SearchCriteria criteria) {
        BoolQuery.Builder bool = new BoolQuery.Builder();

        applyFilters(bool, criteria);
        applyFullText(bool, criteria.query());

        Query baseQuery = Query.of(q -> q.bool(bool.build()));

        if (hasText(criteria)) {
            return applyBoosting(baseQuery, criteria.query());
        }

        return baseQuery;
    }

    private void applyFilters(BoolQuery.Builder bool, SearchCriteria criteria) {
        if (criteria.city() != null && !criteria.city().isBlank()) {
            bool.filter(f -> f.term(t -> t.field("city").value(criteria.city())));
        }

        if (criteria.country() != null && !criteria.country().isBlank()) {
            bool.filter(f -> f.term(t -> t.field("country").value(criteria.country())));
        }

        if (criteria.minRating() != null || criteria.maxRating() != null) {
            bool.filter(f -> f.range(r -> r.number(n -> {
                n.field("rating");
                if (criteria.minRating() != null) n.gte(criteria.minRating());
                if (criteria.maxRating() != null) n.lte(criteria.maxRating());
                return n;
            })));
        }
    }

    private void applyFullText(BoolQuery.Builder bool, String query) {
        if (query != null && !query.isBlank()) {
            bool.must(m -> m.match(mm -> mm
                    .field("name")
                    .query(query)
            ));
        }
    }

    private boolean hasText(SearchCriteria criteria) {
        return criteria.query() != null && !criteria.query().isBlank();
    }

    private Query applyBoosting(Query baseQuery, String query) {
        return Query.of(q -> q.functionScore(fs -> fs
                .query(baseQuery)
                .functions(
                        List.of(
                                exactMatchBoost(query),
                                ratingBoost()
                        )
                )
                .scoreMode(FunctionScoreMode.Sum)
                .boostMode(FunctionBoostMode.Multiply)
        ));
    }

    private FunctionScore exactMatchBoost(String query) {
        return FunctionScore.of(fs -> fs
                .weight(2.0)
                .filter(f -> f.term(t -> t
                        .field("name.keyword")
                        .value(query)
                ))
        );
    }

    private FunctionScore ratingBoost() {
        return FunctionScore.of(fs -> fs
                .fieldValueFactor(fvf -> fvf
                        .field("rating")
                        .factor(1.5)
                        .modifier(FieldValueFactorModifier.Sqrt)
                )
        );
    }

    private NativeQuery buildNativeQuery(Query query, Pageable pageable) {
        return NativeQuery.builder()
                .withQuery(query)
                .withPageable(adaptSort(pageable))
                .build();
    }

    private Page<SearchResponse> execute(NativeQuery query, Pageable pageable) {
        var hits = operations.search(query, HotelDocument.class);
        var content = hits.stream()
                .map(SearchHit::getContent)
                .map(SearchMapper::fromDocument)
                .toList();

        return new PageImpl<>(content, pageable, hits.getTotalHits());
    }

    private Pageable adaptSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        var adaptedOrders = pageable.getSort()
                .stream()
                .map(order -> {
                    var property = order.getProperty();
                    property = SORT_FIELD_MAPPING.getOrDefault(property, property);

                    return new Sort.Order(order.getDirection(), property);
                })
                .toList();

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(adaptedOrders)
        );
    }
}
