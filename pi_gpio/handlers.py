from flask.ext.restful import fields
from meta import BasicResource
from config.pins import PinHttpManager
from sensors.temphumidity import TempHumiditySensor

HTTP_MANAGER = PinHttpManager()
TEMP_HUMID_SENSOR = TempHumiditySensor()

class Pin(BasicResource):

    def __init__(self):
        super(Pin, self).__init__()
        self.fields = {
            "num": fields.Integer,
            "name": fields.String,
            "mode": fields.String,
            "value": fields.Integer,
            "resistor": fields.String,
            "initial": fields.String,
            "event": fields.String,
            "bounce": fields.Integer
        }

    def pin_not_found(self):
        return {'message': 'Pin not found'}, 404


class PinList(Pin):

    def get(self):
        result = HTTP_MANAGER.read_all()
        return self.response(result, 200)


class PinDetail(Pin):

    def get(self, pin_num):
        result = HTTP_MANAGER.read_one(pin_num)
        if not result:
            return self.pin_not_found()
        return self.response(result, 200)

    def patch(self, pin_num):
        self.parser.add_argument('value', type=int)
        args = self.parser.parse_args()
        result = HTTP_MANAGER.update_value(pin_num, args['value'])
        if not result:
            return self.pin_not_found()
        return self.response(HTTP_MANAGER.read_one(pin_num), 200)

class Data(BasicResource):

    def __init__(self):
        super(Data, self).__init__()
        self.fields = {
            "lighton": fields.String,
            "temp": fields.String,
            "humidity": fields.String,
            "islight": fields.String,
            "motion": fields.String
        }

    def get(self):

        lighton = 1
        if HTTP_MANAGER.read_value(13) == 1:
            lighton = 0

        motion = HTTP_MANAGER.read_value(12)

        islight = 1
        if HTTP_MANAGER.read_value(15) == 1:
            islight = 0

      #  tempAndHumidity = TEMP_HUMID_SENSOR.getTempAndHumidity()

        data = {
            'lighton': lighton,
            'temp': "22",
            'humidity': "45",
            'islight': islight,
            'motion': motion
        }
        return self.response(data, 200)

class DataChanger(BasicResource):

    def patch(self, value):

        try:
            SmartThingsPin = 11

            if value == "1":
                return {'message': 'high set'}, 200
            else:
                return {'message': 'low set'}, 200
        except Exception as e:
            return {'message': "Dint work!" + e}, 500
