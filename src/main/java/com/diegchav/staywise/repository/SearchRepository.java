package com.diegchav.staywise.repository;

import com.diegchav.staywise.domain.document.HotelDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchRepository extends ElasticsearchRepository<HotelDocument, Long> {
}
