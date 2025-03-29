package ssafy.d210.backend.dto.request.lecture;
//
import lombok.Getter;
import lombok.Setter;
import ssafy.d210.backend.dto.request.payment.RatioRequest;
import ssafy.d210.backend.dto.request.quiz.QuizRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LectureRegisterRequest {
    private String title;
    private String categoryName;
    private String goal;
    private String description;
    private int price;
    private String walletKey;
    private List<RatioRequest> ratios = new ArrayList<>();
    private List<SubLectureRequest> subLectures = new ArrayList<>();
    private List<QuizRequest> quizzes = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public String getWalletKey() { return walletKey; }
    public void setWalletKey(String walletKey) { this.walletKey = walletKey; }
    public List<RatioRequest> getRatios() { return ratios; }
    public void setRatios(List<RatioRequest> ratios) { this.ratios = ratios; }
    public List<SubLectureRequest> getSubLectures() { return subLectures; }
    public void setSubLectures(List<SubLectureRequest> subLectures) { this.subLectures = subLectures; }
    public List<QuizRequest> getQuizzes() { return quizzes; }
    public void setQuizzes(List<QuizRequest> quizzes) { this.quizzes = quizzes; }
}
