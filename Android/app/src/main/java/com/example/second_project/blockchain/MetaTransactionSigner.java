package com.example.second_project.blockchain;

import org.web3j.crypto.*;
import org.web3j.crypto.StructuredData.EIP712Domain;
import org.web3j.crypto.StructuredData.Entry;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MetaTransactionSigner {

    public static CompletableFuture<SignedRequest> signMetaTxRequest(
            Credentials signer,
            LectureForwarder forwarder,
            Web3j web3j,
            MetaTxRequest input) throws Exception {

        CompletableFuture<SignedRequest> future = new CompletableFuture<>();

        // Get domain name from forwarder contract
        String domainName = forwarder.name().send();

        // Get chain ID
        EthChainId chainIdResponse = web3j.ethChainId().send();
        BigInteger chainId = chainIdResponse.getChainId();

        // Create EIP712 domain
        EIP712Domain domain = new EIP712Domain(
                domainName,
                "1",
                chainId.toString(),
                forwarder.getContractAddress(),
                ""
        );

        // Get nonce from forwarder contract
        BigInteger onChainNonce = forwarder.nonces(input.getFrom()).send();

        // Create typed data structure
        List<Entry> forwardRequestEntries = new ArrayList<>();
        forwardRequestEntries.add(new Entry("from", "address"));
        forwardRequestEntries.add(new Entry("to", "address"));
        forwardRequestEntries.add(new Entry("value", "uint256"));
        forwardRequestEntries.add(new Entry("gas", "uint256"));
        forwardRequestEntries.add(new Entry("nonce", "uint256"));
        forwardRequestEntries.add(new Entry("deadline", "uint48"));
        forwardRequestEntries.add(new Entry("data", "bytes"));

        HashMap<String, List<Entry>> types = new HashMap<>();
        types.put("ForwardRequest", forwardRequestEntries);

        // Create message
        HashMap<String, Object> message = new HashMap<>();
        message.put("from", input.getFrom());
        message.put("to", input.getTo());
        message.put("value", BigInteger.ZERO);
        message.put("gas", input.getGas());
        message.put("nonce", onChainNonce);
        message.put("deadline", input.getDeadline());
        message.put("data", input.getData());

        // Create structured data
        StructuredData.EIP712Message eip712Message = new StructuredData.EIP712Message(
                types,
                "ForwardRequest",
                message,
                domain
        );

        // Create forward request object
        ForwardRequest request = new ForwardRequest(
                input.getFrom(),
                input.getTo(),
                BigInteger.ZERO,
                input.getGas(),
                onChainNonce,
                input.getDeadline(),
                input.getData()
        );

        // Sign the message
        StructuredDataEncoder encoder = new StructuredDataEncoder(eip712Message);
        byte[] hashStructuredData = encoder.hashStructuredData();

        ECKeyPair keyPair = signer.getEcKeyPair();
        Sign.SignatureData signatureData = Sign.signMessage(hashStructuredData, keyPair, false);

        // Convert signature to hex
        String signature = joinSignature(signatureData);

        future.complete(new SignedRequest(request, signature));
        return future;
    }

    // MetaTransactionSigner2 클래스에 다음 메서드를 추가합니다
    private static String joinSignature(Sign.SignatureData signatureData) {
        byte[] v = signatureData.getV();
        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();

        byte[] result = new byte[65]; // v(1) + r(32) + s(32) = 65 bytes
        System.arraycopy(r, 0, result, 0, 32);
        System.arraycopy(s, 0, result, 32, 32);
        result[64] = v[0];

        return Numeric.toHexString(result);
    }
}