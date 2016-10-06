package in.parapluie.model;

/**
 * Created by surbhimanurkar on 21-03-2016.
 */
public class HelpQuestion {

    private String question;
    private int relevance;

    private HelpQuestion(){

    }

    public HelpQuestion(String question, int relevance) {
        this.question = question;
        this.relevance = relevance;
    }

    public int getRelevance() {
        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
