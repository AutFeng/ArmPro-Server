package armadillo.action;

import armadillo.Application;
import armadillo.Arm;
import armadillo.Constant;
import armadillo.dao.ShellTask;
import armadillo.enums.LanguageEnums;
import armadillo.enums.TaskStatus;
import armadillo.model.SysUser;
import armadillo.plugin.PluginClassloader;
import armadillo.result.TaskInfo;
import armadillo.shell.ShellHelper;
import armadillo.transformers.base.BaseTransformer;
import armadillo.utils.*;
import com.alibaba.fastjson.JSONArray;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TaskAction implements Runnable, Comparable<TaskAction> {
    private final Logger logger = Logger.getLogger(TaskAction.class);
    private final long flags;
    private final String rule;
    private final List<TaskInfo> task;
    private final String uuid;
    private final LanguageEnums languageEnums;
    private final String md5;
    private final SysUser sysUser;
    private final long start = System.currentTimeMillis();
    private final long timeout = SysConfigUtil.getIntConfig("task.timeout") * 1000;
    private String dow_url = null;
    private ZipFile zipFile = null;
    private boolean is_delete = true;
    private volatile TaskStatus status = TaskStatus.Wait;

    public TaskAction(
            long flags,
            String rule,
            List<TaskInfo> task,
            String uuid,
            LanguageEnums languageEnums,
            String md5,
            SysUser sysUser) {
        this.flags = flags;
        this.rule = rule != null ? new String(Base64.getDecoder().decode(rule.getBytes()), StandardCharsets.UTF_8) : null;
        this.task = task;
        this.uuid = uuid;
        this.languageEnums = languageEnums;
        this.md5 = md5;
        this.sysUser = sysUser;
    }

    private Integer priority;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(TaskAction o) {
        if (this.getPriority() < o.priority) {
            return 1;
        } else if (this.getPriority() > o.priority) {
            return -1;
        }
        return 0;
    }

    @Override
    public void run() {
        try {
            if (status == TaskStatus.Stop) {
                File cache = new File(Constant.getTmp(), uuid);
                if (cache.exists()) {
                    if (cache.delete()) {
                        if (Constant.isDevelopment())
                            logger.info(String.format("任务状态:%s,删除临时文件:%s,成功", status, cache.getAbsolutePath()));
                    } else {
                        if (Constant.isDevelopment())
                            logger.info(String.format("任务状态:%s,删除临时文件:%s,失败", status, cache.getAbsolutePath()));
                    }
                }
                if (QiniuUtils.getInstance().deleteQiniuRes(uuid)) {
                    if (Constant.isDevelopment())
                        logger.info(String.format("任务状态:%s,删除OSS资源ID:%s,成功", status, uuid));
                } else {
                    if (Constant.isDevelopment())
                        logger.info(String.format("任务状态:%s,删除OSS资源ID:%s,失败", status, uuid));
                }
                return;
            }
            Thread task_thread = new Thread(() -> {
                try {
                    File cacheFile = new File(Constant.getCache(), md5);
                    dow_url = QiniuUtils.getInstance().getTaskAddress(uuid);
                    ActionUtils actionUtils = new ActionUtils(languageEnums);
                    /**
                     * 脱壳任务
                     */
                    if (actionUtils.isSet(Long.parseLong(SysConfigUtil.getStringConfig("shell.flags")), flags)
                            || actionUtils.isSet(Long.parseLong(SysConfigUtil.getStringConfig("xposed.flags")), flags)) {
                        is_delete = !is_delete;
                        RedisUtil redisUtil = RedisUtil.getRedisUtil();
                        while (true) {
                            try {
                                if ((actionUtils.isSet(Long.parseLong(SysConfigUtil.getStringConfig("shell.flags")), flags) ? Application.getYoupkSet().size() : Application.getXposedSet().size()) == 0) {
                                    task.add(new TaskInfo(404, SysConfigUtil.getLanguageConfigUtil(languageEnums, "shell.fail")));
                                    if (redisUtil.exists(uuid))
                                        redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                                    task.clear();
                                    status = TaskStatus.Success;
                                    return;
                                } else {
                                    for (ShellHelper helper : (actionUtils.isSet(Long.parseLong(SysConfigUtil.getStringConfig("shell.flags")), flags) ? Application.getYoupkSet() : Application.getXposedSet())) {
                                        if (!helper.is_handle) {
                                            if (helper.SendShell(new ShellTask(uuid, md5, dow_url, QiniuUtils.getInstance().createTask("shell_" + uuid)), uuid, cacheFile)) {
                                                task.add(new TaskInfo(100, Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.start"))));
                                                redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                                                helper.setState(task);
                                                status = TaskStatus.Success;
                                                return;
                                            }
                                        }
                                    }
                                }
                                Thread.sleep(1000 * 10);
                            } catch (Exception e) {
                                is_delete = true;
                                task.add(new TaskInfo(404, String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.fail")), e.getMessage())));
                                if (redisUtil.exists(uuid))
                                    redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                                task.clear();
                                status = TaskStatus.Success;
                                return;
                            }
                        }
                    }
                    /**
                     * 其他任务
                     */
                    else {
                        if (new File(Constant.getTmp(), uuid).exists()) {
                            handle(actionUtils, cacheFile, start);
                        } else {
                            OkHttpClient okHttpClient = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(dow_url)
                                    .addHeader("Connection", "close")
                                    .build();
                            okhttp3.Response response = okHttpClient.newCall(request).execute();
                            if (response.isSuccessful()) {
                                FileOutputStream fileOutputStream = new FileOutputStream(new File(Constant.getTmp(), uuid));
                                fileOutputStream.write(Objects.requireNonNull(response.body()).bytes());
                                fileOutputStream.close();
                                handle(actionUtils, cacheFile, start);
                            } else {
                                logger.info("******************************************************************");
                                logger.error(String.format("任务ID %s 发生异常:%s", uuid, response.message()));
                                logger.info("******************************************************************");
                                task.add(new TaskInfo(404, String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.fail")), response.message())));
                                RedisUtil redisUtil = RedisUtil.getRedisUtil();
                                if (redisUtil.exists(uuid))
                                    redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                                task.clear();
                            }
                        }
                    }
                    status = TaskStatus.Success;
                } catch (ThreadDeath threadDeath) {
                    if (Constant.isDevelopment())
                        logger.info(String.format("任务监听到:%s终止", uuid));
                    is_delete = true;
                    if (new File(Constant.getTask(), uuid).exists()) {
                        if (new File(Constant.getTask(), uuid).delete()) {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除任务资源:%s,成功", status, uuid));
                        } else {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除任务资源:%s,失败", status, uuid));
                        }
                    }
                } catch (Exception e) {
                    is_delete = true;
                    status = TaskStatus.Fail;
                    logger.info("******************************************************************");
                    logger.error(String.format("任务ID %s 发生异常", uuid), e);
                    logger.info("******************************************************************");
                    task.add(new TaskInfo(404, String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.fail")), e.getMessage() == null ? e.toString() : e.getMessage())));
                    RedisUtil redisUtil = RedisUtil.getRedisUtil();
                    if (redisUtil.exists(uuid))
                        redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                    task.clear();
                    if (new File(Constant.getTask(), uuid).exists()) {
                        if (new File(Constant.getTask(), uuid).delete()) {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除任务资源:%s,成功", status, uuid));
                        } else {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除任务资源:%s,失败", status, uuid));
                        }
                    }
                } finally {
                    if (zipFile != null)
                        try {
                            zipFile.close();
                        } catch (IOException ignored) {
                        }
                    File cache = new File(Constant.getTmp(), uuid);
                    if (cache.exists()) {
                        if (cache.delete()) {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除临时文件:%s,成功", status, cache.getAbsolutePath()));
                        } else {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除临时文件:%s,失败", status, cache.getAbsolutePath()));
                        }
                    }
                    if (is_delete) {
                        if (QiniuUtils.getInstance().deleteQiniuRes(uuid)) {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除OSS资源ID:%s,成功", status, uuid));
                        } else {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除OSS资源ID:%s,失败", status, uuid));
                        }
                    }
                }
            });
            task_thread.start();
            while (true) {
                if (Constant.isDevelopment())
                    logger.info(String.format("读取任务状态:%s", uuid));
                if (status == TaskStatus.Stop) {
                    if (Constant.isDevelopment())
                        logger.info(String.format("任务:%s终止", uuid));
                    status = TaskStatus.Stop;
                    task_thread.stop();
                    break;
                } else if (status == TaskStatus.Success || status == TaskStatus.Fail) {
                    break;
                } else if (System.currentTimeMillis() - start > timeout || status == TaskStatus.TimeOut) {
                    status = TaskStatus.TimeOut;
                    logger.info(String.format("任务超时:%s", uuid));
                    task_thread.stop();
                    task.add(new TaskInfo(404, Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.timeout"))));
                    RedisUtil redisUtil = RedisUtil.getRedisUtil();
                    if (redisUtil.exists(uuid))
                        redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                    task.clear();
                    if (new File(Constant.getTask(), uuid).exists()) {
                        if (new File(Constant.getTask(), uuid).delete()) {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除任务资源:%s,成功", status, uuid));
                        } else {
                            if (Constant.isDevelopment())
                                logger.info(String.format("任务状态:%s,删除任务资源:%s,失败", status, uuid));
                        }
                    }
                    break;
                } else
                    Thread.sleep(1000);
            }
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        } finally {
            if (Constant.isDevelopment())
                logger.info(String.format("任务:%s移除", uuid));
            Constant.getTask_map().remove(uuid);
        }
    }

    private void handle(ActionUtils actionUtils, File cacheFile, long start) throws Exception {
        zipFile = new ZipFile(new File(Constant.getTmp(), uuid));
        task.add(new TaskInfo(100, Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.start"))));
        RedisUtil redisUtil = RedisUtil.getRedisUtil();
        if (redisUtil.exists(uuid))
            redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
        List<byte[]> dexs = new ArrayList<>();
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory())
                continue;
            if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith("dex"))
                dexs.add(StreamUtil.readBytes(zipFile.getInputStream(new ZipEntry(zipEntry.getName()))));
        }
        Arm arm = new Arm(zipFile, task, dexs, uuid, languageEnums);
        arm.setConfig(rule);
        arm.setSysUser(sysUser);
        for (ActionUtils.ActionFlag actionFlag : actionUtils.getFlags(flags)) {
            PluginClassloader pluginClassloader = new PluginClassloader(ClassLoader.getSystemClassLoader(), Constant.getPlugin().getAbsolutePath() + File.separator + actionFlag.getJarPath());
            Class<?> loadClass = pluginClassloader.loadClass(actionFlag.getJarCls());
            Object newInstance = loadClass.newInstance();
            arm.addTransformer((BaseTransformer) newInstance);
        }
        arm.Run();
        task.add(new TaskInfo(200, String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(languageEnums, "processing.success")), (float) (System.currentTimeMillis() - start) / 1000)));
        if (redisUtil.exists(uuid))
            redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
        task.clear();
        /**
         * 写缓存
         */
        if (!actionUtils.isSet(flags, Long.parseLong(SysConfigUtil.getStringConfig("verify.flags")))) {
            byte[] readBytes = StreamUtil.readBytes(new FileInputStream(new File(Constant.getTask(), uuid)));
            FileOutputStream outputStream = new FileOutputStream(cacheFile);
            outputStream.write(readBytes);
            outputStream.close();
        }
    }

    public void Cancel() {
        if (Constant.isDevelopment())
            logger.info(String.format("强制终止任务ID:%s", uuid));
        status = TaskStatus.Stop;
    }
}
