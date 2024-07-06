package armadillo.result;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

public class SingleResult {
    private String token;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date time;
    private String card;
    private int type;

    public SingleResult(String token, Date time, String card, int type) {
        this.token = token;
        this.time = time;
        this.card = card;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
