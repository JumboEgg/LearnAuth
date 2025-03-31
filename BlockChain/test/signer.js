// utils/signer.js

const { ethers } = require("hardhat");

/**
 * EIP-712 메타트랜잭션 요청 서명 함수
 * @param {Object} domainData - EIP-712 도메인 데이터
 * @param {Object} message - 포워더에 전달할 요청 객체
 * @param {Object} signer - 요청에 서명할 계정
 * @returns {Object} 서명된 요청과 서명 값
 */
async function signMetaTxRequest(domainData, message, signer) {
  const types = {
    ForwardRequest: [
      { name: "from", type: "address" },
      { name: "to", type: "address" },
      { name: "value", type: "uint256" },
      { name: "gas", type: "uint256" },
      { name: "nonce", type: "uint256" },
      { name: "deadline", type: "uint48"},
      { name: "data", type: "bytes" }
    ]
  };

  // 논스 값 가져오기
  const forwarder = await ethers.getContractAt("LectureForwarder", domainData.verifyingContract);
  const nonce = await forwarder.nonces(message.from);

  const blockTime = (await ethers.provider.getBlock("latest")).timestamp;

  // 요청 객체 구성
  const request = {
    from: message.from,
    to: message.to,
    value: 0,
    gas: 1000000,
    nonce: nonce.toString(),
    deadline: message.deadline,
    data: message.data
  };

  // EIP-712 서명 생성
  const signature = await signer._signTypedData(domainData, types, request);

  return {
    request,
    signature
  };
}

module.exports = {
  signMetaTxRequest
};