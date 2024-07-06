package armadillo.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class SingleTrial implements Serializable {
    private static final long serialVersionUID = -5053637231451305276L;
    @JSONField(serialize = false)
    private Integer id;

    @JSONField(serialize = false)
    private Integer softId;

    private Integer count;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    @SerializedName("zxcjzxchxzjhc")
    private Date lastTime;

    private String mac;

    @JSONField(serialize = false)
    private String token;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSoftId() {
        return softId;
    }

    public void setSoftId(Integer softId) {
        this.softId = softId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
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
}