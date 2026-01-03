package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final FilmService filmService;

    @PostMapping("/create-five-films")
    public List<Film> createFiveFilms() {
        List<Film> films = new ArrayList<>();

        // Фильм 1 (корректный)
        Film film1 = Film.builder()
                .name("Фильм 1")
                .description("Описание 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();
        films.add(filmService.create(film1));

        // Фильм 2 (корректный)
        Film film2 = Film.builder()
                .name("Фильм 2")
                .description("A".repeat(200))  // Ровно 200 символов
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(90)
                .mpa(new MpaRating(2, "PG", "Рекомендуется присутствие родителей"))
                .build();
        films.add(filmService.create(film2));

        // Фильм 3 (пустое название - должно быть исправлено)
        Film film3 = Film.builder()
                .name("")  // Пустое название
                .description("Описание 3")
                .releaseDate(LocalDate.of(2002, 1, 1))
                .duration(150)
                .mpa(new MpaRating(3, "PG-13", "Детям до 13 лет просмотр не желателен"))
                .build();
        films.add(filmService.create(film3));

        // Фильм 4 (описание 201 символ - должно быть обрезано)
        Film film4 = Film.builder()
                .name("Фильм 4")
                .description("A".repeat(201))  // 201 символ
                .releaseDate(LocalDate.of(2003, 1, 1))
                .duration(180)
                .mpa(new MpaRating(4, "R", "Лицам до 17 лет обязательно присутствие взрослого"))
                .build();
        films.add(filmService.create(film4));

        // Фильм 5 (отрицательная продолжительность - должна быть исправлена)
        Film film5 = Film.builder()
                .name("Фильм 5")
                .description("Описание 5")
                .releaseDate(LocalDate.of(2004, 1, 1))
                .duration(-60)  // Отрицательная продолжительность
                .mpa(new MpaRating(5, "NC-17", "Лицам до 18 лет просмотр запрещен"))
                .build();
        films.add(filmService.create(film5));

        log.info("Создано {} фильмов", films.size());
        return films;
    }
}