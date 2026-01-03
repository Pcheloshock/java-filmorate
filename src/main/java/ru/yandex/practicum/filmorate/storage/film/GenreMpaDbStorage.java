package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

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
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, id);

        if (genres.isEmpty()) {
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
        return genres.get(0);
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
        try {
            return jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new MpaRating(rs.getInt("id"), rs.getString("name"), rs.getString("description")),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA с ID " + id + " не найден");
        }
    }
}