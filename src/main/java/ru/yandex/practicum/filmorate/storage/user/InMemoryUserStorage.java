package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);

    @Override
    public User create(User user) {
        int id = nextId.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> findById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void delete(int id) {
        users.remove(id);
    }

    @Override
    public boolean existsById(int id) {
        return users.containsKey(id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = users.get(userId);
        if (user != null) {
            if (user.getFriends() == null) {
                user.setFriends(new HashSet<>());
            }
            user.getFriends().add(friendId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = users.get(userId);
        if (user != null && user.getFriends() != null) {
            user.getFriends().remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        User user = users.get(userId);
        if (user == null || user.getFriends() == null) {
            return new ArrayList<>();
        }
        return user.getFriends().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        User user = users.get(userId);
        User otherUser = users.get(otherId);

        if (user == null || otherUser == null ||
                user.getFriends() == null || otherUser.getFriends() == null) {
            return new ArrayList<>();
        }

        return user.getFriends().stream()
                .filter(friendId -> otherUser.getFriends().contains(friendId))
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}