package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public Film create(Film film) {
        validateFilmForCreate(film);

        // Если имя пустое, устанавливаем логин (но это для User, а не Film!)
        // Для фильма, вероятно, это не нужно, но оставим по аналогии с User
        if (film.getName() != null && film.getName().isBlank()) {
            // Что делать, если имя пустое? Возможно, бросить исключение
            throw new ValidationException("Название не может быть пустым");
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Film existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + film.getId() + " не найден"));

        // Частичное обновление - ТОЛЬКО если передано значение
        if (film.getName() != null) {
            if (film.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            existingFilm.setName(film.getName());
        }

        if (film.getDescription() != null) {
            // Длина 200 допустима! (тест shouldAcceptDescriptionAtMaxLength)
            if (film.getDescription().length() > 200) {
                throw new ValidationException("Максимальная длина описания — 200 символов");
            }
            existingFilm.setDescription(film.getDescription());
        }

        // ВАЖНО: releaseDate может быть null для частичного обновления!
        if (film.getReleaseDate() != null) {
            validateReleaseDate(film.getReleaseDate());
            existingFilm.setReleaseDate(film.getReleaseDate());
        }

        // Для int используем != 0, так как 0 - значение по умолчанию
        if (film.getDuration() != 0) {
            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность должна быть положительным числом");
            }
            existingFilm.setDuration(film.getDuration());
        }

        if (film.getGenres() != null) {
            existingFilm.setGenres(film.getGenres());
        }

        if (film.getMpa() != null) {
            existingFilm.setMpa(film.getMpa());
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
        userService.findById(userId); // Проверяем существование пользователя

        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        film.getLikes().add(userId);
        filmStorage.update(film); // Сохраняем изменения
    }

    public void removeLike(int filmId, int userId) {
        Film film = findById(filmId);
        userService.findById(userId); // Проверяем существование пользователя

        if (film.getLikes() != null) {
            film.getLikes().remove(userId);
            filmStorage.update(film); // Сохраняем изменения
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> {
                    int likes1 = f1.getLikes() != null ? f1.getLikes().size() : 0;
                    int likes2 = f2.getLikes() != null ? f2.getLikes().size() : 0;
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }

    private void validateFilmForCreate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }

        // Изменено: проверяем только если description != null
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        // Изменено: проверяем releaseDate != null (как в тестах)
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза обязательна");
        }

        validateReleaseDate(film.getReleaseDate());

        // Изменено: продолжительность > 0 (не >= 1)
        if (film.getDuration() <= 0) {
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