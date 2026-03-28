package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HeroData(
    @JsonProperty("heading")
    String heading,
    
    @JsonProperty("subheading")
    String subheading
) {}
