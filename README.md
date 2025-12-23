# java-filmorate
Template repository for Filmorate project.

CREATE TABLE users (
user_id INT PRIMARY KEY AUTO_INCREMENT,
email VARCHAR(255) NOT NULL UNIQUE,
login VARCHAR(255) NOT NULL UNIQUE,
name VARCHAR(255),
birthday DATE NOT NULL
);

CREATE TABLE mpa_ratings (
mpa_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(10) NOT NULL UNIQUE,
description VARCHAR(255)
);

CREATE TABLE films (
film_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(255) NOT NULL,
description VARCHAR(200),
release_date DATE NOT NULL,
duration INT NOT NULL CHECK (duration > 0),
mpa_id INT,
FOREIGN KEY (mpa_id) REFERENCES mpa_ratings(mpa_id)
);

CREATE TABLE genres (
genre_id INT PRIMARY KEY AUTO_INCREMENT,
name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE film_genres (
film_id INT NOT NULL,
genre_id INT NOT NULL,
PRIMARY KEY (film_id, genre_id),
FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

CREATE TABLE likes (
film_id INT NOT NULL,
user_id INT NOT NULL,
PRIMARY KEY (film_id, user_id),
FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE friendships (
user_id INT NOT NULL,
friend_id INT NOT NULL,
status ENUM('UNCONFIRMED', 'CONFIRMED') DEFAULT 'UNCONFIRMED',
PRIMARY KEY (user_id, friend_id),
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE
);

┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   users     │     │   films     │     │ mpa_ratings │
├─────────────┤     ├─────────────┤     ├─────────────┤
│ user_id (PK)│◄────┤ mpa_id (FK) │────►│ mpa_id (PK) │
│ email       │     │ film_id (PK)│     │ name        │
│ login       │     │ name        │     │ description │
│ name        │     │ description │     └─────────────┘
│ birthday    │     │ release_date│
└─────────────┘     │ duration    │     ┌─────────────┐
│                   └─────────────┘     │  genres     │
│                     │                 ├─────────────┤
│                     │                 │ genre_id(PK)│
│          ┌───────-───────┐            │ name        │
│          │       │       │            └─────────────┘
│          ▼       ▼       ▼              │
│    ┌─────────────┐      ┌─────────────┐ │
└──► │   likes     │      │film_genres  │◄┘
     ├─────────────┤      ├─────────────┤
     │ film_id(FK) │      │ film_id(FK) │
     │ user_id (FK)│      │ genre_id(FK)│
     └─────────────┘      └─────────────┘
        │
┌─────────────┐
│friendships  |
├─────────────┤
│ user_id (FK)│
│ friend_id(FK)│
│ status      │
└─────────────┘

