package com.diegchav.staywise.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("esConfig")
public class ElasticsearchConfig {
    @Value("${app.hotels.search.index}")
    private String indexName;

    public String getIndexName() {
        return indexName;
    }
}
