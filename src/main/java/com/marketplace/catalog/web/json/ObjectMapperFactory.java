package com.marketplace.catalog.web.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ObjectMapperFactory {

    private static final ObjectMapper OBJECT_MAPPER = build();

    private static ObjectMapper build() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        return mapper;
    }

    public static ObjectMapper get() {
        return OBJECT_MAPPER;
    }
}
