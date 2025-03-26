const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("HelloWorld with LectureForwarder", function () {
  let helloWorld, forwarder, deployer, user;
  
  before(async function () {
    [deployer, user] = await ethers.getSigners();
    
    // LectureForwarder 배포 (생성자에서 "LectureForwarder" 사용)
    const LectureForwarder = await ethers.getContractFactory("LectureForwarder");
    forwarder = await LectureForwarder.deploy();
    await forwarder.deployed();
    
    // HelloWorld 배포 (forwarder.address 전달)
    const HelloWorld = await ethers.getContractFactory("HelloWorld");
    helloWorld = await HelloWorld.deploy(forwarder.address);
    await helloWorld.deployed();
    
    console.log("LectureForwarder deployed to:", forwarder.address);
    console.log("HelloWorld deployed to:", helloWorld.address);
    console.log("Forwarder name():", await forwarder.name());
  });

  it("Direct call to setMessage works", async function () {
    await helloWorld.connect(deployer).setMessage();
    expect(await helloWorld.getMessage()).to.equal("hello world");
    expect(await helloWorld.getLastCaller()).to.equal(deployer.address);
  });
  
  it("Deployer can use forwarder to call setMessage", async function () {
    // 새로 배포하여 상태 초기화
    const HelloWorldFactory = await ethers.getContractFactory("HelloWorld");
    helloWorld = await HelloWorldFactory.deploy(forwarder.address);
    await helloWorld.deployed();
    
    // setMessage() 함수 호출에 대한 ABI 인코딩된 data
    const data = helloWorld.interface.encodeFunctionData("setMessage");
    
    // deadline: 현재 블록타임 + 1000초 (uint48 범위 내)
    const blockTime = (await ethers.provider.getBlock("latest")).timestamp;
    const futureDeadline = blockTime + 1000;
    
    // 메타트랜잭션 서명: on-chain nonce, deadline, keccak256(data) 포함 (타입명: "ForwardRequest")
    const { request, signature } = await signMetaTxRequest(
      deployer,
      forwarder,
      {
        from: deployer.address,
        to: helloWorld.address,
        data,
        gas: 1_000_000,
        deadline: futureDeadline
      }
    );
    
    // forwarder.execute() 호출 (하나의 struct에 서명을 포함)
    await forwarder.connect(user).execute({
      from:      request.from,
      to:        request.to,
      value:     request.value,
      gas:       request.gas,
      deadline:  request.deadline,
      data:      data, // 원본 ABI 인코딩된 data (내부에서 keccak256(data)로 재해싱)
      signature: signature,
    });
    
    expect(await helloWorld.getMessage()).to.equal("hello world");
    expect(await helloWorld.getLastCaller()).to.equal(deployer.address);
  });

  it("User can use forwarder to call setMessage", async function () {
    // HelloWorld 새로 배포
    const HelloWorldFactory = await ethers.getContractFactory("HelloWorld");
    helloWorld = await HelloWorldFactory.deploy(forwarder.address);
    await helloWorld.deployed();
    
    const data = helloWorld.interface.encodeFunctionData("setMessage");
    const blockTime = (await ethers.provider.getBlock("latest")).timestamp;
    const futureDeadline = blockTime + 1000;
    
    const { request, signature } = await signMetaTxRequest(
      user,
      forwarder,
      {
        from: user.address,
        to: helloWorld.address,
        data,
        gas: 1_000_000,
        deadline: futureDeadline
      }
    );
    
    // 이번에는 배포자가 execute() 호출 (가스비 대신 부담)
    await forwarder.connect(deployer).execute({
      from:      request.from,
      to:        request.to,
      value:     request.value,
      gas:       request.gas,
      deadline:  request.deadline,
      data:      data,
      signature: signature,
    });
    
    expect(await helloWorld.getMessage()).to.equal("hello world");
    expect(await helloWorld.getLastCaller()).to.equal(user.address);
  });
});

/**
 * 메타트랜잭션 서명 헬퍼 함수
 *
 * Forwarder 내부에서 서명 검증은 다음과 같이 진행됩니다:
 *
 *   keccak256(
 *     abi.encode(
 *       _FORWARD_REQUEST_TYPEHASH,
 *       request.from,
 *       request.to,
 *       request.value,
 *       request.gas,
 *       nonces(request.from),
 *       request.deadline,
 *       keccak256(request.data)
 *     )
 *   )
 *   => _hashTypedDataV4(...) => ECDSA.recover(request.signature)
 *
 * 따라서 서명 시에는 on-chain nonce와, 
 * 입력 data의 keccak256 해시를 포함한 객체를 생성해야 합니다.
 *
 * 참고로, 이 구현은 Medium 글에서 설명한 방식과 유사합니다.
 */
async function signMetaTxRequest(signer, forwarder, input) {
  const domainName = await forwarder.name();
  const chainId = (await signer.provider.getNetwork()).chainId;
  const domain = {
    name: domainName,
    version: "1",
    chainId,
    verifyingContract: forwarder.address,
  };

  const onChainNonce = await forwarder.nonces(input.from);

  const types = {
    ForwardRequest: [
      { name: "from",     type: "address" },
      { name: "to",       type: "address" },
      { name: "value",    type: "uint256" },
      { name: "gas",      type: "uint256" },
      { name: "nonce",    type: "uint256" },
      { name: "deadline", type: "uint48" },
      { name: "data",     type: "bytes" }, // ✅ 바뀐 부분
    ]
  };

  const typedRequest = {
    from: input.from,
    to: input.to,
    value: 0,
    gas: input.gas,
    nonce: onChainNonce,
    deadline: input.deadline,
    data: input.data, // ✅ 원본 bytes 그대로
  };

  const signature = await signer._signTypedData(domain, types, typedRequest);

  return {
    request: {
      from: typedRequest.from,
      to: typedRequest.to,
      value: typedRequest.value,
      gas: typedRequest.gas,
      deadline: typedRequest.deadline,
      data: typedRequest.data,
    },
    signature,
  };
}

