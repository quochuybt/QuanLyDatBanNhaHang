# Tài liệu triển khai chức năng qua Socket (End-to-End)

**Dự án:** StarGuardian Restaurant  
**Mục tiêu:** Chuyển một chức năng từ gọi local service sang client/server socket theo chuẩn thống nhất, dễ mở rộng realtime.

---

## 1) Mục tiêu và phạm vi

## 1.1 Mục tiêu
- Chuyển luồng gọi nghiệp vụ từ `GUI -> Service` sang `GUI -> Socket -> Server -> Service`.
- Chuẩn hóa giao thức để các tính năng khác tái sử dụng.
- Đảm bảo ổn định trong LAN, gần realtime, dễ debug.

## 1.2 Phạm vi tài liệu
- Áp dụng cho **1 chức năng** (ví dụ: `AUTH_LOGIN`, `HOADON_GET_BY_PAGE`, `PHANCONG_ADD`).
- Bao gồm đầy đủ: contract, server handler, client remote service, GUI integration, test, rollout.

---

## 2) Kiến trúc chuẩn

```text
GUI
  -> RemoteService (client facade)
    -> SocketClientConnection.sendCommand(...)
      -> Socket Server
        -> CommandDispatcher
          -> <Feature>Handler
            -> Existing Service/Repository
          <- ResponseEnvelope
    <- ResponseEnvelope
<- DTO cho GUI
```

### Nguyên tắc bắt buộc
1. Không gửi Entity qua socket (chỉ gửi DTO transport).
2. Tất cả request/response dùng `MessageEnvelope`.
3. Request có `messageId`, response trả `correlationId`.
4. Lỗi trả về qua `ErrorCode`, không phụ thuộc text exception.
5. GUI không block EDT (dùng `SwingWorker`).

---

## 3) Chuẩn protocol

## 3.1 Envelope
`MessageEnvelope` gồm:
- `messageId`
- `type` (`COMMAND | RESPONSE | EVENT | PING | PONG`)
- `name`
- `correlationId`
- `timestamp`
- `success`
- `errorCode`
- `message`
- `payload`

## 3.2 Message types
- `COMMAND`: client gọi nghiệp vụ server
- `RESPONSE`: server trả kết quả cho command
- `EVENT`: server push chủ động
- `PING/PONG`: heartbeat

## 3.3 Error groups
- Auth: `AUTH_INVALID`, `AUTH_LOCKED`
- Request/protocol: `BAD_REQUEST`
- Server/system: `SERVER_ERROR`, `SERVER_UNREACHABLE`
- Discovery: `DISCOVERY_NOT_FOUND`

---

## 4) Quy trình migrate một chức năng (step-by-step)

Ví dụ minh họa dùng chức năng: `HOADON_GET_BY_PAGE`.

## Bước 1: Chốt contract

### Input
- `page`
- `itemsPerPage`
- `trangThai`
- `keyword`
- `tuNgay`
- `denNgay`

### Output
- Danh sách hóa đơn theo trang

### DTO transport
- `HoaDonPageRequestDTO`
- `HoaDonPageResponseDTO` (hoặc `List<HoaDonDTO>` nếu đã chuẩn)

> Khuyến nghị: DTO transport độc lập với DTO domain nội bộ để tránh vỡ hợp đồng API.

---

## Bước 2: Định nghĩa action

Trong `CommandAction` thêm:

```java
HOADON_GET_BY_PAGE
```

---

## Bước 3: Tạo handler phía server

Tạo class:

```java
public class HoaDonGetByPageHandler implements CommandHandler { ... }
```

### Logic chuẩn trong handler
1. Parse payload -> `HoaDonPageRequestDTO`
2. Validate request (page >= 1, itemsPerPage > 0)
3. Gọi `HoaDonService.getHoaDonByPage(...)`
4. Map dữ liệu trả về
5. Trả `MessageEnvelope.responseOk(...)`

### Mapping lỗi
- Validate sai -> `BAD_REQUEST`
- Exception hệ thống -> `SERVER_ERROR`

---

## Bước 4: Đăng ký vào dispatcher

Trong `CommandDispatcher`:

```java
handlers.put(CommandAction.HOADON_GET_BY_PAGE.name(), new HoaDonGetByPageHandler(...));
```

---

## Bước 5: Tạo client facade (remote service)

Tạo class:

```java
public class HoaDonRemoteService {
   ...
   public List<HoaDonDTO> getHoaDonByPage(...) { ... }
}
```

Flow:
1. Build request DTO
2. `sendCommand(...)`
3. Nếu fail -> map `ErrorCode` sang exception GUI hiểu được
4. Nếu success -> parse payload -> DTO result

