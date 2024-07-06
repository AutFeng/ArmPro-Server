package armadillo.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

public class SingleVerify implements Serializable {
    private static final long serialVersionUID = -614502725202555974L;

    @JSONField(serialize = false)
    private Integer cid;

    private String title;

    private String msg;

    private String weburl;

    private String backgroundUrl;

    private String confirmText;

    private String cancelText;

    private String extraText;

    private Integer titleTextColor;

    private Integer msgTextColor;

    private Integer confirmTextColor;

    private Integer cancelTextColor;

    private Integer extraTextColor;

    private Integer extraAction;

    private Integer tryCount;

    private Integer tryMinutes;

    private Integer dialogStyle;

    private Integer bindMode;

    @JSONField(serialize = false)
    private Integer softId;

    public Integer getCid() {
        return cid;
    }

    public void setCid(Integer cid) {
        this.cid = cid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg == null ? null : msg.trim();
    }

    public String getWeburl() {
        return weburl;
    }

    public void setWeburl(String weburl) {
        this.weburl = weburl == null ? null : weburl.trim();
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl == null ? null : backgroundUrl.trim();
    }

    public String getConfirmText() {
        return confirmText;
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText == null ? null : confirmText.trim();
    }

    public String getCancelText() {
        return cancelText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText == null ? null : cancelText.trim();
    }

    public String getExtraText() {
        return extraText;
    }

    public void setExtraText(String extraText) {
        this.extraText = extraText == null ? null : extraText.trim();
    }

    public Integer getTitleTextColor() {
        return titleTextColor;
    }

    public void setTitleTextColor(Integer titleTextColor) {
        this.titleTextColor = titleTextColor;
    }

    public Integer getMsgTextColor() {
        return msgTextColor;
    }

    public void setMsgTextColor(Integer msgTextColor) {
        this.msgTextColor = msgTextColor;
    }

    public Integer getConfirmTextColor() {
        return confirmTextColor;
    }

    public void setConfirmTextColor(Integer confirmTextColor) {
        this.confirmTextColor = confirmTextColor;
    }

    public Integer getCancelTextColor() {
        return cancelTextColor;
    }

    public void setCancelTextColor(Integer cancelTextColor) {
        this.cancelTextColor = cancelTextColor;
    }

    public Integer getExtraTextColor() {
        return extraTextColor;
    }

    public void setExtraTextColor(Integer extraTextColor) {
        this.extraTextColor = extraTextColor;
    }

    public Integer getExtraAction() {
        return extraAction;
    }

    public void setExtraAction(Integer extraAction) {
        this.extraAction = extraAction;
    }

    public Integer getTryCount() {
        return tryCount;
    }

    public void setTryCount(Integer tryCount) {
        this.tryCount = tryCount;
    }

    public Integer getTryMinutes() {
        return tryMinutes;
    }

    public void setTryMinutes(Integer tryMinutes) {
        this.tryMinutes = tryMinutes;
    }

    public Integer getDialogStyle() {
        return dialogStyle;
    }

    public void setDialogStyle(Integer dialogStyle) {
        this.dialogStyle = dialogStyle;
    }

    public Integer getBindMode() {
        return bindMode;
    }

    public void setBindMode(Integer bindMode) {
        this.bindMode = bindMode;
    }

    public Integer getSoftId() {
        return softId;
    }

    public void setSoftId(Integer softId) {
        this.softId = softId;
    }
}