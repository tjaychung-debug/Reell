# Reel Shuffle (Galaxy Android prototype)

인스타그램 릴스에서 `다음` 방향을 릴스별로 독립적으로 무작위로 정하는 실험용 앱입니다.

## 동작

- 접근성 서비스를 활성화하면 인스타그램 위에 작은 `↕` 버튼이 표시됩니다.
- 릴스 화면에서 `↕` 버튼을 누르면 투명한 터치 레이어가 활성화되고 버튼이 `?`가 됩니다.
- 현재 릴스의 정답 방향이 위라면 위로, 아래라면 아래로 밀어야 다음 릴스로 넘어갑니다.
- 정답이 아닌 방향으로 밀면 이전 릴스로 이동하도록 정상 하향 스와이프를 전달합니다.
- 다음 릴스로 이동할 때마다 새로운 50:50 방향을 생성합니다. 이전 릴스로 되돌아갈 때는 해당 릴스의 방향을 복원하려고 합니다.
- 설정에서 정답 방향 표시를 켜면 버튼에 `↑`/`↓`가 뜹니다. 기본값은 숨김입니다.
- 릴스가 아닌 화면에서는 `?` 버튼을 눌러 해제하세요. 인스타그램을 나가면 자동으로 해제합니다.

## 설치 방법

이 압축파일에는 **안드로이드 스튜디오 프로젝트 소스**가 들어 있습니다. 미리 컴파일된 APK가 아닙니다.

1. PC에 **Android Studio**를 설치합니다. 가능하면 기본 제공 JDK 17을 사용하세요.
2. 압축을 풀고 `ReelShuffle` 프로젝트를 열어 Gradle 동기화를 수행하세요.
3. 빌드 환경에서 Gradle Wrapper JAR이 없다는 오류가 나면 **프로젝트 설정에서 Gradle 8.9 로컬 설치를 선택**하거나 기존 Android Studio 프로젝트의 `gradle/wrapper/gradle-wrapper.jar`와 Gradle wrapper 실행 스크립트를 넣고 다시 동기화하세요. 이 ZIP에는 외부에서 받는 바이너리 파일이 포함되지 않았습니다.
4. Galaxy 폰에서 개발자 옵션 → USB 디버깅을 켜고 PC에 연결합니다.
5. Android Studio에서 Run 또는 Build > Build APK(s)를 수행합니다.
6. 폰에서 **설정 → 접근성 → 설치된 앱 → 릴스 방향 셔플**을 활성화합니다. 설치 방식과 One UI 버전에 따라 '제한된 설정 허용' 조작이 추가로 필요할 수 있습니다.
7. 인스타그램을 열고 릴스 화면에서 화면 우측에 표시되는 `↕` 버튼으로 켜고 끕니다.

## 동작 구조 및 한계

1. 인스타그램 전경 여부는 AccessibilityEvent로 감지하며 **릴스 화면인지 자동 판정하지 않습니다**.
2. Android `TYPE_ACCESSIBILITY_OVERLAY` 투명 화면이 단일 손가락 입력을 받고, `AccessibilityService.dispatchGesture()`가 인스타그램으로 대체 스와이프를 재전송합니다.
3. 대상 릴스가 실제로 넘어갔는지 확인하지는 않습니다. `dispatchGesture`의 onCompleted는 입력 전달 완료만 의미하므로 광고·네트워크 지연·인스타그램 UI 변경 등에 따라 내부 릴스 카운터와 실제 영상이 어긋날 수 있습니다.
4. 멀티터치, 복합 제스처, 연속 스크롤/빠른 재입력, 시스템 뒤로 가기 모서리 스와이프 및 댓글창 등의 사용 경험이 달라질 수 있습니다.
5. 삼성 One UI, 안드로이드/인스타그램 버전, 보안 설정에 따라 오버레이나 터치 주입이 정상 작동하지 않을 수 있습니다. 실기기 동작은 아직 확인하지 않았습니다.
6. **접근성 서비스를 사용해 입력을 대리 실행합니다**. 앱의 인터넷 권한은 없고, 텍스트/화면 콘텐츠는 읽지 않으며, 데이터를 수집·업로드하지 않습니다.

## 프로젝트 환경

- Java 17 / Android Gradle Plugin 8.7.3 / Gradle 8.9
- compileSdk / targetSdk 35, minSdk 26
- 외부 라이브러리 없음

### 순수 Java 로직 테스트

```bash
mkdir -p /tmp/reel-test
javac -d /tmp/reel-test app/src/main/java/com/example/reelshuffle/DirectionEngine.java app/src/test/java/com/example/reelshuffle/DirectionEngineSmokeTest.java
java -cp /tmp/reel-test com.example.reelshuffle.DirectionEngineSmokeTest
```

## PC에서 Android Studio 빌드가 어렵다면: GitHub Actions

프로젝트 폴더 내용 전체(숨김 `.github` 포함)를 GitHub 저장소에 올리고 **Actions → Build Galaxy APK → Run workflow**를 누르세요. 작업이 성공하면 `ReelShuffle-debug-apk` artifact에서 APK를 받을 수 있습니다. 해당 빌드 절차는 GitHub 서버에서 Gradle 8.9 및 Android SDK 35를 설치하며, ZIP에 Wrapper JAR이 없어도 실행되도록 설계되어 있습니다. 실제 CI 실행은 아직 확인하지 않았습니다.
