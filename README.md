# ai-chat

Kotlin + Spring Boot 기반의 AI 챗봇 서비스 데모입니다.

## Requirements

- JDK 17
- PostgreSQL 15.8+
- OpenAI API Key (유료 API 플랜 필요)

## Quick Start

```bash
./gradlew bootRun
```

## Configuration

환경 변수로 설정합니다.

- `DB_URL` (default: `jdbc:postgresql://localhost:5432/ai_chat`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: `postgres`)
- `JWT_SECRET` (default: `change-me-in-prod`)
- `OPENAI_API_KEY` (required for real responses)
- `OPENAI_MODEL` (default: `gpt-4o-mini`)
- `OPENAI_BASE_URL` (default: `https://api.openai.com`)

## Notes

- ChatGPT Plus 구독만으로는 API 호출 권한이 없습니다. OpenAI API 키와 결제 활성화가 필요합니다.
- 문서 섹션(과제 분석/AI 활용/어려운 기능 설명)은 추후 작성됩니다.
