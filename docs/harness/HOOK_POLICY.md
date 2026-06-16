# Hook Policy

자동 트리거 hook을 만들 때 무한 루프와 토큰 누수를 막기 위한 종료 조건이다.

## 기본 원칙

- hook은 기본 비활성이다. 필요할 때만 만든다.
- hook은 `.codex/hooks`에 두되 빈 폴더만 만들지 않는다.
- hook은 프로젝트 전용 작업만 수행한다.
- hook은 네트워크, DB, git push 같은 외부 상태 변경을 자동 실행하지 않는다.

## Stop 조건

모든 hook은 아래 중 최소 2개 이상의 종료 조건을 가진다.

- 최대 실행 횟수: 예 `MAX_RUNS=1`
- 최대 실행 시간: 예 `MAX_SECONDS=30`
- 변경 파일 제한: 예 `docs/**` 또는 `app/src/**`
- sentinel 파일: 예 `.codex/hooks/.last-run`
- 동일 diff 재실행 방지: 이전 hash와 같으면 종료
- 실패 횟수 제한: 예 1회 실패 후 종료

## 금지 패턴

- hook이 자기 자신의 파일을 계속 수정하는 구조
- formatter hook이 저장 이벤트마다 무조건 전체 repository를 수정하는 구조
- test hook이 실패 후 자동으로 코드를 반복 수정하는 구조
- hook 안에서 다른 hook을 호출하는 구조
- 종료 조건 없는 watch loop

## 권장 템플릿

```text
start
  load changed files
  if no relevant files: stop
  if run count exceeded: stop
  if elapsed time exceeded: stop
  run one deterministic action
  write sentinel
stop
```

