package ssafy.d210.backend.dto.request.payment;

import lombok.Getter;
import lombok.Setter;
//
@Getter
@Setter
public class RatioRequest {
    private String email;
    private int ratio;
    private boolean lecturer;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getRatio() { return ratio; }
    public void setRatio(int ratio) { this.ratio = ratio; }
    public boolean isLecturer() { return lecturer; }
    public void setLecturer(boolean lecturer) { this.lecturer = lecturer; }
}
