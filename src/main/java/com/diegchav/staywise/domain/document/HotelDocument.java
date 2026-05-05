package com.diegchav.staywise.domain.document;

import jakarta.persistence.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;

@Document(indexName = "#{@esConfig.getIndexName()}")
public class HotelDocument {
    @Id
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String name;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Date)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    private Instant indexedAt;

    public HotelDocument(String id, String name, String city, String country, Double rating, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.country = country;
        this.rating = rating;
        this.createdAt = createdAt;
        this.indexedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Double getRating() {
        return rating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getIndexedAt() {
        return indexedAt;
    }
}
