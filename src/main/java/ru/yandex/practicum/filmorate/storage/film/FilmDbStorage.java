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
import java.util.Map;

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
        log.info("Storage: Получение всех фильмов");

        // Используем подзапрос для подсчета лайков
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description, " +
                "(SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) as likes_count " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "ORDER BY f.id";

        log.info("Storage: SQL запрос для findAll: {}", sql);

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());

        if (films.isEmpty()) {
            log.warn("Storage: В базе данных НЕТ фильмов!");
        } else {
            log.info("Storage: Найдено {} фильмов", films.size());
            for (Film film : films) {
                log.info("Storage:   Фильм: ID={}, название='{}', лайков={}",
                        film.getId(), film.getName(),
                        film.getRate() != null ? film.getRate() : 0);
            }
        }

        log.info("Storage: Загружены жанры и лайки для {} фильмов", films.size());
        return films;
    }

    @Override
    public int getTotalFilmsCount() {
        String sql = "SELECT COUNT(*) FROM films";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public Optional<Film> findById(int id) {
        log.info("Поиск фильма по ID: {}", id);

        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description, " +
                "(SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id) as likes_count " +
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

        // Проверяем существование фильма
        if (!existsById(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        // Проверяем существование пользователя
        // (нужен доступ к UserStorage или таблице users)
        String checkUserSql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (userCount == null || userCount == 0) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count == null || count == 0) {
            String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            try {
                jdbcTemplate.update(insertSql, filmId, userId);
                log.info("Лайк добавлен: filmId={}, userId={}", filmId, userId);
            } catch (DataIntegrityViolationException e) {
                log.error("Ошибка при добавлении лайка: {}", e.getMessage());
                throw new DataIntegrityViolationException("Не удалось добавить лайк");
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
        log.info("Storage: Получение {} популярных фильмов", count);

        // Упрощенный запрос
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, " +
                "m.description as mpa_description, " +
                "COALESCE(fl.likes_count, 0) as likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN (SELECT film_id, COUNT(*) as likes_count " +
                "           FROM film_likes GROUP BY film_id) fl " +
                "ON f.id = fl.film_id " +
                "ORDER BY likes_count DESC, f.id DESC " +
                "LIMIT ?";

        log.info("Storage: SQL запрос для популярных фильмов: {}", sql);

        List<Film> popularFilms = jdbcTemplate.query(sql, new FilmRowMapper(), count);
        log.info("Storage: Получено {} фильмов из БД", popularFilms.size());

        if (!popularFilms.isEmpty()) {
            loadAllGenres(popularFilms);
            loadAllLikes(popularFilms); // Исправленный без setRate

            // Выводим информацию для отладки
            for (int i = 0; i < popularFilms.size(); i++) {
                Film film = popularFilms.get(i);
                log.info("Storage: Популярный фильм {}: ID={}, название='{}', лайков={}",
                        i + 1, film.getId(), film.getName(),
                        film.getRate());
            }
        }

        return popularFilms;
    }

    private void loadAllLikes(List<Film> films) {
        if (films.isEmpty()) {
            log.info("Storage: Нет фильмов для загрузки лайков");
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

        log.debug("Storage: Загрузка лайков для фильмов: {}", filmIds);

        Map<Integer, Set<Integer>> likesByFilmId = new HashMap<>();
        jdbcTemplate.query(sql, filmIds.toArray(), rs -> {
            Integer filmId = rs.getInt("film_id");
            Integer userId = rs.getInt("user_id");
            likesByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        for (Film film : films) {
            Set<Integer> likes = likesByFilmId.getOrDefault(film.getId(), new HashSet<>());
            film.setLikes(likes);
            // УБЕДИТЕСЬ, что ЭТОЙ СТРОКИ НЕТ: film.setRate(likes.size());
        }
    }

    private void loadAllGenres(List<Film> films) {
        if (films.isEmpty()) {
            log.info("Storage: Нет фильмов для загрузки жанров");
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

        log.debug("Storage: Загрузка жанров для фильмов: {}", filmIds);

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
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = film.getGenres().stream()
                    .collect(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparingInt(Genre::getId))
                    ));

            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            // Создаем список аргументов для batchUpdate
            List<Object[]> batchArgs = new ArrayList<>();
            for (Genre genre : uniqueGenres) {
                batchArgs.add(new Object[]{film.getId(), genre.getId()});
            }

            jdbcTemplate.batchUpdate(sql, batchArgs);

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
        log.info("Загружено {} лайков для фильма ID {}", likes.size(), film.getId());
    }

    private static class FilmRowMapper implements RowMapper<Film> {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));

            java.sql.Date releaseDate = rs.getDate("release_date");
            if (releaseDate != null) {
                film.setReleaseDate(releaseDate.toLocalDate());
            }

            film.setDuration(rs.getInt("duration"));

            // Прямое получение likes_count без try-catch
            int likesCount = rs.getInt("likes_count");
            // Проверяем wasNull после getInt
            if (rs.wasNull()) {
                likesCount = 0;
            }
            film.setRate(likesCount);

            // Устанавливаем MPA рейтинг
            int mpaId = rs.getInt("mpa_rating_id");
            if (!rs.wasNull() && mpaId > 0) {
                MpaRating mpa = new MpaRating();
                mpa.setId(mpaId);
                mpa.setName(rs.getString("mpa_name"));
                mpa.setDescription(rs.getString("mpa_description"));
                film.setMpa(mpa);
            }

            log.debug("FilmRowMapper: Фильм ID={}, name='{}', likes={}",
                    film.getId(), film.getName(), likesCount);

            return film;
        }
    }
}