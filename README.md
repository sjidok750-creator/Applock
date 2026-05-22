# AppLock

설치된 Android 앱을 **4자리 PIN**으로 잠그는 간단한 앱입니다.

## 동작 방식

1. 첫 실행 시 4자리 PIN을 등록합니다.
2. 보호할 앱을 목록에서 토글합니다.
3. 백그라운드 서비스가 포그라운드 앱을 감시하다가, 잠긴 앱이 열리면 PIN 입력 화면을 띄웁니다.
4. PIN을 맞게 입력하면 5분간 잠금이 해제된 상태로 유지됩니다 (앱을 백그라운드로 보내면 즉시 다시 잠김).

## 필요한 권한

| 권한 | 용도 |
|---|---|
| **사용 정보 접근** (`PACKAGE_USAGE_STATS`) | 어떤 앱이 포그라운드에 있는지 감지 |
| **다른 앱 위에 표시** (`SYSTEM_ALERT_WINDOW`) | 잠금 화면을 다른 앱 위에 띄우기 |
| `QUERY_ALL_PACKAGES` | 설치된 앱 목록 표시 |
| `FOREGROUND_SERVICE_SPECIAL_USE` | 백그라운드 감시 |

권한은 메인 화면 상단의 배너를 통해 시스템 설정으로 이동해서 허용하면 됩니다.

## 빌드

Android Studio에서 열고 실행하거나, CLI에서:

```bash
./gradlew assembleDebug
```

빌드된 APK는 `app/build/outputs/apk/debug/` 에 생성됩니다.

## 배포

### 랜딩 페이지 (GitHub Pages)

`docs/` 폴더에 정적 사이트가 들어있고, `main` 브랜치에 푸시되면 `.github/workflows/pages.yml`이 자동 배포합니다.

**최초 1회 설정**: 저장소 Settings → Pages → Build and deployment → **Source: GitHub Actions** 로 변경.

배포 후 URL: `https://sjidok750-creator.github.io/applock/`

### APK 자동 빌드 & 릴리스

`v` 로 시작하는 태그를 푸시하면 `.github/workflows/release-apk.yml`이 APK를 빌드하고 GitHub Releases에 첨부합니다.

```bash
git tag v0.1.0
git push origin v0.1.0
```

릴리스 후 다운로드 URL: `https://github.com/sjidok750-creator/applock/releases/latest/download/applock.apk`

랜딩 페이지의 "APK 다운로드" 버튼이 이 URL을 가리킵니다.

## 제한사항 (MVP)

- PIN 분실 복구 기능 없음 (앱 데이터 삭제 후 재설정)
- 지문/얼굴 인증 미지원
- 알림 권한이 거부되면 Android 13+ 에서 포그라운드 알림이 보이지 않음 (서비스는 동작)
- 디바이스 제조사별 절전 정책에 의해 서비스가 종료될 수 있음 (배터리 최적화 예외 처리 권장)

## 구조

```
app/src/main/kotlin/com/example/applock/
├── MainActivity.kt            # 라우팅: 설정 / 잠금해제 / 메인
├── data/
│   ├── PinStore.kt            # PIN SHA-256 해시 + EncryptedSharedPreferences
│   ├── LockedAppsStore.kt     # 잠근 앱 목록
│   └── AppRepository.kt       # 설치 앱 목록 로딩
├── service/
│   ├── AppLockService.kt      # 포그라운드 앱 폴링
│   ├── LockScreenActivity.kt  # PIN 입력 오버레이
│   ├── UnlockTracker.kt       # 5분 grace 관리
│   └── BootReceiver.kt        # 부팅 시 자동 시작
└── ui/
    ├── PinSetupScreen.kt
    ├── PinEntryScreen.kt
    ├── AppListScreen.kt
    └── theme/Theme.kt
```
