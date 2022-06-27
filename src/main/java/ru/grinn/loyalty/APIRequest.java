package ru.grinn.loyalty;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.grinn.loyalty.dto.Account;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public final class APIRequest {
    private IntegrationProperties properties;
    private ObjectMapper objectMapper;

    public APIRequest(IntegrationProperties properties) {
        this.properties = properties;
        initObjectMapper();
    }

    private void initObjectMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("dd/MM/yyyy"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    private String getApiUrl() {
        //return properties.getServiceProperties().get("url");
        return "http://172.31.1.100/sapi/";
    }

    public Account getAccount(String id) throws IOException {
        URL url = new URL( getApiUrl() + "account/" + id);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(properties.getServiceProperties().getInt("connectionTimeout"));
        connection.setReadTimeout(properties.getServiceProperties().getInt("readTimeout"));
        connection.setRequestProperty("X-API-Key", APIKey.API_KEY);
        connection.setRequestMethod("GET");
        connection.connect();

        return objectMapper.readValue(connection.getInputStream(), Account.class);
    }
}
