# Power Controller
This is simple electronic component which is responsible for switting on and off the vaccum cleaner robot. A push-button is connected to it. If the robot is turned off,
and the user pushes the button, then it switches on the main power. At that point the whole system is booting up. If the robot is working, and the user pushes the button, then firstly the signal is send from the component to the core. The core starts shutting down. Once it is done, A1 module sends call back signal to the power controller and then the main power is cutted off.


# Commands
|    Command ID    |   Description    |
|-----------------:|:----------------:|
|      0x01        |  Set state: TURN_ON. It will work only if the current state is BOOTING_UP |
|      0x02        |  Set state: SHUTTING_DOWN. It will work only if the current state is TURNED_ON | 
|      0x03        |  Set state: TURNED_OFF. It will work only if the current state is SHUTTING_DOWN |
|      0x06        |  Sets error state; the button starts blinking with red and green colors undepentently on the power state |
|      0x07        |  Reset error state |


# Responses
The response consist of two bytes where the first byte represents power state and the second one represents battery state.

## Power State
|    ID    |   Description    |
|-----------------:|:----------------:|
|      0x0        |  The powet is cutted off from the motherboard |
|      0x1        |  Power is on however it expects to get command (0x01) to set TURNED_ON state | 
|      0x2        |  Power is completely on |
|      0x3        | The power controller is in state SHUTTING_DOWN which means that the power is going to be cutted off. It expects command (0x03) to turn cut off the power and set the state TURNED_OFF |

## Charding State

|    ID    |   Description    |
|-----------------:|:----------------:|
|   0x0   |  No charging detected  |
|   0x1   | Charging |
|   0x2   | Charging, however the voltage is higher then acceptable |
|   0x3   | Charging, however the voltage is lower then acceptable |

When the charged is connected the led starts fading in and out undepentently on the power state and error state.
