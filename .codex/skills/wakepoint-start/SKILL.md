---
name: wakepoint-start
description: Start or resume work in the Wakepoint Android project with minimal context. Use when the user asks to continue development, summarize next work, inspect current status, choose what to do next, reduce token usage, or route a request to the right Wakepoint module skill.
---

# Wakepoint Start

Use this as the entry skill for Wakepoint Android work.

## Workflow

1. Read `../../../docs/harness/START_HERE.md`.
2. Read `../../../docs/harness/CURRENT_STATE.md`.
3. Read `../../../docs/harness/TASK_ROUTER.md`.
4. Pick exactly one module skill or harness for the user request.
5. Read the long work log only if the compressed state conflicts with the code.

## Module Commands

- `wakepoint-auth`: authentication, signup, login, session, logout.
- `wakepoint-maps-location`: Google Maps, foreground location, current position, Kakao Local search.
- `wakepoint-alarms`: alarm creation, Room cache, Supabase alarm rows, alarms list.
- `wakepoint-tracking-notifications`: foreground tracking service, distance trigger, notifications, permission UX.
- `wakepoint-supabase-db`: schema, RLS, DTO, repository, Supabase migrations.
- `wakepoint-design-system`: Compose theme, Pretendard, colors, components, strings.
- `wakepoint-verify`: choose verification commands and manual checks.

## Token Rules

- Prefer `../../../docs/harness/FILE_MAP.md` before repository-wide search.
- Prefer the selected module skill over loading multiple harness documents.
- Keep final summaries short and include only changed files and verification.
