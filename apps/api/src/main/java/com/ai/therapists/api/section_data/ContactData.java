package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ContactData(
    @JsonProperty("title")
    String title,
    
    @JsonProperty("description")
    String description,
    
    @JsonProperty("cta_text")
    String ctaText,
    
    @JsonProperty("phone")
    String phone,
    
    @JsonProperty("email")
    String email
) {}
