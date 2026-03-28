package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HowIWorkData(
    @JsonProperty("title")
    String title,
    
    @JsonProperty("description")
    String description
) {}
