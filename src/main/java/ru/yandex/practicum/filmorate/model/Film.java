package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    // При создании фильма id всегда null, пока БД не присвоит значение
    private Integer id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    @Builder.Default
    private Set<Integer> likes = new HashSet<>();

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    private MpaRating mpa;
    private Integer rate;

    public boolean isValid() {
        // 1. Название обязательно
        if (name == null || name.isBlank()) {
            return false;
        }

        // 2. Описание: если есть, то не длиннее 200 символов
        if (description != null && description.length() > 200) {
            return false;
        }

        // 3. Дата релиза: может быть null (для частичных обновлений)
        // Если указана, должна быть не раньше 28.12.1895
        if (releaseDate != null && releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            return false;
        }

        // 4. Продолжительность: > 0 (аннотация @Positive уже проверяет)
        // duration может быть null, это допустимо для частичных обновлений
        if (duration != null && duration <= 0) {
            return false;
        }

        return true;
    }
}