# Review code và thiết kế ứng dụng

Ngày review: 25/07/2026

## Trạng thái khắc phục

Cập nhật ngày 26/07/2026:

- Đã xử lý F01: database v3 dùng migration bảo toàn bảng/dữ liệu thay vì drop toàn bộ schema. Lưu ý dữ liệu từng bị xóa bởi lần nâng cấp cũ không thể tự khôi phục.
- Đã xử lý F03: màn hình ghi danh lấy `CourseSummary` bằng một query `LEFT JOIN ... GROUP BY`; `render()` không còn truy vấn database.
- Đã xử lý F04: ghi danh nhiều học viên chạy trong một transaction theo chính sách all-or-nothing.
- Đã xử lý F09: thêm index `index_enrollments_course_id`.
- Đã xử lý một phần F06: bổ sung instrumented test cho migration, rollback batch và course summary. Test đã biên dịch; môi trường hiện tại không có lệnh `adb` nên chưa chạy được trên device/emulator.
- Đã xử lý F02: mọi thao tác repository từ bốn màn hình được chạy trên single-thread background executor; UI chỉ nhận kết quả trên main thread.
- Đã xử lý phần phụ thuộc implementation của F05: `ClassroomApplication` là composition root và cấp `ClassroomRepository`; presentation không còn import/khởi tạo SQLite implementation. Business rule vẫn cần tiếp tục tách khỏi Activity.
- Đã xử lý phần kích thước pixel của F12: padding tạo trong Java được quy đổi từ dp theo mật độ màn hình. Phần chuỗi/style hard-code vẫn còn.
- Đã xử lý thêm F05 theo SRP: validation form nằm trong `domain.validation`; lọc học viên khả dụng và batch ghi danh nằm trong `EnrollmentUseCase`; các hàm Activity dài được chia thành bước đọc input, hiển thị lỗi, persist và render dialog.
- Đã mở rộng F06 bằng unit test thuần Java cho validation và `EnrollmentUseCase`. Contract repository cũng đã bỏ hai method ghi danh đơn lẻ/đọc toàn bộ ghi danh không còn được sử dụng.

Các hạng mục còn lại trong tài liệu vẫn là backlog, đặc biệt state/ViewModel ở F10, result type chi tiết ở F07 và phần test UI/repository còn thiếu của F06.

## 1. Phạm vi và kết luận nhanh

Phạm vi review gồm yêu cầu nghiệp vụ, cấu trúc source, mô hình dữ liệu SQLite, repository, các Activity, resource giao diện, cấu hình build và test hiện có.

Ứng dụng đã đáp ứng các chức năng cơ bản được mô tả trong `requirements.md`: CRUD học viên/khóa học, ghi danh, liệt kê học viên theo khóa và truy vấn học viên 10–12 tuổi thuộc khóa Python cơ bản. Cấu trúc package đã tách `presentation`, `domain`, `data`; câu SQL dùng tham số; bảng ghi danh có khóa chính ghép và khóa ngoại xóa liên hoàn.

Tuy vậy, thiết kế hiện tại phù hợp với bài tập/demo nhỏ hơn là ứng dụng có dữ liệu thật. Rủi ro lớn nhất là nâng phiên bản database sẽ xóa toàn bộ dữ liệu. Các truy vấn SQLite cũng đang chạy trực tiếp trên UI thread, và màn hình ghi danh tạo truy vấn N+1 trong lúc render danh sách.

## 2. Thang ưu tiên

| Mức | Ý nghĩa |
|---|---|
| P0 | Có thể gây mất dữ liệu hoặc hậu quả nghiêm trọng; cần xử lý trước khi phát hành |
| P1 | Ảnh hưởng rõ đến độ đúng, hiệu năng hoặc khả năng bảo trì; nên xử lý sớm |
| P2 | Chưa gây lỗi ngay với dữ liệu nhỏ nhưng sẽ tạo nợ kỹ thuật/UX kém |
| P3 | Cải thiện chất lượng, tính nhất quán và khả năng mở rộng |

