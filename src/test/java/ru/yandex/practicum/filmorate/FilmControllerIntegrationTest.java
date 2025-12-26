package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // Добавлена аннотация для профиля
@TestPropertySource(properties = "filmorate.storage.type=memory")
class FilmControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateValidFilm() throws Exception {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Film"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.duration").value(120));
    }

    @Test
    void shouldGetAllFilms() throws Exception {
        // Сначала создаем фильм
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated());

        // Затем получаем все фильмы
        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Film"));
    }

    @Test
    void shouldRejectFilmWithEmptyName() throws Exception {
        Film film = Film.builder()
                .name("")
                .description("Test Description")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectFilmWithTooLongDescription() throws Exception {
        String longDescription = "A".repeat(201);
        Film film = Film.builder()
                .name("Test Film")
                .description(longDescription)
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAcceptFilmWithMaxLengthDescription() throws Exception {
        String maxDescription = "A".repeat(200);
        Film film = Film.builder()
                .name("Test Film")
                .description(maxDescription)
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value(maxDescription));
    }

    @Test
    void shouldRejectFilmWithNegativeDuration() throws Exception {
        Film film = Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(1999, 12, 28))
                .duration(-120)
                .mpa(new MpaRating(1, "G", "Нет возрастных ограничений"))
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }
}