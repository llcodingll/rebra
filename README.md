## 👋 프로젝트 소개

### 📊 *"리밸런싱 기반 포트폴리오 자동관리 플랫폼"*

<img width="868" height="310" alt="image" src="https://github.com/user-attachments/assets/3f2499fb-7bab-47db-ba8e-32f30a85fd5d" />


**Rebra**는 사용자가 **목표 비중을 설정하면 실시간 시세를 기반으로 매수/매도 주문을 자동 생성**하는
**주식 포트폴리오 리밸런싱 서비스**입니다.

- **자동 리밸런싱**: 목표 비중 대비 현재 비중을 분석해 최소 거래로 포트폴리오를 최적화해요.
- **백테스트**: 과거 데이터 기반으로 리밸런싱 전략의 성과를 시뮬레이션할 수 있어요.
- **실시간 시세 & 거래**: 한국투자증권 WebSocket으로 실시간 시세를 받아 매수/매도 주문을 즉시 실행합니다.

<br/>
<br/>

## 🧩 **고민과 해결 방안**

### 백엔드

### 🔀 **백테스트 서버 분리 — Kafka 기반 비동기 파이프라인**

백테스트는 수십 년치 일별 데이터를 기반으로 수백~수천 번의 리밸런싱을 시뮬레이션하는 CPU 집약적 작업이었습니다. 이 연산을 메인 서버에서 처리하면 실시간 시세 조회, 주문 실행 같은 지연에 민감한 요청들이 블로킹되는 문제가 있었습니다.

백테스트를 동기 HTTP로 분리하면 타임아웃 위험이 남아있고, 메인 서버가 응답을 기다리는 동안 스레드가 낭비되는 문제도 해결되지 않았습니다.

**Kafka 기반 비동기 파이프라인**으로 해결했습니다. 메인 서버는 요청을 Kafka 토픽에 발행하고 즉시 응답을 반환합니다. 별도 백테스트 계산 서버가 메시지를 소비해 연산을 수행하고, 결과를 다시 Kafka로 발행하면 메인 서버의 `@KafkaListener`가 수신해 WebSocket으로 사용자에게 전달합니다.

