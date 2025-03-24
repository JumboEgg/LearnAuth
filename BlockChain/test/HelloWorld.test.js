const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("HelloWorld with LectureForwarder", function () {
  let helloWorld;
  let forwarder;
  let deployer;
  let user;
  
  before(async function () {
    // 계정 설정
    [deployer, user] = await ethers.getSigners();
    
    // LectureForwarder 배포
    const LectureForwarder = await ethers.getContractFactory("LectureForwarder");
    forwarder = await LectureForwarder.deploy();
    await forwarder.waitForDeployment();
    
    // HelloWorld 컨트랙트 배포
    const HelloWorld = await ethers.getContractFactory("HelloWorld");
    helloWorld = await HelloWorld.deploy(await forwarder.getAddress());
    await helloWorld.waitForDeployment();
    
    console.log("LectureForwarder deployed to:", await forwarder.getAddress());
    console.log("HelloWorld deployed to:", await helloWorld.getAddress());
  });

  it("Direct call to setMessage works", async function () {
    // 배포자가 직접 호출
    await helloWorld.connect(deployer).setMessage();
    
    // 메시지 확인
    expect(await helloWorld.getMessage()).to.equal("hello world");
    expect(await helloWorld.getLastCaller()).to.equal(await deployer.getAddress());
  });
  
  // it("Deployer can use forwarder to call setMessage", async function () {
  //   // 초기화를 위해 메시지 리셋 (새 컨트랙트 배포)
  //   const HelloWorldFactory = await ethers.getContractFactory("HelloWorld");
  //   helloWorld = await HelloWorldFactory.deploy(await forwarder.getAddress());
    
  //   // 호출할 함수 데이터 생성
  //   const data = helloWorld.interface.encodeFunctionData("setMessage");
    
  //   // 메타트랜잭션 요청 생성
  //   const { request, signature } = await signMetaTxRequest(
  //     deployer,
  //     forwarder,
  //     {
  //       from: await deployer.getAddress(),
  //       to: await helloWorld.getAddress(),
  //       data,
  //       gas: 1000000,
  //     }
  //   );
    
  //   // 포워더를 통해 트랜잭션 전송
  //   await forwarder.connect(user).execute(request, signature);
    
  //   // 메시지와 호출자 확인
  //   expect(await helloWorld.getMessage()).to.equal("hello world");
  //   expect(await helloWorld.getLastCaller()).to.equal(await deployer.getAddress());  // 실제 서명자가 호출자로 기록됨
  // });

  it("Deployer can use forwarder to call setMessage", async function () {
    // Initialize the HelloWorld contract again after reset
    const HelloWorldFactory = await ethers.getContractFactory("HelloWorld");
    helloWorld = await HelloWorldFactory.deploy(await forwarder.getAddress());
    
    // Encode the function call with no parameters for setMessage
    const data = helloWorld.interface.encodeFunctionData("setMessage", []);
    
    // Prepare meta-transaction request and signature
    const { request, signature } = await signMetaTxRequest(
      deployer,
      forwarder,
      {
        from: await deployer.getAddress(),
        to: await helloWorld.getAddress(),
        data,
        gas: 1000000,
      }
    );
    
    // Execute the transaction via the forwarder
    await forwarder.connect(user).execute(request, signature);
    
    // Verify the message and the caller
    expect(await helloWorld.getMessage()).to.equal("hello world");
    expect(await helloWorld.getLastCaller()).to.equal(await deployer.getAddress());
});

  
  it("User can use forwarder to call setMessage", async function () {
    // 초기화를 위해 메시지 리셋 (새 컨트랙트 배포)
    const HelloWorldFactory = await ethers.getContractFactory("HelloWorld");
    helloWorld = await HelloWorldFactory.deploy(await forwarder.getAddress());
    
    // 호출할 함수 데이터 생성
    const data = helloWorld.interface.encodeFunctionData("setMessage");
    
    // 메타트랜잭션 요청 생성
    const { request, signature } = await signMetaTxRequest(
      user,
      forwarder,
      {
        from: await user.getAddress(),
        to: await helloWorld.getAddress(),
        data,
        gas: 1000000,
      }
    );
    
    // 포워더를 통해 트랜잭션 전송 (배포자가 가스 비용 지불)
    await forwarder.connect(deployer).execute(request, signature);
    
    // 메시지와 호출자 확인
    expect(await helloWorld.getMessage()).to.equal("hello world");
    expect(await helloWorld.getLastCaller()).to.equal(await user.getAddress());  // 실제 서명자가 호출자로 기록됨
  });
  
  // 메타트랜잭션 서명을 위한 헬퍼 함수
  async function signMetaTxRequest(signer, forwarder, input) {
    const forwarderAddress = await forwarder.getAddress();
    const chainId = await signer.provider.getNetwork().then(n => n.chainId);
    const domain = {
      name: await forwarder.name,
      version: "1",
      chainId,
      verifyingContract: forwarderAddress,
    };
    
    const types = {
      ForwardRequest: [
        { name: "from", type: "address" },
        { name: "to", type: "address" },
        { name: "value", type: "uint256" },
        { name: "gas", type: "uint256" },
        { name: "nonce", type: "uint256" },
        { name: "data", type: "bytes" },
      ],
    };
    
    const nonce = await forwarder.nonces(input.from);
    
    const request = {
      from: input.from,
      to: input.to,
      value: 0,
      gas: input.gas,
      nonce,
      data: input.data,
    };
    
    const signature = await signer.signTypedData(domain, types, request);
    return { request, signature };
  }
});