## 3. Danh sách phát hiện

| ID | Mức | Vấn đề | Vị trí chính |
|---|---|---|---|
| F01 | P0 | Migration xóa toàn bộ dữ liệu và seed lại dữ liệu mẫu | `ClassroomDatabase.java:55-60` |
| F02 | P1 | Tất cả thao tác SQLite chạy đồng bộ trên main thread | `BaseListActivity.java:32`, các Activity gọi repository trực tiếp |
| F03 | P1 | Màn hình ghi danh có truy vấn N+1 ngay trong `render()` | `EnrollmentActivity.java:47-51` |
| F04 | P1 | Ghi danh nhiều học viên không có transaction, có thể thành công một phần | `EnrollmentActivity.java:151-169` |
| F05 | P1 | “Phân tầng” chưa đúng ranh giới: presentation tự khởi tạo data implementation và chứa business rule | `BaseListActivity.java:17-33`, `StudentActivity.java:74-99` |
| F06 | P1 | Hầu như không có kiểm thử nghiệp vụ/database/UI | hai file test mặc định |
| F07 | P2 | API repository dùng `long`/`boolean` làm mất nguyên nhân lỗi | `ClassroomRepository.java:9-23`, các hàm `save*` |
| F08 | P2 | Domain model cho phép tạo trạng thái không hợp lệ | `Student.java:12-17`, `Course.java:16-21` |
| F09 | P2 | Thiếu index phục vụ truy vấn ghi danh theo `course_id` | `ClassroomDatabase.java:35-40` |
| F10 | P2 | Vòng đời dữ liệu và UI state gắn chặt Activity, không có ViewModel/state holder | toàn bộ package `presentation` |
| F11 | P2 | UI khó khám phá và khó tiếp cận: sửa/xóa phụ thuộc tap/long-press | `activity_entity_list.xml:14-35`, các listener danh sách |
| F12 | P2 | Kích thước UI viết bằng pixel trong Java; nhiều chuỗi hard-code | `BaseListActivity.java:52-55`, `FormViews.java:13-33` |
| F13 | P2 | Backup dữ liệu đang bật nhưng rule vẫn là template chưa có quyết định rõ | `AndroidManifest.xml:5-8`, hai file XML backup |
| F14 | P3 | Seed dữ liệu mẫu nằm trong database production và dựa vào ID cố định | `ClassroomDatabase.java:41-52` |
| F15 | P3 | Truy vấn báo cáo đặc thù bị đóng cứng thành một method repository | `ClassroomRepository.java:23`, `SqliteClassroomRepository.java:128-136` |
| F16 | P3 | Tên project/package “quản lý nhân sự” không khớp domain lớp học | `app/build.gradle.kts:6,14` |
| F17 | P3 | Cấu hình release tắt tối ưu hóa; còn resource bitmap không dùng | `app/build.gradle.kts:23-28`, báo cáo lint |

## 4. Phân tích chi tiết

### F01 — P0: Migration làm mất dữ liệu

`onUpgrade()` drop cả ba bảng rồi gọi lại `onCreate()`. Mọi lần tăng `DATABASE_VERSION` đều xóa học viên, khóa học và ghi danh của người dùng, sau đó chèn lại dữ liệu mẫu.

Đề xuất:

- Viết migration theo từng phiên bản bằng `ALTER TABLE`, tạo bảng mới và copy dữ liệu khi cần.
- Bao migration trong transaction.
- Thêm instrumented test mở database phiên bản cũ, nâng cấp và xác nhận dữ liệu còn nguyên.
- Chỉ cho phép destructive migration ở build demo/test nếu thật sự cần.

### F02 — P1: I/O database chạy trên UI thread

