package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
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

        // Загружаем полную информацию о фильме с MPA и жанрами
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
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

        // Обновляем жанры
        deleteGenres(film.getId());
        saveGenres(film);

        // Загружаем обновленный фильм с полной информацией
        return findById(film.getId()).orElse(film);
    }

    @Override
    public List<Film> findAll() {
        String filmsSql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id";

        List<Film> films = jdbcTemplate.query(filmsSql, new FilmRowMapper());

        // Загружаем все жанры одним запросом
        loadAllGenres(films);
        loadAllLikes(films);

        return films;
    }

    private void loadAllLikes(List<Film> films) {
        if (films.isEmpty()) return;

        // Создаем список ID фильмов
        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        // Создаем строку с плейсхолдерами для SQL IN
        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = String.format(
                "SELECT film_id, user_id FROM film_likes WHERE film_id IN (%s)",
                placeholders
        );

        // Загружаем все лайки за один запрос
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        // Группируем лайки по ID фильма
        Map<Integer, Set<Integer>> likesByFilmId = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");
            Integer userId = (Integer) row.get("user_id");

            likesByFilmId.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        }

        // Присваиваем лайки фильмам
        for (Film film : films) {
            Set<Integer> likes = likesByFilmId.getOrDefault(film.getId(), new HashSet<>());
            film.setLikes(likes);
            film.setRate(likes.size());
        }
    }

    private void loadAllGenres(List<Film> films) {
        if (films.isEmpty()) return;

        // Создаем список ID фильмов
        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        // Создаем строку с плейсхолдерами для SQL IN
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

        // Выполняем запрос
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, filmIds.toArray());

        // Группируем жанры по ID фильма
        Map<Integer, Set<Genre>> genresByFilmId = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer filmId = (Integer) row.get("film_id");

            Genre genre = new Genre();
            genre.setId((Integer) row.get("id"));
            genre.setName((String) row.get("name"));

            genresByFilmId.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }

        // Присваиваем жанры фильмам
        for (Film film : films) {
            Set<Genre> genres = genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>());
            film.setGenres(genres);
        }
    }

    @Override
    public Optional<Film> findById(int id) {
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id WHERE f.id = ?";

        return jdbcTemplate.query(sql, new FilmRowMapper(), id)
                .stream()
                .findFirst()
                .map(film -> {
                    loadGenres(film);
                    loadLikes(film);
                    return film;
                });
    }

    @Override
    public void delete(int id) {
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
        // Проверяем, есть ли уже лайк
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count == null || count == 0) {
            String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(insertSql, filmId, userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.id as mpa_id, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, m.id, m.name, m.description " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper(), count);
        films.forEach(film -> {
            loadGenres(film);
            loadLikes(film);
        });
        return films;
    }

    // Вспомогательные методы
    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            // Удаляем дубликаты и сохраняем порядок по id
            Set<Genre> uniqueGenres = film.getGenres().stream()
                    .collect(Collectors.toCollection(
                            () -> new TreeSet<>(Comparator.comparingInt(Genre::getId))
                    ));

            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            uniqueGenres.forEach(genre -> {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            });
        }
    }

    private void deleteGenres(int filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
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

        // Используем LinkedHashSet для сохранения порядка
        Set<Genre> sortedGenres = new LinkedHashSet<>(genres);
        film.setGenres(sortedGenres);
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getInt("user_id"),
                film.getId());
        film.setLikes(new HashSet<>(likes));
        film.setRate(likes.size());
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

            // Устанавливаем MPA рейтинг с названием и описанием
            int mpaId = rs.getInt("mpa_rating_id");
            if (mpaId != 0) {
                MpaRating mpa = new MpaRating();
                mpa.setId(mpaId);

                String mpaName = rs.getString("mpa_name");
                if (mpaName == null) {
                    // Если имя не получено из JOIN, используем дефолтное значение
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