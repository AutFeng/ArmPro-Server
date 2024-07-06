package armadillo.result;

public enum ResultBasic {
    SHELLMISS(404,"shell.miss"),
    DEVICEMAX(404, "device.max"),
    TASKMAX(404, "task.max"),
    SHELLFALL(404, "shell.fail"),
    XPOSEDSHELLFALL(404, "xposed.shell.fail"),
    TASKSUCCESS(200, "task.success"),
    UUIDNULL(404, "uuid.fail"),
    USRV(404, "usr.v"),
    USRV1(404, "usr.v1"),
    USRV2(404, "usr.v2"),
    USRV3(404, "usr.v3"),
    USRV4(404, "usr.v4"),
    USRV5(404, "usr.v5"),
    USRV6(404, "usr.v6"),
    USRV7(404, "usr.v7"),
    USRV8(404, "usr.v8"),
    USRV9(404, "usr.v9"),
    USRV10(404, "usr.v10"),
    NOTNOTICE(404, "not.notice"),
    NOTNEWVER(404, "notnew.ver"),
    SHELLNOTFAIL(404, "shell.fail"),
    CARDFAIL(404, "create.card.max.fail"),
    NEWVERUPDATESUCCESS(300, "new.ver.updata"),
    GETSUCCESS(200, "get.success"),
    UNKNOWNFAIL(404, "unknown.fail"),
    USERNOTEXISTS(404, "user.not.exists"),
    USEREXISTS(404, "user.exists"),
    USERREGSUCCESS(200, "user.reg.success"),
    PASSWORDFAIL(404, "user.pass.fail"),
    USEREMAILFAIL(404, "user.email.fail"),
    USEREMAILEXISTS(404, "user.email.exists"),
    USERRETRIEVEFAIL(404, "user.retrieve.fail"),
    USERRETRIEVESUCCESS(200, "user.retrieve.success"),
    USERSRCPASSFAIL(404, "user.srcpass.fail"),
    RECODEPAYSUCCESS(200, "recode.pay.success"),
    RECODEUSRFAIL(404, "recode.usr"),
    RECODENOTEXISTS(404, "recode.not.exists"),
    VERIFYSUCCESS(200, "verify.success"),
    ILLEGALPACK(404, "illegal.package"),
    CLASSOBIGFAIL(404, "classbig.fail"),
    SAVESUCCESS(200, "save.success"),
    DELETESUCCESS(200, "delete.success"),
    CREATESUCCESS(200, "create.success"),
    SINGLECARDNOTEXISTS(404, "single.not.exists"),
    SINGLECARDEXPIRED(404, "single.expired"),
    SINGLECARDMACFAIL(404, "single.mac.fail"),
    SINGLECARDMACBINDFAIL(404, "single.mac.bind.fail"),
    SINGLECARDFROZEN(404, "single.frozen"),
    SINGLENOTTRIAL(404, "single.not.trial"),
    SINGLECARDMAX(404, "single.card.max"),
    SINGLETRIALMAX(404, "single.trial.max");
    private final int code;
    private final String config;

    ResultBasic(int code, String config) {
        this.code = code;
        this.config = config;
    }

    public int getCode() {
        return code;
    }

    public String getConfig() {
        return config;
    }
}
