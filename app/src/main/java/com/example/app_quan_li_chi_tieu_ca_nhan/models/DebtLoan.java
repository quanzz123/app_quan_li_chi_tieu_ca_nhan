package com.example.app_quan_li_chi_tieu_ca_nhan.models;

import java.io.Serializable;

public class DebtLoan implements Serializable {
    private String debtId;
    private String userId;
    private String contactName;
    private double amount;
    private boolean isDebt; // true = Debt (Nợ phải trả), false = Loan (Cho vay cần thu hồi)
    private long dueDate;
    private String status; // "pending" or "paid"
    private String notes;

    public DebtLoan() {
        // Required for Firebase deserialization
    }

    public DebtLoan(String debtId, String userId, String contactName, double amount, boolean isDebt, long dueDate, String status, String notes) {
        this.debtId = debtId;
        this.userId = userId;
        this.contactName = contactName;
        this.amount = amount;
        this.isDebt = isDebt;
        this.dueDate = dueDate;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public String getDebtId() {
        return debtId;
    }

    public void setDebtId(String debtId) {
        this.debtId = debtId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isDebt() {
        return isDebt;
    }

    public void setDebt(boolean debt) {
        isDebt = debt;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
