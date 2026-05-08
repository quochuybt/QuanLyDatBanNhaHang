# Socket Protocol Reference

## Envelope
`MessageEnvelope` gồm các field chính:
- `messageId`
- `type`
- `name`
- `correlationId`
- `timestamp`
- `success`
- `errorCode`
- `message`
- `payload`

## MessageType
- `COMMAND`
- `RESPONSE`
- `EVENT`
- `PING`
- `PONG`

## CommandAction hiện có
- `AUTH_LOGIN`
- `AUTH_LOGOUT` (reserved)
- `PING`

## EventType hiện có
- `SESSION_KICKED`
- `SERVER_NOTICE`

## ErrorCode hiện có
- `AUTH_INVALID`
- `AUTH_LOCKED`
- `BAD_REQUEST`
- `SERVER_ERROR`
- `SERVER_UNREACHABLE`
- `DISCOVERY_NOT_FOUND`

## Ví dụ command login
```json
{
  "messageId":"<uuid>",
  "type":"COMMAND",
  "name":"AUTH_LOGIN",
  "payload":{"tenTK":"admin","matKhau":"123456"}
}
```
