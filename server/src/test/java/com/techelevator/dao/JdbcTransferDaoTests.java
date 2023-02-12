package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JdbcTransferDaoTests extends BaseDaoTests {

    private static final Account BOB_ACCOUNT = new Account(2001, 1001, BigDecimal.valueOf(1000));
    private static final Account USER_ACCOUNT = new Account(2002, 1002, BigDecimal.valueOf(1000));
    private static final Account ERIC_ACCOUNT = new Account(2003, 1003, BigDecimal.valueOf(1000));

    private static final Transfer EXPECTED_1 = new Transfer(3001, 2003, 2001, BigDecimal.valueOf(1000), "Approved");
    private static final Transfer EXPECTED_2 = new Transfer(3002, 2001, 2003, BigDecimal.valueOf(999), "Approved");
    private static final Transfer EXPECTED_3 = new Transfer(3003, 2001, 2002, BigDecimal.valueOf(1), "Approved");

    private JdbcTransferDao sut;
    private JdbcAccountDao dao;

    @Before
    public void setup() {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        sut = new JdbcTransferDao(jdbcTemplate);
        dao = new JdbcAccountDao(jdbcTemplate);
    }

    @Test
    public void transfer100Bucks() {

        BigDecimal transferAmount = BigDecimal.valueOf(100);
        BigDecimal expectedSendingAccountBalance = new BigDecimal("900.00");
        BigDecimal expectedReceivingAccountBalance = new BigDecimal("1100.00");

        sut.transfer(BOB_ACCOUNT, USER_ACCOUNT, transferAmount);
        BigDecimal actualSendingBalance = dao.getFunds("bob");
        BigDecimal actualReceivingBalance = dao.getFunds("user");

        Assert.assertEquals(expectedReceivingAccountBalance, actualReceivingBalance);
    }

    @Test
    public void transfer1000Bucks() {

        BigDecimal transferAmount = BigDecimal.valueOf(1000);
        BigDecimal expectedSendingAccountBalance = new BigDecimal("0");
        BigDecimal expectedReceivingAccountBalance = new BigDecimal("2000.00");

        sut.transfer(BOB_ACCOUNT, ERIC_ACCOUNT, transferAmount);
        BigDecimal actualSendingBalance = dao.getFunds("bob");
        BigDecimal actualReceivingBalance = dao.getFunds("eric");

        Assert.assertEquals(expectedReceivingAccountBalance, actualReceivingBalance);
    }

    @Test
    public void transferOneBuck() {

        BigDecimal transferAmount = BigDecimal.valueOf(1);
        BigDecimal expectedSendingAccountBalance = new BigDecimal("999.00");
        BigDecimal expectedReceivingAccountBalance = new BigDecimal("1001.00");

        sut.transfer(BOB_ACCOUNT, USER_ACCOUNT, transferAmount);
        BigDecimal actualSendingBalance = dao.getFunds("bob");
        BigDecimal actualReceivingBalance = dao.getFunds("user");

        Assert.assertEquals(expectedReceivingAccountBalance, actualReceivingBalance);
        Assert.assertEquals(expectedSendingAccountBalance, actualSendingBalance);
    }


    @Test
    public void testGetAllTransfersFor2001() {

        List<Transfer> expectedTransfers = new ArrayList<>();
        expectedTransfers.add(EXPECTED_1);
        expectedTransfers.add(EXPECTED_2);

        sut.transfer(ERIC_ACCOUNT, BOB_ACCOUNT, BigDecimal.valueOf(1000));
        sut.transfer(BOB_ACCOUNT, ERIC_ACCOUNT, BigDecimal.valueOf(999));
        List<Transfer> actualTransfers = sut.getAllTransfers(BOB_ACCOUNT.getUserId());

        Assert.assertEquals(expectedTransfers.size(), actualTransfers.size());

        for (int i = 0; i < expectedTransfers.size(); i++){
            assertTransfersMatch(expectedTransfers.get(i), actualTransfers.get(i));
        }
    }

    @Test
    public void testGetAllTransfersFor2003() {

        List<Transfer> expectedTransfers = new ArrayList<>();
        expectedTransfers.add(new Transfer(3001, 2003, 2001, BigDecimal.valueOf(250), "Approved"));
        expectedTransfers.add(new Transfer(3002, 2001, 2003, BigDecimal.valueOf(100), "Approved"));
        expectedTransfers.add(new Transfer(3004, 2002, 2003, BigDecimal.valueOf(500), "Approved"));

        sut.transfer(ERIC_ACCOUNT, BOB_ACCOUNT, BigDecimal.valueOf(250));
        sut.transfer(BOB_ACCOUNT, ERIC_ACCOUNT, BigDecimal.valueOf(100));
        sut.transfer(BOB_ACCOUNT, USER_ACCOUNT, BigDecimal.valueOf(111));
        sut.transfer(USER_ACCOUNT, ERIC_ACCOUNT, BigDecimal.valueOf(500));
        List<Transfer> actualTransfers = sut.getAllTransfers(ERIC_ACCOUNT.getUserId());

        Assert.assertEquals(3, actualTransfers.size());

        for (int i = 0; i < expectedTransfers.size(); i++){
            assertTransfersMatch(expectedTransfers.get(i), actualTransfers.get(i));
        }
    }

    @Test
    public void testGetTransferByTransferId() {

        //following static Transfer Objects at top of class, these will be sequential 3004, 3005, 3006
        sut.transfer(ERIC_ACCOUNT, BOB_ACCOUNT, BigDecimal.valueOf(1000));
        sut.transfer(BOB_ACCOUNT, ERIC_ACCOUNT, BigDecimal.valueOf(999));
        sut.transfer(BOB_ACCOUNT, USER_ACCOUNT, BigDecimal.valueOf(1));

        Transfer actual1 = sut.getTransferByTransferId(3004, BOB_ACCOUNT.getUserId());
        Transfer actual2 = sut.getTransferByTransferId(3005, BOB_ACCOUNT.getUserId());
        Transfer actual3 = sut.getTransferByTransferId(3006, USER_ACCOUNT.getUserId());

        assertTransfersMatch(EXPECTED_1, actual1);
        assertTransfersMatch(EXPECTED_2, actual2);
        assertTransfersMatch(EXPECTED_3, actual3);
    }

    @Test
    public void testGetTransferByTransferIdNull() {

        Assert.assertNull(sut.getTransferByTransferId(3002, BOB_ACCOUNT.getUserId()));
        Assert.assertNull(sut.getTransferByTransferId(3003, USER_ACCOUNT.getUserId()));
        Assert.assertNull(sut.getTransferByTransferId(4000, ERIC_ACCOUNT.getUserId()));
    }

    //helper method to compare Transfer Objects
    private void assertTransfersMatch(Transfer expected, Transfer actual){
        Assert.assertEquals(expected.getSending_account_id(), actual.getSending_account_id());
        Assert.assertEquals(expected.getReceiving_account_id(), actual.getReceiving_account_id());
        Assert.assertEquals(expected.getTransfer_amount(), actual.getTransfer_amount());
        Assert.assertEquals(expected.getTransfer_status(), actual.getTransfer_status());
    }
}
