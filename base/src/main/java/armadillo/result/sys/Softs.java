package armadillo.result.sys;

import armadillo.model.UserSoft;

import java.util.List;

public class Softs extends UserSoft {
    private static final long serialVersionUID = 4474922856143277522L;

    private int total_user;

    private List<ChartInfo> chartInfos;

    public int getTotal_user() {
        return total_user;
    }

    public void setTotal_user(int total_user) {
        this.total_user = total_user;
    }

    public List<ChartInfo> getChartInfos() {
        return chartInfos;
    }

    public void setChartInfos(List<ChartInfo> chartInfos) {
        this.chartInfos = chartInfos;
    }
}
