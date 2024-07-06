package armadillo.model;

import java.io.Serializable;
import java.util.Date;

public class SysCard implements Serializable {
    private static final long serialVersionUID = 2599642935207675073L;
    private Integer id;

    private String card;

    private Integer count;

    private Integer type;

    private Integer userId;

    private Date usrTime;

    private Boolean usable;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card == null ? null : card.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getUsrTime() {
        return usrTime;
    }

    public void setUsrTime(Date usrTime) {
        this.usrTime = usrTime;
    }

    public Boolean getUsable() {
        return usable;
    }

    public void setUsable(Boolean usable) {
        this.usable = usable;
    }

}