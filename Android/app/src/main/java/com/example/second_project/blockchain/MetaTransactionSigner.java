package com.example.second_project.blockchain;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MetaTransactionSigner {

    private static final String EIP712_DOMAIN_TYPE = "EIP712Domain(string name,string version,uint256 chainId,address verifyingContract)";
    private static final String FORWARD_REQUEST_TYPE = "ForwardRequest(address from,address to,uint256 value,uint256 gas,uint256 nonce,uint48 deadline,bytes data)";

    public static CompletableFuture<SignedRequest> signMetaTxRequest(
            Credentials signer,
            LectureForwarder forwarder,
            Web3j web3j,
            MetaTxRequest input) throws Exception {

        // 체인에서 nonce 조회
        BigInteger nonce = forwarder.nonces(input.getFrom()).send();
        return signMetaTxRequestWithNonce(signer, forwarder, web3j, input, nonce);
    }

    public static CompletableFuture<SignedRequest> signMetaTxRequestWithNonce(
            Credentials signer,
            LectureForwarder forwarder,
            Web3j web3j,
            MetaTxRequest input,
            BigInteger explicitNonce) throws Exception {

        CompletableFuture<SignedRequest> future = new CompletableFuture<>();

        try {
            // 1. 도메인 관련 데이터 가져오기
            String domainName = forwarder.name().send();
            BigInteger chainId = web3j.ethChainId().send().getChainId();
            String verifyingContract = forwarder.getContractAddress();

            // 2. 요청 객체 생성
            ForwardRequest request = new ForwardRequest(
                    input.getFrom(),
                    input.getTo(),
                    BigInteger.ZERO,
                    input.getGas(),
                    explicitNonce,
                    input.getDeadline(),
                    input.getData()
            );

            // 3. 도메인 구분자 계산
            byte[] domainSeparator = calculateDomainSeparator(domainName, "1", chainId, verifyingContract);

            // 4. 타입 해시 계산
            byte[] typeHash = Hash.sha3(FORWARD_REQUEST_TYPE.getBytes());

            // 5. 구조체 해시 계산
            byte[] structHash = calculateStructHash(typeHash, request);

            // 6. 최종 해시 계산 (EIP-712 방식)
            byte[] digest = calculateDigest(domainSeparator, structHash);

            // 7. 개인키로 서명
            ECKeyPair keyPair = signer.getEcKeyPair();
            Sign.SignatureData signatureData = Sign.signMessage(digest, keyPair, false);

            // 8. 서명 결합
            String signature = joinSignature(signatureData);

            // 9. 결과 반환
            future.complete(new SignedRequest(request, signature));

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private static byte[] calculateDomainSeparator(String name, String version, BigInteger chainId, String verifyingContract) {
        byte[] typeHash = Hash.sha3(EIP712_DOMAIN_TYPE.getBytes());

        byte[] nameHash = Hash.sha3(name.getBytes());
        byte[] versionHash = Hash.sha3(version.getBytes());

        return Hash.sha3(concatenateDataForDomain(
                typeHash,
                nameHash,
                versionHash,
                Numeric.toBytesPadded(chainId, 32),
                Numeric.hexStringToByteArray(padAddress(verifyingContract))
        ));
    }

    private static byte[] calculateStructHash(byte[] typeHash, ForwardRequest request) {
        byte[] fromAddress = Numeric.hexStringToByteArray(padAddress(request.getFrom()));
        byte[] toAddress = Numeric.hexStringToByteArray(padAddress(request.getTo()));
        byte[] value = Numeric.toBytesPadded(request.getValueAmount(), 32);
        byte[] gas = Numeric.toBytesPadded(request.getGas(), 32);
        byte[] nonce = Numeric.toBytesPadded(request.getNonce(), 32);
        byte[] deadline = Numeric.toBytesPadded(request.getDeadline(), 32);
        byte[] dataHash = Hash.sha3(Numeric.hexStringToByteArray(request.getData()));

        return Hash.sha3(concatenateDataForStruct(
                typeHash,
                fromAddress,
                toAddress,
                value,
                gas,
                nonce,
                deadline,
                dataHash
        ));
    }

    private static byte[] calculateDigest(byte[] domainSeparator, byte[] structHash) {
        ByteBuffer buffer = ByteBuffer.allocate(66); // 2 + 32 + 32
        buffer.put(new byte[] {0x19, 0x01});
        buffer.put(domainSeparator);
        buffer.put(structHash);
        return Hash.sha3(buffer.array());
    }

    private static byte[] concatenateDataForDomain(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        for (byte[] array : arrays) {
            buffer.put(array);
        }

        return buffer.array();
    }

    private static byte[] concatenateDataForStruct(byte[]... arrays) {
        return concatenateDataForDomain(arrays);
    }

    private static String padAddress(String address) {
        String cleanAddress = Numeric.cleanHexPrefix(address.toLowerCase());
        while (cleanAddress.length() < 64) {
            cleanAddress = "0" + cleanAddress;
        }
        return "0x" + cleanAddress;
    }

    private static String joinSignature(Sign.SignatureData signatureData) {
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        byte v = signatureData.getV()[0];

        byte[] result = new byte[65];
        System.arraycopy(r, 0, result, 0, 32);
        System.arraycopy(s, 0, result, 32, 32);
        result[64] = v;

        return Numeric.toHexString(result);
    }
}