package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreMpaStorage genreMpaStorage;

    public Film create(Film film) {
        validateFilmForCreate(film);

        if (film.getMpa() != null) {
            try {
                genreMpaStorage.getMpaRatingById(film.getMpa().getId());
            } catch (NotFoundException e) {
                throw new NotFoundException("MPA с ID " + film.getMpa().getId() + " не найден");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreMpaStorage.getGenreById(genre.getId());
                } catch (NotFoundException e) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + film.getId() + " не найден"));

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

        if (film.getMpa() != null) {
            try {
                genreMpaStorage.getMpaRatingById(film.getMpa().getId());
            } catch (Exception e) {
                throw new NotFoundException("MPA с ID " + film.getMpa().getId() + " не найден");
            }
            existingFilm.setMpa(film.getMpa());
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreMpaStorage.getGenreById(genre.getId());
                } catch (Exception e) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не найден");
                }
            }
            existingFilm.setGenres(film.getGenres());
        }

        return filmStorage.update(existingFilm);
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден"));
    }

    public void addLike(int filmId, int userId) {
        Film film = findById(filmId);
        userService.findById(userId);

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = findById(filmId);
        userService.findById(userId);

        if (film.getLikes() != null) {
            film.getLikes().remove(userId);
            filmStorage.update(film);
        }
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Service: Запрос {} популярных фильмов", count);
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