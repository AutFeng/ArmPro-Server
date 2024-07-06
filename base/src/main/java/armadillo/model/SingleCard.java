package armadillo.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class SingleCard implements Serializable {
    private static final long serialVersionUID = 1250574089178254039L;
    @JSONField(serialize = false)
    private Integer id;

    private String card;

    private Integer value;

    private Integer type;

    private String mac;

    private String token;

    private String mark;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @SerializedName("zxcjzxchxzjhc")
    private Date usrTime;

    private Integer usrCount;

    private Boolean usable;

    @JSONField(serialize = false)
    private Integer softId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card == null ? null : card.trim();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac == null ? null : mac.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark == null ? null : mark.trim();
    }

    public Date getUsrTime() {
        return usrTime;
    }

    public void setUsrTime(Date usrTime) {
        this.usrTime = usrTime;
    }

    public Integer getUsrCount() {
        return usrCount;
    }

    public void setUsrCount(Integer usrCount) {
        this.usrCount = usrCount;
    }

    public Boolean getUsable() {
        return usable;
    }

    public void setUsable(Boolean usable) {
        this.usable = usable;
    }

    public Integer getSoftId() {
        return softId;
    }

    public void setSoftId(Integer softId) {
        this.softId = softId;
    }
}