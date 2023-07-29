package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JdbcUserDao implements UserDao {

    //Make Jdbc Template
    private JdbcTemplate jdbcTemplate;

    //Constructor for JdbcTemplate
    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //returns user_id for a specific username
    @Override
    public int findIdByUsername(String username) {
        String sql = "SELECT user_id FROM tenmo_user WHERE username ILIKE ?;";
        Integer id = -1;
        try{
            id = jdbcTemplate.queryForObject(sql, Integer.class, username);
            return id;
        } catch (DataAccessException e){
            return id;
        }
    }

    //Returns map with user_id and username
    @Override
    public Map<Integer, String> findAllUserIdAndName(int user_id) {
        Map<Integer,String> userMap = new HashMap<>();
        String sql = "SELECT user_id, username FROM tenmo_user WHERE user_id NOT IN (?);";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql,user_id);
        while(result.next()){
            userMap.put(result.getInt("user_id") , result.getString("username"));
        }
        return userMap;
    }

    //Returns User object based on Username
    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE username ILIKE ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    //User Creation
    @Override
    public boolean createUser(String username, String password) {
        // create user
        String sql = "INSERT INTO tenmo_user (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        try {
            jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    //Admin Account Only
    //Retrieve all active users
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
       }
        return users;
    }

    //helper method to create User object
    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }


}
