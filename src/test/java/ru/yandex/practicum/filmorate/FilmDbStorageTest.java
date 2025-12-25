package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ContextConfiguration(classes = TestJdbcConfig.class)
@TestPropertySource(properties = {
        "filmorate.storage.type=jdbc"
})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    private Film testFilm;

    @BeforeEach
    void setUp() {
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
    }
}