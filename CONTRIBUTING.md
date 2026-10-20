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

##  Báo Cáo Tiến Độ & Kỷ Luật Nhóm

* Họp Walkthrough (Cuối tuần): Dành 20 phút demo giao diện/chức năng và giải thích các đoạn code phức tạp vừa viết cho cả nhóm.
* Tôn trọng Deadline: Nếu gặp khó khăn/blocker quá 24h, bắt buộc phải đăng lên nhóm để các thành viên khác hỗ trợ, tuyệt đối không im lặng giấu lỗi.
