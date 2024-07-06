package armadillo.dao;

public class ShellTask {
    private String uuid;
    private String dow;
    private int code;
    private String msg;
    private String uploadtoken;
    private String md5;

    public ShellTask(String uuid,String md5, String dow, String uploadtoken) {
        this.uuid = uuid;
        this.md5 = md5;
        this.dow = dow;
        this.uploadtoken = uploadtoken;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDow() {
        return dow;
    }

    public void setDow(String dow) {
        this.dow = dow;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getUploadtoken() {
        return uploadtoken;
    }

    public void setUploadtoken(String uploadtoken) {
        this.uploadtoken = uploadtoken;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
