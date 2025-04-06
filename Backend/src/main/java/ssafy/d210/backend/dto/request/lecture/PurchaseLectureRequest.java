package ssafy.d210.backend.dto.request.lecture;

import lombok.Getter;
import lombok.Setter;
import ssafy.d210.backend.dto.request.transaction.SignedRequest;

//
@Getter
@Setter
public class PurchaseLectureRequest {
    private long userId;
    private long lectureId;
    private SignedRequest approveRequest;
    private SignedRequest purchaseRequest;
}
