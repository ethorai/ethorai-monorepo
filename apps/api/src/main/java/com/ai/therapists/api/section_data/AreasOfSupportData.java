package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AreasOfSupportData(
    @JsonProperty("title")
    String title,
    
    @JsonProperty("items")
    List<String> items
) {}
