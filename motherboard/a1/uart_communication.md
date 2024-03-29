# A1 and Core communication
The communication between A1 and Core modules is done through UART protocol. The following document describes the format of the commands.

# Input
The format of the instruction to send to A1:
`#<instruction number(2 bytes of hex)>:<ID(4 bytes of hex)>:<parameters>\n`

The whole instruction must start with `#`. After that, there always must be 2 bytes of instruction number e.g. 01, f1 etc. ID - unique number representing id of the request, must be always 4 bytes. The id is used in the responses. `parameters` - is a sequence of char elements (max length should be: 54 bytes). The whole instruction must end with `\n` character.

There are two types of requests: 
* **executive** - after instruction execution, a simple callback with the status is sent. Response format:</br>
   `$S:<Id(4 bytes of hex)>\n` - if the command is executed successfully</br>
   `$F:<Id(4 bytes of hex)>:<code error(one byte of hex)>\n` - if the instruction has failed. The response also contains error code
* **demandable** - after instruction execution, response callback with some data is sent. Response format:</br>
  `$R:<Id(4 bytes of hex)>:<reponse data(char array, max should be 54 bytes)>\n` - if the command is executed successfully</br>
  `$F:<Id(4 bytes of hex)>:<code error(one byte of hex)>\n` - if the instruction has failed. The response also contains error code

Example of the command: `#F1:7A31:H\n` and the response: `$S:7A31\n`.

# Instructions table(Executive)
| Instruction ID  |      parameters      |    Error Codes    |     Description      |
|-----------------|:--------------------:|:-----------------:|---------------------:|
| 0x01 | `S` - initialization is successful; `F` - something went wrong (power controller button starts blinking with red and green colors) | `0x1` - wrong parameter | Initialize and inform that the core has been initialized. This instruction is supposed to be sent once the core is initialized. At that point Power Button stops blinking and turns on just led green which means that the robot is working |
| 0x02 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | `0x1` - wrong parameter | Turns on, off or blink the Wifi led |
| 0x03 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | `0x1` - wrong parameter | Turns on, off or blink the Error led |
| 0x04 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | `0x1` - wrong parameter | Turns on, off or blink the Status led |
| 0x05 | `<beep count(max 2 bytes of hex)>;<period(max 4 bytes of hex)>` | `0x1` - Wrong parameters | Makes beeps |
| 0x06 | `<direction(1 byte of hex): 0x1 - forward or 0x2 - backward>;<distance(4 bytes of hex) in CM>;<speed(4 bytes of hex) cm per minute>;<halt mode(1 byte of hex) where 0x1 - stop, 0x2 - neutral>`, | `0x01` - Cannot parse the command, `0x02` - Wrong direction flag, `0x03` - wrong halt mode | Move the robot forward or backward |
| 0x07 | `<direction(1 byte of hex): 0x1-Left or 0x2-right>;<angle(4 bytes of hex)>;<speed(4 bytes of hex) cm per minute>;<halt mode(1 byte of hex) where 0x1 - stop, 0x2 - neutral>` | `0x01` - Cannot parse the command, `0x02` - Wrong direction flag, `0x03` - wrong halt mode | Turns left or right the robot |
| 0x08 | `<speed(2 bytes of hex)>` | `0x01` - Wrong speed value, must in range 0..0x64 | Sets speed for the vacuum motor |
| 0x09 | `<speed(2 bytes of hex)>` | `0x01` - Wrong speed value, must in range 0..0x64 | Sets speed for the left brush motor |
| 0x0A | `<speed(2 bytes of hex)>` | `0x01` - Wrong speed value, must in range 0..0x64 | Sets speed for the right brush motor |
| 0x0D | `<year(4 bytes of hex)>;<month(1 byte of hex)>;<day(2 bytes of hex)>;<hour(2 bytes of hex)>;<minute(2 bytes of hex)>;<second(2 bytes of hex)>` | `0x01` - wrong format | Sets Data Time |
| 0x0F | None | None | Sets shutting down state. This state must be set before sending the instruction (0x10) to cut off the power |
| 0x10 | None | None | Cuts off the power. Before sending this instruttion, the power state must be shutting down |
| 0x11 | `T` - sets error state for Power Controller; `F` - resets the error state | `0x1` - wrong parameter | Sets error state in Power Controller |
| 0x12 | `<speed(2 bytes of hex)>` | `0x01` - Wrong speed value, must in range 0..0x64 | Sets speed for the main brush motor |
| 0x13 | `<direction(1 byte of hex) where 0x01 - forward, 0x02 - backward>;<speed(4 bytes of hex) cm per minute>` | `0x01` - Cannot parse the command, `0x02` - Wrong direction flag | Move the robot endless |
| 0xFF | `<seconds(1 byte of hex)>` | `0x01` -  Wrong value. `0x02` - can not perform turing off while it is not shutting down | Sets timer in seconds when to cut off the power. Can be set only when the current status is Shutting Down |

# Instructions table(Demandable)
| Instruction ID  |      parameters      |      Response      |    Error Codes    |    Description      |
|-----------------|:--------------------:|:------------------:|------------------:|--------------------:|
|       0x0C      |  Data Time format, e.g `d-m-Y H:i:s`, `d F Y H:i:s` etc. | According to the data time format | None | Returns Data Time according to the given format |
|       0x0E      |         None         | `<temperature(float number) in celsius>;<humidity(float number)>;<heat index(float number) in celsius>` | 0x01 - can not read the data from the sensor | Gets temperature in Celsius, humidity in percentage and heat index in Celsius |

# Output
A1 reads the states of the sensors (cliff ends, buttons, etc.) and sends them out. The following format is made of key and value:

`@<id(2 bytes if hex)>:<value(4 bytes of hex)>;<id(2 bytes if hex)>:<value(4 bytes of hex)>\n`

# Output table
|  Id  |              Value            |                     Description                    |
|------|:-----------------------------:|:--------------------------------------------------:|
| 0x01 | Bits `**^**^**^**` where 00 - unpressed, 01 - click, 11 - long click. First pare - Bluetooth button, Second - Up, Third - OK, Fourth - Down. | Reads click events of control panel(Bluetooth,Up, Ok, Down buttons) |
| 0x02 | 4 bits where 1st - Right End, 2nd - Left End, 3rd - Lid End, 4th - Dust Box End. __Counting starts from the right__ | The state of ends such as right and left obstacle ends, lid end (checks if the cap is closed) and dust box end (check if the dust box is installed) |
| 0x03 | 3 bytes where 1st byte represents - Left Rangefinder(0..250 mm), 2nd - Center Rangefinder, 3rd - Right Rangefinder | Reads the distance of Right, Left and Center rangefinders. The range is 0..250 millimeters |
| 0x04 | 6 bits where(one if breakage): 1 - back right, 2 - back center, 3 - back left, 4 - front right, 5 - front center, 6 - front left | Reads cliff sensors. If the signal is high - then there is a breakage |
| 0x05 | 6 bits where the first three represents the robot state, next 2 - charging state and next 3 charging work state. Bits counting starts from the right to the left | Powering state, charging state and charging work state. Refer to Power Controller README.md |
| 0x06 | 2 bytes representing battery voltage. The first byte(from right to left) - decimal part, second byte - integer part | Voltages of the battery |
