#!/usr/bin/env python3
"""
더미 데이터 생성 및 전송 프로그램
여러 날짜에 걸쳐 테스트용 데이터를 Django 서버로 전송합니다.
"""

import requests
import random
from datetime import datetime, timedelta
from pathlib import Path
import os

# 서버 설정
HOST = 'https://somyonn.pythonanywhere.com'
# HOST = 'http://127.0.0.1:8000'  # 로컬 테스트용
username = 'user'
password = 'user'

# 인증 토큰 획득
def get_token():
    """서버에서 인증 토큰을 받아옵니다."""
    try:
        res = requests.post(HOST + '/api-token-auth/', {
            'username': username,
            'password': password,
        })
        res.raise_for_status()
        token = res.json()['token']
        print(f"✅ 인증 토큰 획득 성공: {token[:20]}...")
        return token
    except Exception as e:
        print(f"❌ 인증 실패: {e}")
        return None

# 더미 이미지 생성
def create_dummy_image(filename):
    """더미 이미지 파일을 생성합니다."""
    from PIL import Image, ImageDraw, ImageFont
    
    # 320x240 크기의 더미 이미지 생성
    img = Image.new('RGB', (320, 240), color=(random.randint(100, 255), 
                                                random.randint(100, 255), 
                                                random.randint(100, 255)))
    draw = ImageDraw.Draw(img)
    
    # 텍스트 추가
    try:
        draw.text((10, 10), f"Dummy Image\n{filename}", fill=(0, 0, 0))
    except:
        pass
    
    img.save(filename, 'JPEG')
    return filename

# 더미 데이터 전송
def send_dummy_post(token, date, title, text, image_path):
    """더미 포스트를 서버로 전송합니다."""
    headers = {'Authorization': 'Token ' + token, 'Accept': 'application/json'}
    
    # 날짜를 ISO 형식 문자열로 변환
    date_str = date.strftime('%Y-%m-%dT%H:%M:%S')
    
    data = {
        'author': 1,
        'title': title,
        'text': text,
        'created_date': date_str,
        'published_date': date_str
    }
    
    try:
        with open(image_path, 'rb') as f:
            files = {'image': f}
            res = requests.post(HOST + '/api_root/Post/', data=data, files=files, headers=headers)
            res.raise_for_status()
            print(f"✅ 전송 성공: {date.strftime('%Y-%m-%d %H:%M:%S')} - {title}")
            return True
    except Exception as e:
        print(f"❌ 전송 실패 ({date.strftime('%Y-%m-%d %H:%M:%S')}): {e}")
        return False

def main():
    """더미 데이터 생성 및 전송 메인 함수"""
    print("=" * 50)
    print("더미 데이터 생성 프로그램")
    print("=" * 50)
    
    # 인증 토큰 획득
    token = get_token()
    if not token:
        return
    
    # 임시 이미지 디렉토리 생성
    temp_dir = Path('temp_images')
    temp_dir.mkdir(exist_ok=True)
    
    # 객체 이름 리스트 (YOLOv5 COCO 80개 클래스 중 일부)
    object_names = [
        'person', 'bicycle', 'car', 'motorcycle', 'airplane', 'bus', 'train', 'truck',
        'boat', 'traffic light', 'fire hydrant', 'stop sign', 'parking meter', 'bench',
        'bird', 'cat', 'dog', 'horse', 'sheep', 'cow', 'elephant', 'bear', 'zebra',
        'giraffe', 'backpack', 'umbrella', 'handbag', 'tie', 'suitcase', 'frisbee',
        'skis', 'snowboard', 'sports ball', 'kite', 'baseball bat', 'baseball glove',
        'skateboard', 'surfboard', 'tennis racket', 'bottle', 'wine glass', 'cup',
        'fork', 'knife', 'spoon', 'bowl', 'banana', 'apple', 'sandwich', 'orange',
        'broccoli', 'carrot', 'hot dog', 'pizza', 'donut', 'cake', 'chair', 'couch',
        'potted plant', 'bed', 'dining table', 'toilet', 'tv', 'laptop', 'mouse',
        'remote', 'keyboard', 'cell phone', 'microwave', 'oven', 'toaster', 'sink',
        'refrigerator', 'book', 'clock', 'vase', 'scissors', 'teddy bear', 'hair drier',
        'toothbrush'
    ]
    
    print("\n📊 더미 데이터 생성 시작...")
    print(f"   - 서버: {HOST}")
    print(f"   - 기간: 최근 30일")
    print(f"   - 일별 데이터: 1~5개 랜덤\n")
    
    success_count = 0
    fail_count = 0
    
    # 최근 30일간의 데이터 생성
    base_date = datetime.now()
    
    for day_offset in range(30):
        # 날짜 설정
        current_date = base_date - timedelta(days=day_offset)
        
        # 하루에 1~5개의 데이터 생성
        num_posts = random.randint(1, 5)
        
        for post_num in range(num_posts):
            # 시간 랜덤 설정 (하루 중 랜덤 시간)
            hour = random.randint(0, 23)
            minute = random.randint(0, 59)
            second = random.randint(0, 59)
            
            post_date = current_date.replace(hour=hour, minute=minute, second=second, microsecond=0)
            
            # 랜덤 객체 선택
            detected_object = random.choice(object_names)
            title = detected_object
            text = f"{detected_object}, {random.choice(object_names)}"
            
            # 더미 이미지 생성
            image_filename = temp_dir / f"dummy_{post_date.strftime('%Y%m%d_%H%M%S')}.jpg"
            create_dummy_image(str(image_filename))
            
            # 서버로 전송
            if send_dummy_post(token, post_date, title, text, str(image_filename)):
                success_count += 1
            else:
                fail_count += 1
            
            # 이미지 파일 삭제
            try:
                os.remove(image_filename)
            except:
                pass
    
    # 정리
    try:
        temp_dir.rmdir()
    except:
        pass
    
    print("\n" + "=" * 50)
    print(f"✅ 완료: 성공 {success_count}개, 실패 {fail_count}개")
    print("=" * 50)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️ 사용자에 의해 중단되었습니다.")
    except Exception as e:
        print(f"\n\n❌ 오류 발생: {e}")
        import traceback
        traceback.print_exc()