Các Activity gọi repository trực tiếp trong `onCreate()`, click listener và dialog listener. Repository dùng `getReadableDatabase()`/`getWritableDatabase()` đồng bộ. Khi dữ liệu lớn, mở database, query hoặc ghi có thể làm đứng giao diện và dẫn tới ANR.

Đề xuất:

- Chuyển thao tác data sang executor/background thread; trả kết quả qua ViewModel/observable state.
- Phương án bền vững hơn là dùng Room, DAO và migration có kiểm thử.
- UI cần trạng thái loading, success, empty và error rõ ràng.

### F03 — P1: N+1 query ở màn hình ghi danh

`EnrollmentActivity.render()` gọi `getStudentsByCourse()` để đếm học viên. Adapter có thể gọi `getView()` nhiều lần khi vẽ hoặc cuộn; vì vậy một lần hiển thị danh sách khóa tạo một query lấy khóa cộng thêm ít nhất một query cho mỗi dòng, thậm chí lặp lại khi view được render lại.

Đề xuất:

- Tạo projection như `CourseSummary(course, studentCount)`.
- Lấy toàn bộ danh sách và số lượng bằng một query `LEFT JOIN ... GROUP BY`.
- `render()` phải thuần: chỉ format dữ liệu đã có, tuyệt đối không I/O.

### F04 — P1: Batch ghi danh không nguyên tử

UI lặp qua từng học viên và gọi `enroll()` riêng lẻ. Nếu một bản ghi lỗi giữa chừng, các bản ghi trước vẫn được lưu nhưng UI chỉ báo số lượng thành công; người dùng không biết ai thất bại và không thể rollback cả thao tác.

Đề xuất:

- Đưa use case `enrollStudents(courseId, studentIds)` xuống repository/data layer.
- Dùng một SQLite transaction cho toàn bộ batch.
- Trả kết quả có cấu trúc, ví dụ `Success`, `Duplicate`, `NotFound`, `DatabaseError`; thống nhất chính sách all-or-nothing.

### F05 — P1: Ranh giới kiến trúc chưa thật sự tách lớp

Tài liệu mô tả luồng `presentation → domain ← data`, nhưng `BaseListActivity` import và khởi tạo trực tiếp `ClassroomDatabase` cùng `SqliteClassroomRepository`. Activity cũng tự giữ validation, orchestration ghi danh và quyết định thông báo lỗi. Vì vậy presentation vẫn phụ thuộc chặt vào SQLite implementation và khó unit test bằng fake repository.

Đề xuất:

- Khởi tạo dependency ở application/composition root rồi inject interface vào ViewModel/use case.
- Đưa quy tắc nghiệp vụ (độ tuổi hợp lệ, ngôn ngữ/cấp độ hợp lệ, batch ghi danh) vào domain/use case.
- Không dùng kế thừa `BaseListActivity` làm nơi vừa tạo dependency vừa dựng adapter; ưu tiên composition.

### F06 — P1: Test pass nhưng không kiểm chứng ứng dụng

`ExampleUnitTest` chỉ kiểm tra `2 + 2 = 4`; `ExampleInstrumentedTest` chỉ kiểm tra package name. Chưa có test cho schema constraint, cascade delete, duplicate code/enrollment, hai truy vấn báo cáo, migration, batch transaction hoặc luồng CRUD.

Đề xuất tối thiểu:

1. Repository/database test cho CRUD và constraint.
2. Migration test bảo toàn dữ liệu.
3. Test truy vấn theo khóa và Python cơ bản 10–12 tuổi với dữ liệu biên.
4. Unit test validation/use case.
5. UI test cho thêm/sửa/xóa/ghi danh và rotation.

### F07 — P2: Contract lỗi quá nghèo thông tin

`saveStudent()` và `saveCourse()` trả `long`; UI hiểu mọi giá trị `< 1` là “mã đã tồn tại”. Nhưng update ID không tồn tại, constraint khác hoặc lỗi database cũng bị báo sai. Các thao tác delete/unenroll bỏ qua giá trị trả về. Điều này làm lỗi khó chẩn đoán và thông báo cho người dùng không chính xác.

