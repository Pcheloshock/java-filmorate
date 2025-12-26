package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        validateUserForCreate(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        User existingUser = userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + user.getId() + " не найден"));

        if (user.getEmail() != null) {
            if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new ValidationException("Email не может быть пустым и должен содержать @");
            }
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null) {
            if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ValidationException("Логин не может быть пустым и содержать пробелы");
            }
            existingUser.setLogin(user.getLogin());
        }

        if (user.getName() != null) {
            if (user.getName().isBlank()) {
                existingUser.setName(user.getLogin());
            } else {
                existingUser.setName(user.getName());
            }
        }

        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(java.time.LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
            existingUser.setBirthday(user.getBirthday());
        }

        return userStorage.update(existingUser);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(int id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }

    public void addFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (userId == friendId) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }
        user.getFriends().add(friendId);
        userStorage.update(user);
    }

    public void removeFriend(int userId, int friendId) {
        User user = findById(userId);
        User friend = findById(friendId); // Проверяем существование друга

        if (user.getFriends() != null) {
            // Если друг есть в списке - удаляем
            if (user.getFriends().contains(friendId)) {
                user.getFriends().remove(friendId);
                userStorage.update(user);
            }
            // Если друга нет в списке - тоже OK (идемпотентность)
        }
    }

    public List<User> getFriends(int userId) {
        User user = findById(userId);
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return new ArrayList<>();
        }
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = findById(userId);
        User otherUser = findById(otherId);

        Set<Integer> userFriends = user.getFriends() != null ? user.getFriends() : new HashSet<>();
        Set<Integer> otherFriends = otherUser.getFriends() != null ? otherUser.getFriends() : new HashSet<>();

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::findById)
                .collect(Collectors.toList());
    }

    private void validateUserForCreate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email не может быть пустым и должен содержать @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}