const { expect } = require("chai");
const { ethers } = require("hardhat");
const { signMetaTxRequest } = require("./signer");

describe("LectureSystem Meta Transactions", function () {
  let lectureSystem;
  let forwarder;
  let catToken;
  let admin, lecturer1, lecturer2, student1, student2;
  let domainData;
  let lectureId = 1;

  const LECTURE_PRICE = 1000;

  beforeEach(async function () {
    // 계정 설정
    [admin, lecturer1, lecturer2, student1, student2] = await ethers.getSigners();

    // 포워더 배포
    const LectureForwarder = await ethers.getContractFactory("LectureForwarder");
    forwarder = await LectureForwarder.deploy();
    await forwarder.deployed();

    // CAT 토큰 배포
    const CatToken = await ethers.getContractFactory("CATToken");
    catToken = await CatToken.deploy(forwarder.address);
    await catToken.deployed();

    // 렉처시스템 배포
    const LectureSystem = await ethers.getContractFactory("LectureSystem");
    lectureSystem = await LectureSystem.deploy(catToken.address, forwarder.address);
    await lectureSystem.deployed();

    // 초기 토큰 민팅
    await catToken.mint(student1.address, ethers.utils.parseEther("1000"));
    await catToken.mint(student2.address, ethers.utils.parseEther("1000"));
    
    // 사용자 추가
    await lectureSystem.addUser(1, lecturer1.address); // 강사1: ID 1
    await lectureSystem.addUser(2, lecturer2.address); // 강사2: ID 2
    await lectureSystem.addUser(3, student1.address);  // 학생1: ID 3
    await lectureSystem.addUser(4, student2.address);  // 학생2: ID 4

    // 도메인 데이터 설정
    domainData = {
      name: await forwarder.name(),
      version: "0.0.1",
      chainId: (await ethers.provider.getNetwork()).chainId,
      verifyingContract: forwarder.address
    };
  });

  it("Deployer can use forwarder to create lecture", async function () {
    const participants = [
      { participantId: student1, settlementRatio: 60 },
      { participantId: student2, settlementRatio: 40 }
    ];

    // Encode createLecture function call
    const data = lectureSystem.interface.encodeFunctionData("createLecture", [
      lectureId,
      "Test Lecture", 
      participants
    ]);
    
    // Get current block timestamp
    const blockTime = (await ethers.provider.getBlock("latest")).timestamp;
    const futureDeadline = blockTime + 1000;
    
    // Sign meta-transaction
    const { request, signature } = await signMetaTxRequest(
      student1,
      forwarder,
      {
        from: student1.address,
        to: lectureSystem.address,
        data,
        gas: 1_000_000,
        deadline: futureDeadline
      }
    );
    
    // Execute meta-transaction via a different user
    await forwarder.connect(admin).execute({
      from:      request.from,
      to:        request.to,
      value:     request.value,
      gas:       request.gas,
      deadline:  request.deadline,
      data:      data,
      signature: signature,
    });

    // Verify lecture was created
    const lecture = await lectureSystem.lectures(lectureId);
    expect(lecture.exists).to.be.true;
    expect(lecture.title).to.equal("Test Lecture");
  });

  it("User can use forwarder to purchase lecture", async function () {
    // Approve token spending for meta-transaction
    await catToken.connect(student1).approve(lectureSystem.address, LECTURE_PRICE);

    // Encode purchaseLecture function call
    const data = lectureSystem.interface.encodeFunctionData("purchaseLecture", [
      3, 
      lectureId,
      LECTURE_PRICE
    ]);
    
    // Get current block timestamp
    const blockTime = (await ethers.provider.getBlock("latest")).timestamp;
    const futureDeadline = blockTime + 1000;
    
    // Sign meta-transaction
    const { request, signature } = await signMetaTxRequest(
      student1,
      forwarder,
      {
        from: student1.address,
        to: lectureSystem.address,
        data,
        gas: 1_000_000,
        deadline: futureDeadline
      }
    );
    
    // Execute meta-transaction via a different user
    await forwarder.connect(admin).execute({
      from:      request.from,
      to:        request.to,
      value:     request.value,
      gas:       request.gas,
      deadline:  request.deadline,
      data:      data,
      signature: signature,
    });

    // Verify lecture purchase
    const userPurchases = await lectureSystem.getUserPurchases(student1.address);
    expect(userPurchases[0]).to.equal(3);
  });

  it("메타트랜잭션으로 NFT 발급하기", async function () {
    // NFT 발급 데이터
    const cid = "QmT1234567890abcdefghijklmnopqrstuvwxyz";
    const data = lectureSystem.interface.encodeFunctionData(
      "issueNFT",
      [3, cid]
    );

    // 학생의 메타트랜잭션 요청 서명
    const { request, signature } = await signMetaTxRequest(
      domainData,
      { from: student1.address, to: lectureSystem.address, data },
      student1
    );

    // 관리자가 학생의 트랜잭션을 대신 제출
    await expect(forwarder.connect(admin).execute(request, signature))
      .to.emit(lectureSystem, "NFTIssued")
      .withArgs(3, 0); // 첫 번째 토큰 ID는 0

    // NFT가 올바르게 발급되었는지 확인
    const owner = await lectureSystem.ownerOf(0);
    expect(owner).to.equal(student1.address);
    
    const tokenURI = await lectureSystem.tokenURI(0);
    expect(tokenURI).to.equal(`ipfs://${cid}`);
  });

  it("잔액 확인 함수 테스트", async function () {
    // 초기 토큰 잔액 확인
    const balance = await lectureSystem.checkBalance(student1.address);
    expect(balance).to.equal(ethers.utils.parseEther("1000"));
  });

  it("토큰 입출금 메타트랜잭션 테스트", async function () {
    // 학생이 CAT 토큰 승인
    const amount = ethers.utils.parseEther("50");
    await catToken.connect(student1).approve(lectureSystem.address, amount);

    // 출금 데이터 준비
    const withdrawData = lectureSystem.interface.encodeFunctionData(
      "withdrawToken",
      [3, amount]
    );

    // 학생의 메타트랜잭션 요청 서명
    const { request: withdrawRequest, signature: withdrawSignature } = await signMetaTxRequest(
      domainData,
      { from: student1.address, to: lectureSystem.address, data: withdrawData },
      student1
    );

    // 포워더를 통해 출금 트랜잭션 전송
    await forwarder.connect(student2).execute(withdrawRequest, withdrawSignature);

    // 컨트랙트의 토큰 잔액 확인
    const contractBalance = await catToken.balanceOf(lectureSystem.address);
    expect(contractBalance).to.equal(amount);

    // 입금 데이터 준비
    const depositData = lectureSystem.interface.encodeFunctionData(
      "depositToken",
      [3, amount]
    );

    // 관리자의 메타트랜잭션 요청 서명
    const { request: depositRequest, signature: depositSignature } = await signMetaTxRequest(
      domainData,
      { from: admin.address, to: lectureSystem.address, data: depositData },
      admin
    );

    // 포워더를 통해 입금 트랜잭션 전송
    await forwarder.connect(student2).execute(depositRequest, depositSignature);

    // 학생의 토큰 잔액 확인 (초기 잔액과 동일해야 함)
    const studentBalance = await catToken.balanceOf(student1.address);
    expect(studentBalance).to.equal(ethers.utils.parseEther("1000"));
  });
});