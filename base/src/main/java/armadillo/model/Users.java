package armadillo.model;

import java.util.List;

public class Users {
    public List<Infos> infos;

    public static class Infos {
        public long reg_time;

        public int login_count;

        public long expire_time;

        public int v_value;

        public String openid;
    }
}
