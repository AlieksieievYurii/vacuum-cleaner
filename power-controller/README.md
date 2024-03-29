# Power Controller
This is simple electronic component which is responsible for proper switting on and off the vaccum cleaner robot. The communication with A1 is done through I2C protocol. To the board a push-button is connected, so if the robot is turned off, and the user pushes the button, then it switches on the main power. At that point the whole system is booting up. If the robot is working, and the user pushes the button, then firstly the signal is send from the component to the core. The core starts shutting down. Once it is done, A1 module sends call back signal to the power controller and then the main power is cutted off. Also Power Controller is responsible for the battery charging. It checks if the voltage is suitable, if the DC-DC charger is working properly. It also reads voltages of all battery cells. You can get data
such as power state, charging state, charging work state, voltage of battery cells just by calling request. You can also send commands to the board by sending only one
byte ID of the command. Please refer to the following table:

# Commands
|    Command ID    |   Description    |
|-----------------:|:----------------:|
|      **0x1**        |  Set state: TURN_ON. It will work only if the current state is BOOTING_UP |
|      **0x2**        |  Set state: SHUTTING_DOWN. It will work only if the current state is TURNED_ON | 
|      **0x3**        |  Set state: TURNED_OFF. It will work only if the current state is SHUTTING_DOWN |
|      **0x4**        |  Sets error state; the button starts blinking with red and green colors undepentently on the power state |
|      **0x5**        |  Reset error state |


# Responses
To get data from the Power Controller, just request 7 bytes of data.
The response consist of 7 bytes where:
* 1 byte -> represents id of power state. See [Powet States Table](#power_states)
* 2 byte -> represents id of charging state. See [Charging States Table](#charging_states)
* 3 byte ->  represents id of charging work state(if the charging is working properly). See [Charging Work States Table](#charging_work_states)
* 4 byte -> represents integer part of the battery voltage
* 5 byte -> represents decimal part of the battery voltage

## <a name="power_states">Power States</a>
|    ID    |   Description    |
|-----------------:|:----------------:|
|    **0x0** (TURNED_OFF)    |  The powet is cutted off from the motherboard |
|    **0x1** (BOOTING_UP)    |  Power is on however it expects to get command (0x01) to set TURNED_ON state | 
|    **0x2** (TURNED_ON)     |  Power is completely on |
|    **0x3** (SHUTTING_DOWN) | The power controller is in state SHUTTING_DOWN which means that the power is going to be cutted off. It expects command (0x03) to turn cut off the power and set the state TURNED_OFF |

## <a name="charging_states">Charging States</a>
|             ID          |              Description                 |
|------------------------:|:-----------------------------------------|
| **0x0** (NOT_CHARGING)  | The charger is not plugged in. Dispice of if the battery is charged or not         |
| **0x1** (CHARGING)      | The charger is plugged in and the battery is charging |
| **0x2** (CHARGED)       | The charger is plugged in but the battery is charged.  |

## <a name="charging_work_states">Charging Work States Table</a>

|    ID    |   Description    |
|-----------------:|:----------------:|
|   **0x0** (OK)   |  Everything is fine. Charging is working properly  |
|   **0x1** (OVERVOLTAGE)  |  Charging voltage(mostly after DC-DC converter) is more then acceptable(15V +- 1) |
|   **0x2** (UNDERVOLTAGE)  | Charging voltage(mostly after DC-DC converter) is lower then acceptable(15V +- 1) |
|   **0x3** (DISCREPANCY)  | Something is wrong with DC-DC(lm2596) converter/charger |

When the charged is connected the led starts fading in and out undepentently on the power state and error state.
