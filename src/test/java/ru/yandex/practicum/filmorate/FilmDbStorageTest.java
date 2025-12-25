package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreMpaDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {FilmDbStorage.class, GenreMpaDbStorage.class}
))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "filmorate.storage.type=jdbc")
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GenreMpaDbStorage genreMpaDbStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM films");

        testFilm = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();
    }

    @Test
    void testCreateAndFindFilm() {
        Film createdFilm = filmStorage.create(testFilm);
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();

        var foundFilm = filmStorage.findById(createdFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Test Film");
        assertThat(foundFilm.get().getDescription()).isEqualTo("Test Description");
        assertThat(foundFilm.get().getDuration()).isEqualTo(120);
        assertThat(foundFilm.get().getMpa()).isNotNull();
        assertThat(foundFilm.get().getMpa().getId()).isEqualTo(1);
        assertThat(foundFilm.get().getMpa().getName()).isEqualTo("G");
    }
}