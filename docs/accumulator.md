# Accumulator

This section describes the process of creating the accumulator for the project

Overview:
Cells: 4S</br>
Charging Voltage: 16.8V

[18650 Battery](https://allegro.pl/oferta/akumulator-18650-samsung-3500mah-35e-nowe-11328113273?snapshot=MjAyMi0wMy0xMlQyMTowODo0My43NThaO2J1eWVyO2E1ZTk4NmY4NDg0NGQ2N2E4YzMwZWNiMWI3NmJjYTk4ZDI0YzBlYTAxZGE5NDUyMmU4Mjk4N2NiNThkZjAwOGU%3D)

[BMS](https://www.aliexpress.com/item/1005002369756124.html?spm=a2g0o.order_detail.0.0.6509f19c1LpNGw)

Circuit:

![image](https://user-images.githubusercontent.com/39415360/158075999-13713601-98f8-441f-a50b-fcde13161558.png)

Outputs (v-Bx-Bx) are supposed to be connected to the microcontroller to read voltages.

Calculating voltages of the cells:</br>
`actual v-B1-B5 = v-B1-B5 - 0`</br>
`actual v-B2-B6 = v-B2-B6 - v-B1-B5`</br>
`actual v-B3-B7 = v-B3-B7 - v-B2-B6`</br>
`actual v-B4-B8 = v-B4-B8 - v-B3-B7`</br>

