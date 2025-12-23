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
    @NotNull(message = "Release date cannot be null")
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

}