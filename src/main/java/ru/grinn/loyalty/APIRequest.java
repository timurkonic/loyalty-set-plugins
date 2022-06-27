package ru.grinn.loyalty;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.crystals.pos.spi.IntegrationProperties;
import ru.grinn.loyalty.dto.Account;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public final class APIRequest {
    private IntegrationProperties properties;

    public APIRequest(IntegrationProperties properties) {
        this.properties = properties;
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

        return new ObjectMapper().readValue(connection.getInputStream(), Account.class);
    }
}
