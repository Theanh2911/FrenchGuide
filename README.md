# FrenchGuide backend

Spring Boot 4.1.2, Java 21+, PostgreSQL và MongoDB.

## Cấu trúc mã nguồn

Package gốc hiện tại là `FrenchGuide`.

```text
src/main/java/FrenchGuide/
├── Application.java
├── config/
│   ├── DatabaseConfig.java
│   └── SecurityConfig.java
├── entity/
│   ├── postgres/
│   │   ├── User.java
│   │   └── SrsProgress.java
│   └── mongo/
│       ├── Vocabulary.java
│       └── Grammar.java
└── repository/
    ├── postgres/
    │   ├── UserRepository.java
    │   └── SrsProgressRepository.java
    └── mongo/
        ├── VocabularyRepository.java
        └── GrammarRepository.java
```

`DatabaseConfig` quét JPA entity tại `FrenchGuide.entity.postgres`, JPA repository tại
`FrenchGuide.repository.postgres` và MongoDB repository tại `FrenchGuide.repository.mongo`.
Các entity mẫu hiện chỉ khai báo ID; bổ sung trường nghiệp vụ khi thiết kế model.

## Cấu hình và chạy

`application.properties` chứa cấu hình chung và chọn `local` làm profile mặc định.
Thông tin kết nối và khóa JWT cho môi trường local nằm trong
`src/main/resources/application-local.yml`, đã bị Git bỏ qua.

```powershell
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
```

Test tích hợp yêu cầu cả PostgreSQL và MongoDB truy cập được. Test kiểm tra `SELECT 1`,
MongoDB `ping` và phân nhóm repository; không tự tạo/cập nhật bảng hoặc ghi dữ liệu.
Test bảo mật dùng khóa riêng dành cho test và không cần kết nối database.

## CORS và JWT

`SecurityConfig` dùng Bearer JWT HS256, không lưu session và cung cấp `JwtEncoder`,
`JwtDecoder`, `PasswordEncoder` để dùng khi triển khai đăng nhập.

| Biến môi trường | Ý nghĩa |
| --- | --- |
| `JWT_SECRET` | Khóa ký Base64, ít nhất 32 byte sau giải mã; local đã có khóa ngẫu nhiên dự phòng trong YAML riêng. |
| `JWT_ISSUER` | Giá trị claim `iss`; mặc định `frenchguide`. |
| `JWT_AUDIENCE` | Giá trị cần có trong claim `aud`; mặc định `frenchguide-api`. |
| `CORS_ALLOWED_ORIGINS` | Danh sách origin cách nhau bằng dấu phẩy; mặc định `http://localhost:3000,http://localhost:5173`. |

Token phải có chữ ký HS256 hợp lệ, `iss`, `aud`, `sub` không rỗng và `exp`.
Thời hạn token và `nbf` được kiểm tra bởi Spring Security.

Chỉ `POST /api/auth/login` và `POST /api/auth/register` được phép truy cập công khai.
Các đường dẫn còn lại yêu cầu Bearer token. Hiện chưa có controller/service đăng nhập,
đăng ký hay phát token; các đường dẫn công khai là cấu hình để triển khai các bước đó.
CORS cho phép gửi header `Authorization`, không dùng cookie xác thực.

Tham khảo API [Spring Security JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
và [CORS](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html).
