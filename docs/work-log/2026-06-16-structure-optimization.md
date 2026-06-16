# 2026-06-16 Structure Optimization

## 목표

- 전역/프로젝트 폴더 역할 분리
- 파일/폴더명 영문화
- 메인 매뉴얼 200줄 이하 유지
- 메모리 분할 규칙 추가
- Skill 조건부 호출 정리
- Hook 무한 루프 방지 규칙 추가

## 진단

- `AGENTS.md`는 111줄로 200줄 제한을 만족한다.
- 전역 `C:\Users\yoose\.codex\skills`에 Wakepoint 전용 Skill 복사본이 있어 분리 원칙에 맞지 않았다.
- 프로젝트 `.codex/skills/*/references`는 빈 폴더였다.
- `docs/design/screens` 이미지 파일명이 한글이었다.
- 기존 work-log는 194줄로 한계에 가까워 새 상세 로그 파일로 분리했다.

## 조치

- 전역 `wakepoint-*` Skill 복사본 제거.
- 프로젝트 `.codex/skills/*/references` 빈 폴더 제거.
- `docs/design/screens/*.png` 파일명을 영문으로 변경.
- `docs/harness/STRUCTURE_POLICY.md` 추가.
- `docs/harness/MEMORY_POLICY.md` 추가.
- `docs/harness/HOOK_POLICY.md` 추가.
- `.codex/memory/INDEX.md` 추가.
- `AGENTS.md`, `START_HERE.md`, `FILE_MAP.md`, `CURRENT_STATE.md`에 새 정책 연결.

## 검증

- 한글 파일/폴더 경로: 0개
- `AGENTS.md`: 111줄
- 전역 `wakepoint-*` Skill: 0개
- 프로젝트 Skill의 빈 `references` 폴더: 0개

