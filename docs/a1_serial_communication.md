# A1 and Core comunication
The comunication between A1 and Core modules is done throught UART protocol. The following document describes the format of the commands.

# Input
To send the command to A1:

`#<command number(2 bytes of hex)>:<id(4 bytes of hex)>:<parameters>\n`

Each command supposed to return a response containing the status `S` or `F`:

  * `$S:<id(4 bytes of hex)>\n` - if the command is executed successfuly
  * `$F:<id(4 bytes of hex)>:<code error>\n` - if the command has failed. Also it contains error code

Example of the command: `#F1:7A31:H\n` and the response: `$S:7A31\n`.

# Output
A1 reads the states of all sensors(ends, buttons, etc) and sends to the core. The following format is made of key and value:

`@<id(2 bytes if hex)>:<value(4 bytes of hex)>;<id(2 bytes if hex)>:<value(4 bytes of hex)>\n`


# Instructions table
| Instruction ID  |      parameters      |    Error Codes    |    Description      |
|--------------|:--------------------:|:-----------------:|--------------------:|
| 0x01 | 0 - initialization is succsessful; 1 - something wrong| `0x1` - wrong parameter | Initialize and inform that the core has been initialized |
| 0x02 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | `0x1` - wrong parameter | Turns on, off or blink the Wifi led |
| 0x03 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | `0x1` - wrong parameter | Turns on, off or blink the Error led |
| 0x04 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | `0x1` - wrong parameter | Turns on, off or blink the Status led |
| 0x05 | `<beep count(max 2 bytes of hex)>;<period(max 4 bytes of hex)>` | `0x1` - Wrong parameters | Makes beeps |
| 0x06 | `<direction(1 byte of hex): 0x1 - forward or 0x2 - backward>;<distance(4 bytes of hex) in SM>;<speed(4 bytes of hex) sm per minute>;<halt mode(1 byte of hex) where 0x1 - stop, 0x2 - neutral, 0x3 - halt and neutral (TODO)>`, | `0x01` - Cannot parse the command, `0x02` - Wrong direction flag, `0x03` - wrong halt mode | Move the robot forward or backward |
| 0x07 | `<direction(1 byte of hex): 0x1-Left or 0x2-right>;<angle(4 bytes of hex)>;<speed(4 bytes of hex) sm per minute>;<halt mode(1 byte of hex) where 0x1 - stop, 0x2 - neutral, 0x3 - halt and neutral (TODO)>` | `0x01` - Cannot parse the command, `0x02` - Wrong direction flag, `0x03` - wrong halt mode | Turns left or right the robot |
| 0x08 | `<speed(2 bytes of hex)>` | `0x01` - Wrong speed value, must in range 0..0x64 | Sets speed for the vacuum motor |
| 0x09 | `<speed(2 bytes of hex)>` | `0x01` - Wrong speed value, must in range 0..0x64 | Sets speed for the left brush motor |
| 0x0A | `<speed(2 bytes of hex)>` | `0x01` - Wrong speed value, must in range 0..0x64 | Sets speed for the right brush motor |

# Output table
|  Id  |              Value            |                     Description                    |
|------|:-----------------------------:|:--------------------------------------------------:|
| 0x01 | Bits `**^**^**` where 00 - unpressed, 01 - click, 11 - long click. First pare - Up, Second - OK, third - Down. | Reads click events of controll panel(Up, Ok, Down buttons) |
| 0x02 | 4 bits where 1st - Right End, 2nd - Left End, 3rd - Lid End, 4th - Dust Box End. __Couting starts from the right__ | The state of ends |
