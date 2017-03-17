#!/usr/bin/env python
import datetime
import time
import RPi.GPIO as GPIO

LightOnSecs = 10

SmartThingsPin = 11 # pin11 smartthings controlled pin --- GPIO-17
LedPin = 13    # pin13 --- led GPIO-
MovPin = 12    # pin12 --- movement sensor GPIO-18
LightPin = 15 # pin15 --- light sensor GPIO-22

lastOnTime = datetime.datetime(2000, 1, 1, 0, 0)

class LightManager():

    def setup(self):
        GPIO.setmode(GPIO.BOARD)       # Numbers GPIOs by physical location
        GPIO.setup(SmartThingsPin, GPIO.OUT)
        GPIO.setup(LedPin, GPIO.OUT)   # Set LedPin's mode is output
        GPIO.setup(MovPin, GPIO.IN, pull_up_down=GPIO.PUD_UP) # Set pins mode is input, and pull up to high level(3.3V)
        GPIO.setup(LightPin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        GPIO.output(LedPin, GPIO.HIGH) # Set LedPin high(+3.3V) to off led

        # wait for falling and set bouncetime to prevent the callback function from being called multiple times when the button is pressed
        GPIO.add_event_detect(MovPin, GPIO.FALLING, callback=self.swMov, bouncetime=200)
        GPIO.add_event_detect(LightPin, GPIO.FALLING, callback=self.swLight, bouncetime=200)

    def swMov(self, ev=None):
        global lastOnTime

        currentTime = datetime.datetime.utcnow()
        lastOnTime = currentTime

        isLightOff = GPIO.input(LightPin)
        if isLightOff:
            print('Moving: Its dark!')
            self.lightOn()
            return

        print('Moving: Its light! I do not need to do anything')

    def swLight(self, ev=None):
        isLightOff = GPIO.input(LightPin)
        if not isLightOff:
            print('Its light!')
            self.lightOff()

    def lightOff(self):
        print('Light OFF')
        lastOnTime = datetime.datetime(2000, 1, 1, 0, 0)
        GPIO.output(LedPin, GPIO.HIGH)

    def lightOn(self):
        print('Light ON')
        lastOnTime = datetime.datetime.utcnow()
        GPIO.output(LedPin, GPIO.LOW)

    def loop(self):
        while True:
            smartOn = GPIO.input(SmartThingsPin)
            if smartOn:
                print('SmartThings requests light is ON')
                self.lightOn()
            else:
                currentTime = datetime.datetime.utcnow()
                elapsedTime = currentTime - lastOnTime

                print('Elapsed time: ' + str(elapsedTime))
                if elapsedTime.total_seconds() > LightOnSecs:
                    self.lightOff()

            time.sleep(1)   # Don't do anything

    def destroy(self):
        GPIO.output(LedPin, GPIO.HIGH)     # led off
        GPIO.cleanup()                     # Release resource

    def start(self):
        self.setup()
        self.loop()

    # if __name__ == '__main__':     # Program start from here
    #     try:
    #         start()
    #     except KeyboardInterrupt:
    #         # When 'Ctrl+C' is pressed, the child program destroy() will be executed.
    #         destroy()
