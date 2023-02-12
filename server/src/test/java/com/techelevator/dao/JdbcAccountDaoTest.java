package com.techelevator.dao;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcAccountDaoTest extends BaseDaoTests{

    private static final Account LUKA_ACCOUNT = new Account(2004, 1004, BigDecimal.valueOf(1000));
    private static final User USER_5 = new User(1005, "test", "$2a$10$G/MIQ7pUYupiVi72DxqHquxl73zfd7ZLNBoB2G6zUb.W16imI2.W2", "");
    private static final Account TEST_ACCOUNT = new Account(2005, 1005, BigDecimal.valueOf(1000));


    private JdbcAccountDao sut;


    @Before
    public void setup() {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        sut = new JdbcAccountDao(jdbcTemplate);
    }
    @Test
    public void createNewAccount() {
        boolean userCreated = sut.create(USER_5.getId());
        Assert.assertTrue(userCreated);
        Assert.assertEquals(TEST_ACCOUNT.getUserId(),USER_5.getId());
    }
    @Test
    public void getFunds_returns_correct_balance_for_given_username_Bob() {
        String username = "bob";
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        BigDecimal result = sut.getFunds(username);
       Assert.assertEquals(expectedBalance, result);
    }

    @Test
    public void getFunds_returns_correct_balance_for_given_username_Eric() {

        String username = "eric";
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        BigDecimal result = sut.getFunds(username);
        Assert.assertEquals(expectedBalance, result);
    }

    @Test
    public void getFunds_returns_correct_balance_for_given_username_Luka() {

        String username = "Luka";
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        BigDecimal result = sut.getFunds(username);
        Assert.assertEquals(expectedBalance, result);
    }

    @Test
    public void getAccountById_returns_correct_account_for_given_account_id_2001() {

        int accountId = 2001;
        int expectedUserId = 1001;
        BigDecimal expectedBalance = new BigDecimal("1000.00");

        Account result = sut.getAccountById(accountId);

        Assert.assertEquals(accountId, result.getAccount_id());
        Assert.assertEquals(expectedUserId, result.getUserId());
        Assert.assertEquals(expectedBalance, result.getBalance());
    }

    @Test
    public void getAccountById_returns_correct_account_for_given_account_id_2003() {

        Account expected = new Account(2003, 1003, BigDecimal.valueOf(1000));
        Account result = sut.getAccountById(expected.getAccount_id());

        assertAccountsMatch(expected, result);
    }

    @Test
    public void getAccountById_returns_correct_account_for_given_account_id_2004() {

        Account result = sut.getAccountById(LUKA_ACCOUNT.getAccount_id());

        assertAccountsMatch(LUKA_ACCOUNT, result);
    }

    public void assertAccountsMatch(Account expected, Account actual){
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        Assert.assertEquals(expected.getAccount_id(), actual.getAccount_id());
        Assert.assertEquals(expected.getUserId(), actual.getUserId());
        Assert.assertEquals(expectedBalance, actual.getBalance());
    }
}
