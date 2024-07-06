package armadillo.result;

import armadillo.enums.LanguageEnums;
import armadillo.utils.SysConfigUtil;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.Objects;


public class Result {
    private int code;
    private String msg;
    private Object data;
    @JSONField(serialize = false)
    private final LanguageEnums languageEnums;
    @JSONField(serialize = false)
    private final ResultBasic resultBasic;

    public Result(ResultBasic resultBasic, Object data, LanguageEnums languageEnums) {
        this.resultBasic = resultBasic;
        this.code = resultBasic.getCode();
        this.msg = SysConfigUtil.getLanguageConfigUtil(languageEnums, resultBasic.getConfig());
        this.data = data;
        this.languageEnums = languageEnums;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Result setMsg(Object... msg) {
        this.msg = String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, resultBasic.getConfig())), msg);
        return this;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
