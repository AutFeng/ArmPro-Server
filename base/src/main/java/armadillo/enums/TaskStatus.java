package armadillo.enums;

public enum TaskStatus {
    Wait("等待中"),
    Processing("处理中"),
    Success("成功"),
    Fail("异常"),
    Stop("终止"),
    TimeOut("超时");
    private final String tag;

    TaskStatus(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return tag;
    }
}
