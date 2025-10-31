# Cloud Storage Backend (Netology Diploma)

## Требования
- JDK 17+
- Maven 3.8+
- (опционально) Docker Desktop для запуска через docker-compose

## Сборка
```bash
mvn clean package
```
Артефакт: `target/cloud-storage-backend-0.1.0.jar`

## Запуск локально (без Docker)
Настройки по умолчанию в `src/main/resources/application.yml`:
- PostgreSQL: `jdbc:postgresql://localhost:5432/cloud`, user/pass: `cloud/cloud`
- API base-path: `/cloud`

Шаги:
1) Установите PostgreSQL и создайте БД/пользователя:
```bash
psql -U postgres -h localhost -c "CREATE DATABASE cloud;"
psql -U postgres -h localhost -c "CREATE USER cloud WITH PASSWORD 'cloud';"
psql -U postgres -h localhost -c "GRANT ALL PRIVILEGES ON DATABASE cloud TO cloud;"
```
2) Запустите приложение:
```bash
java -jar target/cloud-storage-backend-0.1.0.jar
```
Сервис будет доступен на `http://localhost:8080/cloud`.

## Запуск через Docker
```bash
# если jar не собран
mvn clean package
# поднять БД и приложение
docker compose up --build
```

## FRONT
В `.env` фронта установите:
```
VUE_APP_BASE_URL=http://localhost:8080
```
Фронт будет вызывать `http://localhost:8080/cloud/...`.

## Примеры запросов
- Логин:
```bash
curl -X POST http://localhost:8080/cloud/login \
  -H "Content-Type: application/json" \
  -d '{"login":"user","password":"password"}'
```
- Загрузка файла:
```bash
curl -X POST "http://localhost:8080/cloud/file?filename=test.txt" \
  -H "auth-token: <TOKEN>" \
  -F "file=@/path/to/test.txt"
```
- Список файлов:
```bash
curl -X GET "http://localhost:8080/cloud/list?limit=10" -H "auth-token: <TOKEN>"
```

## Тесты
```bash
mvn test
```
Интеграционные тесты используют Testcontainers (PostgreSQL).
