package com.example.second_project.blockchain;

import java.math.BigInteger;

public class ForwardRequest {
    private String from;
    private String to;
    private BigInteger value;
    private BigInteger gas;
    private BigInteger nonce;
    private BigInteger deadline;
    private String data;

    public ForwardRequest(String from, String to, BigInteger value, BigInteger gas,
                          BigInteger nonce, BigInteger deadline, String data) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.gas = gas;
        this.nonce = nonce;
        this.deadline = deadline;
        this.data = data;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getGas() {
        return gas;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getDeadline() {
        return deadline;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ForwardRequest{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", value=" + value +
                ", gas=" + gas +
                ", nonce=" + nonce +
                ", deadline=" + deadline +
                ", data='" + data + '\'' +
                '}';
    }
}