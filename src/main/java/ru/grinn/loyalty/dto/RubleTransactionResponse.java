package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class RubleTransactionResponse {
    private String transactionId;
    private BigDecimal newBalance;
    private String error;

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return "RubleTransactionResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", newBalance=" + newBalance +
                ", error='" + error + '\'' +
                '}';
    }
}
