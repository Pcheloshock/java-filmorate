package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void shouldCreateValidFilm() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty(), "Валидный фильм не должен иметь нарушений валидации");
    }

    @Test
    void shouldFailWhenNameIsBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Пустое название должно вызывать ошибку валидации");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Название не может быть пустым")));
    }

    @Test
    void shouldFailWhenDescriptionIsTooLong() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("a".repeat(201)); // 201 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Описание длиннее 200 символов должно вызывать ошибку валидации");
    }

    @Test
    void shouldAcceptDescriptionAtMaxLength() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("a".repeat(200)); // Ровно 200 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Описание длиной 200 символов должно быть допустимо");
    }

    @Test
    void shouldFailWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Отрицательная продолжительность должна вызывать ошибку валидации");
    }

    @Test
    void shouldFailWhenReleaseDateIsNull() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(null);
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Null дата релиза должна вызывать ошибку валидации");
    }

    @Test
    void shouldAcceptMinimalDuration() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Продолжительность 1 должна быть допустима");
    }
}