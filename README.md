## 👋 프로젝트 소개

### 📊 *"리밸런싱 기반 포트폴리오 자동관리 플랫폼"*

<img width="868" height="310" alt="image" src="https://github.com/user-attachments/assets/3f2499fb-7bab-47db-ba8e-32f30a85fd5d" />


**Rebra**는 사용자가 **목표 비중을 설정하면 실시간 시세를 기반으로 매수/매도 주문을 자동 생성**하는
**주식 포트폴리오 리밸런싱 서비스**입니다.

- **수동/자동 리밸런싱**: 목표 비중 대비 현재 비중을 분석해 최소 거래로 포트폴리오를 최적화해요.
- **백테스트**: 과거 데이터 기반으로 리밸런싱 전략의 성과를 시뮬레이션할 수 있어요.
- **실시간 시세 & 거래**: 한국투자증권 WebSocket으로 실시간 시세를 받아 매수/매도 주문을 즉시 실행합니다.

<br/>
<br/>

## 🎯 주요 기능 소개

### ⚖️ 자동 리밸런싱
> **"목표 비중만 설정하면, 나머지는 Rebra가 알아서!"**

<img width="2349" height="1714" alt="image" src="https://github.com/user-attachments/assets/b54fcb7c-0511-4c4c-b146-9e867eba7d48" />
<img width="2385" height="1725" alt="image" src="https://github.com/user-attachments/assets/2aa20d8a-3407-47c9-bede-516d31f0685d" />


**목표 비중 대비 현재 비중을 실시간으로 분석**해 최소 거래 횟수로 포트폴리오를 목표에 맞춥니다.
**매수/매도 주문을 자동으로 생성**하고, 한국투자증권 API를 통해 실제 주문을 실행합니다.
서버 장애 상황에서도 **중단 없이** 리밸런싱이 동작하도록 설계해, 거래 누락이나 중복 없이 안정적으로 목표 비중을 맞춥니다.

<br/>
<br/>

### 📈 백테스트
> **"과거로 돌아가 전략을 검증해보세요!"**

<img width="2200" height="1797" alt="image" src="https://github.com/user-attachments/assets/61e0fc24-03ff-4340-abdd-96f34f925733" />


**과거 일별 시세 데이터를 기반으로 리밸런싱 전략을 시뮬레이션**합니다.
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
| **Database / Infra** | [![My Skills](https://skillicons.dev/icons?i=postgres,redis)](https://skillicons.dev) |
| **배포** | [![My Skills](https://skillicons.dev/icons?i=docker,jenkins,nginx)](https://skillicons.dev) |
| **협업 / 개발도구** | [![My Skills](https://skillicons.dev/icons?i=git,gitlab,notion,jira)](https://skillicons.dev) |

<br/>
<br/>

## 🤼 팀원 소개

| 배수한 | 유윤지 | 권순주 | 고세규 | 하헌석 | 이은수 |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="https://avatars.githubusercontent.com/u/128581113?v=4" width="120"> | <img src="https://avatars.githubusercontent.com/u/105447233?v=4" width="120"> | <img src="https://github.com/milkkwong2.png" width="120"> | <img src="https://github.com/segyugo.png" width="120"> | <img src="https://github.com/rickyhi99.png" width="120"> | <img src="https://github.com/leunsoo.png" width="120"> |
| **Backend** | **Backend** |**Backend** | **Frontend** | **Frontend** | **Frontend** |
| [@SwnBae](https://github.com/SwnBae) | [@llcodingll](https://github.com/llcodingll) | [@milkkwong2](https://github.com/milkkwong2) | [@segyugo](https://github.com/segyugo) | [@rickyhi99](https://github.com/rickyhi99) | [@leunsoo](https://github.com/leunsoo) |

<br/>
<br/>

## 🧑‍💻 역할 및 기여

| 담당자 | 주요 작업 |
|--------|-----------|
| **배수한** | - [리밸런싱 서버 분리 및 메시지큐 도입](https://velog.io/@swnbae/redis-streams-%EC%84%9C%EB%B2%84%EB%B6%84%EB%A6%AC)<br>- [공공데이터 포털 화재 장애 대응](https://velog.io/@swnbae/%EC%A0%80%EC%AA%BD-%EC%A7%91%EC%9D%B4-%EB%AC%B4%EB%84%88%EC%A1%8C%EB%8B%A4%EA%B3%A0-%ED%95%B4%EC%84%9C-%EA%B5%AC%EA%B2%BD%ED%95%98%EB%9F%AC-%EA%B0%94%EC%A3%A0.-%EA%B7%B8%EB%9F%B0%EB%8D%B0-%EB%B3%B4%EA%B3%A0-%EC%98%A4%EB%8B%88)<br>- [백테스트 요청 동시성 문제 해결](https://velog.io/@swnbae)<br>- [사용자 민감정보(계좌번호) AES 암호화 구조 설계 및 구현](https://velog.io/@swnbae/%ED%95%98%EB%93%9C%EC%BD%94%EB%94%A9%EB%90%9C-%ED%82%A4%EB%A1%9C-%EC%95%94%ED%98%B8%ED%99%94%ED%95%98%EB%A9%B4-%EC%99%9C-%EC%9C%84%ED%97%98%ED%95%A0%EA%B9%8C)<br>- 거래 성과 기록 스케줄러 구현 및 배치 INSERT 도입<br>- Kakao OAuth2 + JWT 액세스/리프레시 토큰 인증 시스템 구현<br>- 포트폴리오 관련 API 구현 |
| **유윤지** | |

<br/>
<br/>

