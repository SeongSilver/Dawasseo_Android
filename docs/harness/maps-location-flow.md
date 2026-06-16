# Maps Location Flow Harness

홈 지도, foreground 현재 위치, 목적지 선택, Kakao Local 검색을 수정할 때 사용하는 하네스다.
사용자가 앱을 열자마자 가장 먼저 체감하는 화면이므로 blank map, 권한 루프, 선택 좌표 누락을 중점적으로 막는다.

## 먼저 읽을 파일

- `app/src/main/java/com/wakepoint/app/feature/home/HomeScreen.kt`
- `app/src/main/java/com/wakepoint/app/feature/home/HomeViewModel.kt`
- `app/src/main/java/com/wakepoint/app/data/location/KakaoLocalRepository.kt`
- `app/src/main/java/com/wakepoint/app/navigation/WakepointNavGraph.kt`
- `app/src/main/AndroidManifest.xml`
- `app/build.gradle.kts`
- `app/src/main/res/values/strings.xml`
- `docs/SETUP.md`
- `docs/CHECKLISTS.md`
- `docs/GOTCHAS.md`

## 기대 흐름

1. 홈 화면이 열리면 GoogleMap이 렌더링된다.
2. foreground 위치 권한 상태를 확인한다.
3. 권한이 없으면 사용자에게 foreground 위치 권한을 요청한다.
4. 권한이 있으면 FusedLocationProviderClient로 현재 위치를 가져온다.
5. 현재 위치를 가져오면 지도 카메라와 기본 위치를 내 위치로 맞춘다.
6. 현재 위치를 못 가져오면 fallback 좌표로 지도와 state를 초기화한다.
7. 지도 탭으로 target lat/lng를 갱신하고 선택 마커를 표시한다.
8. 검색창 클릭 시 장소 검색 UI를 연다.
9. Kakao Local 검색 결과를 선택하면 지도 카메라와 target lat/lng/address를 갱신한다.
10. 알람 설정 바텀시트가 선택 좌표, 반경, 별칭을 받는다.

## 변경 규칙

- 홈 화면에서는 foreground 위치 권한만 요청한다.
- background location 권한 요청은 홈 지도 UX에 섞지 않는다.
- 지도 API key와 Kakao REST key는 BuildConfig를 통해 주입한다.
- key 값을 코드나 로그에 출력하지 않는다.
- 현재 위치를 가져오지 못해도 화면은 fallback 좌표로 정상 동작해야 한다.
- 지도 탭 선택과 검색 결과 선택은 같은 target state로 수렴해야 한다.
- 사용자에게 보이는 새 문자열은 `strings.xml`로 분리한다.
- 지도 로딩 실패를 full-screen crash로 만들지 않는다.

## 자동 검증

```powershell
.\gradlew assembleDebug
.\gradlew test
```

Gradle dependency 다운로드나 wrapper 실행이 sandbox 네트워크 제한으로 실패하면 같은 명령을 escalated 권한으로 다시 실행한다.

## 수동 검증

1. `local.properties`에 `GOOGLE_MAPS_API_KEY`와 `KAKAO_REST_API_KEY`가 있는지 확인한다.
2. 앱을 새로 설치하거나 권한을 초기화한 상태로 홈에 진입한다.
3. 위치 권한 요청이 표시되는지 확인한다.
4. 권한 허용 후 지도 카메라가 내 위치로 이동하는지 확인한다.
5. 지도 위 임의 좌표를 탭했을 때 선택 마커와 바텀시트 좌표가 갱신되는지 확인한다.
6. 검색창을 클릭해 장소 검색 UI가 열리는지 확인한다.
7. 키워드 검색 후 결과를 선택한다.
8. 지도 카메라, 선택 마커, 알람 생성 좌표/주소가 검색 결과로 바뀌는지 확인한다.
9. 네트워크를 끄거나 잘못된 키로 검색 실패 상태가 깨지지 않는지 확인한다.

## Blank Map 문제 해결

- Google Maps SDK for Android가 Google Cloud Console에서 enabled인지 확인한다.
- API key restriction에 package name과 SHA-1이 맞는지 확인한다.
- `GOOGLE_MAPS_API_KEY`가 `local.properties`에 있고 rebuild가 되었는지 확인한다.
- emulator image가 Google Play services를 포함하는지 확인한다.
- emulator 또는 기기가 네트워크에 연결되어 있는지 확인한다.
- Logcat에서 `Google Maps Android API` 관련 인증 오류를 확인한다.

## Kakao 검색 문제 해결

- `KAKAO_REST_API_KEY`가 비어 있지 않은지 확인한다.
- Kakao Developers 앱 설정과 REST API key가 현재 프로젝트용인지 확인한다.
- 네트워크 제한이나 quota 초과 여부를 확인한다.
- 응답 lat/lng 파싱에서 x/y 순서를 확인한다.
  - Kakao Local `x`는 longitude, `y`는 latitude다.

## 다음 확장 지점

- 장소 검색 debounce
- 검색 결과 empty/error state 다듬기
- 최근 검색어 또는 최근 목적지 저장
- 지도 marker clustering 또는 custom marker
- 사용자가 권한을 거부했을 때 설정 이동 UX

