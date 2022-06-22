# A1 and Core comunication
The comunication between A1 and Core modules is done throught UART protocol. The following document describes the format of the commands.

# Format
To send the command to A1

`#<command number(2)>:<id(4)>:<parameters>\n`

Command number and ID must be hex.

A1 responces: 
  * `$S:<id(4)>\n` - if the command is executed successfuly
  * `$E:<id(4)>\n` - if the command is failed

Example: #021:1343:1\n

Read sensors:

`@<input_a>:<value_a>;<input_b>:<value_b>\n`


Instructions
| Command  |      parameters      |  Description |
|----------|:-------------:|------:|
| 0x01 | 0 - if everything is fine; 1 - something wrong | Inform that the core has been initialized |
| col 2 is |    centered   |   $12 |
| col 3 is | right-aligned |    $1 |
