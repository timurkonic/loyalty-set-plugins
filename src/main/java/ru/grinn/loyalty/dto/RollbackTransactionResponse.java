package ru.grinn.loyalty.dto;

public class RollbackTransactionResponse {
    private String transactionId;
    private String error;

    public String getTransactionId() {
        return transactionId;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "RollbackTransactionResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
