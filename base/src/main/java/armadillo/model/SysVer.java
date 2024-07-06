package armadillo.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Date;

public class SysVer implements Serializable {
    private static final long serialVersionUID = 6286813878639854490L;
    @JSONField(serialize = false)
    private Integer id;

    private Integer version;

    private String versionName;

    private Boolean versionMode;

    private Date time;

    private String versionMsg;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName == null ? null : versionName.trim();
    }

    public Boolean getVersionMode() {
        return versionMode;
    }

    public void setVersionMode(Boolean versionMode) {
        this.versionMode = versionMode;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getVersionMsg() {
        return versionMsg;
    }

    public void setVersionMsg(String versionMsg) {
        this.versionMsg = versionMsg == null ? null : versionMsg.trim();
    }
}