package com.diegchav.staywise.service;

import co.elastic.clients.elasticsearch._types.query_dsl.*;
import com.diegchav.staywise.api.dto.SearchResponse;
import com.diegchav.staywise.domain.document.HotelDocument;
import com.diegchav.staywise.mapper.SearchMapper;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
        Pageable adaptedPageable = adaptSort(pageable);

        var baseQuery = getQuery(q, city, country, minRating, maxRating);

        var query = NativeQuery.builder()
                .withQuery(baseQuery)
                .withPageable(adaptedPageable)
                .build();

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

    private static Query getQuery(
            String q,
            String city,
            String country,
            Double minRating,
            Double maxRating
    ) {
        var bool = new BoolQuery.Builder();

        if (q != null && !q.isBlank()) {
            bool.must(m -> m.match(match -> match
                    .field("name")
                    .query(q)
            ));
        }

        if (city != null && !city.isBlank()) {
            bool.filter(f -> f.term(t -> t
                    .field("city")
                    .value(city)
            ));
        }

        if (country != null && !country.isBlank()) {
            bool.filter(f -> f.term(t -> t
                    .field("country")
                    .value(country)
            ));
        }

        if (minRating != null || maxRating != null) {
            bool.filter(f -> f.range(r -> r.number(n -> {
                n.field("rating");

                if (minRating != null) {
                    n.gte(minRating);
                }

                if (maxRating != null) {
                    n.lte(maxRating);
                }

                return n;
            })));
        }

        Query finalQuery;

        if (q != null && !q.isBlank()) {
            finalQuery = Query.of(qb -> qb.functionScore(fsb -> fsb
                    .query(inner -> inner.bool(bool.build()))
                    .functions(
                            Arrays.asList(
                                    FunctionScore.of(fs -> fs
                                            .weight(2.0)
                                            .filter(f -> f.term(t -> t
                                                    .field("name.keyword")
                                                    .value(q)
                                            ))
                                    ),
                                    FunctionScore.of(fs -> fs
                                            .fieldValueFactor(fvf -> fvf
                                                    .field("rating")
                                                    .factor(1.5)
                                                    .modifier(FieldValueFactorModifier.Sqrt)
                                            )
                                    )
                            )
                    )
                    .scoreMode(FunctionScoreMode.Sum)
                    .boostMode(FunctionBoostMode.Multiply)
            ));
        } else {
            finalQuery = Query.of(qb -> qb.bool(bool.build()));
        }

        return finalQuery;
    }
}
