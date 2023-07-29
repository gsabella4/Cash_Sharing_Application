package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;

//Tag Component to connect to Controller and allows dependecies injection
@Component
public class JdbcAccountDao implements AccountDao{

    //Make Jdbc Template
    private JdbcTemplate jdbcTemplate;

    //static final variable for Starting Account balance of $1000.00
    private static final BigDecimal ACCOUNT_START = BigDecimal.valueOf(1000.00);

    //Constructor for JdbcTemplate
    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //Create new Account w/ starting balance of $1000.00
    @Override
    public boolean create(int user_id) {
        String sqlGetNewAccount = "INSERT INTO account (user_id, balance) VALUES (?, ?) RETURNING account_id;";
        try {
            jdbcTemplate.queryForObject(sqlGetNewAccount, Integer.class, user_id, ACCOUNT_START);
            return true;
        } catch (ResourceAccessException e) {
            System.out.println("ERROR RETURNING FALSE");
            return false;
        }
    }

    //Gets funds from username
    @Override
    public BigDecimal getFunds(String name) {
        String sql = "SELECT account_id, tenmo_user.user_id, balance FROM account " +
                     "JOIN tenmo_user ON account.user_id = tenmo_user.user_id " +
                     "WHERE username = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, name);
        if (rowSet.next()){
            return mapRowToAccount(rowSet).getBalance();
        }
        return null;
    }

    //Get Account through accountId
    @Override
    public Account getAccountById(int account_id) {
        Account account = new Account();
        String sql = "SELECT account_id, user_id, balance FROM account "+
                     "WHERE account_id = ?";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql,account_id);
        if(results.next()) {
            account = mapRowToAccount(results);
        }
        return account;
    }

    //helper method to create Account object
    private Account mapRowToAccount(SqlRowSet rowSet) {
        Account account = new Account();
        account.setAccount_id(rowSet.getInt("account_id"));
        account.setUser_id(rowSet.getInt("user_id"));
        account.setBalance(rowSet.getBigDecimal("balance"));
        return account;
    }
}
