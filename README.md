<img width="1920" height="1080" alt="12%EA%B8%B0_%ED%8A%B9%ED%99%94PJT_%EB%B0%9C%ED%91%9C%EC%9E%90%EB%A3%8C_D210" src="https://github.com/user-attachments/assets/8db7ca2b-93ce-4c76-b3b9-9280541b0849" />

# ✍️ 프로젝트 소개
블록체인과 IPFS 기술을 활용하여 강의 콘텐츠의 위변조를 방지하고, NFT 형태의 수료증을 발급하는 디지털 교육 플랫폼입니다. <br/>
스마트 컨트랙트를 통한 자동 정산 시스템과 P2P 강의 거래를 지원하여 투명하고 신뢰할 수 있는 온라인 교육 생태계를 구축합니다.
<br/>

# ✔️ 구현 사항
- **강의 등록 및 자동 정산 시스템**
    - 강의 제작 참여자별 기여도에 따른 정산 비율 설정 및 스마트 컨트랙트를 통한 자동 수익 분배
- **NFT 수료증 발급**
    - IPFS에 수료증 정보를 업로드하여 NFT로 생성, QR코드를 통한 수료증 검증 시스템
- **IPFS 기반 콘텐츠 보호**
    - 강의 자료를 IPFS에 저장하여 위변조 방지 및 영구적 보존
- **Meta Transaction 구현**
    - ERC-2771을 활용한 트랜잭션 비용 대리 지불로 사용자 편의성 향상
<br/>

# 📍 주요 기능
### 1. 강의 등록
| <img height="400" alt="image" src="https://github.com/user-attachments/assets/32acc335-b5cb-40fb-aa15-93bef92d843b" /> |
|:------:|
| 사용자는 자유롭게 강의를 등록할 수 있습니다. 강의 자료는 **IPFS**에 등록되어 위변조가 어렵습니다. <br/> 이 과정에서, 강의 참여자에 대한 **정산 비율을 지정**할 수 있습니다. |
<br>

### 2. 충전 및 자동 정산 기능
| <img height="400" alt="image" src="https://github.com/user-attachments/assets/1e75d07e-858a-4e74-b094-aed5410300aa" /> |
|:------:|
| 수강자는 코인(토큰)을 충전할 수 있고, 강의 구매시 강의 참여자에게 자동으로 정산됩니다.<br>이때, **강의 등록시에 설정한 비율대로 투명하게 분배가 이루어집니다.** |
<br>

### 3. 강의 수강 기능
| <img height="400" alt="image" src="https://github.com/user-attachments/assets/c6df09c2-a1d3-4821-88aa-5e5541031997" /> |
|:------:|
| 가장 최근 수강한 강의, 정지한 지점에서 이어보기 등을 지원합니다.<br>모든 개별 강의를 80% 이상 수강한 이력이 있어야만 ‘퀴즈 풀기’ 단계로 이동할 수 있습니다. |
<br>

### 4. 퀴즈 풀기(강의 수료) 기능
| <img height="400" alt="image" src="https://github.com/user-attachments/assets/2ed4db23-2bf9-4979-ac16-4e6c8929e060" /> |
|:------:|
| 강의 제공자가 등록한 퀴즈를 60% 이상 맞혀야만 수료가 완료됩니다. |
<br>

### 5. 수료증 발급 기능
| <img height="400" alt="image" src="https://github.com/user-attachments/assets/db386bcd-5ec0-48f6-ade7-a84de5b32689" /> |
|:------:|
| 퀴즈를 통과한 수강자는 수료증을 발급받을 수 있습니다. **수료증 정보는 IPFS에 업로드되며,** <br>이를 QR코드로 제공함으로써 **러너스 서비스가 종료돼도 수료증 정보에 접근이 가능하여 진위여부를 판별할 수 있습니다.** |
<br>

# ⚙️ 기술 스택

