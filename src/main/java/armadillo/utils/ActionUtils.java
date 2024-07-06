package armadillo.utils;

import armadillo.Constant;
import armadillo.enums.LanguageEnums;
import armadillo.model.tree.Node;
import armadillo.model.tree.TreeNode;
import armadillo.result.ResultBasic;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ActionUtils {
    private ActionFlag[] allflags;

    public ActionUtils(LanguageEnums languageEnums) throws IOException {
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
        Type listType = new TypeToken<List<TreeNode>>() {
        }.getType();
        List<TreeNode> fromJson = new Gson().fromJson(new String(StreamUtil.readBytes(new FileInputStream(new File(Constant.getHandle(), json)))), listType);
        List<ActionFlag> actionFlags = new ArrayList<>();
        for (TreeNode treeNode : fromJson) {
            for (Node node : treeNode.getChild()) {
                if (node.getType() == -1 || containsActionType(actionFlags, node))
                    continue;
                if (node.getPluginPath() != null)
                    actionFlags.add(new ActionFlag(node.getType(), node.getName(), getV(node.getVip()), getResult(node.getVip()), node.getPluginPath().getPath(), node.getPluginPath().getCls()));
                else
                    actionFlags.add(new ActionFlag(node.getType(), node.getName(), getV(node.getVip()), getResult(node.getVip())));
            }
        }
        allflags = actionFlags.toArray(new ActionFlag[0]);
    }

    private boolean containsActionType(List<ActionFlag> actionFlags, Node node) {
        for (ActionFlag flag : actionFlags) {
            if (flag.getType() == node.getType())
                return true;
        }
        return false;
    }

    public ActionFlag[] getFlags(long FlagValue) {
        int size = 0;
        for (ActionFlag Flag : allflags) {
            if ((FlagValue & Flag.type) != 0) {
                size++;
            }
        }
        ActionFlag[] Flags = new ActionFlag[size];
        int FlagsPosition = 0;
        for (ActionFlag Flag : allflags) {
            if ((FlagValue & Flag.type) != 0) {
                Flags[FlagsPosition++] = Flag;
            }
        }
        return Flags;
    }

    private int getV(int vip) {
        switch (vip) {
            case 1:
                return 1;
            case 2:
                return 100;
            case 3:
                return 300;
            case 4:
                return 600;
            case 5:
                return 1000;
            case 6:
                return 1500;
            case 7:
                return 2100;
            case 8:
                return 2800;
            case 9:
                return 3600;
            case 10:
                return 4500;
        }
        return 0;
    }

    private ResultBasic getResult(int vip) {
        switch (vip) {
            case 1:
                return ResultBasic.USRV1;
            case 2:
                return ResultBasic.USRV2;
            case 3:
                return ResultBasic.USRV3;
            case 4:
                return ResultBasic.USRV4;
            case 5:
                return ResultBasic.USRV5;
            case 6:
                return ResultBasic.USRV6;
            case 7:
                return ResultBasic.USRV7;
            case 8:
                return ResultBasic.USRV8;
            case 9:
                return ResultBasic.USRV9;
            case 10:
                return ResultBasic.USRV10;
        }
        return null;
    }

    public static class ActionFlag {
        private final long type;
        private final String flagName;
        private final int v;
        private final ResultBasic result;
        private String JarPath;
        private String JarCls;

        public ActionFlag(long type, String flagName, int v, ResultBasic result) {
            this.type = type;
            this.flagName = flagName;
            this.v = v;
            this.result = result;
        }

        public ActionFlag(long type, String flagName, int v, ResultBasic result, String jarPath, String jarCls) {
            this.type = type;
            this.flagName = flagName;
            this.v = v;
            this.result = result;
            this.JarPath = jarPath;
            this.JarCls = jarCls;
        }

        public long getType() {
            return type;
        }

        public String getFlagName() {
            return flagName;
        }

        public int getV() {
            return v;
        }

        public ResultBasic getResult() {
            return result;
        }

        public String getJarPath() {
            return JarPath;
        }

        public String getJarCls() {
            return JarCls;
        }

        @Override
        public String toString() {
            return flagName;
        }
    }

    public boolean isSet(long src, long flags) {
        return (src & flags) != 0;
    }

    public boolean isSet(String[] src, long flags) {
        for (String s : src) {
            if ((Long.parseLong(s) & flags) != 0)
                return true;
        }
        return false;
    }

    public long getValue(String[] src, long flags) {
        for (String s : src) {
            if ((Long.parseLong(s) & flags) != 0)
                return Long.getLong(s);
        }
        return -1;
    }

    public String getValues(String[] src, long flags) {
        for (String s : src) {
            if ((Long.parseLong(s) & flags) != 0)
                return s;
        }
        return null;
    }
}
