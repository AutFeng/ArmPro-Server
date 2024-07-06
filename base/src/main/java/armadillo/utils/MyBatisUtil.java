package armadillo.utils;

import armadillo.Constant;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

public class MyBatisUtil {
    private static SqlSessionFactory sqlSessionFactory = null;

    private static void initSqlSessionFactory() {
        InputStream inputStream = MyBatisUtil.class.getClassLoader().getResourceAsStream(Constant.getProfile() + "/mybatis-config.xml");
        synchronized (MyBatisUtil.class) {
            if (sqlSessionFactory == null) {
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            }
        }
    }

    public static SqlSession getSqlSession() {
        if (sqlSessionFactory == null) {
            initSqlSessionFactory();
        }
        return sqlSessionFactory.openSession();
    }

    public static SqlSession getSqlSession(boolean isAutoCommit) {
        if (sqlSessionFactory == null) {
            initSqlSessionFactory();
        }
        return sqlSessionFactory.openSession(isAutoCommit);
    }
}
