package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(TestJdbcConfig.class)  // Импортируем тестовую конфигурацию
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;

    @Test
    public void testCreateAndFindFilm() {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isPositive();

        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());
        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f.getName()).isEqualTo("Test Film")
                );
    }
}