package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class BonusTransactionResponse {
    private String transactionId;
    private BigDecimal newBalanceBns;
    private String error;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getNewBalanceBns() {
        return newBalanceBns;
    }

    public void setNewBalanceBns(BigDecimal newBalanceBns) {
        this.newBalanceBns = newBalanceBns;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "WriteOffBonusResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", newBalanceBns=" + newBalanceBns +
                ", error='" + error + '\'' +
                '}';
    }
}
