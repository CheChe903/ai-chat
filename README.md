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

## API Overview

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `POST /api/chats` (JSON)
- `POST /api/chats` (SSE, `Accept: text/event-stream` + `isStreaming=true`)
- `GET /api/chats`
- `DELETE /api/threads/{threadId}`
- `POST /api/feedbacks`
- `GET /api/feedbacks`
- `PATCH /api/feedbacks/{feedbackId}/status` (admin)
- `GET /api/admin/stats` (admin)
- `GET /api/admin/reports/chats` (admin, CSV)

## 과제 분석

- 요구사항의 핵심은 인증 기반의 멀티 테넌트 대화 관리와, 관리자를 위한 관측/보고 기능입니다.
- 스레드 기준의 대화 그룹화와 30분 기준 스레드 유지 규칙을 우선 구현 대상으로 봤습니다.
- 데이터 조회 권한(회원/관리자)과 페이지네이션/정렬 필터가 요구사항의 주요 평가 요소로 판단했습니다.
- 향후 확장성을 위해 OpenAI 호출부를 별도 클라이언트로 분리하고 WebClient 기반으로 구성했습니다.

## AI 활용 및 어려움

- 설계 초기에는 모델링과 요구사항 해석(스레드 생성 규칙, 권한 범위)에서 AI를 사용했습니다.
- Kotlin + Spring Boot 3에서 JWT 서명/검증과 WebClient 타임아웃 설정의 세부 옵션 확인에 활용했습니다.
- 어려움은 요구사항을 과도하게 해석하지 않으면서도 확장 가능하도록 계층을 분리하는 균형이었습니다.

## 가장 어려웠던 기능

- 스레드 유지 규칙(마지막 질문 30분 기준)과 스레드별 대화 조회를 결합하는 부분이 가장 까다로웠습니다.
- 마지막 질문 시각으로 스레드를 선택/신규 생성하고, 대화 목록을 스레드 단위로 정렬/페이지네이션하는 흐름을 설계해야 했습니다.

## Notes

- ChatGPT Plus 구독만으로는 API 호출 권한이 없습니다. OpenAI API 키와 결제 활성화가 필요합니다.
