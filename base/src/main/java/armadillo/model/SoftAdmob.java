package armadillo.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class SoftAdmob implements Serializable {
    private static final long serialVersionUID = 5135112237486972639L;
    @JSONField(serialize = false)
    private Integer cid;

    private Integer handle;

    private String bannerIds;

    private String interstitialIds;

    private String rewardedIds;

    private String openIds;

    private String rules;

    @JSONField(serialize = false)
    private Integer softId;

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public Integer getHandle() {
        return handle;
    }

    public void setHandle(Integer handle) {
        this.handle = handle;
    }

    public String getBannerIds() {
        return bannerIds;
    }

    public void setBannerIds(String bannerIds) {
        this.bannerIds = bannerIds == null ? null : bannerIds.trim();
    }

    public String getInterstitialIds() {
        return interstitialIds;
    }

    public void setInterstitialIds(String interstitialIds) {
        this.interstitialIds = interstitialIds == null ? null : interstitialIds.trim();
    }

    public String getRewardedIds() {
        return rewardedIds;
    }

    public void setRewardedIds(String rewardedIds) {
        this.rewardedIds = rewardedIds == null ? null : rewardedIds.trim();
    }

    public String getOpenIds() {
        return openIds;
    }

    public void setOpenIds(String openIds) {
        this.openIds = openIds == null ? null : openIds.trim();
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules == null ? null : rules.trim();
    }

    public Integer getSoftId() {
        return softId;
    }

    public void setSoftId(Integer softId) {
        this.softId = softId;
    }
}