package armadillo.utils;

import armadillo.Constant;
import armadillo.enums.LanguageEnums;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SysConfigUtil {
    private static final Logger logger = Logger.getLogger(SysConfigUtil.class);

    public static JSONObject getSysConfigUtil(String properties) {
        Properties pro = new Properties();
        try (InputStream is = SysConfigUtil.class.getClassLoader().getResourceAsStream(Constant.getProfile() + "/" + properties)) {
            pro.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return JSONObject.parseObject(JSON.toJSONString(pro));
    }

    public static String getLanguageConfigUtil(LanguageEnums languageEnums, String config) {
        Properties pro = new Properties();
        String properties = "default.properties";
        switch (languageEnums) {
            case ZH:
                properties = "zh.properties";
                break;
            case DEFAULT:
                properties = "default.properties";
                break;
            case RU:
                properties = "ru.properties";
                break;
        }
        try (FileInputStream fileInputStream = new FileInputStream(new File(Constant.getLanguage(), properties));
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            pro.load(bufferedReader);
            return pro.getProperty(config);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public static String getConfigUtil(String config) {
        Properties pro = new Properties();
        String properties = "config.properties";
        try (FileInputStream fileInputStream = new FileInputStream(new File(Constant.getConfig(), properties));
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader is = new BufferedReader(inputStreamReader)) {
            pro.load(is);
            return pro.getProperty(config);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static JSONObject getConfig() {
        Properties pro = new Properties();
        String properties = "config.properties";
        try (FileInputStream fileInputStream = new FileInputStream(new File(Constant.getConfig(), properties));
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader is = new BufferedReader(inputStreamReader)) {
            pro.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
        return JSONObject.parseObject(JSON.toJSONString(pro));
    }

    /**
     * 获取系统Int配置
     *
     * @param config
     * @return
     */
    public static int getIntConfig(String config) {
        return getConfig().getIntValue(config);
    }

    /**
     * 获取系统String配置
     *
     * @param config
     * @return
     */
    public static String getStringConfig(String config) {
        return getConfig().getString(config);
    }

    public static boolean getBooleanConfig(String config) {
        return getConfig().getBooleanValue(config);
    }
}
