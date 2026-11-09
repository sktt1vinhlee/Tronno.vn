# TRONNO (Trốn nợ)

Ứng dụng quản lý các khoản nợ **bạn đang phải trả**: theo dõi lịch trả gốc + lãi, ghi nhận & đảo bút toán, nhắc hạn tự động hằng ngày, và tự soạn thư bút nợ.

## Cấu trúc

- `client/` — React 18 + Vite + React Router + Recharts
- `server/` — Spring Boot 4 (Java 17), Spring Security + JWT, MySQL 8

## Tính năng

| Nhóm | Mô tả |
| --- | --- |
| Quản lý khoản nợ | Nhiều loại nợ (ngân hàng, thẻ tín dụng, trả góp 0%, vay app, bạn bè), lịch trả gốc đều / niên kim / chỉ trả lãi, lãi suất thả nổi, gia hạn hợp đồng, ghi nhận & đảo bút toán khoản trả |
| Tính lãi & phí | Lãi theo dư nợ (REDUCING) hoặc trên gốc, phí phạt trễ theo %/ngày hoặc số tiền cố định, phí tất toán sớm, ngày thông tin trễ hạn (grace) |
| Nhắc hạn tự động | Mỗi kỳ trả chưa được trả sẽ được nhắc **mỗi ngày**, bắt đầu từ **5 ngày trước hạn** và tiếp tục cho tới khi khoản nợ được thanh toán |
| Bảng điều khiển | Tổng nợ còn lại, tỷ lệ DTI, ngày tất toán, dòng tiền 12 tháng, đường cong tất toán, cảnh báo dòng tiền 7 ngày tới |
| Né chủ nợ & cảnh báo tín dụng đen | Sinh thư bút nợ theo giọng văn, kiểm tra trần lãi suất hợp pháp 20%/năm (Điều 298 Bộ luật Dân sự 2015) |

## Tài khoản demo (mật khẩu `123456aA@`)

- `member@tronno.vn` — người dùng
- `admin@tronno.vn` — quản trị viên

Tài khoản được tạo tự động khi khởi động backend lần đầu (xem `DataSeeder`).
**Không seed khoản nợ** — bạn tự nhập tay từng khoản nợ trên giao diện.
Đăng ký mới qua `/register` **không cần xác thực email**.

## Chạy dự án

### 1. Tạo database

TRONNO **không dùng Flyway**. Schema được tạo bằng file SQL thủ công:

```powershell
cmd /c '"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p < server\src\main\resources\db\01_schema.sql'
```

File này tạo database `tronno` và 8 bảng: `users`, `creditors`, `debts`, `repayment_schedules`, `interest_rate_history`, `payments`, `payment_allocations`, `reminders`.

> Lưu ý: **không** thêm `tronno` vào cuối lệnh khi redirect file, vì `01_schema.sql` đã có sẵn `CREATE DATABASE` + `USE tronno` bên trong.

Khi khởi động app, `SchemaGuard` sẽ kiểm tra đủ 8 bảng và báo `Schema OK` hoặc hướng dẫn cách chạy lại file.

### 2. Backend

```powershell
cd server
mvn spring-boot:run
```

Yêu cầu MySQL tại `localhost:3306`. Cấu hình trong `server/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tronno?createDatabaseIfNotExist=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:12345678}
```

### 3. Frontend

```powershell
cd client
npm install
npm run dev
```

Mặc định API: `http://localhost:8080/api` (đổi bằng `VITE_API_URL` trong `.env.local`).

## API chính

| Method | Endpoint | Quyền |
| --- | --- | --- |
| POST | `/api/auth/register` | công khai |
| POST | `/api/auth/login` | công khai |
| GET/PUT | `/api/auth/me`, `/api/auth/profile` | đã đăng nhập |
| GET/POST/PUT/DELETE | `/api/debts/**` | MEMBER |
| GET/POST/PUT/DELETE | `/api/creditors/**` | MEMBER |
| POST | `/api/debts/{id}/payments` | MEMBER |
| POST | `/api/debts/payments/{id}/reverse` | MEMBER |
| GET/POST | `/api/debts/{id}/rate-history` | MEMBER |
| POST | `/api/debts/{id}/extend` | MEMBER |
| GET | `/api/reminders`, `/api/debts/{id}/reminders` | MEMBER |
| POST | `/api/reminders/run` | MEMBER (chạy tay job nhắc hạn) |
| GET | `/api/dashboard`, `/api/reports/**` | MEMBER |
| GET/POST | `/api/letters/**` | MEMBER |

Swagger UI: `http://localhost:8080/swagger-ui.html`
