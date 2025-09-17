package com.rebra.enums;

public enum TransactionStatus {
    EXECUTING("진행중"),
    COMPLETED("성공"),
    FAILED("실패");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}