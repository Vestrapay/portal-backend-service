package com.example.vestrapay.merchant.transactions.enums;

public enum PaymentTypeEnum {
    CARD("CARD"),
    PAYMENT_LINK("PAYMENT LINK"),
    TRANSFER("TRANSFER"),
    NQR("NQR"),
    REFUND("REFUND"),
    CHARGEBACK("CHARGEBACK");

    private final String description;

    PaymentTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
