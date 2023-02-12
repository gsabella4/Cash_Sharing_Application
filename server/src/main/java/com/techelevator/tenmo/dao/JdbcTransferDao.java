package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{

    //Make Jdbc template
    private JdbcTemplate jdbcTemplate;

    //Constructor for JdbcTemplate
    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Transfer funds between accounts
    @Override
    public void transfer(Account sendingAccount, Account receivingAccount, BigDecimal transfer_amount) {
        String sql = "UPDATE account " +
                     "SET balance = balance - ? " +
                     "WHERE user_id = ?; " +
                     "UPDATE account " +
                     "SET balance = balance + ? " +
                     "WHERE user_id = ?; " +
                     "INSERT INTO transfer (transfer_id, sending_account_id, receiving_account_id, transfer_amount) " +
                     "VALUES (Default, ?, ?, ?);";
        try {
            jdbcTemplate.update(sql, transfer_amount, sendingAccount.getUserId(), transfer_amount,
                    receivingAccount.getUserId(), sendingAccount.getAccount_id(), receivingAccount.getAccount_id(), transfer_amount);
        } catch (RestClientResponseException e) {
            System.out.println(e.getRawStatusCode() + " : " + e.getStatusText());
        }
    }

    //Method to return all transfers involving current logged-in user
    @Override
    public List<Transfer> getAllTransfers(int user_id) {
        List<Transfer> allTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, sending_account_id, receiving_account_id, transfer_amount, transfer_status  FROM transfer " +
                     "JOIN account ON (transfer.sending_account_id = account.account_id) OR (transfer.receiving_account_id = account.account_id) " +
                     "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                     "WHERE tenmo_user.user_id = ? AND transfer_status = 'Approved' OR transfer_status = 'Rejected';";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user_id);
        while(results.next()){
            allTransfers.add(mapRowToTransfer(results));
        }
        return allTransfers;
    }

    //Returns transfer information by a specific transfer id, resulting transfer must involve current logged-in user, else throws exception
    @Override
    public Transfer getTransferByTransferId(int transferId,int userId) {
        String sql = "SELECT transfer_id, sending_account_id, receiving_account_id, transfer_amount, transfer_status " +
                     "FROM transfer " +
                     "JOIN account ON (transfer.sending_account_id = account.account_id) OR (transfer.receiving_account_id = account.account_id) " +
                     "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                     "WHERE transfer_id = ? AND tenmo_user.user_id = ?;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transferId, userId);
        if(results.next()) {
            return mapRowToTransfer(results);
        }
        return null;
    }

    //Establish transfer records with Pending transfers with status of Pending
    @Override
    public boolean requestTransfer(Account sendingAccount, Account receivingAccount, BigDecimal transferAmount) {
        String sql = "INSERT INTO transfer " +
                "(transfer_id, sending_account_id, receiving_account_id, transfer_amount, transfer_status) " +
                "VALUES (Default, ?, ?, ?, ?) RETURNING transfer_id;";
        try {
            jdbcTemplate.queryForObject(sql, Integer.class,
                    sendingAccount.getAccount_id(),
                    receivingAccount.getAccount_id(),
                    transferAmount,
                    "Pending");
            return true;
        } catch (RestClientResponseException e) {
            System.out.println(e.getRawStatusCode() + " : " + e.getStatusText());
            return false;
        }
    }


    //Updates Transfer status and initiates transfer
    @Override
    public void respondToTransferRequest(int transferId, Account sendingAccount, Account receivingAccount, BigDecimal transferAmount, boolean approved) {
        String sql;
        if (approved) {
            sql = "UPDATE account " +
                    "SET balance = balance - ? " +
                    "WHERE user_id = ?; " +
                    "UPDATE account " +
                    "SET balance = balance + ? " +
                    "WHERE user_id = ?; " +
                    "UPDATE transfer " +
                    "SET transfer_status = 'Approved' " +
                    "WHERE transfer_id = ?; ";
        } else {
            sql = "UPDATE transfer " +
                    "SET transfer_status = 'Rejected' " +
                    "WHERE transfer_id = ?; ";
        }
        try {
            if (approved) {
                jdbcTemplate.update(sql, transferAmount, sendingAccount.getUserId(), transferAmount,
                        receivingAccount.getUserId(), transferId);
            } else {
                jdbcTemplate.update(sql, transferId);
            }
        } catch (RestClientResponseException e) {
            System.out.println(e.getRawStatusCode() + " : " + e.getStatusText());
        }
    }


    @Override
    public List<Transfer> getAllRequest(int user_id) {
        List<Transfer> allTransfers = new ArrayList<>();
        String sql = "SELECT transfer_id, sending_account_id, receiving_account_id, transfer_amount, transfer_status  FROM transfer " +
                "JOIN account ON (transfer.sending_account_id = account.account_id) OR (transfer.receiving_account_id = account.account_id) " +
                "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                "WHERE tenmo_user.user_id = ? AND transfer_status = 'Pending';";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user_id);
        while(results.next()){
            allTransfers.add(mapRowToTransfer(results));
        }
        return allTransfers;
    }

    //helper method to create a Transfer object
    private Transfer mapRowToTransfer(SqlRowSet rowSet){
        Transfer transfer = new Transfer();
        transfer.setTransfer_id(rowSet.getInt("transfer_id"));
        transfer.setSending_account_id(rowSet.getInt("sending_account_id"));
        transfer.setReceiving_account_id(rowSet.getInt("receiving_account_id"));
        transfer.setTransfer_amount(rowSet.getBigDecimal("transfer_amount"));
        transfer.setTransfer_status(rowSet.getString("transfer_status"));
        return transfer;
    }
}
