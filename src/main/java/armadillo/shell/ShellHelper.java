package armadillo.shell;

import armadillo.Application;
import armadillo.Constant;
import armadillo.dao.ShellTask;
import armadillo.result.TaskInfo;
import armadillo.utils.QiniuUtils;
import armadillo.utils.RedisUtil;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipInputStream;

public class ShellHelper implements Runnable {
    private final Logger logger = Logger.getLogger(ShellHelper.class);
    private final Socket client;
    public boolean is_handle = false;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private String uuid;
    private List<TaskInfo> state;
    private File cacheFile;

    public ShellHelper(Socket client) throws IOException {
        this.client = client;
        this.inputStream = new DataInputStream(client.getInputStream());
        this.outputStream = new DataOutputStream(client.getOutputStream());
    }

    @Override
    public void run() {
        logger.info(String.format("脱壳设备 -> %s 上线", client.getInetAddress().getHostAddress()));
        try {
            Receive();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            logger.info(String.format("脱壳设备 -> %s 下线", client.getInetAddress().getHostAddress()));
            Application.getYoupkSet().remove(this);
            Application.getXposedSet().remove(this);
            if (uuid != null) {
                state.add(new TaskInfo(404, "失败:抱歉当前服务暂时无法使用"));
                RedisUtil redisUtil = RedisUtil.getRedisUtil();
                redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(state));
                state.clear();
            }
            try {
                inputStream.close();
                outputStream.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void Receive() throws IOException {
        while (true) {
            int type = inputStream.readInt();
            if (type == 520) {
                byte[] info = new byte[inputStream.readInt()];
                inputStream.readFully(info);
                ShellTask task = new Gson().fromJson(new String(info, StandardCharsets.UTF_8), ShellTask.class);
                switch (task.getCode()) {
                    case 200: {
                        Response response = new OkHttpClient()
                                .newCall(new Request
                                        .Builder()
                                        .url(QiniuUtils.getInstance().getTaskAddress("shell_" + task.getUuid()))
                                        .addHeader("Connection", "close")
                                        .build())
                                .execute();
                        if (response.isSuccessful()) {
                            try (FileOutputStream out = new FileOutputStream(new File(Constant.getTask(), task.getUuid()));
                                 FileOutputStream success_out = new FileOutputStream(cacheFile)) {
                                byte[] bytes = Objects.requireNonNull(response.body()).bytes();
                                out.write(bytes);
                                success_out.write(bytes);
                            }
                            WriteSuccess(task.getUuid(), task.getMsg());
                        } else
                            WriteError(task.getUuid(), response.message());
                        QiniuUtils.getInstance().deleteQiniuRes(task.getUuid());
                        QiniuUtils.getInstance().deleteQiniuRes("shell_" + task.getUuid());
                    }
                    break;
                    case 404:
                        WriteError(task.getUuid(), task.getMsg());
                        break;
                }
                is_handle = false;
                this.uuid = null;
            }
        }
    }

    public boolean SendShell(ShellTask taskInfo, String uuid, File cacheFile) throws IOException {
        if (outputStream == null) return false;
        String body = new Gson().toJson(taskInfo);
        outputStream.writeInt(520);
        outputStream.writeInt(body.getBytes().length);
        outputStream.write(body.getBytes());
        outputStream.flush();
        is_handle = true;
        this.uuid = uuid;
        this.cacheFile = cacheFile;
        return true;
    }

    public void setState(List<TaskInfo> state) {
        this.state = state;
    }

    private void WriteError(String uuid, String msg) {
        state.add(new TaskInfo(404, "处理失败:" + msg));
        RedisUtil redisUtil = RedisUtil.getRedisUtil();
        redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(state));
        state.clear();
        QiniuUtils.getInstance().deleteQiniuRes(uuid);
        QiniuUtils.getInstance().deleteQiniuRes("shell_" + uuid);
        logger.info("******************************************************************");
        logger.info(String.format("脱壳任务ID:%s 失败:%s", uuid, msg));
        logger.info("******************************************************************");
        redisUtil.sadd("shell_miss", cacheFile.getName());
    }

    private void WriteSuccess(String uuid, String msg) {
        state.add(new TaskInfo(200, msg));
        RedisUtil redisUtil = RedisUtil.getRedisUtil();
        redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(state));
        state.clear();
        QiniuUtils.getInstance().deleteQiniuRes(uuid);
        QiniuUtils.getInstance().deleteQiniuRes("shell_" + uuid);
        logger.info("******************************************************************");
        logger.info(String.format("脱壳任务ID:%s %s", uuid, msg));
        logger.info("******************************************************************");
    }
}
