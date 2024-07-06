package armadillo.controller;

import armadillo.Application;
import armadillo.Constant;
import armadillo.action.TaskAction;
import armadillo.enums.LanguageEnums;
import armadillo.enums.SoftEnums;
import armadillo.mapper.*;
import armadillo.model.*;
import armadillo.result.*;
import armadillo.result.sys.ChartInfo;
import armadillo.result.sys.Softs;
import armadillo.utils.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.util.ReferenceCountUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.redisson.api.RLock;

import java.io.*;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;

public class SocketController implements Runnable{
    private final Logger logger = Logger.getLogger(SocketController.class);
    private final ChannelHandlerContext ctx;
    private final ByteBuf msg;
    private final InetSocketAddress inetSocketAddress;
    private final int LocalUpload = 560;

    public SocketController(ChannelHandlerContext ctx, ByteBuf msg) {
        this.ctx = ctx;
        this.msg = msg;
        this.inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    }

    public void run() {
        try {
            int type = msg.readInt();
            int request_len = msg.readInt();
            msg.skipBytes(4);
            byte[] request = new byte[request_len];
            msg.readBytes(request);
            if (type == LocalUpload) {
                int uuid_len = 0;
                {
                    byte[] len = new byte[4];
                    System.arraycopy(request, 0, len, 0, 4);
                    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(len));
                    uuid_len = dataInputStream.readInt();
                }
                byte[] uuid = new byte[uuid_len];
                {
                    System.arraycopy(request, 4, uuid, 0, uuid_len);
                }
                byte[] data = new byte[request_len - uuid_len - 4];
                {
                    System.arraycopy(request, uuid_len + 4, data, 0, data.length);
                }
                FileOutputStream fileOutputStream = new FileOutputStream(new File(Constant.getTmp(), new String(uuid)));
                fileOutputStream.write(data);
                fileOutputStream.close();
                Writr(new Result(ResultBasic.GETSUCCESS, null, LanguageEnums.DEFAULT), Charset.forName("GBK"));
            } else {
                DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(request));
                if (dataInputStream.readShort() == 8075) {
                    request = readGzip(request);
                    JSONObject jsonObject = JSONObject.parseObject(new String(request, StandardCharsets.UTF_8));
                    LanguageEnums languageEnums = LanguageEnums.DEFAULT;
                    switch (jsonObject.getString("u")) {
                        case "zh":
                            languageEnums = LanguageEnums.ZH;
                            break;
                        case "ru":
                            languageEnums = LanguageEnums.RU;
                            break;
                    }
                    Receive(type, jsonObject, languageEnums);
                } else {
                    RSAUtils rsaUtils = new RSAUtils();
                    request = rsaUtils.decrypt(request, rsaUtils.getPublicKey(Base64.getDecoder().decode(LoaderRes.getInstance().getStaticResAsBytes("rsa/sign_public.txt"))));
                    JSONObject jsonObject = JSONObject.parseObject(new String(request, StandardCharsets.UTF_8));
                    LanguageEnums languageEnums = LanguageEnums.DEFAULT;
                    switch (jsonObject.getString("u")) {
                        case "zh":
                            languageEnums = LanguageEnums.ZH;
                            break;
                        case "ru":
                            languageEnums = LanguageEnums.RU;
                            break;
                    }
                    Receive(type, jsonObject, languageEnums);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.close();
        } finally {
            ctx.close();
            ReferenceCountUtil.release(msg);
        }
    }

