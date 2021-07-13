package com.crio.xcommerce.sale.insights;

// import lombok.AllArgsConstructor;
// import lombok.Getter;
// import lombok.Setter;

import java.time.LocalDate;

// @AllArgsConstructor
// @Getter
// @Setter
public class Record {
    private LocalDate transactionDate;
    private String transactionStatus;
    private Double amount;

    public Record(LocalDate transactionDate, String transactionStatus, Double amount) {
        this.transactionDate = transactionDate;
        this.transactionStatus = transactionStatus;
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public Double getAmount() {
        return amount;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Record[" + " transaction_date= " + transactionDate + " transaction_status= " + transactionStatus
                + " amount= " + amount + "]\n";
    }
}