[Wiki로 자세히 보기](https://velog.io/@swnbae/MSA)

<br/>

### 🔥 **공공데이터 포털 화재 장애 대응 — 외부 API 의존 구조 개선**

주식 검색, 백테스트 등 핵심 기능 대부분이 공공데이터포털 API에 의존하고 있었습니다. 발표 이틀 전, 국가정보자원관리원 화재로 API가 전면 중단되면서 서비스 핵심 기능이 전부 마비됐습니다.

KRX CSV로 전 종목 정보를, 미리 받아둔 백업 CSV로 5년치 과거 주가 데이터를 DB에 긴급 적재해 이틀 안에 기능을 복구했습니다.

같은 상황이 재발해도 버틸 수 있도록 구조를 개선했습니다. 한 번 조회한 주가 데이터는 DB에 저장해두고, 이후 요청은 부족한 구간만 API를 호출하는 **Look-Aside 패턴**을 도입했습니다. 발표 당일 실측 기준으로 API 호출이 약 70% 감소했습니다.

[Wiki로 자세히 보기](https://velog.io/@swnbae/%EC%A0%80%EC%AA%BD-%EC%A7%91%EC%9D%B4-%EB%AC%B4%EB%84%88%EC%A1%8C%EB%8B%A4%EA%B3%A0-%ED%95%B4%EC%84%9C-%EA%B5%AC%EA%B2%BD%ED%95%98%EB%9F%AC-%EA%B0%94%EC%A3%A0.-%EA%B7%B8%EB%9F%B0%EB%8D%B0-%EB%B3%B4%EA%B3%A0-%EC%98%A4%EB%8B%88)

<br/>

### 🔐 **사용자 민감 정보 암호화 — KDF + IV + Pepper**

한국투자증권 API 연동을 위해 계좌번호, 앱키, 앱시크릿을 DB에 저장해야 했습니다. 서버 고정 키 하나로 모두 암호화하면 키가 유출되는 순간 전체 사용자 데이터가 노출되고, 같은 값은 항상 같은 암호문이 만들어져 패턴 분석에도 취약했습니다.

사용자별 랜덤 SALT와 마스터 키를 조합해 필드별로 독립된 AES 키를 유도하고, 매 암호화마다 랜덤 IV를 생성해 같은 값이라도 암호문이 매번 달라지도록 했습니다. 한 사용자의 키가 유출되어도 다른 사용자는 영향받지 않고, 재암호화 시에도 암호문이 갱신됩니다.

사용자마다 키가 달라 중복 계좌 확인을 위해 전체를 복호화하면 O(n)이 됩니다. 계좌번호와 전역 비밀값(Pepper)을 조합한 해시를 별도 컬럼에 저장해, 인덱스로 O(1) 중복 확인이 가능하도록 했습니다.

[Wiki로 자세히 보기](https://velog.io/@swnbae/%ED%95%98%EB%93%9C%EC%BD%94%EB%94%A9%EB%90%9C-%ED%82%A4%EB%A1%9C-%EC%95%94%ED%98%B8%ED%99%94%ED%95%98%EB%A9%B4-%EC%99%9C-%EC%9C%84%ED%97%98%ED%95%A0%EA%B9%8C)

## 🎯 주요 기능 소개

### ⚖️ 자동 리밸런싱
> **"목표 비중만 설정하면, 나머지는 Rebra가 알아서!"**

<img width="2349" height="1714" alt="image" src="https://github.com/user-attachments/assets/b54fcb7c-0511-4c4c-b146-9e867eba7d48" />
<img width="2385" height="1725" alt="image" src="https://github.com/user-attachments/assets/2aa20d8a-3407-47c9-bede-516d31f0685d" />


**목표 비중 대비 현재 비중을 실시간으로 분석**해 최소 거래 횟수로 포트폴리오를 목표에 맞춥니다.
**매수/매도 주문을 자동으로 생성**하고, 한국투자증권 API를 통해 실제 주문을 실행합니다.

<br/>
<br/>

### 📈 백테스트
> **"과거로 돌아가 전략을 검증해보세요!"**

<img width="2200" height="1797" alt="image" src="https://github.com/user-attachments/assets/61e0fc24-03ff-4340-abdd-96f34f925733" />


**과거 일별 시세 데이터를 기반으로 리밸런싱 전략을 시뮬레이션**합니다.
Kafka 비동기 파이프라인으로 계산 서버에서 처리하고, 결과를 **WebSocket으로 실시간 전달**해요.
리밸런싱 주기, 목표 비중 등 전략 파라미터를 자유롭게 설정할 수 있습니다.

<br/>
<br/>

### 📡 실시간 시세 조회
> **"한국투자증권 WebSocket으로 실시간 시세를!"**

<img width="2312" height="880" alt="image" src="https://github.com/user-attachments/assets/261261da-aead-4320-b50b-8557c12b2deb" />

**한국투자증권 실시간 WebSocket**에 연결해 구독 중인 종목의 시세를
**STOMP 기반 WebSocket**으로 프론트엔드에 즉시 전달합니다.
연결 끊김 시 자동 재연결 및 구독 복구 로직이 동작합니다.

<br/>
<br/>

### 🔐 카카오 소셜 로그인
> **"간편하고 안전한 로그인!"**

<img width="1257" height="374" alt="image" src="https://github.com/user-attachments/assets/fe632ada-0153-425d-8319-a3762c843318" />


**카카오 OAuth2 연동**으로 원클릭 소셜 로그인을 지원합니다.
JWT 액세스/리프레시 토큰 기반 인증으로 보안성을 보장하며,
신규 가입 시 임시 토큰(30분)을 발급해 전화번호 인증 완료 후 정식 계정으로 전환됩니다.

<br/>
<br/>

## ✅ 서비스 구조도

<img width="883" height="463" alt="image" src="https://github.com/user-attachments/assets/63380e2d-0f3a-453f-b542-ca10eb530dc3" />

<br/>
<br/>

## ⚒️ Tech Stacks

| 분류 | 기술 스택 |
|------|-----------|
| **Backend** | [![My Skills](https://skillicons.dev/icons?i=java,spring,hibernate)](https://skillicons.dev) |
| **Database / Infra** | [![My Skills](https://skillicons.dev/icons?i=postgres,redis,kafka)](https://skillicons.dev) |
| **배포** | [![My Skills](https://skillicons.dev/icons?i=docker,jenkins,nginx)](https://skillicons.dev) |
| **협업 / 개발도구** | [![My Skills](https://skillicons.dev/icons?i=git,gitlab,notion,jira)](https://skillicons.dev) |

<br/>
<br/>

## 🤼 팀원 소개

| 배수한 | 유윤지 |
|:---:|:---:|
| <img src="https://avatars.githubusercontent.com/u/128581113?v=4" width="120"> | <img src="https://avatars.githubusercontent.com/u/105447233?v=4" width="120"> |
| **Backend** | **Backend** |
| [@SwnBae](https://github.com/SwnBae) | [@llcodingll](https://github.com/llcodingll) |

<br/>
<br/>

## 🧑‍💻 역할 및 기여

| 담당자 | 주요 작업 |
|--------|-----------|
| **배수한** | - [백테스트 계산 서버 분리 및 비동기 파이프라인 구축](https://velog.io/@swnbae)<br>- [공공데이터 포털 화재 장애 대응](https://velog.io/@swnbae)<br>- [백테스트 요청 동시성 문제 해결](https://velog.io/@swnbae)<br>- [사용자 민감정보(계좌번호) AES 암호화 구조 설계 및 구현](https://velog.io/@swnbae/%ED%95%98%EB%93%9C%EC%BD%94%EB%94%A9%EB%90%9C-%ED%82%A4%EB%A1%9C-%EC%95%94%ED%98%B8%ED%99%94%ED%95%98%EB%A9%B4-%EC%99%9C-%EC%9C%84%ED%97%98%ED%95%A0%EA%B9%8C)<br>- [거래 성과 기록 스케줄러 구현 및 배치 INSERT 도입](https://velog.io/@swnbae/jpa-batch-insert)<br>- Kakao OAuth2 + JWT 액세스/리프레시 토큰 인증 시스템 구현<br>- 포트폴리오 관련 API 구현 |
| **유윤지** | |

<br/>
<br/>
