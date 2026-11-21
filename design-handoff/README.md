# 다왔어 디자인 핸드오프

Montage 시멘틱 컬러를 입힌 라이트 · 다크 디자인 44개 화면과, Claude Code 에 줄 지시문이다.

| 경로 | 내용 |
|---|---|
| `PROMPT.md` | Claude Code 에 붙여 넣을 프롬프트와 `/goal` 문구 |
| `DESIGN.md` | 색 역할, 타입, 모양, 컴포넌트 수치, 화면 목록, 제약 |
| `tokens/colors.json` | 색 역할 32개 × 라이트 · 다크 |
| `tokens/Theme.reference.kt` | Compose 테마 초안 (컴파일 확인 안 함) |
| `screens/light`, `screens/dark` | 화면별 HTML. 1px = 1dp, `data-layer` 에 컴포넌트 이름 |
| `png/light`, `png/dark` | 화면별 렌더 이미지 (2배율, 824×1830) |
| `assets/ic_brand_mark.xml` | 브랜드 심볼 벡터 드로어블 초안 |

사용법: 이 폴더를 저장소 루트에 `design-handoff/` 로 복사하고, `PROMPT.md` 의 본문을 Claude Code 에 붙여 넣는다.
