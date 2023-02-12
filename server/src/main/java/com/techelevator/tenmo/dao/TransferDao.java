package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    void transfer(Account sendingAccount, Account receivingAccount, BigDecimal transfer_amount);

    Transfer getTransferByTransferId(int transferId,int userId);

    List<Transfer> getAllTransfers(int account_id);

    boolean requestTransfer(Account sendingAccount, Account receivingAccount, BigDecimal transferAmount);

    void respondToTransferRequest(int transferId, Account sendingAccount, Account receivingAccount, BigDecimal transferAmount, boolean approved);

    List<Transfer> getAllRequest(int user_id);

}
