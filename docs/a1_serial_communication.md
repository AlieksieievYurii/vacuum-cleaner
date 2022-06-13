# A1 and Core comunication
The comunication between A1 and Core modules is done throught UART protocol. The following document describes the format of the commands.

# Format
To send the command to A1

`#<command number(3)>:<id(4)>:<parameters>\n`

A1 responces: 
  * `$S:<id(4)>\n` - if the command is executed successfuly
  * `$E:<id(4)>:<description>\n` - if the command is failed

Example: #021:1343:1\n

Read sensors:

`@<input_a>:<value_a>;<input_b>:<value_b>\n`
