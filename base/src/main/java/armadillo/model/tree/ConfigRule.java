package armadillo.model.tree;

public class ConfigRule {
    private String def;
    private String desc;
    private String name;

    public ConfigRule(String def, String desc, String name) {
        this.def = def;
        this.desc = desc;
        this.name = name;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
