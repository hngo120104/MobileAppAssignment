# SQLite database và dữ liệu mẫu

Thư mục này chứa:

- `LopHocLapTrinh.db`: file SQLite đã tạo sẵn ở schema version 5.
- `seed.sql`: schema và seed idempotent.
- `build-database.js`: tạo/cập nhật file `.db` và in kết quả kiểm tra.

Chạy lại:

```text
node database/build-database.js
```

Kết quả chuẩn:

- 12 học viên.
- 6 khóa học.
- 13 ghi danh.
- 4 học viên thuộc báo cáo 10–12 tuổi, Python Cơ bản.
- `PRAGMA integrity_check = ok`.
- Không có vi phạm foreign key.

File `.db` dùng để kiểm tra bằng SQLite Browser hoặc công cụ tương đương. Khóa học mẫu có ngày bắt đầu/kết thúc; mỗi ghi danh có thời điểm ghi danh. Ứng dụng Android không đọc trực tiếp file trong thư mục này; `ClassroomDatabase` chứa cùng bộ seed và migration v5 để cài mới hoặc database hiện có đều nhận dữ liệu.

Seed dùng `INSERT OR IGNORE` theo mã học viên/khóa học nên có thể chạy lại mà không tạo bản ghi trùng và không ghi đè bản ghi cùng mã đã được người dùng chỉnh sửa.
