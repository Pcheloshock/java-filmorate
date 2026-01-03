package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Stats;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final JdbcTemplate jdbcTemplate;

    public Stats getStats() {
        // Получаем количество фильмов
        String filmsSql = "SELECT COUNT(*) FROM films";
        Long filmCount = jdbcTemplate.queryForObject(filmsSql, Long.class);

        // Получаем количество пользователей
        String usersSql = "SELECT COUNT(*) FROM users";
        Long userCount = jdbcTemplate.queryForObject(usersSql, Long.class);

        // Получаем общее количество лайков
        String likesSql = "SELECT COUNT(*) FROM film_likes";
        Long totalLikes = jdbcTemplate.queryForObject(likesSql, Long.class);

        // Получаем общее количество дружеских связей
        String friendsSql = "SELECT COUNT(*) FROM friendships";
        Long totalFriendships = jdbcTemplate.queryForObject(friendsSql, Long.class);

        return new Stats(
                filmCount != null ? filmCount : 0,
                userCount != null ? userCount : 0,
                totalLikes != null ? totalLikes : 0,
                totalFriendships != null ? totalFriendships : 0
        );
    }
}