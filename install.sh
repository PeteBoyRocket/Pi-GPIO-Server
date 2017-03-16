#!/usr/bin/env bash

# System requirements
sudo python install/install_system.py

# Python requirements
sudo pip install -r requirements.txt

# Temp humidity sensor code
sudo curl https://raw.githubusercontent.com/szazo/DHT11_Python/master/dht11.py > dht11.py
