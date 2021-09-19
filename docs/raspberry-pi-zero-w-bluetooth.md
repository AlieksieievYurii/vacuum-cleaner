# Raspberry Pi Zero W - Bluethooth Connection 

This document describes how to connect custom Android Application to Raspberry Pi Zero W. Android Application is supposed to be a **Client**, and Raspberry Pi as **Server**.
The final purpose of the document is guide how to create communication beetwen Android Application and Raspberry Pi Zero W and sending custom data.

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

2. For RFCOMM communication special Python library is used: [pybluez](https://github.com/pybluez/pybluez). There you can find how to install it
3. For an experiment the following example was used: [example](https://github.com/pybluez/pybluez/blob/master/examples/simple/rfcomm-server.py).
4. Just run the script. It will wait for connection. It the error `_bluetooth.error: no advertisable device` is thrown, turn on discoverable mode `sudo bluetoothctl discoverable on`
