package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DisclaimerData(
    @JsonProperty("text")
    String text
) {}
