package com.codzilla.backend.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import com.codzilla.backend.PreMatch.model.Category;
import java.util.*;

@Converter
public class JsonToMapOptionsConverter implements AttributeConverter<Map<Category, Set<String>>, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<Category, Set<String>> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public Map<Category, Set<String>> convertToEntityAttribute(String dbData) {
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<Category, Set<String>>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}