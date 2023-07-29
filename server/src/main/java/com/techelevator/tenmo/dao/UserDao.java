package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;

import java.util.List;
import java.util.Map;

public interface UserDao {
//    Admin Account ONLY
//    List<User> findAll();

    Map<Integer, String> findAllUserIdAndName(int userId);

    User findByUsername(String username);

    int findIdByUsername(String username);

    boolean createUser(String username, String password);

    public List<User> findAll();
}
