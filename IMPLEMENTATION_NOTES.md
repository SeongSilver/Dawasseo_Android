# IMPLEMENTATION_NOTES.md

Android Studio 초기 프로젝트 구축 전에 문서 간 충돌 또는 구현 시 확인이 필요한 내용을 기록한다.

## 우선순위 기준

문서 간 내용이 충돌하면 아래 우선순위를 따른다.

1. `AGENTS.md`
2. `README.md`
3. `SETUP.md`
4. `SCHEMA.md`
5. `API.md`
6. `DESIGN.md`

## 충돌 사항

### 알람 반경 최소값

- `AGENTS.md`: 반경 최솟값 100m / 최댓값 50km
- `SCHEMA.md`: `radius_km >= 0.1 and radius_km <= 50`
- `README.md`: 반경 설정 300m~50km

결정: 우선순위와 DB 제약 기준에 따라 초기 구현은 100m~50km로 맞춘다. UI 기본값은 문서 흐름상 0.5km 또는 500m를 사용할 수 있다.

## 애매하거나 확인 필요한 사항

### CBM.md 부재

`AGENTS.md`는 파일 직접 읽기 전에 `CBM.md`를 확인하라고 하지만, 현재 저장소에는 `CBM.md`가 없다. 초기 구축은 사용자가 지정한 6개 문서(`AGENTS.md`, `README.md`, `SETUP.md`, `SCHEMA.md`, `API.md`, `DESIGN.md`)를 기준으로 진행한다.

### 문서 위치

문서의 디렉토리 예시는 `docs/` 아래에 `README.md`, `SETUP.md`, `SCHEMA.md`, `API.md`, `DESIGN.md`가 있는 형태를 보여주지만, 현재 실제 파일은 루트에 존재한다. 초기 프로젝트 구축에서는 현재 루트 문서를 유지하고, Android 프로젝트 파일을 추가한다.

### Supabase Edge Functions 의존성

`API.md`는 `supabase.functions.invoke(...)` 사용 예시를 포함하지만, `SETUP.md`의 Supabase 의존성 예시에는 Functions 모듈이 명시되어 있지 않다. 초기 Gradle 구성 시 Functions 연동을 바로 구현한다면 Supabase Functions 관련 의존성 추가 여부를 확인해야 한다.

### Firebase 구성 파일

FCM을 사용하려면 `app/google-services.json`이 필요하지만, 현재 저장소에는 없다. 초기 프로젝트는 파일이 없어도 빌드 가능한 형태를 우선 고려하거나, google-services 플러그인 적용 시 해당 파일 배치가 필요하다는 안내를 남겨야 한다.

### Kakao 로그인 토큰

`API.md`는 Kakao SDK 로그인 후 `idToken`을 Supabase OIDC로 전달하는 흐름을 제시한다. 실제 Kakao OIDC 설정 및 idToken 발급 조건은 Kakao 개발자 콘솔과 Supabase Auth Provider 설정에 따라 달라질 수 있으므로, 초기 구현에서는 인터페이스와 설정 지점만 마련하고 실연동은 별도 검증이 필요하다.

### 대리 알람 커스텀 사운드 접근

`SCHEMA.md`는 친구가 대리로 설정한 알람의 사운드는 owner가 접근할 수 있도록 별도 Storage 정책이 필요할 수 있다고 한다. 초기 버전에서는 문서 제안대로 대리 알람은 기본 알람음만 사용하는 방향이 안전하다.
