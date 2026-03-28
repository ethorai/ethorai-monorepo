package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SessionFormatsData(
    @JsonProperty("title")
    String title,
    
    @JsonProperty("formats")
    List<FormatItem> formats
) {
    public record FormatItem(
        @JsonProperty("type")
        String type,
        
        @JsonProperty("details")
        String details
    ) {}
}
