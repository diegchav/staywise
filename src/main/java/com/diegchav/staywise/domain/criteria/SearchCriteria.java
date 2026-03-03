package com.diegchav.staywise.domain.criteria;

public record SearchCriteria(
        String query,
        String city,
        String country,
        Double minRating,
        Double maxRating
) {}
