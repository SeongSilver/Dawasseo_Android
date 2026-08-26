# 2026-08-26 — Phase 6: v1.5 정합/마감 + 문서 갱신 + 통합 회귀

> 브랜치: `feature/phase6-polish` → main 머지. v2 재구축 로드맵(Phase 1~6) **최종 phase**.

## 작업 로그

| 커밋 | 내용 |
|---|---|
| `(6-1)` | 백로그 소규모 정리 4건: 친구 검색 placeholder 문자열 리소스 추출(TASKS_NOW #2), 인증 화면 뒤로가기 contentDescription(#3), 미사용 signup 문자열 5개 제거(#4), NetworkRetry 지수 백오프 maxDelayMs(5s) 상한 캡 + 테스트(#7). |
| `(6-2)` | AlarmDto 로컬 전용 필드(soundEnabled/vibrate/isFavorite/repeating) 정책을 "로컬 전용 확정"으로 DTO 주석·SCHEMA.md 에 명시(#6). time_alarms/places 가 원격 스키마 없는 로컬 전용 테이블임도 기록. CHECKLISTS 빌드 명령 macOS/Windows 병기(#9). |
| `(6-3)` | TASKS_NOW.md 를 2026-08-26 기준으로 전면 갱신(완료 항목 정리 + 신규 즉시/Blocked 백로그). 재구축 계획서(04)에 Phase 1~6 완료 표기 + 각 phase 로그 링크. |
| `(6-4)` | 통합 회귀 + 이 로그. |

## 통합 회귀 결과

| 검증 | 결과 |
|---|---|
| `assembleDebug` (APK 패키징) | ✅ BUILD SUCCESSFUL |
| 유닛테스트 전체 | ✅ **129개 그린** (사이클 시작 시점 90 → +39) |
| 계측 E2E (`e2e` 패키지, 에뮬레이터 API 36) | ✅ **24/24 통과** — 기존 6 + 시간 알람 5 + 내 장소 3 + 친구 알람 4 + 막차 6 |
| 알려진 실패(제외) | `ScreenshotTourTest.tour2`(캡처 도구) — 변경 전(main)에서도 재현되는 기존 환경 실패, TASKS_NOW #3 에 수리 항목 등재 |

## 에러 로그
- 이번 phase 신규 오류 없음.

## 사이클 요약 (Phase 2~6, 브랜치→main 머지 이력)
1. `feature/phase2-time-alarm-ui` → `8d97670` — 시간 알람 UI(1-1~1-6)+알람 탭 통합
2. `feature/phase3-my-places` → `0296d00` — 내 장소(도메인+Room v10+2-1~2-4+마이 진입점)
3. `feature/phase4-friend-alarm` → `526c513` — 친구 알람(3-6·3-7 실기능, 3탭 스펙 UI)
4. `feature/phase5-last-train` → `711701c` — 막차(UI 4-1~4-6+모의 계산+시간 알람 실연동)
5. `feature/phase6-polish` → (이번 머지) — 정합/문서/회귀

남은 백로그와 선행조건은 `TASKS_NOW.md`(2026-08-26) 참조.
