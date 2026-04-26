package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record WhatYouCanExpectData(
    @JsonProperty("title")
    String title,

    @JsonProperty("statements")
    List<Statement> statements
) {
    public record Statement(
        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description
    ) {}
}