---

## Bước 6: Tích hợp GUI

Ví dụ `HoaDonGUI`:
- Đổi từ local call sang remote call.
- Gọi trong `SwingWorker.doInBackground()`.
- `done()` cập nhật table như cũ.

---

## Bước 7: Kiểm thử

## 7.1 Functional
- Phân trang đúng
- Lọc đúng
- Search đúng

## 7.2 Error handling
- Payload thiếu field -> lỗi `BAD_REQUEST`
- Server down -> GUI báo mất kết nối

## 7.3 Runtime
- Chạy ổn định trong LAN
- Không treo UI

---

## 5) Template code chuẩn

## 5.1 Handler template

```java
public class FeatureHandler implements CommandHandler {
    private final FeatureService featureService = new FeatureService();

    @Override
    public MessageEnvelope handle(ClientSession session, MessageEnvelope request) {
        try {
            FeatureRequestDTO dto = JsonCodec.fromJsonNode(request.getPayload(), FeatureRequestDTO.class);
            validate(dto);

            var result = featureService.doSomething(...);
            return MessageEnvelope.responseOk(request.getMessageId(), JsonCodec.toJsonNode(result));

        } catch (IllegalArgumentException ex) {
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.BAD_REQUEST, ex.getMessage());
        } catch (Exception ex) {
            return MessageEnvelope.responseFail(request.getMessageId(), ErrorCode.SERVER_ERROR, "Lỗi hệ thống");
        }
    }
}
```

## 5.2 Remote service template

```java
public class FeatureRemoteService {
    private final SocketClientConnection connection;

    public FeatureRemoteService(SocketClientConnection connection) {
        this.connection = connection;
    }

    public FeatureResponseDTO callFeature(FeatureRequestDTO req) {
        MessageEnvelope response = connection.sendCommand(CommandAction.FEATURE_ACTION.name(), req, 5000);

        if (!response.isSuccess()) {
            throw mapError(response);
        }
        return JsonCodec.fromJsonNode(response.getPayload(), FeatureResponseDTO.class);
    }
}
```

---

## 6) Quy ước comment/log

## 6.1 Comment
- Viết tiếng Việt.
- Giữ keyword kỹ thuật nguyên gốc: `session`, `payload`, `event`, `command`, `timeout`, `fallback`, `correlationId`.

## 6.2 Log
- Prefix chuẩn:
  - `[SocketServer] ...`
  - `[SocketClient] ...`

Ví dụ:
- `[SocketServer] Đã có client kết nối: ...`
- `[SocketServer] Người dùng đã đăng nhập: ... (session=...)`

---

## 7) Checklist Done cho mỗi chức năng

- [ ] Có action trong `CommandAction`
- [ ] Có DTO transport request/response
- [ ] Có server handler
- [ ] Đăng ký dispatcher
- [ ] Có remote service method
- [ ] GUI đã gọi remote service
- [ ] Mapping error rõ ràng
- [ ] Test functional pass
- [ ] Test server-down/timeout pass
- [ ] Không block EDT

---

## 8) Chiến lược rollout

## 8.1 Migrate theo vertical slice
1. Login
2. Hóa đơn read-only
3. Phân ca
4. Khách hàng

## 8.2 Feature flag (khuyến nghị)
- Cho phép bật/tắt remote per-feature để fallback khi sự cố.

---

## 9) Pitfalls thường gặp

1. Reuse DTO nội bộ làm protocol -> dễ vỡ contract.
2. Quên correlation mapping -> response lẫn request.
3. Gọi network trên EDT -> UI freeze.
4. Không chuẩn hóa `ErrorCode` -> GUI xử lý lỗi rối.
5. Không timeout -> request treo vô hạn.

---

## 10) Mở rộng realtime sau migrate command

Sau khi command ổn định:
- Bổ sung event push:
  - `INVOICE_UPDATED`
  - `TABLE_STATUS_CHANGED`
  - `SHIFT_UPDATED`
- Client đăng ký listener và refresh UI theo event.

---

## 11) Kế hoạch thực thi nhanh (1 feature)

- **Day 1:** Contract + DTO + Handler skeleton
- **Day 2:** Remote service + GUI integration
- **Day 3:** Test + fix + update docs

---

## 12) Mẫu section cho Pull Request

- **Summary:** migrate chức năng nào, command nào
- **Protocol:** request/response JSON mẫu
- **Error handling:** mapping error code
- **GUI impact:** class/method đổi
- **Testing:** test cases đã chạy
