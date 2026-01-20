# ai-chat

Kotlin + Spring Boot 기반의 AI 챗봇 서비스 데모입니다.

## Requirements

- JDK 17
- Docker (for local Postgres)
- OpenAI API Key (유료 API 플랜 필요)

## Quick Start

```bash
docker compose up -d
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

## SSE Streaming

SSE 응답은 이벤트 타입으로 구분됩니다.

- `delta`: 토큰 조각 (`ApiResponse.ok("...")`)
- `complete`: 최종 응답 (`ApiResponse.ok(ChatResponse)`)
- `error`: 오류 (`ApiResponse.error(...)`)

연결이 중간에 끊기면 스트림은 취소되고, 부분 응답은 `(incomplete)` 표시로 저장됩니다.

## 과제 분석

- 요구사항의 핵심은 인증 기반의 멀티 테넌트 대화 관리와, 관리자를 위한 관측/보고 기능입니다.
- 스레드 기준의 대화 그룹화와 30분 기준 스레드 유지 규칙을 우선 구현 대상으로 봤습니다.
- 데이터 조회 권한(회원/관리자)과 페이지네이션/정렬 필터가 요구사항의 주요 평가 요소로 판단했습니다.
- 향후 확장성을 위해 OpenAI 호출부를 별도 클라이언트로 분리하고 WebClient 기반으로 구성했습니다.

## AI 활용 및 어려움

- 설계 초기에는 모델링과 요구사항 해석(스레드 생성 규칙, 권한 범위)에서 AI를 사용했습니다.
- Kotlin + Spring Boot 3에서 JWT 서명/검증과 WebClient 타임아웃 설정의 세부 옵션 확인에 활용했습니다.
- 어려움은 요구사항을 과도하게 해석하지 않으면서도 확장 가능하도록 계층을 분리하는 균형이었습니다.

## 가장 어려웠던 기능: SSE 스트리밍

### 스트리밍 구현 방식 후보

- Long Polling: 주기적으로 요청/응답을 반복해 유사 실시간 구현
- WebSocket: 양방향 실시간 통신, 세션 관리 필요
- SSE: 서버 -> 클라이언트 단방향 스트리밍, 구현이 단순

### SSE를 선택한 이유

- 단방향 스트리밍이면 충분하고, 클라이언트 구현이 단순함
- HTTP 기반이라 기존 인프라/보안 정책과 충돌이 적음
- 토큰 단위로 점진적 응답을 전달하기에 적합함

### SSE 구현 중 어려웠던 점과 극복 방법

- 연결 종료 시 스트림 취소 처리: `SseEmitter` 종료 이벤트를 받아 OpenAI 스트림 구독을 명시적으로 해제
  - 이유: 클라이언트 연결이 끊긴 뒤에도 스트림을 소비하면 비용과 리소스가 낭비되기 때문
- 이벤트 스키마 정의와 예외 통합: `delta`, `complete`, `error` 이벤트로 고정하고 `ApiResponse` 포맷으로 통일
  - 이유: 클라이언트가 이벤트별 처리를 단순하게 구현할 수 있고, 실패/성공 응답의 일관성이 보장됨
- 스트림 중단 시 부분 응답 저장: 중단 시 `(incomplete)` 꼬리표로 저장해 데이터 일관성을 유지
  - 이유: 사용자 요청에 대해 기록이 남지 않으면 추적/분석이 어려워지고, 재시도 기준이 불명확해짐

## 대화 요약 및 메모리 전략: 하이브리드

### 후보 전략

- 전체 히스토리 유지: 문맥 손실이 적지만 비용과 토큰 한계에 취약
- 최근 N개만 유지: 구현이 단순하지만 오래된 핵심 맥락 손실
- 요약 기반: 비용 절감에 효과적이나 요약 품질이 중요
- 하이브리드(요약 + 최근 N개): 장기/단기 기억을 균형 있게 유지

### 하이브리드를 선택한 이유

- 토큰 비용과 문맥 유지의 균형이 가장 좋음
- 요약은 장기 기억, 최근 N개는 단기 맥락을 보존 가능
- 구현 복잡도 대비 효과가 크고 운영 중 조정이 쉬움

### 어려웠던 점과 극복 방법

- 요약 시점 결정: 메시지 수/토큰 수 임계치를 두고 초과 시에만 요약 수행
  - 이유: 과도한 요약으로 정보 손실이 발생하는 것을 방지
- 요약 품질 편차: 요약 템플릿에 핵심 정보(결정/선호/제약)를 명시적으로 포함
  - 이유: 장기 기억의 신뢰도를 높이고 재질문 시 일관성을 유지
- 저장 구조: 스레드 단위로 `summary`를 별도 필드로 분리
  - 이유: 검색/재구성 비용을 줄이고, 컨텍스트 조립을 단순화
