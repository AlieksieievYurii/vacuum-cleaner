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
| Instruction  |      parameters      |    Error Codes    |    Description      |
|--------------|:--------------------:|:-----------------:|--------------------:|
| 0x01 | 0 - initialization is succsessful; 1 - something wrong| 1 - wrong parameter | Initialize and inform that the core has been initialized |
| 0x02 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | 1 - wrong parameter | Turns on, off or blink the Wifi led |
| 0x03 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | 1 - wrong parameter | Turns on, off or blink the Error led |
| 0x04 |  Values: `H` - turn on the led, `L` - turn off the led, `B` - blinking the led | 1 - wrong parameter | Turns on, off or blink the Status led |

# Output table
|  Id  |              Value            |                     Description                    |
|------|:-----------------------------:|:--------------------------------------------------:|
| 0x01 | Bits `**^**^**` where 00 - unpressed, 01 - click, 11 - long click. First pare - Up, Second - OK, third - Down. | Reads click events of controll panel(Up, Ok, Down buttons) |
