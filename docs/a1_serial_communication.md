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

## A1 Commands
### Enable Output Reading
Description: This command enables reading of A1 output.</br>
Command: `EOR`</br>
Parameters:
  * `s` [bool] - if `true` - enables reading A1 output, if `false` - disables


### Test Led
Description: this command turn off/on connected led to 13 port.</br>
Command: `TL`</br>
Parameters:
  * `s` [bool] - if `true` - turn on test led, if `false` - turn off test led

### Start Vaccum Motor
Description: This command starts main vaccum motor.</br>
Command: `VM`</br>
Parameters:
  * `v` [int] - value in 0..100 range. 0 - disable the motor completely 

# Sensors
Once the EOR is enable, the all sensors are readed and sent to the serrial. 
```
{
  "out": {
    "tms": int
  }
}
```

Outputs:
* `tms` - main vaccum mottor cover temperature
