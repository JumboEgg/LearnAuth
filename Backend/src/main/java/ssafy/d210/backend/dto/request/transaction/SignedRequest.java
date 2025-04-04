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
public class SignedRequest {
    private ForwardRequest request;
    private String signature;

    @Override
    public String toString() {
        return "SignedRequest{" +
                "request=" + request +
                ", signature='" + signature + '\'' +
                '}';
    }
}

//public static class SignedRequest {
//    private ForwardRequest request;
//    private String signature;
//
//    public SignedRequest(ForwardRequest request, String signature) {
//        this.request = request;
//        this.signature = signature;
//    }
//
//    public ForwardRequest getRequest() { return request; }
//    public String getSignature() { return signature; }
//
//    @Override
//    public String toString() {
//        return "SignedRequest{" +
//                "request=" + request +
//                ", signature='" + signature + '\'' +
//                '}';
//    }
//}