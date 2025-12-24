package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());

        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(id.intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper());

        // Загружаем друзей для каждого пользователя
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        loadFriends(user);
        return Optional.of(user);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'CONFIRMED' " +
                "UNION SELECT user_id FROM friendships WHERE friend_id = ? AND status = 'CONFIRMED'";
        List<Integer> friendIds = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getInt("friend_id"),
                user.getId(), user.getId());
        user.setFriends(new HashSet<>(friendIds));
    }

    public void addFriend(int userId, int friendId) {
        // Проверяем, существует ли уже дружба
        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count == null || count == 0) {
            // Создаем новую неподтвержденную дружбу
            String insertSql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'UNCONFIRMED')";
            jdbcTemplate.update(insertSql, userId, friendId);
        }

        // Проверяем, есть ли обратная дружба
        Integer reverseCount = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);
        if (reverseCount != null && reverseCount > 0) {
            // Подтверждаем обе дружбы
            String updateSql = "UPDATE friendships SET status = 'CONFIRMED' WHERE " +
                    "(user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
            jdbcTemplate.update(updateSql, userId, friendId, friendId, userId);
        }
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);

        // Если была взаимная дружба, меняем статус обратной связи на UNCONFIRMED
        String updateSql = "UPDATE friendships SET status = 'UNCONFIRMED' WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(updateSql, friendId, userId);
    }

    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id AND f1.user_id = ? AND f1.status = 'CONFIRMED' " +
                "UNION " +
                "SELECT u.* FROM users u " +
                "JOIN friendships f2 ON u.id = f2.user_id AND f2.friend_id = ? AND f2.status = 'CONFIRMED'";
        return jdbcTemplate.query(sql, new UserRowMapper(), userId, userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "WHERE u.id IN (" +
                "    SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'CONFIRMED' " +
                "    INTERSECT " +
                "    SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'CONFIRMED'" +
                ") " +
                "OR u.id IN (" +
                "    SELECT user_id FROM friendships WHERE friend_id = ? AND status = 'CONFIRMED' " +
                "    INTERSECT " +
                "    SELECT user_id FROM friendships WHERE friend_id = ? AND status = 'CONFIRMED'" +
                ")";
        return jdbcTemplate.query(sql, new UserRowMapper(), userId, otherId, userId, otherId);
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }
    }
}