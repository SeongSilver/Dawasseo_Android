# Memory Policy

토큰 낭비를 막기 위한 프로젝트 메모리 관리 규칙이다.

## 파일 역할

- `.codex/memory/INDEX.md`: 현재 프로젝트 메모리 인덱스. 100줄 이하 유지.
- `docs/harness/CURRENT_STATE.md`: 최신 압축 상태. 새 세션에서 우선 읽는다.
- `docs/work-log/*.md`: 날짜별 상세 작업 로그. 필요할 때만 읽는다.

## 줄 수 제한

- `AGENTS.md`: 200줄 이하.
- `.codex/memory/INDEX.md`: 100줄 이하.
- `docs/harness/CURRENT_STATE.md`: 120줄 이하.
- 단일 work-log 파일: 200줄 이하.

## 분할 규칙

파일이 제한을 넘기기 전에 아래 순서로 정리한다.

1. 핵심 상태만 `CURRENT_STATE.md`에 10줄 이내로 반영한다.
2. 상세 내용은 `docs/work-log/YYYY-MM-DD-topic.md`로 분리한다.
3. 오래된 상세는 `docs/work-log/archive/`로 이동한다.
4. 메인 파일에는 링크와 한 줄 요약만 남긴다.

## 기록 규칙

- "무엇을 바꿨는지", "검증은 무엇을 했는지", "다음 작업은 무엇인지"만 남긴다.
- 코드 전체, 긴 로그, 빌드 출력 전문은 저장하지 않는다.
- 민감 키와 토큰은 기록하지 않는다.
- 중복된 성공 이력은 최신 요약 하나로 합친다.

