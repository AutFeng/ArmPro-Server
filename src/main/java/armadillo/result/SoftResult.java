package armadillo.result;

import armadillo.model.*;

import java.io.Serializable;

public class SoftResult implements Serializable {
    private static final long serialVersionUID = 2398618742348677979L;

    private Integer handle;

    private Integer version;

    private SingleVerify singleVerify;

    private RemoteNotice remoteNotice;

    private SoftUpdate softUpdate;

    private SoftCustom softCustom;

    private SoftAdmob softAdmob;

    public SoftResult(Integer handle, Integer version) {
        this.handle = handle;
        this.version = version;
    }

    public Integer getHandle() {
        return handle;
    }

    public void setHandle(Integer handle) {
        this.handle = handle;
    }

    public RemoteNotice getRemoteNotice() {
        return remoteNotice;
    }

    public void setRemoteNotice(RemoteNotice remoteNotice) {
        this.remoteNotice = remoteNotice;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public SingleVerify getSingleVerify() {
        return singleVerify;
    }

    public void setSingleVerify(SingleVerify singleVerify) {
        this.singleVerify = singleVerify;
    }

    public SoftUpdate getSoftUpdate() {
        return softUpdate;
    }

    public void setSoftUpdate(SoftUpdate softUpdate) {
        this.softUpdate = softUpdate;
    }

    public SoftCustom getSoftCustom() {
        return softCustom;
    }

    public void setSoftCustom(SoftCustom softCustom) {
        this.softCustom = softCustom;
    }

    public SoftAdmob getSoftAdmob() {
        return softAdmob;
    }

    public void setSoftAdmob(SoftAdmob softAdmob) {
        this.softAdmob = softAdmob;
    }
}
