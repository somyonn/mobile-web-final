# mobile-web-final

모바일/웹서비스 프로젝트 기말 과제

- `PhotoBlogServer/`: Django 서버
- `PhotoViewer/`: Android 클라이언트
- `yolov5/`: Edge 클라이언트 (변화 감지 후 서버로 업로드)

## 설정

비밀값은 코드에 넣지 않고 `.env`로 관리한다. `.env.example`을 `.env`로 복사해 값을 채운다.

```bash
cp .env.example .env
```

- Django 서버는 `PhotoBlogServer/.env`를 읽는다 (`cp .env.example PhotoBlogServer/.env`).
- `yolov5/changedetection.py`, `generate_dummy_data.py`는 저장소 루트의 `.env`를 읽는다.
- `python-dotenv`가 필요하다 (`pip install python-dotenv`).
- Android 앱은 `PhotoViewer/HelloAndroid/local.properties`에 `photoblog.token=<토큰>`을 넣으면 빌드 시 `BuildConfig.API_TOKEN`으로 들어간다.
