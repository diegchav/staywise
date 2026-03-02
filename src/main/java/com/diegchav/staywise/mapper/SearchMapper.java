package com.diegchav.staywise.mapper;

import com.diegchav.staywise.api.dto.SearchResponse;
import com.diegchav.staywise.domain.document.HotelDocument;

public class SearchMapper {
    public static SearchResponse fromDocument(HotelDocument document) {
        return new SearchResponse(
                document.getId(),
                document.getName(),
                document.getCity(),
                document.getCountry(),
                document.getRating()
        );
    }
}
