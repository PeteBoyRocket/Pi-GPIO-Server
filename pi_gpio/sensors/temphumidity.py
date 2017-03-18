import dht11

pin = 7

DHT11 = dht11.DHT11(pin)

class TempHumiditySensor():

    def getTempAndHumidity(self):

        for i in range(15):
            result = DHT11.read()
            if result.is_valid():
                return (result.temperature, result.humidity)
        return (None, None)
