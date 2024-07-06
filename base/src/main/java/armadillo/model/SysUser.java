package armadillo.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;

public class SysUser implements Serializable {

    private static final long serialVersionUID = -6301709981019654983L;

    private Integer id;

    private String username;

    @JSONField(serialize = false)
    private String password;

    @JSONField(serialize = false)
    private String email;

    private String token;

    @JSONField(serialize = false)
    private String openid;

    private Integer loginCount;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date expireTime;

    @JSONField(serialize = false)
    private Date regTime;

    private Integer value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return "SysUser{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", openid='" + openid + '\'' +
                ", loginCount=" + loginCount +
                ", expireTime=" + expireTime +
                ", regTime=" + regTime +
                '}';
    }
}