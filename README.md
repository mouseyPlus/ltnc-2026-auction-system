# Online Auction System - Hệ thống Đấu giá trực tuyến

> **Bài tập lớn môn Lập trình nâng cao (Học kỳ I, 2026 - 2027)**  
> **Trường Đại học Công nghệ - ĐHQGHN (VNU-UET)**

[![Java CI with Maven](https://github.com/mouseyPlus/ltnc-2026-auction-system/actions/workflows/maven-ci.yml/badge.svg)](https://github.com/mouseyPlus/ltnc-2026-auction-system/actions)
![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-orange.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

---

## Giới thiệu tổng quan

Hệ thống Đấu giá trực tuyến (Online Auction System) là ứng dụng theo kiến trúc **Client-Server** cho phép nhiều người dùng tham gia đấu giá các sản phẩm theo thời gian thực (Realtime). 

Hệ thống được phát triển bằng ngôn ngữ **Java**, xây dựng giao diện đồ họa người dùng bằng **JavaFX**, áp dụng mô hình kiến trúc **MVC**, giao tiếp qua **Java Socket** và quản lý phụ thuộc bằng **Maven**.

---

## Chức năng hệ thống

### Chức năng bắt buộc (Mandatory)
* **Quản lý người dùng (User Management):** Phân quyền vai trò người dùng bao gồm `Bidder` (Người mua), `Seller` (Người bán) và `Admin` (Quản trị viên).
* **Quản lý sản phẩm (Product Management):** Đăng mới, chỉnh sửa, xóa và quản lý trạng thái các mặt hàng đấu giá (Điện tử, Nghệ thuật, Phương tiện...).
* **Tham gia đấu giá (Bidding Engine):** Đặt giá realtime, kiểm tra tính hợp lệ của bước giá, cập nhật người dẫn đầu tự động.
* **Kết thúc phiên đấu giá:** Tự động đóng phiên khi hết thời gian, chuyển trạng thái phiên (`OPEN` $\rightarrow$ `RUNNING` $\rightarrow$ `FINISHED` $\rightarrow$ `PAID`/`CANCELED`) và xác định người thắng cuộc.
* **Xử lý đồng thời (Concurrency Handling):** Đảm bảo an toàn luồng (Thread-safe) tránh race condition, lost update khi nhiều người dùng cùng bấm đặt giá tại một mốc thời gian.
* **Realtime Updates:** Cập nhật thông tin phiên đấu giá tới tất cả các Client đang theo dõi ngay lập tức thông qua giao tiếp Socket + Observer Pattern.
* **Xử lý ngoại lệ & Kiểm thử:** Xây dựng bộ Custom Exceptions chuyên biệt và viết Unit Test (JUnit 5) đảm bảo logic nghiệp vụ hoạt động chính xác.

### Chức năng nâng cao (Advanced Features)
* **Auto-Bidding:** Tự động trả giá hộ người dùng dựa trên `maxBid` và `increment` thiết lập trước.
* **Anti-Sniping Algorithm:** Tự động gia hạn thời gian phiên đấu giá thêm $Y$ giây nếu có lượt đặt giá mới trong $X$ giây cuối.
* **Bid History Visualization:** Hiển thị biểu đồ đường (LineChart) diễn biến giá theo thời gian thực.

---

## Kiến trúc hệ thống & Design Patterns

### 1. Mô hình Client - Server & MVC
* **Server:** Tiếp nhận các kết nối Socket, quản lý luồng đồng thời, xử lý logic nghiệp vụ đấu giá, lưu trữ dữ liệu (Serialization) và điều phối các bản tin Realtime.
* **Client:** Giao diện JavaFX thiết kế theo mô hình **MVC** (FXML View - Controller - Model), gửi yêu cầu và nhận phản hồi từ Server qua Socket.

### 2. Design Patterns áp dụng
* **Singleton Pattern:** Quản lý trung tâm dữ liệu `AuctionManager` và kết nối Server.
* **Factory Method Pattern:** Khởi tạo linh hoạt các đối tượng mặt hàng (`Electronics`, `Art`, `Vehicle`).
* **Observer Pattern:** Phát thông báo (notify) đến tất cả các Client khi có cập nhật giá mới hoặc thay đổi trạng thái phiên.

---

## Công nghệ & Thư viện sử dụng

* **Ngôn ngữ:** Java 17+
* **Giao diện:** JavaFX 17, SceneBuilder (FXML)
* **Lập trình mạng:** Java Sockets, Multi-threading (`ReentrantLock`, `synchronized`)
* **Lưu trữ dữ liệu:** Java Object Serialization / File I/O
* **Build Tool:** Apache Maven
* **Kiểm thử & CI/CD:** JUnit 5, Checkstyle (Google Java Style), GitHub Actions

---

## Cấu trúc thư mục dự án

```text
online-auction-system/
├── .github/
│   └── workflows/
│       └── maven-ci.yml        # Cấu hình GitHub Actions (CI/CD)
├── src/
│   ├── main/
│   │   ├── java/com/auction/
│   │   │   ├── model/          # Domain Entities (User, Item, Auction, Bid...)
│   │   │   ├── view/           # Giao diện FXML
│   │   │   ├── controller/     # JavaFX Controller
│   │   │   ├── network/        # Socket Client & Server Handler
│   │   │   ├── exception/      # Custom Exceptions (InvalidBidException...)
│   │   │   └── util/           # Data Persistence (Serialization, File I/O)
│   │   └── resources/          # File FXML, CSS, Image, Checkstyle config
│   └── test/                   # Unit Tests với JUnit 5
├── pom.xml                     # Quản lý dependencies Maven
└── README.md
```

# Hướng Dẫn Đóng Góp Mã Nguồn (Contributing Guidelines)

Cảm ơn tất cả các thành viên đã tham gia phát triển dự án Online Auction System. Để đảm bảo dự án vận hành mượt mà, mã nguồn sạch đẹp, tránh xung đột (conflict) và đáp ứng đầy đủ tiêu chí chấm điểm của môn học, tất cả thành viên bắt buộc phải tuân thủ quy trình dưới đây.

---

## 3 Nguyên Tắc Sinh Tồn (Bắt Buộc)

1. Hiểu 100% mã nguồn do mình đưa lên: Theo quy định của giảng viên, nếu bất kỳ thành viên nào không giải thích được một dòng code bất kỳ trong bài, TOÀN BỘ NHÓM SẼ BỊ 0 ĐIỂM. Hãy đảm bảo bạn hiểu rõ logic (đặc biệt là code tham khảo hoặc sinh bởi AI) trước khi tạo Pull Request.
2. Commit thường xuyên: Mỗi tuần mỗi cá nhân phải có ít nhất 2–3 commits. Tuyệt đối KHÔNG làm việc offline rồi dồn 1 commit duy nhất vào cuối kỳ.
3. Không push trực tiếp lên main hoặc dev: Mọi thay đổi đều phải thực hiện trên nhánh tính năng riêng (feature/*) và thông qua Pull Request (PR) có review.

---

## Quy Trình Nhánh (Git Branching Strategy)

Dự án áp dụng mô hình Gitflow đơn giản gồm 2 nhánh chính và các nhánh tính năng:

* main: Nhánh sản phẩm ổn định, chỉ merge từ dev vào cuối các cột mốc lớn (Milestones) hoặc khi nộp bài.
* dev: Nhánh tích hợp chính. Tất cả tính năng mới sau khi review xong sẽ gộp vào đây.
* Nhánh tính năng / sửa lỗi cá nhân: Xuất phát từ dev và đặt tên theo quy chuẩn:
  - feature/ten-chuc-nang (VD: feature/login-ui, feature/auto-bidding)
  - bugfix/ten-loi (VD: bugfix/socket-reconnect-issue)
  - test/ten-test-case (VD: test/auction-concurrency-junit)

---

## Quy Trình 5 Bước Làm Việc Hàng Ngày

### 1. Đồng bộ code mới nhất từ dev
Trước khi bắt đầu viết code mới, luôn cập nhật nhánh dev cá nhân:
git checkout dev
git pull origin dev

### 2. Tạo nhánh làm việc mới
git checkout -b feature/ten-tinh-nang-cua-ban

### 3. Viết code & Lưu lại (Commit)
Nên chia nhỏ công việc thành nhiều commit ngắn gọn thay vì 1 commit khổng lồ.
git add .
git commit -m "feat: implement bid validation logic"

### 4. Đẩy nhánh lên GitHub
git push origin feature/ten-tinh-nang-cua-ban

### 5. Tạo Pull Request (PR) & Yêu cầu Review
1. Truy cập Repo trên GitHub, bấm Compare & pull request.
2. Đảm bảo chọn Base: dev <- Compare: feature/ten-tinh-nang-cua-ban.
3. Điền đầy đủ mô tả công việc vào Form PR (đã làm gì, cách test).
4. Phân công (Assign) ít nhất 1 thành viên khác trong nhóm làm Reviewer.

---

## Quy Chuẩn Đặt Tên Commit (Conventional Commits)

Tin nhắn commit phải rõ ràng, bắt đầu bằng các tiền tố chuẩn sau:

- feat: Thêm tính năng/giao diện mới (VD: feat: add bid history line chart)
- fix: Sửa lỗi trong code (VD: fix: resolve race condition in concurrency test)
- docs: Cập nhật tài liệu (README, CONTRIBUTING...) (VD: docs: update setup instructions)
- style: Format code, sửa khoảng trắng, định dạng (VD: style: reformat code according to Checkstyle)
- refactor: Cấu trúc lại code - không đổi logic/tính năng (VD: refactor: extract AuctionFactory method)
- test: Bổ sung hoặc chỉnh sửa Unit Test (JUnit) (VD: test: add unit test for InvalidBidException)
- chore: Cập nhật cấu hình Maven, .gitignore, CI/CD (VD: chore: update pom.xml dependencies)

---

## Quy Trình Code Review & Merge PR

Một Pull Request chỉ được phép Merge vào nhánh dev khi đáp ứng đủ các điều kiện sau:

1. GitHub Actions (CI) màu xanh: Lớp CI/CD phải build thành công (mvn clean test không báo lỗi compile hay gãy test).
2. Đã qua Checkstyle: Không vi phạm các quy tắc đặt tên và trình bày code của Google Java Style.
3. Có ít nhất 1 Reviewer Approved: Thành viên được assign review đã đọc code, đặt câu hỏi (nếu có) và nhấn Approve.
4. Giải trình được Code: Người tạo PR sẵn sàng giải thích cơ chế hoạt động của đoạn code trong buổi họp tuần của nhóm.

Mẹo cho Reviewer: Đừng ngần ngại để lại comment hỏi "Đoạn code này xử lý case gì?" hoặc "Tại sao lại dùng ReentrantLock ở đây thay vì synchronized?" trên PR của đồng đội. Điều này giúp cả nhóm cùng hiểu bài!

---

## Quy Chuẩn Lập Trình (Coding Standards)

* Style guide: Tuân thủ theo Google Java Style Guide (đã tích hợp plugin Checkstyle trong Maven).
* Đặt tên:
  - Class / Interface: PascalCase (VD: AuctionManager, InvalidBidException)
  - Method / Variable: camelCase (VD: currentBid, processNewBid())
  - Constant: UPPER_SNAKE_CASE (VD: MAX_BID_INCREMENT, DEFAULT_PORT)
* Xử lý ngoại lệ:
  - KHÔNG nuốt ngoại lệ (catch trống hoặc chỉ printStackTrace mà không xử lý).
  - Sử dụng các Custom Exceptions đã được định nghĩa trong package com.auction.exception.
* Đa luồng (Concurrency): Bắt buộc phải xử lý thread-safe cho các tài nguyên dùng chung (ví dụ: danh sách phiên đấu giá, số tiền bid hiện tại) bằng synchronized hoặc ReentrantLock.

---

## Báo Cáo Tiến Độ & Kỷ Luật Nhóm

* Họp Walkthrough (Cuối tuần): Dành 20 phút demo giao diện/chức năng và giải thích các đoạn code phức tạp vừa viết cho cả nhóm.
* Tôn trọng Deadline: Nếu gặp khó khăn/blocker quá 24h, bắt buộc phải đăng lên nhóm để các thành viên khác hỗ trợ, tuyệt đối không im lặng giấu lỗi.
