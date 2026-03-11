# ⚔️ CodeZilla — Battle Arena for Developers

> LeetCode meets Clash Royale. Real-time 1v1 algorithmic duels with cards, arenas, and chaos.

---

## 🎮 What is this?

**CodeZilla** — онлайн платформа для соревновательного программирования в реальном времени.

Зарегистрировался — и сразу в бой. Решай алгоритмические, SQL, Shell и другие задачи быстрее соперника. Побеждай. Прокачивайся. Открывай новые арены.

### Как это работает
- Формат боёв: **1v1** или **3v3**
- В начале раунда перед обоими игроками одновременно появляется задача
- Кто решил первым — получает очки (у каждой задачи своя стоимость)
- Перед боем можно **забанить темы задач**, которые не хочешь встречать
- Победа → очки → новая арена → более сложные задачи

### Система арен
Новичок попадает на Арену 1 — задачи лёгкого уровня (строки, массивы).  
С каждой победой открываются новые арены и темы: графы, DP, деревья и т.д.

### Карточки и сундуки 🃏
Выбивай карточки из сундуков — это баффы и дебаффы для боя:

| Карточка | Эффект |
|----------|--------|
| ❄️ Заморозка | Соперник не может писать код N секунд |
| 🔄 Инверсия | Экран соперника переворачивается |
| 🔍 Лупа | Шрифт соперника увеличивается в 5 раз |

Карточки можно **улучшать** — эффект становится сильнее.

### Достижения и прогресс бар
Во время боя отображаются ачивки соперника и его прогресс:
- скорость написания кода
- количество символов
- разблокированные достижения

---

## 🛠️ Tech Stack

- **Java 21** + **Spring Boot 3**
- **PostgreSQL**
- **Docker / Docker Compose**
- **Gradle**

---

## 🚀 Локальный запуск

### 1. Клонируй репозиторий
```bash
git clone git@github.com:Codzilla-HSE/backend.git
cd codzilla
```

### 2. Настрой конфиг
```bash
cp src/main/resources/application-dev.properties.example \
   src/main/resources/application-dev.properties
```

### 3. Запусти базу данных
```bash
docker-compose up -d
```

### 4. Собери и запусти приложение

**Mac / Linux:**
```bash
./gradlew clean build -x test
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Windows:**
```bash
gradlew.bat clean build -x test
gradlew.bat bootRun --args='--spring.profiles.active=dev'
```

### Остановить контейнер
```bash
docker-compose down -v
```

---

## 🐳 Запуск через Docker

```bash
./gradlew bootJar
docker build -t codzilla-backend .
docker-compose up
```

---

## ⚙️ Переменные окружения (прод)

| Переменная | Описание |
|------------|----------|
| `DB_URL` | JDBC URL базы данных |
| `DB_USERNAME` | Имя пользователя БД |
| `DB_PASSWORD` | Пароль БД |

```bash
export DB_URL=jdbc:postgresql://host:5432/proddb
export DB_USERNAME=produser
export DB_PASSWORD=supersecret

java -jar -Dspring.profiles.active=prod build/libs/backend-0.0.1.jar
```

---

## 🧪 Тесты

```bash
./gradlew test
```
