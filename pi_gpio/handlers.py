from flask.ext.restful import fields
from meta import BasicResource
from config.pins import PinHttpManager

import RPi.GPIO as GPIO

HTTP_MANAGER = PinHttpManager()


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

        # http://stackoverflow.com/questions/27114545/parse-args-doesnt-seem-to-understand-json-arguments
        # self.reqparser.add_argument('events', type=list, location='json', required=True)
        # request.json.get('events')

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
            "temperature": fields.String,
            "humidity": fields.String
        }

    def get(self):
        data = {
            'lighton': "True",
            'temperature': "22",
            'humidity': "23"
        }
        return self.response(data, 200)

    def patch(self, value):

        SmartThingsPin = 11

        if value == 1:
            GPIO.output(SmartThingsPin, GPIO.HIGH)
            return self.response("High set", 200)
        else:
            GPIO.output(SmartThingsPin, GPIO.LOW)
            return self.response("Low set", 200)

