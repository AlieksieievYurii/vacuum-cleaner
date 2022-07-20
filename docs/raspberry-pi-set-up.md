# Raspberry Pi Zero W Set Up

This section describes how to install OS and set up Raspberry Pi Zero.

1. Download Raspberry Pi OS Lite -> [link](https://downloads.raspberrypi.org/raspios_lite_armhf/images/raspios_lite_armhf-2021-05-28/2021-05-07-raspios-buster-armhf-lite.zip)
2. Download Raspberry Pi Imager -> [link](https://www.raspberrypi.org/software/)
3. Insert microSD(note: enable writing mode -> turn on physically the switcher on the microSD Adapter), run Raspberry Pi Imager: select downloaded OS, choose the SD card and press “Write”
4. Once flashing microSD is done successfully, open `boot` folder and create a file `wpa_supplicant.conf` to connect automatically to Wifi. Put the following content into the file: 
```
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1
country=US

network={
  ssid="<SSID>"
  psk="<PASSWORD>"
  key_mgmt=WPA-PSK
  scan_ssid=1
}
```
Where:
* `<SSID>` - your Wi-Fi SSID.
* `<PASSWORD>` - and its password.

**Note:** Raspberry Pi Zero W does not connect to 5GHz Wi-Fi

5. Since the Pixel version of Raspbian in December 2016, the SSH connection is disabled by default. To enable SSH, just create a file **ssh** (no file extension needed).

6. Once flashing microSD is done successfully, insert the microSD card into the Pi and power with the micro USB cable. Now, after the Pi turns on, in about 30-90 seconds.
7. Once the above steps are done, try to connect to the raspberry pi:
    * If you only have one Raspberry Pi on your network, you can SSH into the device via its hostname: `ssh pi@raspberrypi.local`
    * If you have more than one Pi on your network, a number will be attached to the `hostname.raspberrypi-NUM.local`. `NUM` will be the order in which they came on the network
    * Default password is `raspberry`
    * **Note:** if you have already tried to connect to Rasbperry Py, you may meet the following error while connecting via SSH: ![image](https://user-images.githubusercontent.com/39415360/133641502-f27d22b7-791f-4bd4-b71c-384008f68f1a.png) 

To solve the issue, go to the .ssh folder, usually it is located in the user’s home directory -> `C:\Users\newgo\.ssh` and open the file `known_hosts`. In that file, you should delete line with raspberrypi.

8. It's highly recommended to update your device and change the default password:
    * To update the Pi: `sudo apt-get update && sudo apt-get upgrade -y`
    * By default python 3 should be installed. However, pip is not installed. To install it for pyhton3: `sudo apt-get install python3-pip`
    * [Optional] To install [virtual environment](https://docs.python.org/3/library/venv.html) for Python using pip: `python3 -m pip install virtualenv`
    * To set up new password: passwd