| 분류 | 기술 스택 |
|------|-----------|
| **Android** | ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white) ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=flat&logo=android-studio&logoColor=white) |
| **Backend** | ![Java](https://img.shields.io/badge/Java-007396?style=flat&logo=java&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=spring&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat&logo=spring&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?style=flat&logo=spring&logoColor=white) ![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=json-web-tokens&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white) |
| **Blockchain** | ![Web3j](https://img.shields.io/badge/Web3j-F16822?style=flat&logo=ethereum&logoColor=white) ![IPFS](https://img.shields.io/badge/IPFS-65C2CB?style=flat&logo=ipfs&logoColor=white) ![Hardhat](https://img.shields.io/badge/Hardhat-FFF100?style=flat&logo=ethereum&logoColor=black) ![Solidity](https://img.shields.io/badge/Solidity-363636?style=flat&logo=solidity&logoColor=white) ![Ethers](https://img.shields.io/badge/Ethers-3C3C3D?style=flat&logo=ethereum&logoColor=white) ![OpenZeppelin](https://img.shields.io/badge/OpenZeppelin-4E5EE4?style=flat&logo=ethereum&logoColor=white) ![Dotenv](https://img.shields.io/badge/Dotenv-ECD53F?style=flat&logo=dotenv&logoColor=black) |
| **Server / Infra** | ![Ubuntu](https://img.shields.io/badge/Ubuntu-E95420?style=flat&logo=ubuntu&logoColor=white) ![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=flat&logo=jenkins&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white) |
| **외부 서비스** | ![YouTube API](https://img.shields.io/badge/YouTube%20API-FF0000?style=flat&logo=youtube&logoColor=white) |
| **협업 도구** | ![GitLab](https://img.shields.io/badge/GitLab-FCA326?style=flat&logo=gitlab&logoColor=white) ![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat&logo=github&logoColor=white) ![Notion](https://img.shields.io/badge/Notion-000000?style=flat&logo=notion&logoColor=white) ![Jira](https://img.shields.io/badge/Jira-0052CC?style=flat&logo=jira&logoColor=white) |

<br>

# ⚒️ 시스템 아키텍처
<img height="400" alt="image" src="https://github.com/user-attachments/assets/c4319292-1c91-45fd-8170-bd959b754bd4" />

<br>

# 🗂️ ERD
<img width="3340" height="1432" alt="image" src="https://github.com/user-attachments/assets/3b84fa84-4275-406c-81fb-7b9f07cfa484" />

<br>

# 👤 팀원 소개

|[이서현](https://github.com/kizizip)|[박진현](https://github.com/PJinhyeon)|[이호정](https://github.com/HoJungL) |[이한나](https://github.com/mathnobi)|[조성윤](https://github.com/SeongyunGit)|[허정은](https://github.com/JumboEgg)|
|:------:|:---:|:---:|:---:|:---:|:------:|
|<img src="https://avatars.githubusercontent.com/kizizip" width="120" />|<img src="https://avatars.githubusercontent.com/PJinhyeon" width="120" />|<img src="https://avatars.githubusercontent.com/HoJungL" width="120" />|<img src="https://avatars.githubusercontent.com/mathnobi" width="120" />|<img src="https://avatars.githubusercontent.com/SeongyunGit" width="120" />|<img src="https://avatars.githubusercontent.com/JumboEgg" width="120" />|
|팀장<br>안드로이드<br>블록체인<br>IPFS 로직 처리<br>수료증 발급 기능<br>강의 재생 및 자료 다운|안드로이드 팀장<br>인프라<br>디자인<br>강의 재생 기능<br>강의 등록 기능|안드로이드<br>블록체인<br>클라이언트 서명 로직<br>블록체인 접근 로직<br>블록체인/UI 최적화|백엔드 팀장<br>강의 등록 기능<br>신고 기능<br>백엔드-블록체인 테스트 케이스|인프라 팀장<br>백엔드<br>쿼리 최적화<br>캐싱 처리<br>배포 자동화|블록체인 팀장<br>백엔드<br>Smart Contract 개발 및 배포<br>Meta Transaction 기능 적용<br>강의 및 수료증 조회 기능<br>블록체인 연동 API|



