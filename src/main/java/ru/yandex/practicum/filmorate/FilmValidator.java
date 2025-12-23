package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.time.LocalDate;

public class FilmValidator {
    public static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public static void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate != null && releaseDate.isBefore(MIN_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    public static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
    }

    public static void validateDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
    }

    public static void validateDuration(int duration) {
        if (duration <= 0) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }
    }
}