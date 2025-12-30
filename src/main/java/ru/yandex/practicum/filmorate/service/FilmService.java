package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreMpaStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreMpaStorage genreMpaStorage;
    private final JdbcTemplate jdbcTemplate; // Добавляем JdbcTemplate для эффективных запросов

    public Film create(Film film) {
        log.info("Создание фильма: name='{}', description length={}, releaseDate={}, duration={}, genres={}",
                film.getName(),
                film.getDescription() != null ? film.getDescription().length() : 0,
                film.getReleaseDate(),
                film.getDuration(),
                film.getGenres() != null ? film.getGenres() : "null");

        validateAndLoadGenres(film);

        if (film.getMpa() != null) {
            log.info("Проверка MPA с ID: {}", film.getMpa().getId());
            genreMpaStorage.getMpaRatingById(film.getMpa().getId());
            log.info("MPA найден");
        }

        validateFilmForCreate(film);

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + film.getId() + " не найден"));

        // 1. Проверяем и обновляем жанры одним запросом
        if (film.getGenres() != null) {
            log.info("Проверка жанров для обновления: {}", film.getGenres());
            validateAndLoadGenres(film);
            existingFilm.setGenres(film.getGenres());
        }

        if (film.getMpa() != null) {
            genreMpaStorage.getMpaRatingById(film.getMpa().getId());
            existingFilm.setMpa(film.getMpa());
        }

        if (film.getName() != null) {
            if (film.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            existingFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() > 200) {
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            existingFilm.setDescription(film.getDescription());
        }

        if (film.getReleaseDate() != null) {
            validateReleaseDate(film.getReleaseDate());
            existingFilm.setReleaseDate(film.getReleaseDate());
        }

        if (film.getDuration() != null) {
            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность должна быть положительным числом");
            }
            existingFilm.setDuration(film.getDuration());
        }

        return filmStorage.update(existingFilm);
    }

    private void validateAndLoadGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        log.info("Проверка жанров: {}", film.getGenres());

        // Собираем все ID жанров
        Set<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        if (genreIds.isEmpty()) {
            return;
        }

        // Создаем строку с плейсхолдерами для SQL IN запроса
        String placeholders = genreIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        // Выполняем один запрос для получения всех жанров
        String sql = String.format("SELECT id, name FROM genres WHERE id IN (%s)", placeholders);

        List<Genre> foundGenres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, genreIds.toArray());

        // Проверяем, все ли запрошенные жанры найдены
        Set<Integer> foundGenreIds = foundGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        for (Integer genreId : genreIds) {
            if (!foundGenreIds.contains(genreId)) {
                throw new NotFoundException("Жанр с ID " + genreId + " не найден");
            }
        }

        // Обновляем объекты жанров в фильме (чтобы были правильные названия)
        // Создаем Map для быстрого поиска жанров по ID
        java.util.Map<Integer, Genre> genreMap = foundGenres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));

        Set<Genre> validatedGenres = film.getGenres().stream()
                .map(genre -> genreMap.get(genre.getId()))
                .collect(Collectors.toSet());

        film.setGenres(validatedGenres);
        log.info("Проверено {} жанров, все найдены", genreIds.size());
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(int filmId, int userId) {
        log.info("Service: Добавление лайка filmId={}, userId={}", filmId, userId);

        // Проверяем существование фильма
        findById(filmId); // Бросает NotFoundException если не найден

        // Проверяем существование пользователя
        userService.findById(userId); // Бросает NotFoundException если не найден

        // Используем оптимизированный метод из FilmStorage
        filmStorage.addLike(filmId, userId);
        log.info("Service: Лайк добавлен успешно");
    }

    public void removeLike(int filmId, int userId) {
        log.info("Service: Удаление лайка filmId={}, userId={}", filmId, userId);

        // Проверяем существование фильма
        findById(filmId); // Бросает NotFoundException если не найден

        // Проверяем существование пользователя
        userService.findById(userId); // Бросает NotFoundException если не найден

        // Используем оптимизированный метод из FilmStorage
        filmStorage.removeLike(filmId, userId);
        log.info("Service: Лайк удален успешно");
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Service: Запрос {} популярных фильмов", count);

        // Проверяем валидность параметра count
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }

        List<Film> films = filmStorage.getPopularFilms(count);
        log.info("Service: Получено {} фильмов из хранилища", films.size());
        return films;
    }

    private void validateFilmForCreate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза обязательна");
        }

        validateReleaseDate(film.getReleaseDate());

        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (releaseDate.isBefore(minDate)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}