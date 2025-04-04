package com.example.second_project.blockchain;


public class SignedRequest {
    private ForwardRequest request;
    private String signature;

    public SignedRequest(ForwardRequest request, String signature) {
        this.request = request;
        this.signature = signature;
    }

    public ForwardRequest getRequest() {
        return request;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "SignedRequest{" +
                "request=" + request +
                ", signature='" + signature + '\'' +
                '}';
    }
}