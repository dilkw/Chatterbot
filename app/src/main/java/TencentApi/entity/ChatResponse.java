package TencentApi.entity;

public class ChatResponse {
    public int ret;
    public String msg;
    public Data data;
    public ChatResponse(int ret, String msg, Data data){
        this.ret = ret;
        this.msg = msg;
        this.data=data;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getRet() {
        return ret;
    }

    public String getMsg() {
        return msg;
    }

    public Data getData() {
        return data;
    }
}
