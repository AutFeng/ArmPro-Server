package armadillo.utils;

import armadillo.enums.LanguageEnums;
import armadillo.result.Result;
import armadillo.result.ResultBasic;
import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class QiniuUtils {
    private final Logger logger = Logger.getLogger(QiniuUtils.class);
    public static QiniuUtils qiniuUtils;

    public static QiniuUtils getInstance() {
        if (qiniuUtils == null) {
            synchronized (QiniuUtils.class) {
                qiniuUtils = new QiniuUtils();
                return qiniuUtils;
            }
        }
        return qiniuUtils;
    }

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domainOfBucket;
    private String callbackUrl;
    private Auth auth;

    public QiniuUtils() {
        if (auth == null) {
            JSONObject jsonObject = SysConfigUtil.getSysConfigUtil("qiniu.properties");
            accessKey = jsonObject.getString("qiniu.accesskey");
            secretKey = jsonObject.getString("qiniu.secretkey");
            bucket = jsonObject.getString("qiniu.bucket");
            domainOfBucket = jsonObject.getString("qiniu.domainOfBucket");
            callbackUrl = jsonObject.getString("qiniu.callbackUrl");
        }
        auth = Auth.create(accessKey, secretKey);
    }

    public Result createTask(String key, LanguageEnums languageEnums) {
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\"}");
        String upToken = auth.uploadToken(bucket, key, 3600 * 12, putPolicy);
        JSONObject object = new JSONObject();
        object.put("token", upToken);
        object.put("uuid", key);
        object.put("cache", false);
        return new Result(ResultBasic.GETSUCCESS, object, languageEnums);
    }

    public String createTask(String key) {
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\"}");
        return auth.uploadToken(bucket, key, 3600 * 12, putPolicy);
    }

    public JSONObject createJsonTask(String key) {
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\"}");
        String upToken = auth.uploadToken(bucket, key, 3600 * 12, putPolicy);
        JSONObject object = new JSONObject();
        object.put("token", upToken);
        object.put("uuid", key);
        return object;
    }

    public String getTaskAddress(String key) throws UnsupportedEncodingException {
        String encodedFileName = URLEncoder.encode(key, "utf-8").replace("+", "%20");
        String publicUrl = String.format("%s/%s", domainOfBucket, encodedFileName);
        return auth.privateDownloadUrl(publicUrl, 3600 * 12);
    }

    public boolean deleteQiniuRes(String key) {
        Configuration cfg = new Configuration(Region.region0());
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, key);
            return true;
        } catch (QiniuException e) {
            return false;
        }
    }
}
