#!/usr/bin/env bash

# System requirements
sudo python install/install_system.py

# Python requirements
sudo pip install -r requirements.txt

# Temp humidity sensor code
#sudo curl https://raw.githubusercontent.com/szazo/DHT11_Python/master/dht11.py > sensors/dht11.py

# Make service run on boot
sudo systemctl enable gpio-server.service

chmod 755 launcher.sh

mkdir /home/pi/logs

# todo:
#sudo crontab -e
#@reboot sh /home/pi/Pi-GPIO-Server/launcher.sh >/home/pi/logs/cronlog 2>&1
