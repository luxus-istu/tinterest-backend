# Tinterest Postman demo

## 1. Start backend

From the project root:

```powershell
docker compose up -d tinterest-db minio redis redis-insight
.\mvnw.cmd spring-boot:run
```

The API starts at:

```text
https://localhost:8443
```

In Postman, open `Settings -> General` and turn off `SSL certificate verification`, because the local backend uses a self-signed HTTPS certificate.

If you prefer plain HTTP for the demo, start the app like this and set `baseUrl` in the Postman environment to `http://localhost:8080`:

```powershell
$env:SSL_ENABLED="false"
$env:SERVER_PORT="8080"
.\mvnw.cmd spring-boot:run
```

## 2. Import into Postman

Import these two files:

```text
postman/Tinterest.postman_collection.json
postman/Tinterest.local.postman_environment.json
```

Select the `Tinterest Local` environment in the top-right corner of Postman.

## 3. Demo flow

Run requests from the `Demo Flow` folder in order.

After `01 Register user 1`, copy the code from backend logs:

```text
Verification code for demo.user1...: 123456
```

Paste it into the Postman environment variable `verificationCode`, then run `02 Verify email user 1`.

After `06 Register user 2`, copy the second code into `verificationCode2`, then run `07 Verify email user 2`.

The login requests automatically save `accessToken`. The profile requests save `userId` and `otherUserId`. The direct chat request saves `chatId`.

Good mentor demo path:

```text
Register user 1 -> verify -> login -> complete profile
Register user 2 -> verify -> login -> complete profile
Login user 1 again
List interests
Get recommendations
Swipe LIKE
Create direct chat
Send message
Get chat messages
List chats
```

## 4. Useful URLs

```text
Swagger UI:  https://localhost:8443/swagger-ui.html
OpenAPI:     https://localhost:8443/v3/api-docs
MinIO UI:    http://localhost:9001
Redis UI:    http://localhost:5540
```

Default local MinIO credentials from `.env.example`:

```text
minioadmin / minioadmin
```
