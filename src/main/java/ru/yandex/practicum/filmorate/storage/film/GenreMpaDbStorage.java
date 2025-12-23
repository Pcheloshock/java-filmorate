package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreMpaDbStorage implements GenreMpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name"))
        );
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                id
        );
    }

    @Override
    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new MpaRating(rs.getInt("id"), rs.getString("name"), rs.getString("description"))
        );
    }

    @Override
    public MpaRating getMpaRatingById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name"), rs.getString("description")),
                id
        );
    }
}