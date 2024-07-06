package armadillo;


import armadillo.common.SimpleNameFactory;
import armadillo.decoder.SmartByteBufDecoder;
import armadillo.helper.ServerHandler;
import armadillo.mapper.SysCardMapper;
import armadillo.mapper.SysUserMapper;
import armadillo.model.SysCard;
import armadillo.model.SysUser;
import armadillo.shell.ShellHelper;
import armadillo.utils.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4JLoggerFactory;
import org.apache.commons.cli.*;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;


public class Application {
    private final static Logger logger = Logger.getLogger(Application.class);
    private final static Options options = new Options();
    private final static ServerBootstrap bootstrap = new ServerBootstrap();
    private final static HashSet<ShellHelper> YoupkSet = new HashSet<>();
    private final static HashSet<ShellHelper> XposedSet = new HashSet<>();
    private final static HashMap<Integer, ChannelFuture> bindServers = new HashMap<>();
    private static ServerSocket XposedServer;
    private static ServerSocket YoupkServer;
    private static EventLoopGroup bossGroup;
    private static EventLoopGroup workerGroup;
    private static EventLoopGroup handlerGroup;

    public static void main(String[] args) throws Exception {
        InitDebugLog();
        InitOptions();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("Armadillo", options, true);
            } else {
                String name = ManagementFactory.getRuntimeMXBean().getName();
                logger.info(String.format("PID:%s", name.split("@")[0]));
                logger.info("Armadillo -> " + Constant.getVER());
                logger.info("Copyright @ 2020 Armadillo Systems Incorporated. All rights reserved.");
                if (cmd.hasOption("http"))
                    Constant.httpport = Integer.parseInt(cmd.getOptionValue("http"));
                if (cmd.hasOption("sp"))
                    Constant.start = Integer.parseInt(cmd.getOptionValue("sp"));
                if (cmd.hasOption("ep"))
                    Constant.end = Integer.parseInt(cmd.getOptionValue("ep"));
                if (cmd.hasOption("io"))
                    InitNetty(1, Constant.handleCount = Integer.parseInt(cmd.getOptionValue("io")));
                else
                    InitNetty(1, Constant.handleCount);
                if (cmd.hasOption("s"))
                    Constant.youpkShell = Integer.parseInt(cmd.getOptionValue("s"));
                if (cmd.hasOption("x"))
                    Constant.xposedShell = Integer.parseInt(cmd.getOptionValue("x"));
                logger.info(String.format("Server Bind Port:(%d - %d)", Constant.start, Constant.end));
                logger.info(String.format("IO Handler Thread Pool:%d", Constant.handleCount));
                logger.info(String.format("Task Thread Pool:%d", Constant.getTask_pool().getActiveCount()));
                logger.info(String.format("Youpk Bind Port:%d", Constant.youpkShell));
                logger.info(String.format("Xposed Bind Port:%d", Constant.xposedShell));
                YoupkServer = new ServerSocket(Constant.youpkShell);
                YoupkServer.setReceiveBufferSize(1024 * 1024 * 20);
                XposedServer = new ServerSocket(Constant.xposedShell);
                XposedServer.setReceiveBufferSize(1024 * 1024 * 20);
                Constant.getRoot_pool().execute(Application::XposedShellRun);
                Constant.getRoot_pool().execute(Application::ShellRun);
                for (int i = 0; i <= Constant.end - Constant.start; i++)
                    Constant.getRoot_pool().execute(new bindServer(Constant.start + i, bootstrap));
                Constant.getRoot_pool().execute(new bindServer(Constant.httpport, bootstrap));
                InitDir();
                logger.info("Armadillo Start Success");
                InitOther();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            formatter.printHelp("Armadillo", options, true);
        }
    }

    private static class bindServer implements Runnable {
        private final int port;
        private final ServerBootstrap bootstrap;

        public bindServer(int port, ServerBootstrap bootstrap) {
            this.port = port;
            this.bootstrap = bootstrap;
        }

        @Override
        public void run() {
            try {
                logger.info(String.format("Bind Server >> %d", port));
                ChannelFuture f = bootstrap.bind(port).addListener(future -> {
                    if (future.isSuccess())
                        logger.info(String.format("Bind Server >> %d Success", port));
                }).sync();
                bindServers.put(port, f);
                f.channel().closeFuture().sync();
                logger.info(String.format("Port:%d Close ...", port));
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    private static void InitNetty(int bossThreads, int maxThreads) {
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(bossThreads, Constant.getBossFactory());
            workerGroup = new EpollEventLoopGroup(maxThreads, Constant.getWorkerFactory());
            handlerGroup = new EpollEventLoopGroup(maxThreads, Constant.getHandlerFactory());
        }else{
            bossGroup = new NioEventLoopGroup(bossThreads, Constant.getBossFactory());
            workerGroup = new NioEventLoopGroup(maxThreads, Constant.getWorkerFactory());
            handlerGroup = new NioEventLoopGroup(maxThreads, Constant.getHandlerFactory());
        }
        bootstrap.group(bossGroup, workerGroup)
                .channel(OsUtils.isOSLinux() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        if (ch.localAddress().getPort() == Constant.httpport) {
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(200 * 1024 * 1024));
                        } else {
                            ch.config().setReuseAddress(true);
                            ch.config().setKeepAlive(false);
                            ch.config().setTcpNoDelay(true);
                            ch.pipeline().addLast(new ReadTimeoutHandler(5));
                            ch.pipeline().addLast(new WriteTimeoutHandler(5));
                            ch.pipeline().addLast(new SmartByteBufDecoder());
                        }
                        ch.pipeline().addLast(handlerGroup, new ServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(EpollChannelOption.SO_REUSEPORT, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, false)
                .handler(new LoggingHandler(LogLevel.DEBUG));
    }

    private static void InitDebugLog() {
        PropertyConfigurator.configure(Application.class.getClassLoader().getResourceAsStream(Constant.getProfile() + "/log4j.properties"));
        if (Constant.isNettyDebug())
            InternalLoggerFactory.setDefaultFactory(Log4JLoggerFactory.INSTANCE);
    }

    private static void InitOptions() {
        options.addOption(Option
                .builder("http")
                .argName("prot(int)")
                .desc("http端口")
                .longOpt("httpport")
                .hasArg(true)
                .build());
        options.addOption(Option
                .builder("sp")
                .argName("prot(int)")
                .desc("端口起始")
                .longOpt("startprot")
                .hasArg(true)
                .build());
        options.addOption(Option
                .builder("ep")
                .argName("prot(int)")
                .desc("端口结束")
                .longOpt("endprot")
                .hasArg(true)
                .build());
        options.addOption(Option
                .builder("io")
                .argName("max(int)")
                .desc("IO工作线程并发最大量")
                .longOpt("iomax")
                .hasArg(true)
                .build());
        options.addOption(Option
                .builder("s")
                .argName("prot(int)")
                .desc("脱壳端口")
                .longOpt("shell")
                .hasArg(true)
                .build());
        options.addOption(Option
                .builder("x")
                .argName("prot(int)")
                .desc("Xposed脱壳端口")
                .longOpt("xshell")
                .hasArg(true)
                .build());
        options.addOption(Option
                .builder("h")
                .longOpt("help")
                .desc("帮助文档")
                .build());
    }

    private static void InitOther() {
        /**
         * 分布锁测试
         */
        //TestLock();
        /**
         * 成长值定时更新
         */
        {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date time = calendar.getTime();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    logger.info("开始更新成长值....");
                    try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                        SysUserMapper userMapper = sqlSession.getMapper(SysUserMapper.class);
                        for (SysUser user : userMapper.selectAll()) {
                            if (user.getExpireTime().getTime() > System.currentTimeMillis()) {
                                user.setValue(user.getValue() + 15);
                                userMapper.updateByPrimaryKey(user);
                            } else if (user.getValue() != 0) {
                                user.setValue(Math.max(user.getValue() - 5, 0));
                                userMapper.updateByPrimaryKey(user);
                            }
                        }
                    }
                }
            }, time, 1000 * 60 * 60 * 24);
        }
        /**
         * 指令监听
         */
        Scanner scanner = new Scanner(System.in);
        Options options = new Options();
        {
            /**
             * 用户拉黑参数
             */
            {
                options.addOption(Option
                        .builder("c")
                        .hasArg(true)
                        .argName("user-key")
                        .desc("拉黑用户KEY")
                        .longOpt("closure")
                        .build());
                options.addOption(Option
                        .builder("cid")
                        .hasArg(true)
                        .argName("user-id")
                        .desc("拉黑用户ID")
                        .longOpt("closure-id")
                        .build());
            }
            /**
             * 设备防火墙
             */
            {
                options.addOption(Option
                        .builder("d")
                        .hasArg(true)
                        .argName("device id")
                        .desc("拉黑设备ID")
                        .longOpt("device")
                        .build());
                options.addOption(Option
                        .builder("rd")
                        .hasArg(true)
                        .argName("device id")
                        .desc("解除设备ID")
                        .longOpt("remove-device")
                        .build());
                options.addOption(Option
                        .builder("ClosePort")
                        .hasArg(true)
                        .argName("Port")
                        .desc("关闭指定端口的监听")
                        .build());
                options.addOption(Option
                        .builder("ReBindPort")
                        .hasArg(true)
                        .argName("Port")
                        .desc("重新监听指定端口")
                        .build());
            }
            /**
             * 创建充值卡参数
             */
            {
                options.addOption(Option
                        .builder("CreateCard")
                        .longOpt("createCard")
                        .desc("创建充值卡")
                        .build());
                options.addOption(Option
                        .builder("CardCount")
                        .longOpt("CardCount")
                        .hasArg(true)
                        .argName("Count")
                        .desc("创建的卡密数量")
                        .build());
                options.addOption(Option
                        .builder("CardType")
                        .longOpt("CardType")
                        .hasArg(true)
                        .argName("Type")
                        .desc("月卡(1) 年卡(2) 试用永久天卡(3)")
                        .build());
                options.addOption(Option
                        .builder("CardValue")
                        .longOpt("CardValue")
                        .hasArg(true)
                        .argName("Value")
                        .desc("卡密面值")
                        .build());
                options.addOption(Option
                        .builder("CardOut")
                        .longOpt("CardOut")
                        .hasArg(true)
                        .argName("filename")
                        .desc("输出文件名")
                        .build());
            }
            /**
             * 数据库缓存参数
             */
            {
                options.addOption(Option
                        .builder("R")
                        .longOpt("Refresh")
                        .desc("刷新全局数据库缓存")
                        .build());
            }
            /**
             * 帮助信息
             */
            {
                options.addOption(Option
                        .builder("h")
                        .longOpt("help")
                        .desc("帮助信息")
                        .build());
            }
        }
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Armadillo", options, true);
        System.out.println("Waiting for input......");
        while (scanner.hasNext()) {
            String instruction = scanner.nextLine();
            try {
                CommandLine cmd = parser.parse(options, instruction.split("\\s+"));
                if (cmd.hasOption("h")) {
                    formatter.printHelp("Armadillo", options, true);
                }
                if (cmd.hasOption("c")) {
                    try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                        SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                        SysUser sysUser = sysUserMapper.findUser(cmd.getOptionValue("c"));
                        if (sysUser != null) {
                            sysUser.setExpireTime(new Date(System.currentTimeMillis()));
                            sysUser.setValue(0);
                            if (sysUserMapper.updateByPrimaryKey(sysUser) == 1)
                                System.out.println(String.format("User %s closure Success", sysUser.getOpenid()));
                            else
                                System.out.println(String.format("User %s closure Fail", sysUser.getOpenid()));
                        } else
                            System.out.println(String.format("User %s does not exist", cmd.getOptionValue("c")));
                    }
                }
                if (cmd.hasOption("d")) {
                    RedisUtil redisUtil = RedisUtil.getRedisUtil();
                    redisUtil.sadd("device", cmd.getOptionValue("d"));

                }
                if (cmd.hasOption("rd")) {
                    RedisUtil redisUtil = RedisUtil.getRedisUtil();
                    redisUtil.srem("device", cmd.getOptionValue("rd"));

                }
                if (cmd.hasOption("cid")) {
                    try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                        SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
                        SysUser sysUser = sysUserMapper.selectByPrimaryKey(Integer.parseInt(cmd.getOptionValue("cid")));
                        if (sysUser != null) {
                            sysUser.setExpireTime(new Date(System.currentTimeMillis()));
                            sysUser.setValue(0);
                            if (sysUserMapper.updateByPrimaryKey(sysUser) == 1)
                                System.out.println(String.format("User %s closure Success", sysUser.getOpenid()));
                            else
                                System.out.println(String.format("User %s closure Fail", sysUser.getOpenid()));
                        } else
                            System.out.println(String.format("UserId %s does not exist", cmd.getOptionValue("cid")));

                    }
                }
                if (cmd.hasOption("CreateCard")) {
                    int type = -1;
                    int value = -1;
                    int count = -1;
                    String filename = null;
                    if (cmd.hasOption("CardType"))
                        type = Integer.parseInt(cmd.getOptionValue("CardType"));
                    if (cmd.hasOption("CardCount"))
                        count = Integer.parseInt(cmd.getOptionValue("CardCount"));
                    if (cmd.hasOption("CardValue"))
                        value = Integer.parseInt(cmd.getOptionValue("CardValue"));
                    if (cmd.hasOption("CardOut"))
                        filename = cmd.getOptionValue("CardOut");
                    if (type < 0 || value < 0 || count < 0 || filename == null)
                        formatter.printHelp("Armadillo", options, true);
                    else if (count > 500000)
                        System.out.println("Too many prepaid cards");
                    else if (type != 1 && type != 2 && type != 3)
                        System.out.println("Wrong recharge card type");
                    else if (value > 100)
                        System.out.println("Recharge card face value is too large");
                    else {
                        long start = System.currentTimeMillis();
                        StringBuilder stringBuilder = new StringBuilder();
                        List<SysCard> sysCards = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            SysCard card = new SysCard();
                            card.setUsable(true);
                            card.setType(type);
                            card.setCount(value);
                            card.setCard(CardRadom.radomCard());
                            sysCards.add(card);
                            stringBuilder.append(card.getCard()).append("\n");
                        }
                        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                            SysCardMapper sysCardMapper = sqlSession.getMapper(SysCardMapper.class);
                            sysCardMapper.insertAll(sysCards);
                        }
                        try {
                            System.out.println(String.format("Insertion time -> %.2f seconds", (float) (System.currentTimeMillis() - start) / 1000));
                            FileOutputStream outputStream = new FileOutputStream(new File(Constant.getRoot(), filename));
                            outputStream.write(stringBuilder.toString().getBytes());
                            outputStream.close();
                            System.out.println("CreateCard Success");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (cmd.hasOption("R")) {
                    try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
                        sqlSession.clearCache();
                        for (Cache cach : sqlSession.getConfiguration().getCaches())
                            cach.clear();
                        System.out.println("Refresh the database cache Success ...");
                    }
                }
                if (cmd.hasOption("ClosePort")) {
                    int close_port = Integer.parseInt(cmd.getOptionValue("ClosePort"));
                    System.out.println(String.format("尝试关闭端口:%d", close_port));
                    if (bindServers.containsKey(close_port)) {
                        ChannelFuture future = bindServers.get(close_port);
                        future.channel().close().addListener(ChannelFutureListener.CLOSE);
                        future.awaitUninterruptibly();
                        bindServers.remove(close_port);
                        System.out.println(String.format("关闭端口:%d成功", close_port));
                    } else
                        System.out.println(String.format("端口:%d,已关闭监听", close_port));
                }
                if (cmd.hasOption("ReBindPort")) {
                    int rebind_port = Integer.parseInt(cmd.getOptionValue("ReBindPort"));
                    System.out.println(String.format("尝试重新监听端口:%d", rebind_port));
                    if (!bindServers.containsKey(rebind_port))
                        Constant.getRoot_pool().execute(new bindServer(rebind_port, bootstrap));
                    else
                        System.out.println(String.format("端口:%d,已监听", rebind_port));
                }
            } catch (ParseException e) {
                formatter.printHelp("Armadillo", options, true);
            }
        }
    }

    private static void TestLock() {
        {
            ExecutorService executors = Executors.newFixedThreadPool(100);
            for (int i = 0; i < 10000; i++) {
                executors.execute(() -> {
                    RedisUtil redisUtil = RedisUtil.getRedisUtil();

//                    {
//                        if (redisUtil.exists("test"))
//                            redisUtil.incr("test");
//                        else {
//                            redisUtil.setex("test", 60 * 60 * 24 * 5, "1");
//                        }
//                    }

                    {
                        RLock lock = redisUtil.getRedissonClient().getLock("test_lock");
                        try {
                            boolean res = lock.tryLock(10, 3, TimeUnit.SECONDS);
                            if (res) {
                                if (redisUtil.exists("test"))
                                    redisUtil.incr("test");
                                else {
                                    redisUtil.setex("test", 60 * 60 * 24 * 5, "1");
                                }
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        } finally {
                            lock.unlock();
                        }
                    }
                });
            }
        }

//        new Thread(() -> {
//            try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
//                CacheMapper mapper = sqlSession.getMapper(CacheMapper.class);
//                FileInputStream inputStream = new FileInputStream("F:\\QBang_F_8e_1105更新\\QBang_F_8e\\总库0.txt");
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//                int index = 0;
//                String buffer = null;
//                List<armadillo.model.Cache> caches = new ArrayList<>();
//                while ((buffer = reader.readLine()) != null) {
//                    index++;
//                    if (index < 200000)
//                        continue;
//                    String[] strings = buffer.split("----");
//                    try {
//                        caches.add(new armadillo.model.Cache(strings[0], strings[1]));
//                        if (caches.size() == 100000) {
//                            mapper.insertAll(caches);
//                            caches.clear();
//                        }
//                        //logger.info(String.format("导入:%s", strings[0]));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//                logger.info("导入完成");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }).start();

    }

    public static void InitDir() {
//        SysUser sysUser;
//        SysUser sysUser2;
//        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
//            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
//            sysUser = sysUserMapper.selectByPrimaryKey(1);
//        }
//        for (int i = 0; i < 10; i++) {
//            try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
//                SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
//                sysUser2 = sysUserMapper.selectByPrimaryKey(i);
//            }
//            if (sysUser == sysUser2)
//                logger.info("命中缓存");
//            else
//                logger.info("未命中");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException interruptedException) {
//                interruptedException.printStackTrace();
//            }
//        }
//        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
//            logger.info("开始插入");
//            byte[] bytes = StreamUtil.readBytes(new FileInputStream("C:\\Users\\Administrator\\Desktop\\users.json"));
//            Users users = new Gson().fromJson(new String(bytes), Users.class);
//            logger.info("大小:" + users.infos.size());
//            SysUserMapper sysUserMapper = sqlSession.getMapper(SysUserMapper.class);
//            for (Users.Infos info : users.infos) {
//                SysUser sysUser = new SysUser();
//                sysUser.setOpenid(info.openid);
//                sysUser.setLoginCount(info.login_count);
//                sysUser.setValue(info.v_value);
//                sysUser.setRegTime(new Date(info.reg_time));
//                sysUser.setExpireTime(new Date(info.expire_time));
//                sysUser.setToken(UUID.randomUUID().toString());
//                sysUserMapper.insert(sysUser);
//                logger.info("插入:" + info.openid);
//            }
//            logger.info("插入成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        if (OsUtils.isOSLinux())
            System.setProperty("project.dir", "/www/arm/");
        else
            System.setProperty("project.dir", new File(System.getenv("SYSTEMDRIVE"), "arm").getAbsolutePath());
        logger.info(String.format("工程数据目录:%s 获取目录状态:%s", System.getProperty("project.dir"), Constant.getRoot().getAbsolutePath()));
    }

    public static HashSet<ShellHelper> getYoupkSet() {
        return YoupkSet;
    }

    public static HashSet<ShellHelper> getXposedSet() {
        return XposedSet;
    }

    private static void ShellRun() {
        logger.info("Shell Start Success");
        while (true) {
            ShellHelper helper = null;
            Socket client = null;
            try {
                client = YoupkServer.accept();
                List<String> shells = Arrays.asList(SysConfigUtil.getStringConfig("shell.ip").split(","));
                if (!shells.contains(client.getInetAddress().getHostName())) {
                    client.close();
                    throw new IOException(client.getInetAddress().getHostName() + "非法设备登录");
                }
                logger.info("Shell Client Address -> " + client.getInetAddress().getHostAddress());
                client.setTcpNoDelay(true);
                client.setSoTimeout(1000 * 60);
                client.setOOBInline(true);
                client.setKeepAlive(true);
                client.setReceiveBufferSize(1024 * 1024 * 20);
                client.setSendBufferSize(1024 * 1024 * 20);
                helper = new ShellHelper(client);
                Constant.getRoot_pool().execute(helper);
                YoupkSet.add(helper);
            } catch (Exception e) {
                logger.error(String.format("脱壳服务 -> 发生异常:%s", e.getMessage()));
                if (helper != null)
                    YoupkSet.remove(helper);
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    private static void XposedShellRun() {
        logger.info("Xposed Shell Start Success");
        while (true) {
            ShellHelper helper = null;
            Socket client = null;
            try {
                client = XposedServer.accept();
                List<String> shells = Arrays.asList(SysConfigUtil.getStringConfig("xposed.shell.ip").split(","));
                if (!shells.contains(client.getInetAddress().getHostName())) {
                    client.close();
                    throw new IOException(client.getInetAddress().getHostName() + "非法设备登录");
                }
                logger.info("Xposed Shell Client Address -> " + client.getInetAddress().getHostAddress());
                client.setTcpNoDelay(true);
                client.setSoTimeout(1000 * 60);
                client.setOOBInline(true);
                client.setKeepAlive(true);
                client.setReceiveBufferSize(1024 * 1024 * 20);
                client.setSendBufferSize(1024 * 1024 * 20);
                helper = new ShellHelper(client);
                Constant.getRoot_pool().execute(helper);
                XposedSet.add(helper);
            } catch (Exception e) {
                logger.error(String.format("Xposed脱壳服务 -> 发生异常:%s", e.getMessage()));
                if (helper != null)
                    XposedSet.remove(helper);
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
