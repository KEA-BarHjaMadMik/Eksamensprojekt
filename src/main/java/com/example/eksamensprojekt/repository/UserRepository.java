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
        String sql = "SELECT user_id, email, password_hash, name, title, external FROM user_account WHERE email = ?";

        List<User> results = jdbcTemplate.query(sql, getUserRowMapper(), email);
        return results.isEmpty() ? null : results.getFirst();
    }

    public User getUserByUserId(int userId) {
        String sql = "SELECT user_id, email, password_hash, name, title, external FROM user_account WHERE user_id = ?";

        List<User> results = jdbcTemplate.query(sql, getUserRowMapper(), userId);
        return results.isEmpty() ? null : results.getFirst();
    }

    public int countByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM user_account WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return (count != null) ? count : 0;
    }

    public void registerUser(User user) {
        String sql = "INSERT INTO user_account (email, password_hash, name, title, external) VALUES(?,?,?,?,?)";

        jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getPasswordHash(),
                user.getName(),
                user.getTitle(),
                user.isExternal()
        );
    }

    public int updateUser(User updatedUser) {
        String sql = "UPDATE user_account SET email = ?, name = ?, title = ?, external = ? WHERE user_id = ?";

        return jdbcTemplate.update(
                sql,
                updatedUser.getEmail(),
                updatedUser.getName(),
                updatedUser.getTitle(),
                updatedUser.isExternal(),
                updatedUser.getUserId()
        );
    }

    public int changePassword(int userId, String passwordHash) {
        String sql = "UPDATE user_account SET password_hash = ? WHERE user_id = ?";

        return jdbcTemplate.update(sql, passwordHash, userId);
    }

    public int deleteUser(int userId) {
        String sql = "DELETE FROM user_account WHERE user_id = ?";

        return jdbcTemplate.update(sql, userId);
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

