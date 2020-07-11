package TencentApi.entity;

public class Data {
    public String session;
    public String answer;

    public Data(String session, String answer){
        this.session = session;
        this.answer = answer;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSession() {
        return session;
    }

    public String getAnswer() {
        return answer;
    }
}
