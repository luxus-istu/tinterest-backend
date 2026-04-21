# Tinterest

Внутреннее корпоративное приложение Т-Банка для знакомства сотрудников по общим интересам и хобби. Позволяет находить коллег с похожими увлечениями, общаться в чате и вступать в тематические группы.

## Возможности

- **Матчинг** — алгоритм подбора коллег по анкете интересов с отображением процента совместимости
- **Личные чаты** — создаются автоматически после подтверждения матча, работают через WebSocket
- **Групповые чаты** — тематические группы по интересам с возможностью создания своих
- **Профиль** — имя, фото, должность, город, отдел, список интересов
- **Административная панель** — управление пользователями и просмотр ключевых метрик

## Технологический стек

| Компонент | Технология |
|-----------|-----------|
| Бэкенд | Java, Spring Boot |
| Фронтенд | React |
| База данных | PostgreSQL |
| Кэш | Redis |
| Хранение изображений | MinIO (S3-совместимое) |
| Авторизация | JWT (Access + Refresh токены) |
| API | REST + WebSocket (чат) |
| Деплой | Docker |

## Требования

- [Docker](https://docs.docker.com/get-docker/) и [Docker Compose](https://docs.docker.com/compose/install/)

## Быстрый старт

### 1. Клонировать репозиторий

```bash
git clone <repository-url>
cd tinterest
```

### 2. Настроить переменные окружения

Основные переменные в `.env`:

```env
# База данных
POSTGRES_DB=tinterest
POSTGRES_USER=tinterest_user
POSTGRES_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000

# MinIO
MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=minioadmin
MINIO_BUCKET=tinterest-images

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Почта (для верификации аккаунтов)
MAIL_HOST=smtp.your-domain.com
MAIL_PORT=587
MAIL_USERNAME=noreply@your-domain.com
MAIL_PASSWORD=your_mail_password
```

### 3. Запустить приложение

```bash
docker compose up -d
```

После запуска будут доступны:

| Сервис | Адрес |
|--------|-------|
| Фронтенд | http://localhost:3000 |
| Бэкенд API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MinIO Console | http://localhost:9001 |

### 4. Остановить приложение

```bash
docker compose down
```

## Регистрация и вход

Регистрация доступна только для сотрудников с корпоративной почтой. После регистрации на указанный email придёт письмо для верификации аккаунта. После подтверждения при первом входе потребуется заполнить анкету интересов — без этого доступ к функциям приложения недоступен.

## API документация

Документация по всем эндпоинтам доступна через Swagger UI после запуска приложения: `http://localhost:8080/swagger-ui.html`
