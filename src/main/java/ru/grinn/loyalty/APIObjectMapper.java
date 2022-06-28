package ru.grinn.loyalty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.text.SimpleDateFormat;

public class APIObjectMapper extends ObjectMapper {

    public APIObjectMapper() {
        setDateFormat(new SimpleDateFormat("dd/MM/yyyy"));
        configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        setVisibility(getSerializationConfig().getDefaultVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }
}
