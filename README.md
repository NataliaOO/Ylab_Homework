# Product Catalog Service - HTTP-сервис на сервлетах

Внутренний сервис маркетплейса для управления каталогом товаров.
Реализован на Jakarta Servlet API (без Spring), PostgreSQL, 
Liquibase, JDBC, Jackson, DTO + MapStruct, валидацией, ролевой авторизацией и Tomcat.
---
## 1. Возможности сервиса

### 📦 Каталог товаров
- CRUD-операции над товарами:
  - создание,
  - обновление,
  - удаление,
  - просмотр списка.
- Фильтрация и поиск по:
  - категории,
  - бренду,
  - диапазону цен,
  - тексту в названии/описании.
- DTO-уровень (ProductRequest, ProductDto).
- MapStruct для маппинга сущностей → DTO → сущности.

### 🔐 Авторизация и роли
- Два типа пользователей:
  - ADMIN — полный доступ (CRUD),
  - VIEWER — только просмотр и поиск.
- Логин / логаут через сервлет AuthServlet.
- Авторизация хранится через HttpSession.
- Ограничение прав в ProductServlet:
  - POST / PUT / DELETE доступны только ADMIN.
  - GET доступен всем авторизованным пользователям (или открытый — по настройкам).

### ⚡ Поиск с кэшированием
- Результаты поисковых запросов кешируются в памяти.
- Записываются метрики:
  - время поиска,
  - попадание в кэш / промах.

### 🗄 PostgreSQL + Liquibase
- Все сущности хранятся в БД:
  - catalog.product
  - catalog.users
  - catalog.audit
- Миграции схемы и тестовых данных выполняются Liquibase при старте.

---

## 2. Требования
- JDK: 17+
- Maven: 3.8+
- PostgreSQL: 16+ (локально или в Docker)
- Tomcat: 10+ (обязателен Jakarta-стек)
- Liquibase (XML changelog’и)
- JDBC
- MapStruct
- Jakarta Servlet API
- Testcontainers (PostgreSQL)
- Docker + docker-compose

---

## 3. Хранение данных и БД
Все данные теперь хранятся в **PostgreSQL**:
- **Таблицы сущностей** находятся в схеме `catalog`:
  - `catalog.product` — товары;
  - `catalog.audit` — записи аудита;
  - `catalog.users` — пользователи.
- **Идентификаторы** генерируются через **sequence** (используется `BIGSERIAL` / sequence в DDL).
- **Служебные таблицы Liquibase** находятся в отдельной схеме,  
  а таблицы доменных сущностей в `catalog`.

---

## 4. Миграции БД (Liquibase)

Все DDL-скрипты и скрипты предзаполнения выполняются **только Liquibase**.

- Changelog’и находятся в каталоге, например:
  - `src/main/resources/db/changelog/db.changelog-master.xml` — мастер-файл;
  - `src/main/resources/db/changelog/00-create-schema.xml` — создание схем (`catalog`, `service` и т.п.);
  - `src/main/resources/db/changelog/01-create-tables.xml` — создание таблиц (`product`, `audit`, `users`, последовательностей и индексов);
  - `src/main/resources/db/changelog/02-insert-data.xml` — предзаполнение тестовыми данными (пользователи, пары демо-товаров и т.п.).

---

## 5. PostgreSQL в Docker (docker-compose)

Для разработки используется Docker + docker-compose.
В корне проекта находится файл docker-compose.yml, который поднимает контейнер с PostgreSQL.

Запуск PostgreSQL через Docker

Из корня проекта:
```
docker-compose up -d
```
Проверить, что контейнер поднялся:
```
docker ps
```
---

## 6. Новые улучшения

### 🔍 AOP-аудит действий пользователя
Реализовано через аспект AuditAspect:
- аудит создания товара;
- аудит обновления;
- аудит удаления;
- аудит login/logout;
- каждая операция пишет запись в таблицу catalog.audit

### 🕒 AOP-логирование выполнения методов

Добавлен LoggingAspect, который:
- логирует вызовы сервисов и репозиториев;
- измеряет время выполнения каждого метода;
- записывает в лог Class.method — X ms.

Это улучшает наблюдаемость системы и позволяет быстро находить медленные операции.

## 7. Сборка WAR-файла
Из корня проекта:
```
mvn clean package
```
Артефакт появится в:
```
target/product-catalog-service.war
```

---

## 8. Запуск в Tomcat 10+
Скопировать product-catalog-service.war в:
```
apache-tomcat-10/webapps/
```
Запустить Tomcat:
Windows:
`bin/startup.bat`

Linux/macOS:
`bin/startup.sh`

Приложение будет доступно по адресу:

http://localhost:8080/product-catalog-service

---

## 9. API сервиса
   ### 🔐 Авторизация (AuthServlet)
| Метод | Endpoint           | Описание                          |
| ----- | ------------------ | --------------------------------- |
| POST  | `/api/auth/login`  | Логин, устанавливает сессию       |
| POST  | `/api/auth/logout` | Завершение сессии                 |
| GET   | `/api/auth/me`     | Информация о текущем пользователе |
Пример запроса входа:
```
POST /api/auth/login
Content-Type: application/json

{
"login": "admin",
"password": "admin"
}
```

### 📦 Работа с товарами (ProductServlet)
| Метод  | Endpoint             | Доступ       | Описание         |
| ------ | -------------------- | ------------ | ---------------- |
| GET    | `/api/products`      | ADMIN/VIEWER | Список + поиск + |
| POST   | `/api/products`      | ADMIN        | Создание         |
| PUT    | `/api/products/{id}` | ADMIN        | Обновление       |
| DELETE | `/api/products/{id}` | ADMIN        | Удаление         |

Примеры запросов поиска:
```
GET /api/products?category=ELECTRONICS&minPrice=1000&maxPrice=5000&text=iphone
```

---

## 10. Пользователи

Пользователи теперь хранятся в таблице catalog.users (через JdbcUserRepository).
Тестовые пользователи создаются миграциями или в коде инициализации БД.

Пример стандартных пользователей:

admin / admin — роль ADMIN

user / user — роль VIEWER

---

## 11. Тесты
Добавлены интеграционные тесты

Для запуска всех модульных тестов:

```
mvn test
```
