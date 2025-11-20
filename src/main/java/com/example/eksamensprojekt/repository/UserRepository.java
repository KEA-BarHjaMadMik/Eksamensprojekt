package com.example.eksamensprojekt.repository;

import com.example.eksamensprojekt.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM user_account WHERE email = ?";

        RowMapper<User> rowMapper = getUserRowMapper();

        List<User> results = jdbcTemplate.query(sql, rowMapper, email);
        return results.isEmpty() ? null : results.getFirst();
    }

    public User getUserByUserID(int userID) {
        String sql = "SELECT * FROM user_account WHERE user_id = ?";

        RowMapper<User> rowMapper = getUserRowMapper();

        List<User> results = jdbcTemplate.query(sql, rowMapper,userID);
        return results.isEmpty() ? null : results.getFirst();
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user_account WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO user_account (email, password_hash, name, title, external) VALUES(?,?,?,?,?)";

        int rowsAffected = jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getPasswordHash(),
                user.getName(),
                user.getTitle(),
                user.isExternal()
        );
        return rowsAffected == 1;
    }

    public boolean updateUser(User updatedUser) {
        String sql = "UPDATE user_account SET email = ?, name = ?, title = ?, external = ? WHERE user_id = ?";

        int rowsAffected = jdbcTemplate.update(
                sql,
                updatedUser.getEmail(),
                updatedUser.getName(),
                updatedUser.getTitle(),
                updatedUser.isExternal(),
                updatedUser.getUserID()
        );
        return rowsAffected == 1;
    }

    public boolean changePassword(int userID, String passwordHash) {
        String sql = "UPDATE user_account SET password_hash = ? WHERE user_id = ?";

        int rowsAffected = jdbcTemplate.update(sql, passwordHash, userID);
        return rowsAffected == 1;
    }

    public boolean deleteUser(int userID) {
        String sql = "DELETE FROM user_account WHERE user_id = ?";

        int rowsAffected = jdbcTemplate.update(sql, userID);
        return rowsAffected == 1;
    }

    private RowMapper<User> getUserRowMapper() {
        return (rs, rowNum) -> new User(
                rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("name"),
                rs.getString("title"),
                rs.getBoolean("external")
        );
    }
}

