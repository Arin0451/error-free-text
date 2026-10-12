# Error Free Text

Веб-приложение автоматической корректировки текста. Система получает запрос с текстом от пользователя, в ответ отдает текст с исправленными опечатками.

## Стек технологий

- **Java:** 17+
- **Spring Boot:** 3.3.4
- **Gradle:** 8.10.2
- **PostgreSQL:** 16 (для Docker-режима)
- **H2:** встроенная (для локального запуска без Docker)
- **Docker:** docker-compose

## Требования

- Docker Desktop (для запуска через docker-compose)
- Java 17+ (для локального запуска)

## Запуск

```bash
docker compose up -d --build
```

После запуска приложение доступно на `http://localhost:8080` с PostgreSQL.

## API

### Создание задачи на корректировку текста

```http
POST /tasks
Content-Type: application/json

{
    "text": "Привет, как делла?",
    "language": "RU"
}
```

**Ответ (201 Created):**

```json
{
  "taskId": "44bd78dc-d08c-41c6-b87d-fb82046bd470"
}
```

### Получение результата по ID задачи

```http
GET /tasks/{taskId}
```

**Ответы:**

- **Задача в процессе (200 OK):**

```json
{
  "status": "IN_PROGRESS"
}
```

- **Зачада завершена (200 OK):**

```json
{
  "status": "COMPLETED",
  "correctedText": "Привет, как дела?"
}
```

- **Задача завершилась с ошибкой (200 OK):**

```json
{
  "status": "FAILED",
  "errorMessage": "Failed to call Yandex Speller API"
}
```

- **Задача не найдена (404 Not Found):**

```json
{
  "errorMessage": "Task with id: 44bd78dc-d08c-41c6-b87d-fb82046bd470 not found",
  "errorCode": 40401,
  "timestamp": "2026-10-12T18:31:42.201",
  "path": "/tasks/44bd78dc-d08c-41c6-b87d-fb82046bd470"
}
```

## Проверка работы

### 1. Создание задачи

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"text":"Привет, как делла?","language":"RU"}'
```

Ожидаемый ответ — `taskId` вида UUID.

### 2. Проверка статуса

```bash
curl http://localhost:8080/tasks/{taskId}
```

Первые несколько секунд статус будет `NEW` или `IN_PROGRESS`. Через ~5-10 секунд (после того как Scheduler обработает задачу) статус изменится на `COMPLETED`, и в ответе появится `correctedText`.

### 3. Проверка валидации

**Некорректный язык:**

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"text":"Hello world","language":"FR"}'
```

**Слишком короткий текст:**

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"text":"Hi","language":"EN"}'
```

**Текст только из цифр и спецсимволов:**

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"text":"123 !!!","language":"EN"}'
```

Все эти запросы должны вернуть ошибку `40001` с описанием.

### 4. Проверка длинного текста

Приложение автоматически разбивает текст длиннее 10 000 символов на части и корректирует их по отдельности.

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"text":"<текст длиннее 10000 символов>","language":"EN"}'
```

### 5. Проверка опций API Яндекса

- Если текст содержит цифры — включается `IGNORE_DIGITS`
- Если текст содержит URL — включается `IGNORE_URLS`
- `FIND_REPEAT_WORDS` и `IGNORE_CAPITALIZATION` всегда выключены

## Тесты

```bash
./gradlew test
```

Запускает 31 юнит-тест, покрывающих все ключевые компоненты приложения.

## Структура проекта

```
src/main/java/com/errorfreetext/
├── config/              # Конфигурация Spring
├── controller/          # REST-контроллеры
├── dto/                 # Объекты передачи данных
├── entity/              # JPA-сущности
├── exception/           # Исключения и их обработка
├── repository/          # Репозитории JPA
├── scheduler/           # Scheduler для обработки задач
├── service/             # Бизнес-логика
└── util/                # Утилиты (разбиение текста, определение опций)
```

