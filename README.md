# BE_FKOTOAI_MB
Back-end for FKOTOAI mobile application
# Ứng Dụng Học Tiếng Nhật

Ứng dụng học tiếng Nhật này được thiết kế để hỗ trợ người dùng ôn luyện JLPT, tra cứu từ vựng, viết chữ Hán bằng tay, quét văn bản từ hình ảnh và dịch thuật. Ngoài ra, ứng dụng còn có tính năng học quizlet và một lobby để người chơi có thể thi đấu cùng nhau.

## Tính Năng

- **Khóa Học Ôn JLPT**: Cung cấp các khóa học ôn luyện cho các cấp độ JLPT N5, N4, N3, N2, N1.
- **Tra Cứu Từ Vựng**: Tìm kiếm và lưu trữ từ vựng tiếng Nhật một cách nhanh chóng và dễ dàng.
- **Viết Handwriting Chữ Hán**: Hỗ trợ người dùng luyện viết chữ Hán thông qua tính năng viết tay.
- **Quét OCR**: Sử dụng công nghệ quét OCR để lấy văn bản từ hình ảnh và dịch sang tiếng Việt hoặc tiếng Anh.
- **Học Quizlet**: Tích hợp tính năng học quizlet để người dùng có thể ôn tập từ vựng một cách hiệu quả.
- **Rank Match Lobby**: Cung cấp một không gian cho người chơi thi đấu và so tài với nhau.

## Công Nghệ Sử Dụng

- **Frontend**: Flutter
- **Backend**: Spring Boot
- **Cơ Sở Dữ Liệu**: MySQL

## Cài Đặt

### Yêu Cầu

- Flutter SDK
- Java JDK
- MySQL Server

### Hướng Dẫn Cài Đặt

1. **Clone Repository**:
   ```bash
   git clone [https://github.com/username/repository-name.git](https://github.com/chur04/BE_FKOTOAI_MB.git)
   cd repository-name
   ```

2. **Cài Đặt Flutter**:
   - Làm theo hướng dẫn cài đặt Flutter tại [flutter.dev](https://flutter.dev/docs/get-started/install).

3. **Cài Đặt Spring Boot**:
   - Tải Spring Boot từ [spring.io](https://spring.io/projects/spring-boot).

4. **Cấu Hình MySQL**:
   - Tạo cơ sở dữ liệu mới cho ứng dụng.
   - Cập nhật thông tin kết nối trong file cấu hình của Spring Boot.

5. **Chạy Ứng Dụng**:
   - Chạy backend:
     ```bash
     cd backend-directory
     ./mvnw spring-boot:run
     ```
   - Chạy frontend:
     ```bash
     cd frontend-directory
     flutter run
     ```



