from pi_gpio import app, socketio
from light.lightManager import lightManager

if __name__ == '__main__':
    socketio.run(app, host="0.0.0.0")

    lightManager.start()
