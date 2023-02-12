package com.techelevator.dao;


import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.HashMap;
import java.util.Map;

public class JdbcUserDaoTests extends BaseDaoTests{

    private static final User USER_1 = new User(1001,"bob","$2a$10$G/MIQ7pUYupiVi72DxqHquxl73zfd7ZLNBoB2G6zUb.W16imI2.W2","");
    private static final User USER_2 = new User(1002,"user","$2a$10$Ud8gSvRS4G1MijNgxXWzcexeXlVs4kWDOkjE7JFIkNLKEuE57JAEy","");
    private static final User USER_3 = new User(1003, "eric", "$2a$10$G/MIQ7pUYupiVi72DxqHquxl73zfd7ZLNBoB2G6zUb.W16imI2.W2", "");

    private JdbcUserDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcUserDao(jdbcTemplate);
    }

    @Test
    public void createNewUser() {
        boolean userCreated = sut.createUser("TEST_USER","test_password");
        Assert.assertTrue(userCreated);
        User user = sut.findByUsername("TEST_USER");
        Assert.assertEquals("TEST_USER", user.getUsername());
    }

    @Test
    public void testFindIdByUsername() {
        String userNameOne = "bob";
        int testId = sut.findIdByUsername(userNameOne);
        System.out.println(testId);
        Assert.assertEquals(1001,testId);
    }

    @Test
    public void testFindIdByInvalidUsername() {
        String userNameOne = "glorUIHIUHUIHbglorbteapot";
        Assert.assertEquals(-1,sut.findIdByUsername(userNameOne));
    }

    @Test
    public void testFindAllUserIdAndNameExceptBob() {

        Map<Integer,String> expected = new HashMap<>();
        expected.put(1002, "user");
        expected.put(1003, "eric");
        expected.put(1004, "Luka");
        expected.put(1005, "test");

        Map<Integer,String> availableUsers = sut.findAllUserIdAndName(USER_1.getId());
        Assert.assertEquals(expected.size(), availableUsers.size());
    }

    @Test
    public void testFindAllUserIdAndNameExceptUser() {

        Map<Integer,String> expected = new HashMap<>();
        expected.put(1001, "bob");
        expected.put(1003, "eric");
        expected.put(1004, "Luka");
        expected.put(1005, "test");


        Map<Integer,String> availableUsers = sut.findAllUserIdAndName(USER_2.getId());
        Assert.assertEquals(expected.size(), availableUsers.size());
    }

    @Test
    public void testFindAllUserIdAndNameExceptEric() {

        Map<Integer,String> expected = new HashMap<>();
        expected.put(1001, "bob");
        expected.put(1002, "user");
        expected.put(1004, "Luka");
        expected.put(1005, "test");

        Map<Integer,String> availableUsers = sut.findAllUserIdAndName(USER_3.getId());
        Assert.assertEquals(expected.size(), availableUsers.size());
    }

    @Test
    public void testFindByUsernameBob() {
        String usernameOne = "bob";
        USER_1.setAuthorities("USER");
        assertUsersMatch(USER_1, sut.findByUsername(usernameOne));
    }

    @Test
    public void testFindByUsernameUser() {
        String usernameTwo = "user";
        USER_2.setAuthorities("USER");
        assertUsersMatch(USER_2, sut.findByUsername(usernameTwo));
    }

    @Test
    public void testFindByUsernameEric() {
        String usernameThree = "eric";
        USER_3.setAuthorities("USER");
        assertUsersMatch(USER_3, sut.findByUsername(usernameThree));
    }

    // helper method to compare User objects
    public void assertUsersMatch(User expected, User actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getUsername(), actual.getUsername());
        Assert.assertEquals(expected.getPassword(), actual.getPassword());
        Assert.assertEquals(expected.getAuthorities().toString(), actual.getAuthorities().toString());
    }
}
