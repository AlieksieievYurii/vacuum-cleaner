# A1 Serial Communication
A1 communication works with JSON format

## Sending commands
```
{
  "id": ...,
  "com": ...,
  "params": ...
}
```
Fields:
* `id` [string] - random unique id.
* `com` [string] - stands for command.
* (optional) `params` [json] - required parameters by a command.

Response:
```
{
  "id": ...,
  "res": ...,
  "err": ...
}
```
Fields:
* `id` [string] - the same id as in sended command.
* `res` [string] - if `OK` - everything is allright, otherwise `ERR`.
* (optional) `err` [string] - if `res` is `ERR` then this field will contain error message
