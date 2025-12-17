import os
import cv2
import pathlib
import requests
import time
from datetime import datetime, timedelta

class ChangeDetection:
    result_prev = []
    # HOST = 'http://127.0.0.1:8000'
    HOST = 'https://somyonn.pythonanywhere.com'
    username = 'user'
    password = 'user'
    # token = '40e61cc85d828c131094bbdf9f21a52f8a6'
    token = 'e384460136b565eccc0c70db839bdf8a85118b5d'
    title = ''
    text = ''
    last_send_time = None  # 마지막 전송 시간
    cooldown_seconds = 600  # 10분 = 600초
    start_time = None  # 초기화 시작 시간
    initial_delay_seconds = 60  # 시작 후 10초간 전송 금지

    def __init__(self, names):
        self.result_prev = [0 for i in range(len(names))]
        self.last_send_time = None  # 초기화
        self.start_time = time.time()  # 시작 시간 기록
        res = requests.post(self.HOST + '/api-token-auth/', {
            'username': self.username,
            'password': self.password,
        })
        res.raise_for_status()
        self.token = res.json()['token']  # 토큰 저장
        print(self.token)
        print(f"초기화 완료. 시작 후 {self.initial_delay_seconds}초간은 전송하지 않습니다.")

    def add(self, names, detected_current, save_dir, image):
        self.title = ''
        self.text = ''
        change_flag = 0  # 변화 감지 플래그
        i = 0
        while i < len(self.result_prev):
            if self.result_prev[i] == 0 and detected_current[i] == 1:
                change_flag = 1
                self.title = names[i]
                self.text += names[i] + ", "
            i += 1
        self.result_prev = detected_current[:]  # 객체 검출 상태 저장
        
        # 변화가 감지되었고, 쿨다운 시간이 지났는지 확인
        if change_flag == 1:
            current_time = time.time()
            
            # 시작 후 초기 지연 시간(10초) 체크
            elapsed_since_start = current_time - self.start_time
            if elapsed_since_start < self.initial_delay_seconds:
                remaining_initial = self.initial_delay_seconds - elapsed_since_start
                print(f"초기 지연 중: {remaining_initial:.1f}초 후 전송 가능 (변화 감지됨: {self.title})")
                return
            
            # 마지막 전송 시간이 없거나, 쿨다운 시간이 지났으면 전송
            if self.last_send_time is None:
                self.send(save_dir, image)
            else:
                elapsed_time = current_time - self.last_send_time
                if elapsed_time >= self.cooldown_seconds:
                    self.send(save_dir, image)
                else:
                    remaining_time = self.cooldown_seconds - elapsed_time
                    print(f"쿨다운 중: {remaining_time:.1f}초 후 전송 가능 (변화 감지됨: {self.title})")

    def send(self, save_dir, image):
        now = datetime.now()
        # ISO 포맷 문자열은 필요 시 사용
        # now_iso = now.isoformat()

        today = now
        save_path = pathlib.Path(os.getcwd()) / save_dir / 'detected' / str(today.year) / str(today.month) / str(today.day)
        pathlib.Path(save_path).mkdir(parents=True, exist_ok=True)
        full_path = save_path / '{0}-{1}-{2}-{3}.jpg'.format(today.hour, today.minute, today.second, today.microsecond)
        dst = cv2.resize(image, dsize=(320, 240), interpolation=cv2.INTER_AREA)
        cv2.imwrite(str(full_path), dst)

        # 인증이 필요한 요청에 아래의 headers를 붙임
        headers = {'Authorization': 'JWT ' + self.token, 'Accept': 'application/json'}

        # Post Create
        data = {
            'author': 1,
            'title': self.title,
            'text': self.text,
            'created_date': now,
            'published_date': now
        }
        file = {'image': open(str(full_path), 'rb')}
        try:
            res = requests.post(self.HOST + '/api_root/Post/', data=data, files=file, headers=headers)
            res.raise_for_status()  # 성공/실패 여부에 따라 예외 발생
            print("응답 성공:", res)
            # 전송 성공 시 마지막 전송 시간 업데이트
            self.last_send_time = time.time()
            print(f"전송 완료. 다음 전송까지 {self.cooldown_seconds}초 대기합니다.")
        except requests.exceptions.HTTPError as err:
            print("HTTP 에러 발생:", err)
            print("응답 내용:", res.text)  # 에러 시 서버가 보내온 상세 메시지 출력
        except requests.exceptions.RequestException as e:
            print("요청 실패:", e)
        finally:
            file['image'].close()