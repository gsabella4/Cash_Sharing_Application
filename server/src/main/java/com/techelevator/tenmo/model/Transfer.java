package com.techelevator.tenmo.model;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Transfer {

    //Transfer fields
    private int transfer_id;
    @NotNull
    private int sending_account_id, receiving_account_id;
    @DecimalMin(value = "0.01", message="Transfer must be greater than zero")
    private BigDecimal transfer_amount;
    private String transfer_status = "Approved";

    //Constructor without parameters
    public Transfer() {
    }

    //Constructor with parameters
    public Transfer(int transfer_id, int sending_account_id, int receiving_account_id, BigDecimal transfer_amount, String transfer_status) {
        this.transfer_id = transfer_id;
        this.sending_account_id = sending_account_id;
        this.receiving_account_id = receiving_account_id;
        this.transfer_amount = transfer_amount;
        this.transfer_status = transfer_status;
    }

    //Getters and Setters
    public int getTransfer_id() {
        return transfer_id;
    }

    public void setTransfer_id(int transfer_id) {
        this.transfer_id = transfer_id;
    }

    public int getSending_account_id() {
        return sending_account_id;
    }

    public void setSending_account_id(int sending_account_id) {
        this.sending_account_id = sending_account_id;
    }

    public int getReceiving_account_id() {
        return receiving_account_id;
    }

    public void setReceiving_account_id(int receiving_account_id) {
        this.receiving_account_id = receiving_account_id;
    }

    public BigDecimal getTransfer_amount() {
        return transfer_amount;
    }

    public void setTransfer_amount(BigDecimal transfer_amount) {
        this.transfer_amount = transfer_amount;
    }

    public String getTransfer_status() {
        return transfer_status;
    }

    public void setTransfer_status(String transfer_status) {
        this.transfer_status = transfer_status;
    }

    //toString
    @Override
    public String toString() {
        return "Transfer{" +
                "transfer_id=" + transfer_id +
                ", sending_account_id=" + sending_account_id +
                ", receiving_account_id=" + receiving_account_id +
                ", transfer_amount=" + transfer_amount +
                ", transfer_status='" + transfer_status + '\'' +
                '}';
    }
}
