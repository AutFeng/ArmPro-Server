import armadillo.utils.RedisUtil;
import org.apache.log4j.Logger;
import org.junit.Test;

public class RedisTest {
    Logger logger = Logger.getLogger(this.getClass());
    @Test
    public void testRedis(){
        RedisUtil.getRedisUtil().set("Test","测试");
        logger.info(RedisUtil.getRedisUtil().get("Test"));
    }
}
