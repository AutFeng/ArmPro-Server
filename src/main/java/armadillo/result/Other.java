package armadillo.result;

public class Other {
    private int total_task;
    private int total_apps;
    private int day_task;
    private String card_buy_url;
    private int group;
    private String telegram_url;
    private String card_price;

    public Other(int total_task, int total_apps, int day_task, String card_buy_url, int group, String telegram_url, String card_price) {
        this.total_task = total_task;
        this.total_apps = total_apps;
        this.day_task = day_task;
        this.card_buy_url = card_buy_url;
        this.group = group;
        this.telegram_url = telegram_url;
        this.card_price = card_price;
    }

    public int getTotal_task() {
        return total_task;
    }

    public void setTotal_task(int total_task) {
        this.total_task = total_task;
    }

    public int getTotal_apps() {
        return total_apps;
    }

    public void setTotal_apps(int total_apps) {
        this.total_apps = total_apps;
    }

    public int getDay_task() {
        return day_task;
    }

    public void setDay_task(int day_task) {
        this.day_task = day_task;
    }

    public String getCard_buy_url() {
        return card_buy_url;
    }

    public void setCard_buy_url(String card_buy_url) {
        this.card_buy_url = card_buy_url;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getTelegram_url() {
        return telegram_url;
    }

    public void setTelegram_url(String telegram_url) {
        this.telegram_url = telegram_url;
    }

    public String getCard_price() {
        return card_price;
    }

    public void setCard_price(String card_price) {
        this.card_price = card_price;
    }
}
