# ai-chat

Kotlin + Spring Boot 기반의 AI 챗봇 서비스 데모입니다.

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

- 가장 중요한 기능은 스레드 단위로 대화를 관리하고, 과거 대화 내용을 지속적으로 포함시키는 것입니다. 이때, 과거 대화 내용 관리를 어떻게 관리할 지가 중요 포인트라 생각했습니다.
- 스트리밍은 전제로 하고, 핵심 포인트는 스트리밍 응답을 어떻게 안정적으로 전달할지였습니다.

## AI 활용 및 어려움

- 프롬프트를 제대로 작성하지 않으면 AI와 나의 컨텍스트가 맞지 않았습니다.
- 반복되는 설명을 줄이기 위해 템플릿 기반 프롬프트가 필요하다는 것을 느꼈습니다.

## 가장 어려웠던 기능: SSE 스트리밍

### SSE 스트리밍

- 서버가 응답을 한 번에 주지 않고, 생성되는 대로 끊어서 보내는 방식입니다.
- 사용자는 답변이 실시간으로 생성되는 것처럼 보게 됩니다.

### 구현 방식 후보

- Long Polling: 주기적으로 요청/응답을 반복해 유사 실시간 구현
- WebSocket: 양방향 실시간 통신, 세션 관리 필요
- SSE: 서버 -> 클라이언트 단방향 스트리밍, 구현이 단순

### SSE를 선택한 이유

- 단방향 스트리밍이면 충분하고, 클라이언트 구현이 단순함
- HTTP 기반이라 기존 인프라/보안 정책과 충돌이 적음
- 토큰 단위로 점진적 응답을 전달하기에 적합함

### 어려웠던 점과 극복 방법

- 연결 종료 시 스트림 취소 처리
  - 문제: 클라이언트가 연결을 끊어도 서버는 OpenAI 스트림을 계속 소비할 수 있음
  - 극복: `SseEmitter`의 `onCompletion/onTimeout/onError`에서 스트림 구독을 해제하고, 서버 측 버퍼 수집을 중단
  - 이유: 토큰 소비 비용과 불필요한 리소스 점유를 줄이기 위함

- 이벤트 스키마 정의와 예외 통합
  - 문제: 스트리밍 중 오류가 발생하면 클라이언트가 어디까지 수신했는지 구분하기 어려움
  - 극복: `delta/complete/error` 이벤트로 타입을 고정하고, 페이로드를 `ApiResponse`로 통일
  - 이유: 클라이언트가 이벤트 타입만으로 UI 상태를 안정적으로 관리할 수 있음

- 부분 응답 저장 정책
  - 문제: 스트림이 중간에 끊기면 대화가 DB에 저장되지 않아 추적/분석이 어려움
  - 극복: 중단 시 `(incomplete)` 꼬리표를 붙여 저장해 이력은 남기되 품질 상태를 구분
  - 이유: 운영 중 장애 분석과 재시도 기준을 명확히 하기 위함

- 장시간 응답에서 타임아웃 관리
  - 문제: 긴 응답에서 서버/클라이언트 타임아웃이 먼저 발생할 수 있음
  - 극복: SSE 타임아웃을 충분히 길게 두고, 완료/에러 이벤트를 명시적으로 전송
  - 이유: 중간 끊김 없이 안정적으로 스트리밍을 유지하기 위함

## 대화 요약 및 메모리 전략: 하이브리드

### 하이브리드 대화 요약

- 오래된 대화는 요약으로 압축해 보관하고, 최근 대화는 원문 그대로 유지합니다.
- 다음 질문 시 요약 + 최근 대화를 같이 보내 문맥을 유지합니다.

### 구현 방식 후보

- 전체 히스토리 유지: 문맥 손실이 적지만 비용과 토큰 한계에 취약
- 최근 N개만 유지: 구현이 단순하지만 오래된 핵심 맥락 손실
- 요약 기반: 비용 절감에 효과적이나 요약 품질이 중요
- 하이브리드(요약 + 최근 N개): 장기/단기 기억을 균형 있게 유지

### 하이브리드를 선택한 이유

- 토큰 비용과 문맥 유지의 균형이 가장 좋음
- 요약은 장기 기억, 최근 N개는 단기 맥락을 보존 가능
- 구현 복잡도 대비 효과가 크고 운영 중 조정이 쉬움

### 어려웠던 점과 극복 방법

- 요약 시점 결정
  - 문제: 잦은 요약은 정보 손실을 키우고 비용이 증가
  - 극복: 메시지 수 기반 임계치(예: 12개 이상)에서만 요약 수행
  - 이유: 요약 빈도와 정보 유지의 균형을 맞추기 위함

- 요약 품질 편차
  - 문제: 요약이 중요한 결정/제약을 누락하면 이후 응답 일관성이 깨짐
  - 극복: 요약 프롬프트에 결정/선호/제약/미결 항목을 명시적으로 포함
  - 이유: 장기 기억의 신뢰도를 높이기 위함

- 컨텍스트 조립 비용
  - 문제: 요약과 최근 대화를 합칠 때 매 요청마다 계산 비용이 발생
  - 극복: `summary`를 스레드에 저장하고, 최신 요약 시점 이후 대화만 추가 요약
  - 이유: 불필요한 재요약을 줄이고 응답 지연을 최소화

## 점진적 개발 로드맵

1) 안정화 단계
- 인증/권한, 스레드 관리, SSE 스트리밍의 안정성 확보
- 오류 응답 포맷 표준화 및 운영 로그 정비

2) 품질 개선 단계
- 대화 요약 품질 개선(템플릿 고도화, 자동 재요약)
- 응답 품질 평가(피드백 기반 개선 루프)

3) 확장 단계
- 다중 LLM Provider 지원
- RAG(파일 업로드/임베딩/벡터DB) 연동
- 조직/팀 단위 멀티테넌시 확장

4) 운영 고도화 단계
- 관리자 대시보드 및 메트릭 시각화
- 배치 리포트/알림 자동화
