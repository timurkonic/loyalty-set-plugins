package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class RollbackTransactionResponse {
    private String transactionId;
    private String error;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "RollbackTransactionResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
