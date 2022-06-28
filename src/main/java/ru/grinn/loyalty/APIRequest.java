package ru.grinn.loyalty;

import ru.crystals.pos.spi.IntegrationProperties;
import ru.grinn.loyalty.dto.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public final class APIRequest {
    private IntegrationProperties properties;
    private APIObjectMapper apiObjectMapper;

    public APIRequest(IntegrationProperties properties) {
        this.properties = properties;
        apiObjectMapper = new APIObjectMapper();
    }

    private String getApiUrl() {
        //return properties.getServiceProperties().get("url");
        return "http://172.31.1.100/sapi/";
    }

    private HttpURLConnection getConnection(String uri, String method) throws IOException {
        URL url = new URL( getApiUrl() + uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(properties.getServiceProperties().getInt("connectionTimeout"));
        connection.setReadTimeout(properties.getServiceProperties().getInt("readTimeout"));
        connection.setRequestProperty("X-API-Key", APIKey.API_KEY);
        connection.setRequestMethod(method);
        if (method.equals("POST")) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
        }
        connection.connect();
        return connection;
    }

    public Account getAccount(String id) throws IOException {
        HttpURLConnection connection = getConnection("account/" + id, "GET");
        return apiObjectMapper.readValue(connection.getInputStream(), Account.class);
    }

    public BonusTransactionResponse addBonus(AddBonusTransaction transaction) throws IOException {
        HttpURLConnection connection = getConnection("transaction/", "POST");
        apiObjectMapper.writeValue(connection.getOutputStream(), transaction);
        return apiObjectMapper.readValue(connection.getInputStream(), BonusTransactionResponse.class);
    }

    public BonusTransactionResponse writeOffBonus(WriteOffBonusTransaction transaction) throws IOException {
        HttpURLConnection connection = getConnection("transaction/", "POST");
        apiObjectMapper.writeValue(connection.getOutputStream(), transaction);
        return apiObjectMapper.readValue(connection.getInputStream(), BonusTransactionResponse.class);
    }

    public RubleTransactionResponse addRuble(AddRubleTransaction transaction) throws IOException {
        HttpURLConnection connection = getConnection("transaction/", "POST");
        apiObjectMapper.writeValue(connection.getOutputStream(), transaction);
        return apiObjectMapper.readValue(connection.getInputStream(), RubleTransactionResponse.class);
    }

    public RollbackTransactionResponse rollback(String txid) throws IOException {
        HttpURLConnection connection = getConnection("transaction/" + txid, "DELETE");
        return apiObjectMapper.readValue(connection.getInputStream(), RollbackTransactionResponse.class);
    }
}
