package com.rebra.calculator.dto;

/**
 * 리밸런싱 액션을 나타내는 클래스
 * 개별 종목의 매매 계획을 담는다.
 */
public class RebalancingAction {
    private String stockCode;
    private String actionType; // "BUY" or "SELL"
    private int quantity;
    private double price;
    private double amount;
    private double currentWeight;
    private double targetWeight;

    public RebalancingAction() {}

    public RebalancingAction(String stockCode, String actionType, int quantity, double price, 
                           double amount, double currentWeight, double targetWeight) {
        this.stockCode = stockCode;
        this.actionType = actionType;
        this.quantity = quantity;
        this.price = price;
        this.amount = amount;
        this.currentWeight = currentWeight;
        this.targetWeight = targetWeight;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(double currentWeight) {
        this.currentWeight = currentWeight;
    }

    public double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(double targetWeight) {
        this.targetWeight = targetWeight;
    }

    @Override
    public String toString() {
        return String.format("RebalancingAction{stockCode='%s', actionType='%s', quantity=%d, " +
                           "price=%.0f, amount=%.0f, currentWeight=%.2f%%, targetWeight=%.2f%%}",
                stockCode, actionType, quantity, price, amount, 
                currentWeight * 100, targetWeight * 100);
    }
}