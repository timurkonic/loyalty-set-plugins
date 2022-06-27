package ru.grinn.loyalty.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class PluginConfiguration {

    @XmlElement
    private final boolean accrueBonusAllowed;

    @XmlElement
    private final boolean payBonusAllowed;

    @XmlElement
    private final boolean cardDiscountAllowed;

    @XmlElement
    private final boolean dinnerDiscountAllowed;

    public PluginConfiguration() {
        accrueBonusAllowed = true;
        payBonusAllowed = true;
        cardDiscountAllowed = true;
        dinnerDiscountAllowed = false;
    }

    @Override
    public String toString() {
        return "PluginConfiguration{" +
                "accrueBonusAllowed=" + accrueBonusAllowed +
                ", payBonusAllowed=" + payBonusAllowed +
                ", cardDiscountAllowed=" + cardDiscountAllowed +
                ", dinnerDiscountAllowed=" + dinnerDiscountAllowed +
                '}';
    }

    public boolean isAccrueBonusAllowed() {
        return accrueBonusAllowed;
    }

    public boolean isPayBonusAllowed() {
        return payBonusAllowed;
    }

    public boolean isCardDiscountAllowed() {
        return cardDiscountAllowed;
    }

    public boolean isDinnerDiscountAllowed() {
        return dinnerDiscountAllowed;
    }

}