    private byte[] readGzip(byte[] gzip_request) throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(gzip_request));
        byte[] bytes = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(bytes)) != -1)
            stream.write(bytes, 0, len);
        stream.close();
        return stream.toByteArray();
    }

    /**
     * 数据请求处理
     *
     * @param type
     * @param jsonObject
     * @param languageEnums
     * @throws Exception
     */
    private void Receive(final int type, final JSONObject jsonObject, final LanguageEnums languageEnums) throws Exception {
        Charset charset;
        if (jsonObject.getString("encoding") == null || jsonObject.getString("encoding").isEmpty())
            charset = Charset.forName("GBK");
        else {
            switch (jsonObject.getString("encoding")) {
                case "UTF-8":
                    charset = StandardCharsets.UTF_8;
                    break;
                default:
                    charset = StandardCharsets.UTF_8;
                    break;
            }
        }
        switch (type) {
            /**
             * 获取今天的公告记录
             */
            case 1000: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysNoticeMapper mapper = sqlSession.getMapper(SysNoticeMapper.class);
                    LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                    List<SysNotice> notices = mapper.LatNotices(Date.from(min.atZone(ZoneId.systemDefault()).toInstant()));
                    if (notices.size() > 0)
                        Writr(new Result(ResultBasic.GETSUCCESS, notices, languageEnums), charset);
                    else
                        Writr(new Result(ResultBasic.NOTNOTICE, null, languageEnums), charset);
                }
            }
            break;
            /**
             * 获取所有公告/更新日志
             */
            case 1001: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);
                    SysNoticeMapper sysNoticeMapper = sqlSession.getMapper(SysNoticeMapper.class);
                    List<SysNotice> notices = new ArrayList<>();
                    for (SysVer sysVer : sysVerMapper.selectAll()) {
                        SysNotice notice = new SysNotice();
                        notice.setTitle(sysVer.getVersionName());
                        notice.setMsg(sysVer.getVersionMsg());
                        notice.setTime(sysVer.getTime());
                        notices.add(notice);
                    }
                    notices.addAll(sysNoticeMapper.selectAll());
                    notices.sort((o1, o2) -> {
                        if (o1.getTime().before(o2.getTime()))
                            return 1;
                        return -1;
                    });
                    Writr(new Result(ResultBasic.GETSUCCESS, notices, languageEnums), charset);
                }
            }
            break;
            /**
             * 获取最新版本
             */
            case 1002: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysVerMapper sysVerMapper = sqlSession.getMapper(SysVerMapper.class);
                    List<SysVer> Vers = sysVerMapper.findNewVer(jsonObject.getInteger("version"));
                    if (Vers.size() > 0)
                        Writr(new Result(ResultBasic.GETSUCCESS, Vers, languageEnums), charset);
                    else
                        Writr(new Result(ResultBasic.NOTNEWVER, null, languageEnums), charset);
                }
            }
            break;
            /**
             * 获取帮助文档
             */
            case 1004: {
                String helper = null;
                switch (languageEnums) {
                    case ZH:
                        helper = "china.help";
                        break;
                    case RU:
                        helper = "russia.help";
                        break;
                    case DEFAULT:
                        helper = "default.help";
                        break;
                }
                Writr(new Result(ResultBasic.GETSUCCESS, new String(StreamUtil.readBytes(
                        new FileInputStream(
                                new File(Constant.getConfig(), helper))), StandardCharsets.UTF_8), languageEnums), charset);
            }
            break;
            /**
             * 获取功能列表
             */
            case 1005: {
                String json = null;
                switch (languageEnums) {
                    case ZH:
                        json = "china.json";
                        break;
                    case RU:
                        json = "russia.json";
                        break;
                    case DEFAULT:
                        json = "default.json";
                        break;
                }
                Writr(new Result(ResultBasic.GETSUCCESS,
                        Base64.getEncoder().encodeToString(
                                StreamUtil.readBytes(
                                        new FileInputStream(
                                                new File(Constant.getHandle(), json)))), languageEnums), charset);
            }
            break;
            /**
             * 第三方授权登录
             */
            case 2000: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysUser sysUser = sysUserMapper.findUser(jsonObject.getString("openid").replaceAll("\\s+", ""));
                    if (sysUser == null) {
                        sysUser = new SysUser();
                        sysUser.setLoginCount(0);
                        sysUser.setExpireTime(new Date(System.currentTimeMillis()));
                        sysUser.setRegTime(new Date(System.currentTimeMillis()));
                        sysUser.setOpenid(jsonObject.getString("openid").replaceAll("\\s+", ""));
                        sysUser.setValue(0);
                        sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getOpenid()));
                        if (of(sysUserMapper.insert(sysUser)))
                            Writr(new Result(ResultBasic.VERIFYSUCCESS, sysUser, languageEnums), charset);
                        else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    } else {
                        sysUser.setLoginCount(sysUser.getLoginCount() == null ? 1 : sysUser.getLoginCount() + 1);
                        sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getOpenid()));
                        if (of(sysUserMapper.updateByPrimaryKey(sysUser)))
                            Writr(new Result(ResultBasic.VERIFYSUCCESS, sysUser, languageEnums), charset);
                        else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * Token校验
             */
            case 2001: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        sysUser.setLoginCount(sysUser.getLoginCount() + 1);
                        sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getOpenid()));
                        if (of(sysUserMapper.updateByPrimaryKey(sysUser)))
                            Writr(new Result(ResultBasic.VERIFYSUCCESS, sysUser, languageEnums), charset);
                        else
                            Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 用户充值
             */
            case 2002: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysCardMapper sysCardMapper = sqlSession.getMapper(SysCardMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        SysCard sysCard = sysCardMapper.findCard(jsonObject.getString("card").replaceAll("\\s+", ""));
                        if (sysCard == null)
                            Writr(new Result(ResultBasic.RECODENOTEXISTS, null, languageEnums), charset);
                        else if (!sysCard.getUsable())
                            Writr(new Result(ResultBasic.RECODEUSRFAIL, null, languageEnums), charset);
                        else {
                            /**
                             * 计算卡密时间
                             */
                            long time = Math.max(sysUser.getExpireTime().getTime(), System.currentTimeMillis());
                            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
                            switch (sysCard.getType()) {
                                /**
                                 * 月卡
                                 */
                                case 1:
                                    localDateTime = localDateTime.plusMonths(sysCard.getCount());
                                    sysUser.setValue(sysUser.getValue() + sysCard.getCount() * 100000);
                                    break;
                                /**
                                 * 年卡
                                 */
                                case 2:
                                    localDateTime = localDateTime.plusYears(sysCard.getCount());
                                    sysUser.setValue(sysUser.getValue() + sysCard.getCount() * 100000);
                                    break;
                                /**
                                 * 永久VIP试用卡
                                 */
                                case 3:
                                    localDateTime = localDateTime.plusDays(sysCard.getCount());
                                    sysUser.setValue(sysUser.getValue() + 100000);
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected value: " + sysCard.getType());
                            }
                            ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
                            sysUser.setExpireTime(Date.from(zdt.toInstant()));
                            if (of(sysUserMapper.updateByPrimaryKey(sysUser))) {
                                sysCard.setUserId(sysUser.getId());
                                sysCard.setUsable(false);
                                sysCard.setUsrTime(new Date(System.currentTimeMillis()));
                                if (of(sysCardMapper.updateByPrimaryKey(sysCard)))
                                    Writr(new Result(ResultBasic.RECODEPAYSUCCESS, null, languageEnums), charset);
                                else
                                    Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                            } else
                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        }
                    }
                }
            }
            break;
            /**
             * 账号密码登录
             */
            case 2003: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysUser sysUser = sysUserMapper.findUserName(jsonObject.getString("username").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else if (!sysUser.getPassword().equals(Md5Utils.md5(jsonObject.getString("password").getBytes())))
                        Writr(new Result(ResultBasic.PASSWORDFAIL, null, languageEnums), charset);
                    else {
                        sysUser.setLoginCount(sysUser.getLoginCount() == null ? 1 : sysUser.getLoginCount() + 1);
                        sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getUsername() + sysUser.getPassword()));
                        if (of(sysUserMapper.updateByPrimaryKey(sysUser)))
                            Writr(new Result(ResultBasic.VERIFYSUCCESS, sysUser, languageEnums), charset);
                        else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 账号注册
             */
            case 2004: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    String username = jsonObject.getString("username").replaceAll("\\s+", "");
                    String password = Md5Utils.md5(jsonObject.getString("password").getBytes());
                    String email = jsonObject.getString("email");
                    SysUser sysUser = sysUserMapper.findUserName(username);
                    SysUser sysUser2 = sysUserMapper.findEmail(email);
                    if (sysUser != null)
                        Writr(new Result(ResultBasic.USEREXISTS, null, languageEnums), charset);
                    else if (sysUser2 != null)
                        Writr(new Result(ResultBasic.USEREMAILEXISTS, null, languageEnums), charset);
                    else {
                        sysUser = new SysUser();
                        sysUser.setUsername(username);
                        sysUser.setPassword(password);
                        sysUser.setEmail(email);
                        sysUser.setLoginCount(0);
                        sysUser.setExpireTime(new Date(System.currentTimeMillis()));
                        sysUser.setRegTime(new Date(System.currentTimeMillis()));
                        sysUser.setValue(0);
                        sysUser.setOpenid("");
                        sysUser.setToken(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis() + sysUser.getUsername() + sysUser.getPassword()));
                        if (of(sysUserMapper.insert(sysUser)))
                            Writr(new Result(ResultBasic.USERREGSUCCESS, null, languageEnums), charset);
                        else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 密码找回
             */
            case 2005: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysUser sysUser = sysUserMapper.findUserName(jsonObject.getString("username").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else if (!sysUser.getEmail().equals(jsonObject.getString("email")))
                        Writr(new Result(ResultBasic.USEREMAILFAIL, null, languageEnums), charset);
                    else
                        Writr(new Result(ResultBasic.USERRETRIEVEFAIL, null, languageEnums), charset);
                }
            }
            break;
            /**
             * 修改密码
             */
            case 2006: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    String src_pass = Md5Utils.md5(jsonObject.getString("password").getBytes());
                    String new_pass = Md5Utils.md5(jsonObject.getString("newpassword").getBytes());
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else if (sysUser.getPassword() == null || sysUser.getPassword().isEmpty())
                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    else if (!sysUser.getPassword().equals(src_pass))
                        Writr(new Result(ResultBasic.USERSRCPASSFAIL, null, languageEnums), charset);
                    else {
                        sysUser.setPassword(new_pass);
                        if (of(sysUserMapper.updateByPrimaryKey(sysUser)))
                            Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                        else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 获取用户任务总量/其他信息
             */
            case 2222: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        int total_task = SysConfigUtil.getIntConfig("v0");
                        int v = sysUser.getValue();
                        //V1
                        if (v > 0 && v < 100)
                            total_task = SysConfigUtil.getIntConfig("v1");
                            //V2
                        else if (v >= 100 && v < 300)
                            total_task = SysConfigUtil.getIntConfig("v2");
                            //V3
                        else if (v >= 300 && v < 600)
                            total_task = SysConfigUtil.getIntConfig("v3");
                            //V4
                        else if (v >= 600 && v < 1000)
                            total_task = SysConfigUtil.getIntConfig("v4");
                            //V5
                        else if (v >= 1000 && v < 1500)
                            total_task = SysConfigUtil.getIntConfig("v5");
                            //V6
                        else if (v >= 1500 && v < 2100)
                            total_task = SysConfigUtil.getIntConfig("v6");
                            //V7
                        else if (v >= 2100 && v < 2800)
                            total_task = SysConfigUtil.getIntConfig("v7");
                            //V8
                        else if (v >= 2800 && v < 3600)
                            total_task = SysConfigUtil.getIntConfig("v8");
                            //V9
                        else if (v >= 3600 && v < 4500)
                            total_task = SysConfigUtil.getIntConfig("v9");
                            //V10
                        else if (v >= 4500)
                            total_task = SysConfigUtil.getIntConfig("v10");
                        int total_apps = userSoftMapper.findAllSoft(sysUser.getId()).size();
                        int day_task = 0;
                        RedisUtil redisUtil = RedisUtil.getRedisUtil();
                        if (redisUtil.exists((sysUser.getOpenid() == null || sysUser.getOpenid().isEmpty()) ? sysUser.getUsername() : sysUser.getOpenid()))
                            day_task = Integer.parseInt(redisUtil.get((sysUser.getOpenid() == null || sysUser.getOpenid().isEmpty()) ? sysUser.getUsername() : sysUser.getOpenid()));
                        Other other = new Other(
                                total_task,
                                total_apps,
                                day_task,
                                languageEnums == LanguageEnums.ZH ? SysConfigUtil.getStringConfig("card.address") : SysConfigUtil.getStringConfig("usd.card.address"),
                                SysConfigUtil.getIntConfig("soft.group"),
                                SysConfigUtil.getStringConfig("telegram.group"),
                                languageEnums == LanguageEnums.ZH ? SysConfigUtil.getStringConfig("card.price") : SysConfigUtil.getStringConfig("usd.card.price"));
                        Writr(new Result(ResultBasic.GETSUCCESS, other, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 获取应用
             */
            case 3000: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        RedisUtil redisUtil = RedisUtil.getRedisUtil();
                        String min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DateTimeFormatter.ofPattern("MM/dd"));
                        List<Softs> softs = userSoftMapper.findOffSetSofts(sysUser.getId(), jsonObject.getInteger("offset"), jsonObject.getInteger("limit"));
                        for (Softs soft : softs) {
                            /**
                             * 获取6天的数据统计
                             */
                            {
                                List<ChartInfo> chartInfos = new ArrayList<>();
                                LocalDateTime today_start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).minusDays(5);
                                for (int i = 0; i < 6; i++) {
                                    LocalDateTime start = today_start.plusDays(i);
                                    String day = start.format(DateTimeFormatter.ofPattern("MM/dd"));
                                    int usr_count = 0;
                                    int start_count = 0;
                                    if (redisUtil.exists(soft.getAppkey() + "-count-" + day))
                                        usr_count = redisUtil.scard(soft.getAppkey() + "-count-" + day).intValue();
                                    if (redisUtil.exists(soft.getAppkey() + "-start-" + day))
                                        start_count = Integer.parseInt(redisUtil.get(soft.getAppkey() + "-start-" + day));
                                    chartInfos.add(new ChartInfo(day, usr_count, start_count));
                                }
                                soft.setChartInfos(chartInfos);
                            }
                            /**
                             * 获取今日活跃用户
                             */
                            if (redisUtil.exists(soft.getAppkey() + "-count-" + min))
                                soft.setTotal_user(redisUtil.scard(soft.getAppkey() + "-count-" + min).intValue());
                        }
                        Writr(new Result(ResultBasic.GETSUCCESS, softs, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 保存功能开关
             */
            case 3001: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key"));
                        if (soft == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else {
                            soft.setHandle(jsonObject.getInteger("handle"));
                            if (of(userSoftMapper.updateByPrimaryKey(soft)))
                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                            else
                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        }
                    }
                }
            }
            break;
            /**
             * 获取对应模块信息
             */
            case 3002: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
                    SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
                    SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key"));
                        if (soft == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else {
                            int value = jsonObject.getIntValue("flag");
                            if (value == 0)
                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                            else {
                                SoftEnums flag = SoftEnums.getFlags(value)[0];
                                switch (flag) {
                                    case Notice:
                                        Writr(new Result(ResultBasic.GETSUCCESS, remoteNoticeMapper.selectBySoftId(soft.getId()), languageEnums), charset);
                                        break;
                                    case SingleVerify:
                                        Writr(new Result(ResultBasic.GETSUCCESS, singleVerifyMapper.selectBySoftId(soft.getId()), languageEnums), charset);
                                        break;
                                    case Update:
                                        Writr(new Result(ResultBasic.GETSUCCESS, softUpdateMapper.selectBySoftId(soft.getId()), languageEnums), charset);
                                        break;
                                    case CustomModule:
                                        Writr(new Result(ResultBasic.GETSUCCESS, softCustomMapper.selectBySoftId(soft.getId()), languageEnums), charset);
                                        break;
                                    case Admob:
                                        Writr(new Result(ResultBasic.GETSUCCESS, softAdmobMapper.selectBySoftId(soft.getId()), languageEnums), charset);
                                        break;
                                    default:
                                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
            break;
            /**
             * 保存对应模块信息
             */
            case 3003: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
                    SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
                    SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key"));
                        if (soft == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else if (soft.getUserId().intValue() == sysUser.getId().intValue()) {
                            int value = jsonObject.getIntValue("flag");
                            String info = jsonObject.getString("info");
                            if (value == 0 || info == null || info.isEmpty())
                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                            else {
                                SoftEnums flag = SoftEnums.getFlags(value)[0];
                                switch (flag) {
                                    case Notice: {
                                        RemoteNotice NewNotice = new Gson().fromJson(new String(Base64.getDecoder().decode(info.getBytes()), StandardCharsets.UTF_8), RemoteNotice.class);
                                        RemoteNotice remoteNotice = remoteNoticeMapper.selectBySoftId(soft.getId());
                                        if (remoteNotice == null) {
                                            NewNotice.setSoftId(soft.getId());
                                            if (of(remoteNoticeMapper.insert(NewNotice)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        } else {
                                            NewNotice.setSoftId(soft.getId());
                                            NewNotice.setCid(remoteNotice.getCid());
                                            if (of(remoteNoticeMapper.updateByPrimaryKey(NewNotice)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        }
                                    }
                                    break;
                                    case SingleVerify: {
                                        SingleVerify NewSingle = new Gson().fromJson(new String(Base64.getDecoder().decode(info.getBytes()), StandardCharsets.UTF_8), SingleVerify.class);
                                        SingleVerify singleVerify = singleVerifyMapper.selectBySoftId(soft.getId());
                                        if (singleVerify == null) {
                                            NewSingle.setSoftId(soft.getId());
                                            if (of(singleVerifyMapper.insert(NewSingle)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        } else {
                                            NewSingle.setSoftId(soft.getId());
                                            NewSingle.setCid(singleVerify.getCid());
                                            if (of(singleVerifyMapper.updateByPrimaryKey(NewSingle)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        }
                                    }
                                    break;
                                    case Update: {
                                        SoftUpdate NewUpdate = new Gson().fromJson(new String(Base64.getDecoder().decode(info.getBytes()), StandardCharsets.UTF_8), SoftUpdate.class);
                                        SoftUpdate softUpdate = softUpdateMapper.selectBySoftId(soft.getId());
                                        if (softUpdate == null) {
                                            NewUpdate.setSoftId(soft.getId());
                                            if (of(softUpdateMapper.insert(NewUpdate)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        } else {
                                            NewUpdate.setSoftId(soft.getId());
                                            NewUpdate.setCid(softUpdate.getCid());
                                            if (of(softUpdateMapper.updateByPrimaryKey(NewUpdate)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        }
                                    }
                                    break;
                                    case CustomModule: {
                                        SoftCustom NewCustom = new Gson().fromJson(new String(Base64.getDecoder().decode(info.getBytes()), StandardCharsets.UTF_8), SoftCustom.class);
                                        SoftCustom softCustom = softCustomMapper.selectBySoftId(soft.getId());
                                        if (softCustom == null) {
                                            NewCustom.setSoftId(soft.getId());
                                            if (of(softCustomMapper.insert(NewCustom)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        } else {
                                            NewCustom.setSoftId(soft.getId());
                                            NewCustom.setCid(softCustom.getCid());
                                            if (of(softCustomMapper.updateByPrimaryKey(NewCustom)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        }
                                    }
                                    break;
                                    case Admob: {
                                        SoftAdmob NewAdmob = new Gson().fromJson(new String(Base64.getDecoder().decode(info.getBytes()), StandardCharsets.UTF_8), SoftAdmob.class);
                                        SoftAdmob softAdmob = softAdmobMapper.selectBySoftId(soft.getId());
                                        if (softAdmob == null) {
                                            NewAdmob.setSoftId(soft.getId());
                                            if (of(softAdmobMapper.insert(NewAdmob)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        } else {
                                            NewAdmob.setSoftId(soft.getId());
                                            NewAdmob.setCid(softAdmob.getCid());
                                            if (of(softAdmobMapper.updateByPrimaryKey(NewAdmob)))
                                                Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                            else
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        }
                                    }
                                    break;
                                }
                            }
                        } else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 单码验证注册码管理
             */
            case 3004: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key"));
                        if (soft == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else if (soft.getUserId().intValue() == sysUser.getId().intValue()) {
                            int value = jsonObject.getIntValue("flag");
                            if (value == 0)
                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                            else {
                                switch (value) {
                                    /**
                                     * 注册码生成
                                     */
                                    case 1: {
                                        if (sysUser.getExpireTime().getTime() < System.currentTimeMillis())
                                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        else if (singleCardMapper.findSoftId(soft.getId()).size() > SysConfigUtil.getIntConfig("card.max"))
                                            Writr(new Result(ResultBasic.SINGLECARDMAX, null, languageEnums), charset);
                                        else {
                                            if (jsonObject.getInteger("count") > 99
                                                    || jsonObject.getInteger("value") > 99)
                                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                            else {
                                                List<SingleCard> singleCards = new ArrayList<>();
                                                for (int i = 0; i < jsonObject.getInteger("count"); i++) {
                                                    SingleCard singleCard = new SingleCard();
                                                    singleCard.setCard(CardRadom.radomCard());
                                                    singleCard.setType(jsonObject.getInteger("type"));
                                                    singleCard.setValue(jsonObject.getInteger("value"));
                                                    singleCard.setMark(jsonObject.getString("mark"));
                                                    singleCard.setSoftId(soft.getId());
                                                    singleCards.add(singleCard);
                                                }
                                                singleCardMapper.insertAll(singleCards);
                                                Writr(new Result(ResultBasic.CREATESUCCESS, null, languageEnums), charset);
                                            }
                                        }
                                    }
                                    break;
                                    /**
                                     * 注册码获取
                                     */
                                    case 2: {
                                        List<SingleCard> cards = singleCardMapper.findOffSetCards(soft.getId(), jsonObject.getInteger("offset"), jsonObject.getInteger("limit"));
                                        Writr(new Result(ResultBasic.GETSUCCESS, cards, languageEnums), charset);
                                    }
                                    break;
                                    /**
                                     * 删除卡密
                                     */
                                    case 3: {
                                        TypeToken<List<SingleCard>> listTypeToken = new TypeToken<List<SingleCard>>() {
                                        };
                                        Type listType = listTypeToken.getType();
                                        List<SingleCard> cards = new Gson().fromJson(
                                                new String(
                                                        Base64.getDecoder().decode(
                                                                jsonObject.getString("info").getBytes()),
                                                        StandardCharsets.UTF_8), listType);
                                        for (SingleCard card : cards) {
                                            singleCardMapper.deleteByCard(card.getCard(), soft.getId());
                                        }
                                        Writr(new Result(ResultBasic.DELETESUCCESS, null, languageEnums), charset);
                                    }
                                    break;
                                    /**
                                     * 更新卡密
                                     */
                                    case 4: {
                                        if (sysUser.getExpireTime().getTime() < System.currentTimeMillis())
                                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        else {
                                            TypeToken<List<SingleCard>> listTypeToken = new TypeToken<List<SingleCard>>() {
                                            };
                                            Type listType = listTypeToken.getType();
                                            List<SingleCard> cards = new Gson().fromJson(
                                                    new String(
                                                            Base64.getDecoder().decode(
                                                                    jsonObject.getString("info").getBytes()),
                                                            StandardCharsets.UTF_8), listType);
                                            for (SingleCard card : cards) {
                                                SingleCard singleCard = singleCardMapper.findSoftAndCard(soft.getId(), card.getCard());
                                                if (singleCard != null) {
                                                    singleCard.setUsable(card.getUsable());
                                                    singleCard.setMac(card.getMac());
                                                    singleCardMapper.updateByPrimaryKey(singleCard);
                                                }
                                            }
                                            Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                        }
                                    }
                                    break;
                                    default:
                                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                }
                            }
                        } else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 单码试用管理
             */
            case 3005: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SingleTrialMapper singleTrialMapper = sqlSession.getMapper(SingleTrialMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key"));
                        if (soft == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else if (soft.getUserId().intValue() == sysUser.getId().intValue()) {
                            int value = jsonObject.getIntValue("flag");
                            if (value == 0)
                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                            else {
                                switch (value) {
                                    /**
                                     * 试用信息获取
                                     */
                                    case 1: {
                                        List<SingleTrial> trials = singleTrialMapper.findOffSetTrials(soft.getId(), jsonObject.getInteger("offset"), jsonObject.getInteger("limit"));
                                        Writr(new Result(ResultBasic.GETSUCCESS, trials, languageEnums), charset);
                                    }
                                    break;
                                    /**
                                     * 删除试用信息
                                     */
                                    case 2: {
                                        TypeToken<List<SingleTrial>> listTypeToken = new TypeToken<List<SingleTrial>>() {
                                        };
                                        Type listType = listTypeToken.getType();
                                        List<SingleTrial> trials = new Gson().fromJson(
                                                new String(
                                                        Base64.getDecoder().decode(
                                                                jsonObject.getString("info").getBytes()),
                                                        StandardCharsets.UTF_8), listType);
                                        for (SingleTrial trial : trials) {
                                            singleTrialMapper.deleteByMacAndSoftId(trial.getMac(), soft.getId());
                                        }
                                        Writr(new Result(ResultBasic.DELETESUCCESS, null, languageEnums), charset);
                                    }
                                    break;
                                    /**
                                     * 更新试用次数
                                     */
                                    case 3: {
                                        if (sysUser.getExpireTime().getTime() < System.currentTimeMillis())
                                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                        else {
                                            TypeToken<List<SingleTrial>> listTypeToken = new TypeToken<List<SingleTrial>>() {
                                            };
                                            Type listType = listTypeToken.getType();
                                            List<SingleTrial> trials = new Gson().fromJson(
                                                    new String(
                                                            Base64.getDecoder().decode(
                                                                    jsonObject.getString("info").getBytes()),
                                                            StandardCharsets.UTF_8), listType);
                                            for (SingleTrial trial : trials) {
                                                SingleTrial singleTrial = singleTrialMapper.findSoftAndMac(soft.getId(), trial.getMac());
                                                if (singleTrial != null) {
                                                    singleTrial.setCount(trial.getCount());
                                                    singleTrialMapper.updateByPrimaryKey(singleTrial);
                                                }
                                            }
                                            Writr(new Result(ResultBasic.SAVESUCCESS, null, languageEnums), charset);
                                        }
                                    }
                                    break;
                                    default:
                                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                }
                            }
                        } else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 删除软件
             */
            case 3100: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
                    SingleTrialMapper singleTrialMapper = sqlSession.getMapper(SingleTrialMapper.class);
                    SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
                    SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
                    SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(jsonObject.getString("token").replaceAll("\\s+", ""));
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else {
                        UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key"));
                        if (soft == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else if (soft.getUserId().intValue() == sysUser.getId().intValue()) {
                            if (of(userSoftMapper.deleteByPrimaryKey(soft.getId()))) {
                                remoteNoticeMapper.deleteBySoftId(soft.getId());
                                singleVerifyMapper.deleteBySoftId(soft.getId());
                                singleCardMapper.deleteBySoftId(soft.getId());
                                singleTrialMapper.deleteBySoftId(soft.getId());
                                softUpdateMapper.deleteBySoftId(soft.getId());
                                softCustomMapper.deleteBySoftId(soft.getId());
                                softAdmobMapper.deleteBySoftId(soft.getId());
                                Writr(new Result(ResultBasic.DELETESUCCESS, null, languageEnums), charset);
                            } else
                                Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        } else
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 任务校验
             */
            case 550: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    /**
                     * 校验设备防火墙
                     */
                    {
                        RedisUtil redisUtil = RedisUtil.getRedisUtil();
                        Set<String> set = null;
                        if (redisUtil.exists("device"))
                            set = redisUtil.sunion("device");
                        if (set != null) {
                            if (set.contains(jsonObject.getString("android_id"))) {
                                throw new IOException("非法设备访问:" + jsonObject.getString("android_id"));
                            }
                        }
                    }
                    long flags = jsonObject.getLongValue("type");
                    String token = jsonObject.getString("token").replaceAll("\\s+", "");
                    String rule = jsonObject.getString("rule");
                    String pack = jsonObject.getString("p");
                    String task_md5 = jsonObject.getString("task_md5");
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(token);
                    /**
                     * 校验用户信息
                     */
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    /**
                     * 缓存任务
                     */
                    else if (of_cache(task_md5)
                            && of_vip(flags, sysUser, languageEnums, charset)) {
                        long start = System.currentTimeMillis();
                        String uuid = UUID.randomUUID().toString();
                        try (FileOutputStream outputStream = new FileOutputStream(new File(Constant.getTask(), uuid))) {
                            File cacheFile = new File(Constant.getCache(), task_md5);
                            outputStream.write(StreamUtil.readBytes(new FileInputStream(cacheFile)));
                            List<TaskInfo> taskInfos = new ArrayList<>();
                            taskInfos.add(new TaskInfo(300, Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.wait"))));
                            taskInfos.add(new TaskInfo(100, Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.start"))));
                            taskInfos.add(new TaskInfo(200, String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.cache.success")), (float) (System.currentTimeMillis() - start) / 1000)));
                            RedisUtil redisUtil = RedisUtil.getRedisUtil();
                            redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(taskInfos));
                            taskInfos.clear();
                            JSONObject object = new JSONObject();
                            object.put("uuid", uuid);
                            object.put("cache", true);
                            Writr(new Result(ResultBasic.GETSUCCESS, object, languageEnums), charset);
                        }
                    }
                    /**
                     * 新任务
                     */
                    else if (of_shell(flags, languageEnums, charset)
                            && of_vip(flags, sysUser, languageEnums, charset)
                            && of_package(pack, sysUser, languageEnums, charset)
                            && of_dex2c(flags, rule, languageEnums, charset)
                            && of_task_count(sysUser, languageEnums, charset)
                            && of_method2native(flags, rule, languageEnums, charset)
                            && of_device_count(sysUser, jsonObject.getString("android_id"), languageEnums, charset)) {
                        ActionUtils actionUtils = new ActionUtils(languageEnums);
                        if (actionUtils.isSet(flags, 0X1) || actionUtils.isSet(flags, 0x200000000L)) {
                            if (of_shell_miss(task_md5, languageEnums, charset)) {
                                String task_id = UUID.randomUUID().toString();
                                Writr(QiniuUtils.getInstance().createTask(task_id, languageEnums), charset);
                            }
                        } else if (SysConfigUtil.getBooleanConfig("LocalUpload")) {
                            String task_id = UUID.randomUUID().toString();
                            JSONObject object = new JSONObject();
                            object.put("token", "LocalUpload");
                            object.put("uuid", task_id);
                            Writr(new Result(ResultBasic.GETSUCCESS, object, languageEnums), charset);
                        } else {
                            String task_id = UUID.randomUUID().toString();
                            Writr(QiniuUtils.getInstance().createTask(task_id, languageEnums), charset);
                        }
                    }
                }
            }
            break;
            /**
             * 任务处理
             * 生成七牛云上传Token
             */
            case 520: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    long flags = jsonObject.getLongValue("type");
                    String token = jsonObject.getString("token").replaceAll("\\s+", "");
                    String rule = jsonObject.getString("rule");
                    String pack = jsonObject.getString("p");
                    String task_md5 = jsonObject.getString("task_md5");
                    String task_id = jsonObject.getString("uuid");
                    SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                    SysUser sysUser = sysUserMapper.findTokenUser(token);
                    /**
                     * 校验用户信息
                     */
                    if (sysUser == null)
                        Writr(new Result(ResultBasic.USERNOTEXISTS, null, languageEnums), charset);
                    else if (of_shell(flags, languageEnums, charset)
                            && of_vip(flags, sysUser, languageEnums, charset)
                            && of_package(pack, sysUser, languageEnums, charset)
                            && of_dex2c(flags, rule, languageEnums, charset)
                            && of_task_md5(task_md5, languageEnums, charset)
                            && of_method2native(flags, rule, languageEnums, charset)) {
                        List<TaskInfo> taskInfos = new ArrayList<>();
                        taskInfos.add(new TaskInfo(300, Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.wait"))));
                        RedisUtil redisUtil = RedisUtil.getRedisUtil();
                        redisUtil.setex(task_id, 60 * 60 * 24, JSONArray.toJSONString(taskInfos));
                        Writr(new Result(ResultBasic.VERIFYSUCCESS, task_id, languageEnums), charset);
                        TaskAction taskthread = new TaskAction(
                                flags,
                                rule,
                                taskInfos,
                                task_id,
                                languageEnums,
                                task_md5,
                                sysUser);
                        taskthread.setPriority(sysUser.getExpireTime().getTime() > new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2030-01-01 00:00:00").getTime() ? 1 : 0);
                        ActionUtils actionUtils = new ActionUtils(languageEnums);
                        logger.info("******************************************************************");
                        logger.info("*" + String.format(" IP:%s:%d", inetSocketAddress.getAddress().getHostAddress(), inetSocketAddress.getPort()));
                        logger.info("*" + String.format(" 请求时间:%s", new SimpleDateFormat("MM/dd(HH:mm:ss)").format(new Date(jsonObject.getLong("uid")))));
                        logger.info("*" + String.format(" 设备信息:%s(%s)", jsonObject.getString("status_machine"), jsonObject.getString("android_id")));
                        logger.info("*" + String.format(" 软件信息:%s(%d)", jsonObject.getString("u"), jsonObject.getInteger("version")));
                        logger.info("*" + String.format(" 用户:%d(%S):%s", sysUser.getId(), sysUser.getOpenid(), jsonObject.getString("u_name")));
                        logger.info("*" + String.format(" 任务:%s", Arrays.toString(actionUtils.getFlags(flags))));
                        logger.info("*" + String.format(" 任务:%s:%s", task_id, pack));
                        logger.info("******************************************************************");
                        Constant.getTask_map().put(task_id, taskthread);
                        if (actionUtils.isSet(flags, Long.parseLong(SysConfigUtil.getStringConfig("verify.flags"))))
                            new Thread(taskthread).start();
                        else
                            Constant.getTask_pool().execute(taskthread);
                    }
                }
            }
            break;
            /**
             * 获取任务信息
             */
            case 9000: {
                RedisUtil redisUtil = RedisUtil.getRedisUtil();
                if (redisUtil.exists(jsonObject.getString("uuid")))
                    Writr(new Result(ResultBasic.GETSUCCESS, JSONObject.parse(
                            redisUtil.get(jsonObject.getString("uuid"))), languageEnums), charset);
                else
                    Writr(new Result(ResultBasic.UUIDNULL, null, languageEnums), charset);
            }
            break;
            /**
             * 强行终止任务
             */
            case 9002: {
                String uuid = jsonObject.getString("uuid");
                File task = new File(Constant.getTask(), uuid);
                if (task.exists())
                    task.delete();
                RedisUtil redisUtil = RedisUtil.getRedisUtil();
                if (redisUtil.exists(uuid))
                    redisUtil.del(uuid);
                if (Constant.getTask_map().containsKey(uuid)) {
                    TaskAction runnable = (TaskAction) Constant.getTask_map().get(uuid);
                    runnable.Cancel();
                }
                ctx.close();
            }
            break;
            /**
             * 获取指定KEY应用信息
             */
            case 20000: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    RemoteNoticeMapper remoteNoticeMapper = sqlSession.getMapper(RemoteNoticeMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    SoftUpdateMapper softUpdateMapper = sqlSession.getMapper(SoftUpdateMapper.class);
                    SoftCustomMapper softCustomMapper = sqlSession.getMapper(SoftCustomMapper.class);
                    SoftAdmobMapper softAdmobMapper = sqlSession.getMapper(SoftAdmobMapper.class);
                    UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key").replaceAll("\\s+", ""));
                    if (soft == null)
                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    else {
                        SoftResult result = new SoftResult(soft.getHandle(), soft.getVersion());
                        SoftEnums[] flags = SoftEnums.getFlags(soft.getHandle());
                        for (SoftEnums flag : flags) {
                            switch (flag) {
                                case SingleVerify:
                                    result.setSingleVerify(singleVerifyMapper.selectBySoftId(soft.getId()));
                                    break;
                                case Notice:
                                    result.setRemoteNotice(remoteNoticeMapper.selectBySoftId(soft.getId()));
                                    break;
                                case Update:
                                    result.setSoftUpdate(softUpdateMapper.selectBySoftId(soft.getId()));
                                    break;
                                case CustomModule:
                                    result.setSoftCustom(softCustomMapper.selectBySoftId(soft.getId()));
                                    break;
                                case Admob:
                                    result.setSoftAdmob(softAdmobMapper.selectBySoftId(soft.getId()));
                                    break;
                            }
                        }
                        if (SysConfigUtil.getBooleanConfig("statistics"))
                            Constant.getStatisticsExec().submit(new Statistics(soft));
                        Writr(new Result(ResultBasic.GETSUCCESS, result, languageEnums), charset);
                    }
                }
            }
            break;
            /**
             * 单码验证
             */
            case 20001: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key").replaceAll("\\s+", ""));
                    if (soft == null)
                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    else {
                        SingleVerify singleVerify = singleVerifyMapper.selectBySoftId(soft.getId());
                        if (singleVerify == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else {
                            String mac = jsonObject.getString("mac");
                            String card = jsonObject.getString("card").replaceAll("\\s+", "");
                            SingleCard singleCard = singleCardMapper.findSoftAndCard(soft.getId(), card);
                            /**
                             * 不存在
                             */
                            if (singleCard == null)
                                Writr(new Result(ResultBasic.SINGLECARDNOTEXISTS, null, languageEnums), charset);
                            /**
                             * 设备码为空
                             */
                            else if (mac == null || mac.isEmpty())
                                Writr(new Result(ResultBasic.SINGLECARDMACFAIL, null, languageEnums), charset);
                            /**
                             * 卡密被冻结
                             */
                            else if (!singleCard.getUsable())
                                Writr(new Result(ResultBasic.SINGLECARDFROZEN, null, languageEnums), charset);
                            /**
                             * 设备码不同
                             */
                            else if (singleCard.getUsrTime() != null
                                    && singleCard.getMac() != null
                                    && !mac.equals(singleCard.getMac())
                                    && (singleVerify.getBindMode() == 0 || singleVerify.getBindMode() == 1))
                                Writr(new Result(ResultBasic.SINGLECARDMACBINDFAIL, null, languageEnums), charset);
                            else {
                                if (singleCard.getUsrTime() == null)
                                    singleCard.setUsrTime(new Date(System.currentTimeMillis()));
                                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(singleCard.getUsrTime().getTime()), ZoneId.systemDefault());
                                switch (singleCard.getType()) {
                                    case 1:
                                        localDateTime = localDateTime.plusMinutes(singleCard.getValue());
                                        break;
                                    case 2:
                                        localDateTime = localDateTime.plusHours(singleCard.getValue());
                                        break;
                                    case 3:
                                        localDateTime = localDateTime.plusDays(singleCard.getValue());
                                        break;
                                    case 4:
                                        localDateTime = localDateTime.plusWeeks(singleCard.getValue());
                                        break;
                                    case 5:
                                        localDateTime = localDateTime.plusMonths(singleCard.getValue());
                                        break;
                                    case 6:
                                        localDateTime = localDateTime.plusYears(singleCard.getValue());
                                        break;
                                }
                                ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
                                Date date = Date.from(zdt.toInstant());
                                /**
                                 * 已到期
                                 */
                                if (date.getTime() < System.currentTimeMillis())
                                    Writr(new Result(ResultBasic.SINGLECARDEXPIRED, null, languageEnums), charset);
                                else {
                                    String token = null;
                                    switch (singleVerify.getBindMode()) {
                                        case 0:
                                            token = SHAUtils.SHA1(mac + card + System.currentTimeMillis()).substring(0, 32);
                                            break;
                                        case 1:
                                            token = SHAUtils.SHA1(mac + card).substring(0, 32);
                                            break;
                                        case 2:
                                            token = SHAUtils.SHA1(card).substring(0, 32);
                                            break;
                                    }
                                    singleCard.setToken(token);
                                    singleCard.setMac(mac);
                                    singleCard.setUsrCount(singleCard.getUsrCount() + 1);
                                    if (of(singleCardMapper.updateByPrimaryKey(singleCard)))
                                        Writr(new Result(ResultBasic.VERIFYSUCCESS, new SingleResult(token, date, card, 1), languageEnums), charset);
                                    else
                                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                }
                            }
                        }
                    }
                }
            }
            break;
            /**
             * 单码查询
             */
            case 20002: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key").replaceAll("\\s+", ""));
                    if (soft == null)
                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    else {
                        SingleVerify singleVerify = singleVerifyMapper.selectBySoftId(soft.getId());
                        if (singleVerify == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else {
                            String card = jsonObject.getString("card").replaceAll("\\s+", "");
                            SingleCard singleCard = singleCardMapper.findSoftAndCard(soft.getId(), card);
                            if (singleCard == null)
                                Writr(new Result(ResultBasic.SINGLECARDNOTEXISTS, null, languageEnums), charset);
                            else
                                Writr(new Result(ResultBasic.GETSUCCESS, singleCard, languageEnums), charset);
                        }
                    }
                }
            }
            break;
            /**
             * Token校验
             */
            case 20003: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SingleCardMapper singleCardMapper = sqlSession.getMapper(SingleCardMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key").replaceAll("\\s+", ""));
                    if (soft == null)
                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    else {
                        SingleVerify singleVerify = singleVerifyMapper.selectBySoftId(soft.getId());
                        if (singleVerify == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else {
                            String mac = jsonObject.getString("mac");
                            String token = jsonObject.getString("token").replaceAll("\\s+", "");
                            Integer token_type = jsonObject.getInteger("type");
                            /**
                             * 单码验证
                             */
                            if (token_type == null || token_type == 1) {
                                SingleCard singleCard = singleCardMapper.findSoftAndToken(soft.getId(), token);
                                if (singleCard == null)
                                    Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                else if (!mac.equals(singleCard.getMac()))
                                    Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                else {
                                    if (singleCard.getUsrTime() == null)
                                        singleCard.setUsrTime(new Date(System.currentTimeMillis()));
                                    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(singleCard.getUsrTime().getTime()), ZoneId.systemDefault());
                                    switch (singleCard.getType()) {
                                        case 1:
                                            localDateTime = localDateTime.plusMinutes(singleCard.getValue());
                                            break;
                                        case 2:
                                            localDateTime = localDateTime.plusHours(singleCard.getValue());
                                            break;
                                        case 3:
                                            localDateTime = localDateTime.plusDays(singleCard.getValue());
                                            break;
                                        case 4:
                                            localDateTime = localDateTime.plusWeeks(singleCard.getValue());
                                            break;
                                        case 5:
                                            localDateTime = localDateTime.plusMonths(singleCard.getValue());
                                            break;
                                        case 6:
                                            localDateTime = localDateTime.plusYears(singleCard.getValue());
                                            break;
                                    }
                                    ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
                                    Date date = Date.from(zdt.toInstant());
                                    if (date.getTime() < System.currentTimeMillis())
                                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                    else
                                        Writr(new Result(ResultBasic.VERIFYSUCCESS, null, languageEnums), charset);
                                }
                            }
                            /**
                             * 试用验证
                             */
                            else if (token_type == 2) {
                                SingleTrialMapper singleTrialMapper = sqlSession.getMapper(SingleTrialMapper.class);
                                SingleTrial trial = singleTrialMapper.findSoftIdAndToken(soft.getId(), token);
                                if (trial == null)
                                    Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                else if (!mac.equals(trial.getMac()))
                                    Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                else {
                                    long all_time = trial.getLastTime().getTime() + (singleVerify.getTryMinutes() * 1000 * 60);
                                    if (System.currentTimeMillis() > all_time)
                                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                                    else
                                        Writr(new Result(ResultBasic.VERIFYSUCCESS, null, languageEnums), charset);
                                }
                            }
                        }
                    }
                }
            }
            break;
            /**
             * 试用
             */
            case 20004: {
                try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                    String mac = jsonObject.getString("mac");
                    UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                    SingleVerifyMapper singleVerifyMapper = sqlSession.getMapper(SingleVerifyMapper.class);
                    UserSoft soft = userSoftMapper.findSoftKey(jsonObject.getString("key").replaceAll("\\s+", ""));
                    if (soft == null)
                        Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                    else {
                        SingleVerify singleVerify = singleVerifyMapper.selectBySoftId(soft.getId());
                        if (singleVerify == null)
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else if (mac == null || mac.isEmpty())
                            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                        else if (singleVerify.getTryCount() <= 0 || singleVerify.getTryMinutes() <= 0)
                            Writr(new Result(ResultBasic.SINGLENOTTRIAL, null, languageEnums), charset);
                        else {
                            SingleTrialMapper singleTrialMapper = sqlSession.getMapper(SingleTrialMapper.class);
                            SingleTrial trial = singleTrialMapper.findSoftIdAndMac(soft.getId(), mac);
                            String token = SHAUtils.SHA1(mac + System.currentTimeMillis()).substring(0, 32);
                            long all_time = singleVerify.getTryMinutes() * 1000 * 60;
                            if (trial == null) {
                                SingleTrial singleTrial = new SingleTrial();
                                singleTrial.setCount(1);
                                singleTrial.setMac(mac);
                                singleTrial.setSoftId(soft.getId());
                                singleTrial.setToken(token);
                                singleTrial.setLastTime(new Date());
                                if (of(singleTrialMapper.insert(singleTrial)))
                                    Writr(new Result(ResultBasic.VERIFYSUCCESS, new SingleResult(token, new Date(System.currentTimeMillis() + all_time), mac, 2), languageEnums), charset);
                                else
                                    Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                            } else if (trial.getCount() >= singleVerify.getTryCount())
                                Writr(new Result(ResultBasic.SINGLETRIALMAX, null, languageEnums), charset);
                            else {
                                trial.setCount(trial.getCount() + 1);
                                trial.setToken(token);
                                trial.setLastTime(new Date());
                                if (of(singleTrialMapper.updateByPrimaryKey(trial)))
                                    Writr(new Result(ResultBasic.VERIFYSUCCESS, new SingleResult(token, new Date(System.currentTimeMillis() + all_time), mac, 2), languageEnums), charset);
                                else
                                    Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
                            }
                        }
                    }
                }
            }
            break;
            default:
                throw new Exception(String.format("Client -> %s -> 错误数据Type -> %d", inetSocketAddress.getAddress().getHostAddress(), type));
        }
    }

    /**
     * 回写数据
     *
     * @param result
     * @param charset
     */
    public void Writr(Result result, Charset charset) {
        try {
            RSAUtils rsaUtils = new RSAUtils();
            byte[] body = rsaUtils.encrypt(JSONObject.toJSONString(result).getBytes(charset), rsaUtils.getPrivateKey(Base64.getDecoder().decode(LoaderRes.getInstance().getStaticResAsBytes("rsa/en_private.txt"))));
            ByteBuf byteBuf = ctx.alloc().buffer(body.length + 9 + 4);
            byteBuf
                    .writeBytes("Armadillo".getBytes())
                    .writeInt(body.length)
                    .writeBytes(body);
            ctx.writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.close();
        }
    }

    /**
     * 判断数据库执行返回值
     *
     * @param type
     * @return
     */
    private boolean of(int type) {
        return type == 1;
    }

    /**
     * 判断是否是非法包名
     *
     * @param name
     * @param sysUser
     * @param languageEnums
     * @param charset
     * @return
     */
    private boolean of_package(String name, SysUser sysUser, LanguageEnums languageEnums, Charset charset) {
        if (name == null
                || Lists.newArrayList(Objects.requireNonNull(SysConfigUtil.getConfigUtil("Illegal.package"))
                .split(","))
                .contains(name)) {
            logger.info(
                    String.format("用户(%d):%s 非法APK包名:%s"
                            , sysUser.getId()
                            , sysUser.getOpenid()
                            , name));
            Writr(new Result(ResultBasic.ILLEGALPACK, null, languageEnums).setMsg(name), charset);
            return false;
        }
        return true;
    }

    /**
     * 校验任务对应的VIP等级
     *
     * @param flags
     * @param sysUser
     * @param languageEnums
     * @param charset
     * @return
     * @throws IOException
     */
    private boolean of_vip(long flags, SysUser sysUser, LanguageEnums languageEnums, Charset charset) throws IOException {
        for (ActionUtils.ActionFlag flag : new ActionUtils(languageEnums).getFlags(flags)) {
            if (flag.getV() > 0) {
                if (sysUser.getExpireTime().getTime() < System.currentTimeMillis()
                        || sysUser.getValue() < flag.getV()) {
                    Writr(new Result(flag.getResult(), null, languageEnums), charset);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断是否是Dex2C任务,并校验类是否超标
     *
     * @param flags
     * @param rule
     * @param languageEnums
     * @param charset
     * @return
     * @throws IOException
     */
    private boolean of_dex2c(long flags, String rule, LanguageEnums languageEnums, Charset charset) throws IOException {
        ActionUtils actionUtils = new ActionUtils(languageEnums);
        String[] dex2c_flags = SysConfigUtil.getStringConfig("dex2c.flags").split(",");
        if (actionUtils.isSet(dex2c_flags, flags)) {
            if (rule == null || rule.isEmpty()) {
                Writr(new Result(ResultBasic.CLASSOBIGFAIL, null, languageEnums).setMsg(0), charset);
                return false;
            }
            JsonArray separate = new JsonParser()
                    .parse(new String(Base64.getDecoder().decode(rule.getBytes()), StandardCharsets.UTF_8))
                    .getAsJsonObject()
                    .getAsJsonArray(actionUtils.getValues(dex2c_flags, flags));
            int max = SysConfigUtil.getIntConfig("dex2c.class.max");
            if (separate.size() > max) {
                Writr(new Result(ResultBasic.CLASSOBIGFAIL, null, languageEnums).setMsg(separate.size() - max, max), charset);
                return false;
            }
            return true;
        } else
            return true;
    }

    /**
     * 判断是否是SE任务,并校验类是否超标
     *
     * @param flags
     * @param rule
     * @param languageEnums
     * @param charset
     * @return
     * @throws IOException
     */
    private boolean of_method2native(long flags, String rule, LanguageEnums languageEnums, Charset charset) throws IOException {
        ActionUtils actionUtils = new ActionUtils(languageEnums);
        String[] se_flags = SysConfigUtil.getStringConfig("se.flags").split(",");
        if (actionUtils.isSet(se_flags, flags)) {
            if (rule == null || rule.isEmpty()) {
                Writr(new Result(ResultBasic.CLASSOBIGFAIL, null, languageEnums).setMsg(0), charset);
                return false;
            }
            JsonArray separate = new JsonParser()
                    .parse(new String(Base64.getDecoder().decode(rule.getBytes()), StandardCharsets.UTF_8))
                    .getAsJsonObject()
                    .getAsJsonArray(actionUtils.getValues(se_flags, flags));
            int max = SysConfigUtil.getIntConfig("se.class.max");
            if (separate.size() > max) {
                Writr(new Result(ResultBasic.CLASSOBIGFAIL, null, languageEnums).setMsg(separate.size() - max, max), charset);
                return false;
            }
            return true;
        } else
            return true;
    }

    /**
     * 判断是否是脱壳任务，并校验是否存在脱壳设备
     *
     * @param flags
     * @param languageEnums
     * @param charset
     * @return
     * @throws IOException
     */
    private boolean of_shell(long flags, LanguageEnums languageEnums, Charset charset) throws IOException {
        ActionUtils actionUtils = new ActionUtils(languageEnums);
        if (actionUtils.isSet(Long.parseLong(SysConfigUtil.getStringConfig("shell.flags")), flags) && Application.getYoupkSet().size() == 0) {
            Writr(new Result(ResultBasic.SHELLFALL, null, languageEnums), charset);
            return false;
        } else if (actionUtils.isSet(Long.parseLong(SysConfigUtil.getStringConfig("xposed.flags")), flags) && Application.getXposedSet().size() == 0) {
            Writr(new Result(ResultBasic.XPOSEDSHELLFALL, null, languageEnums), charset);
            return false;
        } else
            return true;
    }

    /**
     * 校验是否存在任务MD5
     *
     * @param md5
     * @param languageEnums
     * @param charset
     * @return
     */
    private boolean of_task_md5(String md5, LanguageEnums languageEnums, Charset charset) {
        if (md5 == null
                || md5.isEmpty()) {
            Writr(new Result(ResultBasic.UNKNOWNFAIL, null, languageEnums), charset);
            return false;
        }
        return true;
    }

    /**
     * 判断今日任务量
     *
     * @param sysUser
     * @param languageEnums
     * @param charset
     * @return
     */
    private boolean of_task_count(SysUser sysUser, LanguageEnums languageEnums, Charset charset) {
        RedisUtil redisUtil = RedisUtil.getRedisUtil();
        if (!redisUtil.exists((sysUser.getOpenid() == null || sysUser.getOpenid().isEmpty()) ? sysUser.getUsername() : sysUser.getOpenid())) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            long start = calendar.getTimeInMillis();
            long seconds = (start - System.currentTimeMillis()) / 1000;
            redisUtil.setex((sysUser.getOpenid() == null || sysUser.getOpenid().isEmpty()) ? sysUser.getUsername() : sysUser.getOpenid(), (int) seconds, "1");
            return true;
        } else {
            int count = redisUtil.incr((sysUser.getOpenid() == null || sysUser.getOpenid().isEmpty()) ? sysUser.getUsername() : sysUser.getOpenid()).intValue();
            int v = sysUser.getValue();
            if ((v == 0 && count > SysConfigUtil.getIntConfig("v0"))
                    || (v < 100 && count > SysConfigUtil.getIntConfig("v1"))
                    || (v < 300 && count > SysConfigUtil.getIntConfig("v2"))
                    || (v < 600 && count > SysConfigUtil.getIntConfig("v3"))
                    || (v < 1000 && count > SysConfigUtil.getIntConfig("v4"))
                    || (v < 1500 && count > SysConfigUtil.getIntConfig("v5"))
                    || (v < 2100 && count > SysConfigUtil.getIntConfig("v6"))
                    || (v < 2800 && count > SysConfigUtil.getIntConfig("v7"))
                    || (v < 3600 && count > SysConfigUtil.getIntConfig("v8"))
                    || (v < 4500 && count > SysConfigUtil.getIntConfig("v9"))
                    || (v > 4500 && count > SysConfigUtil.getIntConfig("v10"))) {
                Writr(new Result(ResultBasic.TASKMAX, null, languageEnums), charset);
                return false;
            } else
                return true;
        }
    }

    /**
     * 判断账户当日登录设备的数量
     *
     * @param sysUser
     * @param android_id
     * @param languageEnums
     * @param charset
     * @return
     */
    private boolean of_device_count(SysUser sysUser, String android_id, LanguageEnums languageEnums, Charset charset) {
        RedisUtil redisUtil = RedisUtil.getRedisUtil();
        String key = (sysUser.getOpenid() == null || sysUser.getOpenid().isEmpty()) ? sysUser.getUsername() + "-device" : sysUser.getOpenid() + "-device";
        if (!redisUtil.exists(key)) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            long start = calendar.getTimeInMillis();
            long seconds = (start - System.currentTimeMillis()) / 1000;
            redisUtil.sadd(key, android_id);
            redisUtil.expire(key, (int) seconds);
            return true;
        } else {
            Set<String> set = redisUtil.sunion(key);
            if (set.size() < SysConfigUtil.getIntConfig("device.max"))
                redisUtil.sadd(key, android_id);
            else if (set.contains(android_id))
                return true;
            else {
                Writr(new Result(ResultBasic.DEVICEMAX, null, languageEnums), charset);
                return false;
            }
            return true;
        }
    }

    /**
     * 判断是否存在缓存
     *
     * @param task_md5
     * @return
     */
    private boolean of_cache(String task_md5) {
        File cacheFile = new File(Constant.getCache(), task_md5);
        return cacheFile.exists();
    }

    /**
     * 判断脱壳MD5是否忽略
     *
     * @param task_md5
     * @param languageEnums
     * @param charset
     * @return
     */
    private boolean of_shell_miss(String task_md5, LanguageEnums languageEnums, Charset charset) {
        RedisUtil redisUtil = RedisUtil.getRedisUtil();
        Set<String> set = redisUtil.sunion("shell_miss");
        if (set == null)
            return true;
        if (set.contains(task_md5)) {
            Writr(new Result(ResultBasic.SHELLMISS, null, languageEnums), charset);
            return false;
        }
        return true;
    }

    /**
     * 统计数据线程
     */
    private class Statistics implements Runnable {
        private final UserSoft soft;

        public Statistics(UserSoft soft) {
            this.soft = soft;
        }

        @Override
        public void run() {
            RedisUtil redisUtil = RedisUtil.getRedisUtil();
            String min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DateTimeFormatter.ofPattern("MM/dd"));
            RLock rLock = redisUtil.getRedissonClient().getLock("lock_" + soft.getAppkey());
            try {
                if (rLock.tryLock(5, 3, TimeUnit.SECONDS)) {
                    /**
                     * 设置启动次数
                     */
                    if (redisUtil.exists(soft.getAppkey() + "-start-" + min))
                        redisUtil.incr(soft.getAppkey() + "-start-" + min);
                    else {
                        /**
                         * 设置一周后过期
                         */
                        redisUtil.setex(soft.getAppkey() + "-start-" + min, 60 * 60 * 24 * 5, "1");
                    }
                    /**
                     * 设置用户数量
                     */
                    if (redisUtil.exists(soft.getAppkey() + "-count-" + min))
                        redisUtil.sadd(soft.getAppkey() + "-count-" + min, inetSocketAddress.getAddress().getHostAddress());
                    else {
                        /**
                         * 设置一周后过期
                         */
                        redisUtil.sadd(soft.getAppkey() + "-count-" + min, inetSocketAddress.getAddress().getHostAddress());
                        redisUtil.expire(soft.getAppkey() + "-count-" + min, 60 * 60 * 24 * 5);
                    }
                }
            } catch (InterruptedException lock) {
                lock.printStackTrace();
            } finally {
                rLock.unlock();
            }
        }
    }
}
