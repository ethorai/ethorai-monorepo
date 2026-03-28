package com.ai.therapists.api.section_data;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StructuredSections(
    @JsonProperty("HEADER")
    HeaderData header,
    
    @JsonProperty("HERO")
    HeroData hero,
    
    @JsonProperty("AREAS_OF_SUPPORT")
    AreasOfSupportData areasOfSupport,
    
    @JsonProperty("HOW_I_WORK")
    HowIWorkData howIWork,
    
    @JsonProperty("WHAT_YOU_CAN_EXPECT")
    WhatYouCanExpectData whatYouCanExpect,
    
    @JsonProperty("SESSION_FORMATS")
    SessionFormatsData sessionFormats,
    
    @JsonProperty("CONTACT")
    ContactData contact,
    
    @JsonProperty("DISCLAIMER")
    DisclaimerData disclaimer,
    
    @JsonProperty("FOOTER")
    FooterData footer
) {}
