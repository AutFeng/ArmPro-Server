package armadillo.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class SoftCustom implements Serializable {
    private static final long serialVersionUID = -4358043379963116301L;
    @JSONField(serialize = false)
    private Integer cid;

    private Integer customLoaderMode;

    private String customLoaderPath;

    private Integer customInvokeMode;

    private String customInvokeRule;

    @JSONField(serialize = false)
    private Integer softId;

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getCustomLoaderMode() {
        return customLoaderMode;
    }

    public void setCustomLoaderMode(Integer customLoaderMode) {
        this.customLoaderMode = customLoaderMode;
    }

    public String getCustomLoaderPath() {
        return customLoaderPath;
    }

    public void setCustomLoaderPath(String customLoaderPath) {
        this.customLoaderPath = customLoaderPath == null ? null : customLoaderPath.trim();
    }

    public Integer getCustomInvokeMode() {
        return customInvokeMode;
    }

    public void setCustomInvokeMode(Integer customInvokeMode) {
        this.customInvokeMode = customInvokeMode;
    }

    public String getCustomInvokeRule() {
        return customInvokeRule;
    }

    public void setCustomInvokeRule(String customInvokeRule) {
        this.customInvokeRule = customInvokeRule == null ? null : customInvokeRule.trim();
    }

    public Integer getSoftId() {
        return softId;
    }

    public void setSoftId(Integer softId) {
        this.softId = softId;
    }
}