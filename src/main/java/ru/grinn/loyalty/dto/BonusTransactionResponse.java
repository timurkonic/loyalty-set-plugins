package ru.grinn.loyalty.dto;

import java.math.BigDecimal;

public class BonusTransactionResponse {
    private String transactionId;
    private BigDecimal newBalanceBns;
    private String error;

    public String getTransactionId() {
        return transactionId;
    }

    public BigDecimal getNewBalanceBns() {
        return newBalanceBns;
    }

    public String getError() {
        return error;
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