Đề xuất dùng result type có kiểu lỗi cụ thể, log lỗi kỹ thuật ở data layer và ánh xạ sang thông báo thân thiện tại presentation.

### F08 — P2: Domain model chưa bảo vệ bất biến

Model chỉ kiểm tra `null`, vẫn cho phép code/name rỗng, tuổi ngoài 5–18, language/level tùy ý hoặc ID không hợp lệ. Validation hiện bị lặp/đặt ở form và schema database; bất kỳ caller mới nào cũng có thể tạo object sai.

Đề xuất dùng factory/value object hoặc constructor validation; dùng enum/value object cho `Language`, `CourseLevel`, `ScratchLevel`, và một biểu diễn rõ cho entity mới thay vì ID `-1`.

### F09 — P2: Thiếu index theo `course_id`

Khóa chính ghép `(student_id, course_id)` hỗ trợ tốt lookup bắt đầu bằng `student_id`, nhưng truy vấn chính lại lọc/join theo `course_id`. Khi bảng ghi danh tăng, SQLite có thể phải scan nhiều bản ghi.

Đề xuất tạo `CREATE INDEX index_enrollments_course_id ON enrollments(course_id)` và kiểm tra query plan.

### F10 — P2: State phụ thuộc Activity

Không có ViewModel hoặc saved state. Danh sách, lựa chọn báo cáo và tiến trình dialog hai bước được giữ trực tiếp trong Activity. Rotation/process recreation sẽ đóng form, mất lựa chọn đang nhập và reset kết quả báo cáo. Repository/database helper cũng được tạo lại theo từng Activity và không có lifecycle quản lý rõ.

Đề xuất dùng ViewModel + saved state cho dữ liệu/selection; một data source cấp application; form phức tạp có state model riêng.

### F11 — P2: UX thao tác và accessibility

Sửa bằng chạm, xóa bằng nhấn giữ chỉ được giải thích bằng một dòng chữ. Long-press là hành vi khó khám phá, khó dùng với một số công cụ hỗ trợ và dễ thao tác nhầm. Row chỉ là `TextView` tạo bằng code, không có nút hành động/semantic rõ; màn hình báo cáo cũng chưa có empty state riêng khi kết quả bằng 0.

Đề xuất:

- Dùng row layout có nút/menu “Sửa”, “Xóa”, mô tả accessibility và vùng chạm chuẩn.
- Dùng RecyclerView/ListAdapter với stable ID.
- Tách empty state ban đầu, không có dữ liệu và không có kết quả.
- Với dữ liệu tăng, bổ sung tìm kiếm/lọc và phân trang hoặc lazy loading.

### F12 — P2: Styling không theo resource

`setPadding(24, 20, ...)` và `setPadding(40, 8, ...)` nhận pixel thật, không phải dp; giao diện sẽ có mật độ hiển thị không nhất quán giữa thiết bị. Màu, cỡ chữ và phần lớn chuỗi cũng hard-code trong Java, gây khó theme, dịch ngôn ngữ và test.

Đề xuất chuyển layout form/list row sang XML hoặc Material component; đưa dimension, color và string vào resource. Cần kiểm tra cả light/dark theme.

### F13 — P2: Chính sách backup chưa được xác định

`allowBackup="true"` trong khi `backup_rules.xml` và `data_extraction_rules.xml` gần như template rỗng. Với dữ liệu học viên trẻ em, cần chủ động quyết định dữ liệu nào được cloud backup/device transfer thay vì dùng mặc định không rõ chủ đích.

Đề xuất lập yêu cầu bảo mật/riêng tư, sau đó hoặc tắt backup, hoặc khai báo include/exclude chính xác và kiểm thử restore.

### F14 — P3: Seed production và ID cố định

