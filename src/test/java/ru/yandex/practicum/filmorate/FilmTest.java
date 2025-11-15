package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
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
    }

    @Test
    void shouldFailWhenNameIsNull() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Null название должно вызывать ошибку валидации");
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
    void shouldFailWhenDurationIsZero() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Продолжительность 0 должна вызывать ошибку валидации");
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
    void shouldAcceptMinimalDuration() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(1);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Продолжительность 1 должна быть допустима");
    }

    @Test
    void shouldAcceptNullDescription() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription(null);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Null описание должно быть допустимо");
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
    void shouldAcceptBoundaryCases() {
        // Граничный случай: минимальная валидная продолжительность
        Film film1 = new Film();
        film1.setName("Min Duration Film");
        film1.setDescription("a".repeat(200)); // Максимальная длина
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(1);

        Set<ConstraintViolation<Film>> violations1 = validator.validate(film1);
        assertTrue(violations1.isEmpty(), "Фильм с граничными значениями должен быть валиден");

        // Граничный случай: очень длинное название (если нет ограничения)
        Film film2 = new Film();
        film2.setName("Very Long Film Name ".repeat(10)); // 200+ символов
        film2.setDescription("Short desc");
        film2.setReleaseDate(LocalDate.of(2000, 1, 1));
        film2.setDuration(9999); // Большая продолжительность

        Set<ConstraintViolation<Film>> violations2 = validator.validate(film2);
        // Длинное название допустимо, так как нет ограничения на длину названия
        assertTrue(violations2.isEmpty(), "Длинное название должно быть допустимо");
    }
}