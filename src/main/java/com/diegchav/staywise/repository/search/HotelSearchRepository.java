package com.diegchav.staywise.repository.search;

import com.diegchav.staywise.domain.document.HotelDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface HotelSearchRepository extends ElasticsearchRepository<HotelDocument, Long> {
}
