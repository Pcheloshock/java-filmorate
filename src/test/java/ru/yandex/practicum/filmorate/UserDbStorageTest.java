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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest(includeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {UserDbStorage.class}
))
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "filmorate.storage.type=jdbc")
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Очистка таблиц перед каждым тестом
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM users");

        testUser = User.builder()
                .email("test@test.com")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void testCreateAndFindUser() {
        User createdUser = userStorage.create(testUser);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();

        var foundUser = userStorage.findById(createdUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@test.com");
        assertThat(foundUser.get().getLogin()).isEqualTo("testuser");
    }

    @Test
    void testUpdateUser() {
        User createdUser = userStorage.create(testUser);
        assertThat(createdUser.getId()).isNotNull();

        User updatedUser = User.builder()
                .id(createdUser.getId())
                .email("updated@test.com")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        User result = userStorage.update(updatedUser);
        assertThat(result.getEmail()).isEqualTo("updated@test.com");
        assertThat(result.getLogin()).isEqualTo("updateduser");

        var foundUser = userStorage.findById(createdUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void testFindAllUsers() {
        userStorage.create(testUser);

        User secondUser = User.builder()
                .email("second@test.com")
                .login("seconduser")
                .name("Second User")
                .birthday(LocalDate.of(1992, 1, 1))
                .build();
        userStorage.create(secondUser);

        var allUsers = userStorage.findAll();
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting("email")
                .containsExactlyInAnyOrder("test@test.com", "second@test.com");
    }
}