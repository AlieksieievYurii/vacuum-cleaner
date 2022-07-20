# Raspberry Pi Zero W - Bluethooth Connection 

This document describes how to connect custom Android Application to Raspberry Pi Zero W. Android Application is supposed to be a **Client**, and Raspberry Pi as **Server**. However, the server can send requests to the Android App too. The final purpose of the document is guideline how to create communication beetwen Android Application and Raspberry Pi Zero W and sending custom data.

## Rasperry Bluetooth
1. Set up bluetooth in compatibility mode. Just modify `/etc/systemd/system/dbus-org.bluez.service`, add `-C` or `--compat` to `ExecStart=/usr/lib/bluetooth/bluetoothd`.
It must be `ExecStart=/usr/lib/bluetooth/bluetoothd -C`. Explanation: [sdptool is broken in Bluez 5](https://bbs.archlinux.org/viewtopic.php?id=201672).
You need to run the bluetooth daemon in compatibility mode to provide deprecated command line interfaces. 
You're running Bluez5 and you need some Bluez4 functions.
2. Restart the bluetooth service: 
    ```
    sudo systemctl daemon-reload
    sudo systemctl restart bluetooth
    ```
3. Run `sudo sdptool add SP` to generate `/var/run/sdp`

### To pair Android Phone to Raspberry Pi Zero W
1. Open [bluetoothctl](https://www.linux-magazine.com/Issues/2017/197/Command-Line-bluetoothctl) tool and provide the following commands:
```
sudo bluetoothctl <<EOF
power on
agent off
discoverable on
pairable on
agent NoInputNoOutput
default-agent 
```
The configuration of Bluetooth is done, however if you want to pair some devices, you do not have to close bluetoothctl tool because you will get a confirmation.
I have written [Python Script](https://github.com/AlieksieievYurii/vacuum-cleaner/blob/main/script/pi_scripts/pairing.py) that waits for incoming Bluetooth pairing requests.

2. For RFCOMM communication, a special Python library is used: [pybluez](https://github.com/pybluez/pybluez). There you can find how to install it
3. For an experiment the following example was used: [example](https://github.com/pybluez/pybluez/blob/master/examples/simple/rfcomm-server.py).
4. Just run the script. It will wait for connection. It the error `_bluetooth.error: no advertisable device` is thrown, type the following `sudo hciconfig hci0 piscan`


### Create Android Application
To create an Android Application that communicates with Raspberry Pi Zero via Bluetooth:
1. Following this guide to create Android App with Bluetooth -> [link](https://developer.android.com/guide/topics/connectivity/bluetooth)
2. To pair the device pragmatically, just call `BluetoothDeviceItem.createBond`. To get status of it, register Broadcast receiver with an action `BluetoothDevice.ACTION_BOND_STATE_CHANGED`.![image](https://user-images.githubusercontent.com/39415360/134776029-af0e1dc7-866d-4bc4-bdc7-96516fdda8d9.png)

