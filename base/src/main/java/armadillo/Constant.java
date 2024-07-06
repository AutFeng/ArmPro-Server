package armadillo;

import armadillo.utils.OsUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.*;

public class Constant {
    private final static String Profile = "development";//production development
    private final static String VER = "1.3.2";
    private final static boolean NettyDebug = false;
    private final static ThreadFactory HandlerFactory = new ThreadFactoryBuilder()
            .setNameFormat("IO Pool:%d")
            .setDaemon(false)
            .build();
    private final static ThreadFactory BossFactory = new ThreadFactoryBuilder()
            .setNameFormat("Accept Pool:%d")
            .setDaemon(false)
            .build();
    private final static ThreadFactory WorkerFactory = new ThreadFactoryBuilder()
            .setNameFormat("Worker Pool:%d")
            .setDaemon(false)
            .build();
    private final static ExecutorService StatisticsExec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final static ExecutorService task_pool =
            new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 4, Runtime.getRuntime().availableProcessors() * 4,
                    0L, TimeUnit.MILLISECONDS,
                    new PriorityBlockingQueue<>(),
                    new ThreadFactoryBuilder()
                            .setNameFormat("IO Pool:%d")
                            .setDaemon(false)
                            .build());
    private final static HashMap<String, Runnable> task_map = new HashMap<>();
    private final static ExecutorService root_pool = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                    .setNameFormat("Server Pool:%d")
                    .setDaemon(false)
                    .build());
    public final static int dataMaxLen = 1024 * 1024 * 1024;
    private final static File Root = OsUtils.isOSLinux() ?
            new File(System.getProperty("project.dir", "/www/arm/"))
            :
            new File(System.getProperty("project.dir", new File(System.getenv("SYSTEMDRIVE"), "arm").getAbsolutePath()));
    public static int start = 8080;
    public static int end = 8080;
    public static int handleCount = 256;
    public static int youpkShell = 15000;
    public static int xposedShell = 16000;
    public static int httpport = 8000;

    public static File getLanguage() {
        return new File(Root, "language");
    }

    public static File getCache() {
        return new File(Root, "cache");
    }

    public static File getTask() {
        return new File(Root, "task");
    }

    public static String getProfile() {
        return Profile;
    }

    public static File getRes() {
        return new File(Root, "res");
    }

    public static File getIcon() {
        return new File(getRes(), "icon");
    }

    public static File getConfig() {
        return new File(Root, "config");
    }

    public static File getHandle() {
        return new File(getConfig(), "handle");
    }

    public static File getPlugin() {
        return new File(getRoot(), "plugin");
    }

    public static File getTmp() {
        return new File(getRoot(), "tmp");
    }

    public static File getRoot() {
        return Root;
    }

    public static boolean isNettyDebug() {
        return NettyDebug;
    }

    public static String getVER() {
        return VER;
    }

    public static boolean isDevelopment() {
        return Profile.equals("development");
    }

    public static ThreadFactory getHandlerFactory() {
        return HandlerFactory;
    }

    public static ThreadFactory getBossFactory() {
        return BossFactory;
    }

    public static ThreadFactory getWorkerFactory() {
        return WorkerFactory;
    }

    public static int getDataMaxLen() {
        return dataMaxLen;
    }

    public static ExecutorService getStatisticsExec() {
        return StatisticsExec;
    }

    public static ThreadPoolExecutor getTask_pool() {
        return (ThreadPoolExecutor) task_pool;
    }

    public static ExecutorService getRoot_pool() {
        return root_pool;
    }

    public static HashMap<String, Runnable> getTask_map() {
        return task_map;
    }
}
