package ssafy.d210.backend.dto.request.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetaTxRequest {
    private String from;
    private String to;
    private BigInteger gas;
    private BigInteger deadline;
    private String data;
}

// Input class for meta transaction request
//public static class MetaTxRequest {
//    private String from;
//    private String to;
//    private BigInteger gas;
//    private BigInteger deadline;
//    private String data;
//
//    public MetaTxRequest(String from, String to, BigInteger gas, BigInteger deadline, String data) {
//        this.from = from;
//        this.to = to;
//        this.gas = gas;
//        this.deadline = deadline;
//        this.data = data;
//    }
//
//    public String getFrom() { return from; }
//    public String getTo() { return to; }
//    public BigInteger getGas() { return gas; }
//    public BigInteger getDeadline() { return deadline; }
//    public String getData() { return data; }
//}

