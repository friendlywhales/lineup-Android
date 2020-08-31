import requests
import json
import random

DEVICE_TOKEN = "dgem4SyYnv0:APA91bFHBdwq8lzAd7br1vx5qbzkPhO_la9VUQ84etseV5ZR8sHzeEXJEbb9oLZFCfrhKs40nx_2-gvjAq7R-KQZ6_Mr8XKDDMcdfP8gJNwdPLDbuKC5_QWvpYoAlwsXNY9njAKQ0Zcx"
SERVER_KEY = "AAAATg27SY8:APA91bFHnCEEEo3uu2MjQ5BjZ1NlrINIfQEmkMwP6GEkH9AQ1CrcgnSEoT4fJDFwbDpUt5o6veEvk38fTOivW28aa2Ed8wGB4cjPIElueL-rZTpwfOOvSYmaKU2e_4dl00EKNj658XdU"

def run():
    payload = {
        "notification": {
            "title": "title",
            "body": "body",
        },
        "priority": "high",
        "data": {
            "something": "important",
            "age": 27,
            "random": random.random(),
        },
        "to": DEVICE_TOKEN,
    }

    r = requests.post('https://fcm.googleapis.com/fcm/send', headers={
            'Content-Type': 'application/json',
            'Authorization': 'key=%s' % SERVER_KEY
        }, data=json.dumps(payload))
    resp = r.json()
    print(resp)


if __name__ == '__main__':
    run()
