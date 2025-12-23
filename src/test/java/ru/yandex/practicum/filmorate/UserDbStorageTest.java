package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(TestJdbcConfig.class)  // Добавьте эту аннотацию!
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testCreateAndFindUser() {
        User user = User.builder()
                .email("test@email.com")
                .login("testLogin")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.create(user);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isPositive();

        Optional<User> foundUser = userStorage.findById(createdUser.getId());
        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u.getEmail()).isEqualTo("test@email.com")
                );
    }

    @Test
    public void testUpdateUser() {
        User user = User.builder()
                .email("old@email.com")
                .login("oldLogin")
                .name("Old Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User createdUser = userStorage.create(user);
        createdUser.setEmail("new@email.com");
        createdUser.setName("New Name");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("new@email.com");
        assertThat(updatedUser.getName()).isEqualTo("New Name");
    }

    @Test
    public void testFindAllUsers() {
        User user1 = User.builder()
                .email("user1@email.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User user2 = User.builder()
                .email("user2@email.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1995, 1, 1))
                .build();

        userStorage.create(user1);
        userStorage.create(user2);

        assertThat(userStorage.findAll()).hasSize(2);
    }
}