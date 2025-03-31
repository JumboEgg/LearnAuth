package ssafy.d210.backend.service;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import ssafy.d210.backend.contracts.LectureForwarder;
import ssafy.d210.backend.dto.request.transaction.MetaTxRequest;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MetaTransactionSigner {

    private static final String EIP712_DOMAIN_TYPE = "EIP712Domain(string name,string version,uint256 chainId,address verifyingContract)";
    private static final String FORWARD_REQUEST_TYPE = "ForwardRequest(address from,address to,uint256 value,uint256 gas,uint256 nonce,uint48 deadline,bytes data)";

    public static CompletableFuture<Map<String, Object>> signMetaTxRequest(
            Credentials signer,
            LectureForwarder forwarder,
            MetaTxRequest input) throws Exception {

        // 도메인 설정
        String domainName = forwarder.name().send();
//        BigInteger chainId = forwarder.getWeb3j().ethChainId().send().getChainId();

        Map<String, Object> domain = new HashMap<>();
        domain.put("name", domainName);
        domain.put("version", "1");
        domain.put("chainId", 80002);
        domain.put("verifyingContract", forwarder.getContractAddress());

        // 논스 가져오기
        BigInteger onChainNonce = forwarder.nonces(input.getFrom()).send();

        // 요청 구성
        Map<String, Object> typedRequest = new HashMap<>();
        typedRequest.put("from", input.getFrom());
        typedRequest.put("to", input.getTo());
        typedRequest.put("value", BigInteger.ZERO);
        typedRequest.put("gas", input.getGas());
        typedRequest.put("nonce", onChainNonce);
        typedRequest.put("deadline", input.getDeadline());
        typedRequest.put("data", input.getData());

        // EIP-712 서명 생성
        byte[] signature = signEIP712Message(signer, domain, typedRequest);

        // 결과 구성
        Map<String, Object> request = new HashMap<>();
        request.put("from", typedRequest.get("from"));
        request.put("to", typedRequest.get("to"));
        request.put("value", typedRequest.get("value"));
        request.put("gas", typedRequest.get("gas"));
        request.put("deadline", typedRequest.get("deadline"));
        request.put("data", typedRequest.get("data"));

        Map<String, Object> result = new HashMap<>();
        result.put("request", request);
        result.put("signature", Numeric.toHexString(signature));

        return CompletableFuture.completedFuture(result);
    }

    private static byte[] signEIP712Message(
            Credentials signer,
            Map<String, Object> domain,
            Map<String, Object> message) {

        // 도메인 구분자 해시 계산
        byte[] domainSeparator = calculateDomainSeparator(domain);

        // 구조체 해시 계산
        byte[] structHash = calculateStructHash(message);

        // EIP-712 인코딩 메시지 생성
        byte[] encodedMessage = calculateEncodedMessage(domainSeparator, structHash);

        // 메시지 서명
        Sign.SignatureData signatureData = Sign.signMessage(encodedMessage, signer.getEcKeyPair(), false);

        // 서명 데이터 구성 (r, s, v)
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        byte[] v = signatureData.getV();

        // 서명 조합
        byte[] signature = new byte[65];
        System.arraycopy(r, 0, signature, 0, 32);
        System.arraycopy(s, 0, signature, 32, 32);
        System.arraycopy(v, 0, signature, 64, 1);

        return signature;
    }

    private static byte[] calculateDomainSeparator(Map<String, Object> domain) {
        // EIP-712 표준에 따른 도메인 구분자 해시 계산
        byte[] typeHash = Hash.sha3(EIP712_DOMAIN_TYPE.getBytes());

        // 각 필드 해시
        byte[] nameHash = Hash.sha3(((String) domain.get("name")).getBytes());
        byte[] versionHash = Hash.sha3(((String) domain.get("version")).getBytes());
        BigInteger chainId = (BigInteger) domain.get("chainId");
        String verifyingContract = (String) domain.get("verifyingContract");

        // keccak256(abi.encode(typeHash, nameHash, versionHash, chainId, verifyingContract))
        ByteBuffer buffer = ByteBuffer.allocate(160); // 32*5 bytes
        buffer.put(typeHash);
        buffer.put(nameHash);
        buffer.put(versionHash);
        buffer.put(encodeUint256(chainId));
        buffer.put(encodeAddress(verifyingContract));

        return Hash.sha3(buffer.array());
    }

    private static byte[] calculateStructHash(Map<String, Object> message) {
        // EIP-712 표준에 따른 구조체 해시 계산
        byte[] typeHash = Hash.sha3(FORWARD_REQUEST_TYPE.getBytes());

        String from = (String) message.get("from");
        String to = (String) message.get("to");
        BigInteger value = (BigInteger) message.get("value");
        BigInteger gas = (BigInteger) message.get("gas");
        BigInteger nonce = (BigInteger) message.get("nonce");
        BigInteger deadline = (BigInteger) message.get("deadline");
        byte[] data = (byte[]) message.get("data");

        ByteBuffer buffer = ByteBuffer.allocate(256); // 인코딩된 모든 필드를 담기에 충분한 크기
        buffer.put(typeHash);
        buffer.put(encodeAddress(from));
        buffer.put(encodeAddress(to));
        buffer.put(encodeUint256(value));
        buffer.put(encodeUint256(gas));
        buffer.put(encodeUint256(nonce));
        buffer.put(encodeUint256(deadline)); // uint48을 uint256으로 처리
        buffer.put(encodeBytes(data));

        return Hash.sha3(buffer.array());
    }

    private static byte[] calculateEncodedMessage(byte[] domainSeparator, byte[] structHash) {
        // "\x19\x01" + domainSeparator + structHash
        byte[] prefix = new byte[] { 0x19, 0x01 };
        return concatenateBytes(prefix, domainSeparator, structHash);
    }

    // 유틸리티 메서드들
    private static byte[] concatenateBytes(byte[]... arrays) {
        int length = Arrays.stream(arrays).mapToInt(array -> array.length).sum();
        byte[] result = new byte[length];
        int currentIndex = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }
        return result;
    }

    private static byte[] encodeUint256(BigInteger value) {
        byte[] encoded = new byte[32];
        byte[] bytes = value.toByteArray();
        System.arraycopy(bytes, Math.max(0, bytes.length - 32), encoded, Math.max(0, 32 - bytes.length), Math.min(32, bytes.length));
        return encoded;
    }

    private static byte[] encodeUint48(BigInteger value) {
        // Uint48은 6바이트이지만 32바이트 슬롯에 패딩됨
        return encodeUint256(value);
    }

    private static byte[] encodeAddress(String address) {
        // 주소는 20바이트
        byte[] result = new byte[32];
        byte[] addressBytes = Numeric.hexStringToByteArray(address);
        System.arraycopy(addressBytes, 0, result, 12, 20);
        return result;
    }

    private static byte[] encodeBytes(byte[] data) {
        // 동적 타입 인코딩 (keccak256 해시)
        return Hash.sha3(data);
    }
}