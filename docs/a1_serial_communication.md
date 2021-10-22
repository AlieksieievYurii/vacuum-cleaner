# A1 Serial Communication
A1 communication works with JSON format

## Sending commands
```
{
  "id": <string>,
  "command": <string>,
  "body": <string>
}
```

Response:
```
{
  "id": <string>,
  "status": OK | ERROR
}
```
