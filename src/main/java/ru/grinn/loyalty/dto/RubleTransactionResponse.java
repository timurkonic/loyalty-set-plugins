package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class RubleTransactionResponse {
    private String transactionId;
    private BigDecimal newBalance;
    private String error;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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
