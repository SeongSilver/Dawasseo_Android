# Structure Policy

전역/프로젝트 폴더 분리와 파일명 규칙을 정의한다.

## 진단 결과

- 프로젝트 전용 `wakepoint-*` Skill이 전역 `C:\Users\yoose\.codex\skills`에도 복사되어 있었다.
  - 조치: 전역 복사본 제거. 프로젝트 전용 Skill은 `.codex/skills`에만 둔다.
- `.codex/skills/*/references` 빈 폴더가 있었다.
  - 조치: 빈 `references` 폴더 삭제.
- `docs/design/screens` 아래 PNG 파일명이 한글이었다.
  - 조치: 쉬운 영문 파일명으로 변경.
- `AGENTS.md`는 108줄로 200줄 제한을 만족한다.

## 분리 원칙

전역 폴더:

- 위치: `C:\Users\yoose\.codex`
- 목적: 모든 프로젝트에서 재사용하는 일반 운영 설정, 범용 Skill, Codex 런타임 상태
- 금지: 특정 프로젝트 파일 경로, Wakepoint 전용 규칙, 앱 schema, 앱 디자인 규칙

프로젝트 폴더:

- 위치: `C:\workspace\Dawasseo_Android`
- 목적: Wakepoint 전용 하네스, Skill, 문서, 코드, 작업 로그
- 허용: `.codex/skills/wakepoint-*`, `docs/harness/*`, `AGENTS.md`

## 목표 트리

```text
C:\Users\yoose\.codex\
  skills/                 # 범용 Skill만
  config.toml             # 전역 Codex 설정
  memories_*.sqlite       # Codex 전역 상태

C:\workspace\Dawasseo_Android\
  AGENTS.md               # 최초 진입, 200줄 이하
  .codex/
    skills/
      wakepoint-start/
      wakepoint-auth/
      wakepoint-maps-location/
      wakepoint-alarms/
      wakepoint-tracking-notifications/
      wakepoint-supabase-db/
      wakepoint-design-system/
      wakepoint-verify/
    memory/
      INDEX.md            # 프로젝트 메모리 인덱스, 100줄 이하
  docs/
    harness/
      START_HERE.md
      CURRENT_STATE.md
      TASK_ROUTER.md
      SKILL_COMMANDS.md
      STRUCTURE_POLICY.md
      MEMORY_POLICY.md
      HOOK_POLICY.md
      VERIFICATION_MATRIX.md
    design/screens/       # 영문 파일명만
    work-log/
  app/
  supabase/
```

## 이름 규칙

- 폴더와 파일명은 lowercase kebab-case 또는 snake_case를 사용한다.
- Android resource 파일은 lowercase snake_case를 사용한다.
- 문서 파일은 `UPPER_SNAKE.md` 또는 lowercase kebab-case 중 기존 위치 규칙을 따른다.
- 한글은 본문과 앱 사용자 문자열에만 사용한다.
- 새 이미지 파일명은 `screen-purpose.png` 형식으로 작성한다.

