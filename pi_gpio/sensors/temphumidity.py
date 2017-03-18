import RPi.GPIO as GPIO
import time
import dht11
#import sys

pin = 7

GPIO.setmode(GPIO.BOARD)

DHT11 = dht11.DHT11(pin)

class TempHumiditySensor():

    def getTempAndHumidity(self):

        for i in range(15):
            result = DHT11.read()
            if result.is_valid():
                return result.temperature, result.humidity
            time.sleep(1)
        return (None, None)


    #     result = DHT11.read()
    #     if result.is_valid():
    #   #  print("Last valid input: " + str(datetime.datetime.now()))
    #   #  print("Temperature: %d C" % result.temperature)
    # #   print("Humidity: %d %%" % result.humidity)
    #   #  print result.temperature
    #         return result.temperature, result.humidity

    #     return "NA", "NA"
