package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    public static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public Film create(Film film) {
        log.info("Создание фильма: {}", film.getName());

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("mpa_rating_id", film.getMpa() != null ? film.getMpa().getId() : null);

        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        film.setId(id.intValue());

        saveGenres(film);

        log.info("Фильм создан с ID: {}", film.getId());
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма с ID: {}", film.getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_rating_id = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        if (rowsUpdated == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        deleteGenres(film.getId());
        saveGenres(film);

        return findById(film.getId()).orElse(film);
    }

    @Override
    public List<Film> findAll() {
        log.info("Получение всех фильмов");

        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id";

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());
        loadAllGenres(films);
        loadAllLikes(films);

        log.info("Найдено {} фильмов", films.size());
        return films;
    }

    private void loadAllLikes(List<Film> films) {
        if (films.isEmpty()) {
            log.info("Нет фильмов для загрузки лайков");
            return;
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(
                "SELECT film_id, user_id FROM film_likes WHERE film_id IN (%s)",
                placeholders
        );

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Map<Integer, Set<Integer>> likesByFilmId = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Integer userId = (Integer) row.get("user_id");

            likesByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }

        for (Film film : films) {
            Set<Integer> likes = likesByFilmId.getOrDefault(film.getId(), new HashSet<>());
            film.setLikes(likes);
            film.setRate(likes.size());
        }

        log.info("Загружены лайки для {} фильмов", films.size());
    }

    private void loadAllGenres(List<Film> films) {
        if (films.isEmpty()) {
            log.info("Нет фильмов для загрузки жанров");
            return;
        }

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(
                "SELECT fg.film_id, g.id, g.name " +
                        "FROM film_genres fg " +
                        "JOIN genres g ON fg.genre_id = g.id " +
                        "WHERE fg.film_id IN (%s) " +
                        "ORDER BY fg.film_id, g.id",
                placeholders
        );

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        Map<Integer, Set<Genre>> genresByFilmId = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");

            Genre genre = new Genre();
            genre.setId((Integer) row.get("id"));
            genre.setName((String) row.get("name"));

            genresByFilmId.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }

        for (Film film : films) {
            Set<Genre> genres = genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>());
            film.setGenres(genres);
        }

        log.info("Загружены жанры для {} фильмов", films.size());
    }

    @Override
    public Optional<Film> findById(int id) {
        log.info("Поиск фильма по ID: {}", id);

        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), id);
        if (films.isEmpty()) {
            log.info("Фильм с ID {} не найден", id);
            return Optional.empty();
        }

        Film film = films.get(0);
        loadGenres(film);
        loadLikes(film);

        log.info("Фильм с ID {} найден: {}", id, film.getName());
        return Optional.of(film);
    }

    @Override
    public void delete(int id) {
        log.info("Удаление фильма с ID: {}", id);

        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(int filmId, int userId) {
        log.info("Добавление лайка: filmId={}, userId={}", filmId, userId);

        // Проверяем, существует ли фильм
        if (!existsById(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        // Проверяем, существует ли пользователь
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (userCount == null || userCount == 0) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        // Проверяем, не поставлен ли уже лайк
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count == null || count == 0) {
            String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            try {
                jdbcTemplate.update(insertSql, filmId, userId);
                log.info("Лайк добавлен: filmId={}, userId={}", filmId, userId);
            } catch (DataIntegrityViolationException e) {
                throw new NotFoundException("Не удалось добавить лайк. Проверьте существование фильма и пользователя.");
            }
        } else {
            log.info("Лайк уже существует: filmId={}, userId={}", filmId, userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        log.info("Удаление лайка: filmId={}, userId={}", filmId, userId);

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, filmId, userId);

        if (rowsDeleted > 0) {
            log.info("Лайк удален: filmId={}, userId={}", filmId, userId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.info("Получение {} популярных фильмов", count);

        // Используем корректный запрос для H2
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description, " +
                "COALESCE(l.likes_count, 0) as likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN ( " +
                "    SELECT film_id, COUNT(*) as likes_count " +
                "    FROM film_likes " +
                "    GROUP BY film_id " +
                ") l ON f.id = l.film_id " +
                "ORDER BY COALESCE(l.likes_count, 0) DESC, f.id DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);
        loadAllGenres(films);
        loadAllLikes(films);

        // Отладочный вывод
        log.info("Найдено {} популярных фильмов", films.size());
        for (Film film : films) {
            log.info("Фильм {} - '{}' - лайков: {}", film.getId(), film.getName(),
                    (film.getLikes() != null ? film.getLikes().size() : 0));
        }

        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = film.getGenres().stream()
                    .collect(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparingInt(Genre::getId))
                    ));

            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            uniqueGenres.forEach(genre -> {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            });

            log.info("Сохранены жанры для фильма ID {}: {}", film.getId(),
                    uniqueGenres.stream().map(Genre::getName).collect(Collectors.joining(", ")));
        }
    }

    private void deleteGenres(int filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
        log.info("Удалены жанры для фильма ID: {}", filmId);
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.id";
        List<Genre> genres = jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                film.getId());

        Set<Genre> sortedGenres = new LinkedHashSet<>(genres);
        film.setGenres(sortedGenres);

        log.info("Загружены {} жанров для фильма ID {}", genres.size(), film.getId());
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getInt("user_id"),
                film.getId());
        film.setLikes(new HashSet<>(likes));
        film.setRate(likes.size());

        log.info("Загружено {} лайков для фильма ID {}", likes.size(), film.getId());
    }

    private static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            // Проверяем, есть ли колонка likes_count в результате запроса
            int likesCount = 0;
            try {
                likesCount = rs.getInt("likes_count");
            } catch (SQLException e) {
                // Колонка не найдена, оставляем 0
            }
            film.setRate(likesCount);

            // Устанавливаем MPA рейтинг
            int mpaId = rs.getInt("mpa_rating_id");
            if (mpaId != 0) {
                MpaRating mpa = new MpaRating();
                mpa.setId(mpaId);

                String mpaName = rs.getString("mpa_name");
                if (mpaName == null) {
                    mpaName = getMpaNameById(mpaId);
                }
                mpa.setName(mpaName);

                String mpaDescription = rs.getString("mpa_description");
                if (mpaDescription == null) {
                    mpaDescription = getMpaDescriptionById(mpaId);
                }
                mpa.setDescription(mpaDescription);

                film.setMpa(mpa);
            }

            return film;
        }

        private String getMpaNameById(int id) {
            return switch (id) {
                case 1 -> "G";
                case 2 -> "PG";
                case 3 -> "PG-13";
                case 4 -> "R";
                case 5 -> "NC-17";
                default -> "";
            };
        }

        private String getMpaDescriptionById(int id) {
            return switch (id) {
                case 1 -> "Нет возрастных ограничений";
                case 2 -> "Рекомендуется присутствие родителей";
                case 3 -> "Детям до 13 лет просмотр не желателен";
                case 4 -> "Лицам до 17 лет обязательно присутствие взрослого";
                case 5 -> "Лицам до 18 лет просмотр запрещен";
                default -> "";
            };
        }
    }
}