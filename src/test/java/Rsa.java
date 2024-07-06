import armadillo.Application;
import armadillo.enums.SoftEnums;
import armadillo.mapper.RemoteNoticeMapper;
import armadillo.mapper.SingleVerifyMapper;
import armadillo.mapper.UserSoftMapper;
import armadillo.model.UserSoft;
import armadillo.result.SoftResult;
import armadillo.utils.MyBatisUtil;
import armadillo.utils.RSAUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import java.security.KeyPair;
import java.util.Base64;
import java.util.Objects;

public class Rsa {
    @Test
    public void genRsa() {
        /*KeyPair keyPair = RSAUtils.generateRSAKeyPair(1024);
        System.out.println(Base64.getEncoder().encodeToString(Objects.requireNonNull(keyPair).getPublic().getEncoded()));

        System.out.println(Base64.getEncoder().encodeToString(Objects.requireNonNull(keyPair).getPrivate().getEncoded()));*/
    }

    @Test
    public void Color() {
//        PropertyConfigurator.configure(Rsa.class.getClassLoader().getResourceAsStream(Application.getProfile() + "/log4j.properties"));
//        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
//            UserSoftMapper softMapper = sqlSession.getMapper(UserSoftMapper.class);
//            UserSoft softKey = softMapper.findSoftKey("123123");
//            SoftResult result = new SoftResult();
//            result.setVersion(softKey.getVersion());
//            SoftEnums[] flags = SoftEnums.getFlags(softKey.getHandle());
//            for (SoftEnums flag : flags) {
//                switch (flag) {
//                    case SingleVerify:
//                        SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
//                        result.setSingleVerify(singleVerifyMapper.selectBySoftId(1));
//                        break;
//                    case LoginVerify:
//                        break;
//                    case DrainageGroup:
//                        break;
//                    case Notice:
//                        RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
//                        result.setRemoteNotice(remoteNoticeMapper.selectBySoftId(1));
//                        break;
//                }
//            }
//            System.out.println(JSONObject.toJSONString(result));
//            UserSoftMapper softMapper = sqlSession.getMapper(UserSoftMapper.class);
//            SoftResult o = softMapper.SeleteSoftKey("123123");
//            System.out.println(JSONObject.toJSONString(o));
        //}
    }
}
