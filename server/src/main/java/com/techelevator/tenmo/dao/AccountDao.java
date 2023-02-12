package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    boolean create(int user_id);

    BigDecimal getFunds(String name);

    Account getAccountById(int account_id);

}
