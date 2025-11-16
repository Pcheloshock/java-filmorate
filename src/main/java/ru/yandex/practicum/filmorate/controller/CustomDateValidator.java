package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class CustomDateValidator implements ConstraintValidator<PastOrEqual, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(PastOrEqual constraint) {
        minDate = LocalDate.of(1895, 12, 28);
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || !value.isBefore(minDate);
    }
}