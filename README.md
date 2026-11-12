# Sổ Chi Tiêu Du Học – Backend

API cho app quản lý chi tiêu của du học sinh ở Hàn. Java 17, Spring Boot 3.3, Spring Security + JWT, JPA.

## Chạy trên máy

Cần: JDK 17 trở lên và Maven (hoặc mở bằng IntelliJ, IntelliJ tự có Maven).

```bash
mvn spring-boot:run
```

API chạy ở `http://localhost:8080`. Dữ liệu lưu vào file H2 trong thư mục `./data`, tắt app vẫn còn.

Gọi thử: mở `api.http` trong IntelliJ và bấm ▶ từng request theo thứ tự.

Muốn dùng PostgreSQL: tạo database `sochitieu`, rồi chạy
`mvn spring-boot:run -Dspring-boot.run.profiles=postgres` (đặt `DB_URL`, `DB_USER`, `DB_PASSWORD` nếu khác mặc định).

## Cấu trúc thư mục

```
com.phong.sochitieu
├── auth/         đăng ký, đăng nhập, tạo và kiểm tra JWT
├── user/         entity User, ngân sách tháng (/api/me)
├── transaction/  khoản thu chi: entity, repository, service, controller
├── summary/      tổng kết tháng, chi theo danh mục, gợi ý mỗi ngày tiêu bao nhiêu
├── config/       Spring Security, CORS
└── common/       xử lý lỗi chung, đọc tham số tháng
```

## Danh sách API

Mọi API trừ `/api/auth/**` cần header `Authorization: Bearer <token>`.

| Method | Đường dẫn | Việc |
|---|---|---|
| POST | `/api/auth/register` | Đăng ký, trả token |
| POST | `/api/auth/login` | Đăng nhập, trả token |
| GET | `/api/me` | Thông tin tài khoản |
| PUT | `/api/me/budget` | Đặt ngân sách tháng |
| GET | `/api/transactions?month=2026-11` | Khoản thu chi trong tháng |
| POST | `/api/transactions` | Thêm khoản |
| PUT | `/api/transactions/{id}` | Sửa khoản |
| DELETE | `/api/transactions/{id}` | Xóa khoản |
| GET | `/api/summary?month=2026-11` | Tổng kết tháng |

Bỏ trống `month` thì lấy tháng hiện tại theo giờ Hàn Quốc.

Ví dụ kết quả `/api/summary`:

```json
{
  "month": "2026-11",
  "income": 1450000,
  "expense": 983000,
  "balance": 467000,
  "budget": 1000000,
  "budgetUsedPercent": 98,
  "daysLeft": 19,
  "dailyLimit": 894,
  "expenseByCategory": [
    { "category": "Tiền phòng (월세)", "total": 400000, "percent": 40.7 }
  ]
}
```

## Mã lỗi

Lỗi trả về JSON `{ status, message, fields, timestamp }`:
`400` dữ liệu sai (xem `fields`), `401` chưa đăng nhập hoặc sai mật khẩu, `404` không tìm thấy hoặc không phải dữ liệu của bạn, `409` email đã đăng ký.

## Trước khi deploy

- Đặt biến môi trường `JWT_SECRET` (chuỗi base64, ít nhất 32 byte). Không dùng secret mặc định trong `application.yml`.
- Đổi CORS trong `SecurityConfig` từ `*` sang domain frontend thật.
- Chuyển `ddl-auto: update` sang Flyway khi dữ liệu đã quan trọng.

## Việc tiếp theo

- Nối frontend vào API này (trang đăng nhập, gọi `/api/transactions` thay cho localStorage)
- Viết test cho `SummaryController` và phần phân quyền
- Mục tiêu tiết kiệm, danh mục tự tạo, xuất Excel
