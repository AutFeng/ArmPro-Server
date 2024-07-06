package armadillo.model.tree;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Node {
    private String name;
    private String desc;
    private String icon;
    private int vip;
    private long type;
    private List<ConfigRule> configRule;
    private List<ResourceRule> resourceRule;
    private boolean isSeleteClass;
    private boolean isSeleteActivity;
    private List<ResourceRule> seleteClassRule;
    private List<Node> child;
    @Expose(deserialize = false)
    private PluginPath pluginPath;

    public static class PluginPath {
        private String path;
        private String cls;

        public PluginPath(String path, String cls) {
            this.path = path;
            this.cls = cls;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getCls() {
            return cls;
        }

        public void setCls(String cls) {
            this.cls = cls;
        }
    }

    public Node(String name, String desc, String icon, int vip, long type) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
    }

    public Node(String name, String desc, String icon, int vip, long type, List<ResourceRule> resourceRule, List<Node> child) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.resourceRule = resourceRule;
        this.child = child;
    }

    public Node(String name, String desc, String icon, int vip, long type, boolean isSeleteClass) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.isSeleteClass = isSeleteClass;
    }

    public Node(String name, String desc, String icon, int vip, long type, boolean isSeleteClass, boolean isSeleteActivity) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.isSeleteClass = isSeleteClass;
        this.isSeleteActivity = isSeleteActivity;
    }

    public Node(String name, String desc, String icon, int vip, long type, List<ResourceRule> resourceRule) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.resourceRule = resourceRule;
    }

    public Node(String name, String desc, String icon, int vip, long type, List<ResourceRule> resourceRule, boolean isSeleteClass, List<ResourceRule> seleteClassRule) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.resourceRule = resourceRule;
        this.isSeleteClass = isSeleteClass;
        this.seleteClassRule = seleteClassRule;
    }

    public Node(String name, String desc, String icon, int vip, long type, List<ConfigRule> configRule, List<ResourceRule> resourceRule, boolean isSeleteClass, boolean isSeleteActivity, List<ResourceRule> seleteClassRule, List<Node> child) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.configRule = configRule;
        this.resourceRule = resourceRule;
        this.isSeleteClass = isSeleteClass;
        this.isSeleteActivity = isSeleteActivity;
        this.seleteClassRule = seleteClassRule;
        this.child = child;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public List<ConfigRule> getConfigRule() {
        return configRule;
    }

    public void setConfigRule(List<ConfigRule> configRule) {
        this.configRule = configRule;
    }

    public List<ResourceRule> getResourceRule() {
        return resourceRule;
    }

    public void setResourceRule(List<ResourceRule> resourceRule) {
        this.resourceRule = resourceRule;
    }

    public boolean isSeleteClass() {
        return isSeleteClass;
    }

    public void setSeleteClass(boolean seleteClass) {
        isSeleteClass = seleteClass;
    }

    public List<ResourceRule> getSeleteClassRule() {
        return seleteClassRule;
    }

    public void setSeleteClassRule(List<ResourceRule> seleteClassRule) {
        this.seleteClassRule = seleteClassRule;
    }

    public List<Node> getChild() {
        return child;
    }

    public void setChild(List<Node> child) {
        this.child = child;
    }

    public boolean isSeleteActivity() {
        return isSeleteActivity;
    }

    public void setSeleteActivity(boolean seleteActivity) {
        isSeleteActivity = seleteActivity;
    }

    public PluginPath getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(PluginPath pluginPath) {
        this.pluginPath = pluginPath;
    }

}