`onCreate()` luôn seed ba học viên, hai khóa học và ba ghi danh; lệnh ghi danh giả định ID là 1/2/3. Điều này trộn dữ liệu demo vào dữ liệu thật và làm seed dễ vỡ nếu cách sinh ID thay đổi.

Đề xuất chỉ seed ở demo/debug hoặc theo quyết định sản phẩm; lấy ID trả về từ insert và dùng trong các insert liên quan.

### F15 — P3: Báo cáo đóng cứng

Method `getPythonBasicStudentsAged10To12()` nhúng một báo cáo cụ thể vào contract repository. Mỗi biến thể độ tuổi/ngôn ngữ/cấp độ sẽ cần thêm method, khiến interface phình to.

Đề xuất model hóa `StudentFilter`/`ReportCriteria` hoặc use case báo cáo với tham số `minAge`, `maxAge`, `language`, `level`. Vẫn giữ named use case ở domain nếu đây là nghiệp vụ quan trọng, nhưng data query nên tái sử dụng tiêu chí.

### F16 — P3: Tên định danh không khớp nghiệp vụ

Workspace/package/application ID là `QuanLyNhanSu`/`com.example.quanlynhansu`, trong khi sản phẩm thực tế là quản lý lớp học lập trình. Điều này gây nhầm trong log, package, artifact và khi phát hành.

Đề xuất đổi namespace/application ID trước khi có bản production; việc đổi application ID sau phát hành được Android xem là ứng dụng khác.

### F17 — P3: Build/resource hygiene

Release đang `enable = false` cho optimization nên APK không shrink/obfuscate. Lint hiện báo 5 warning, gồm SDK/tooling version và `anh_ho_so.jpg` vừa không dùng vừa đặt trong densityless drawable folder.

Đề xuất bật tối ưu hóa release sau khi bổ sung keep rule/test, xóa resource không dùng hoặc đặt đúng density, và đánh giá nâng SDK/plugin có kiểm soát thay vì nâng chỉ để hết warning.

## 5. Điểm đang làm tốt

- Schema có `UNIQUE`, `CHECK`, composite primary key và foreign key `ON DELETE CASCADE`.
- Foreign key được bật trong `onConfigure()`.
- Query có input dùng placeholder/selection args, không nối trực tiếp giá trị người dùng.
- Cursor được đóng bằng try-with-resources.
- Model bất biến ở mức field `final`.
- Chức năng hiện tại bám khá sát yêu cầu bài toán và có thông báo xác nhận trước thao tác xóa.
- Gradle build, unit test và lint chạy thành công tại thời điểm review.

## 6. Lộ trình đề xuất

### Giai đoạn 1 — Bảo toàn dữ liệu và độ đúng

1. Thay destructive migration bằng migration bảo toàn dữ liệu.
2. Thêm database/repository/migration tests thực chất.
3. Batch ghi danh bằng transaction và result type rõ lỗi.
4. Thêm index `course_id`.

### Giai đoạn 2 — Tách kiến trúc và tránh treo UI

1. Bổ sung ViewModel/use case và dependency injection ở composition root.
2. Chạy database I/O ngoài main thread.
3. Thay query N+1 bằng query summary duy nhất.
4. Đưa validation/bất biến về domain.

### Giai đoạn 3 — UX, lifecycle và vận hành

1. Chuyển row/form sang resource + Material component; bỏ kích thước pixel.
2. Thiết kế action sửa/xóa dễ khám phá và accessibility.
3. Khôi phục state khi rotation/process recreation.
4. Chốt chính sách backup, đổi package đúng domain và hoàn thiện release config.

## 7. Kết quả kiểm chứng

Lệnh đã chạy:

```text
.\gradlew.bat testDebugUnitTest lintDebug --offline
```

Kết quả: `BUILD SUCCESSFUL`, unit test pass, lint không có error và có 5 warning. Cần lưu ý các test hiện tại là test mẫu nên kết quả pass chưa chứng minh các luồng nghiệp vụ hoạt động đúng.
