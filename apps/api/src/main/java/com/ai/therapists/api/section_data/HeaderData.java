package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeaderData(
    @JsonProperty("name")
    String name,
    
    @JsonProperty("role")
    String role,
    
    @JsonProperty("location")
    String location,
    
    @JsonProperty("phone")
    String phone,
    
    @JsonProperty("email")
    String email
) {}
