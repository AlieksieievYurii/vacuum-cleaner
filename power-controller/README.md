# Power Controller
This is simple electronic component which is responsible for switting on and off the vaccum cleaner robot. A push-button is connected to it. If the robot is turned off,
and the user pushes the button, then it switches on the main power. At that point the whole system is booting up. If the robot is working, and the user pushes the button,
then firstly the signal is send from the component to the core. The core starts shutting down. Once it is done, A1 module sends call back signal to the power controller 
and then the main power is cutted off.
