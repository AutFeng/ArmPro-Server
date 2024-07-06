package armadillo.result.sys;

import java.io.Serializable;

public class ChartInfo implements Serializable {
    private static final long serialVersionUID = 2577418493333011952L;
    private String time;
    private int usr_count;
    private int start_count;

    public ChartInfo(String time, int usr_count, int start_count) {
        this.time = time;
        this.usr_count = usr_count;
        this.start_count = start_count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getUsr_count() {
        return usr_count;
    }

    public void setUsr_count(int usr_count) {
        this.usr_count = usr_count;
    }

    public int getStart_count() {
        return start_count;
    }

    public void setStart_count(int start_count) {
        this.start_count = start_count;
    }
}
