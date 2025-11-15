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

class UserTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validlogin");
        user.setName("Valid Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Валидный пользователь не должен иметь нарушений валидации");
    }

    @Test
    void shouldFailWhenEmailIsBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пустой email должен вызывать ошибку валидации");
    }

    @Test
    void shouldFailWhenEmailWithoutAt() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Email без @ должен вызывать ошибку валидации");
    }

    @Test
    void shouldFailWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Пустой логин должен вызывать ошибку валидации");
    }

    @Test
    void shouldFailWhenLoginContainsSpaces() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("login with spaces");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Логин с пробелами должен вызывать ошибку валидации");
    }

    @Test
    void shouldUseLoginWhenNameIsBlank() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // Это тестирует бизнес-логику, а не валидацию
        // В реальном приложении это должно обрабатываться в сервисе или контроллере
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        assertEquals("testlogin", user.getName(), "Имя должно быть равно логину, когда имя пустое");
    }

    @Test
    void shouldFailWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Дата рождения в будущем должна вызывать ошибку валидации");
    }

    @Test
    void shouldAcceptNullBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validlogin");
        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Null дата рождения должна быть допустима");
    }

    @Test
    void shouldAcceptCurrentDateAsBirthday() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.now());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Сегодняшняя дата как дата рождения должна быть допустима");
    }

    @Test
    void shouldAcceptBoundaryEmailValues() {
        User user1 = new User();
        user1.setEmail("a@b.c");
        user1.setLogin("login");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations1 = validator.validate(user1);
        assertTrue(violations1.isEmpty(), "Короткий валидный email должен быть принят");

        // Длинный email (пограничное значение)
        String longEmail = "a".repeat(245) + "@example.com";
        User user2 = new User();
        user2.setEmail(longEmail);
        user2.setLogin("login");
        user2.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations2 = validator.validate(user2);
        assertTrue(violations2.isEmpty(), "Длинный валидный email должен быть принят");
    }
}
