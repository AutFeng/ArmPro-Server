package armadillo.model.tree;

public class ResourceRule {
    private String name;
    private String startWith;
    private String endWith;
    public ResourceRule(String name) {
        this.name = name;
    }

    public ResourceRule(String startWith, String endWith) {
        this.startWith = startWith;
        this.endWith = endWith;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartWith() {
        return startWith;
    }

    public void setStartWith(String startWith) {
        this.startWith = startWith;
    }

    public String getEndWith() {
        return endWith;
    }

    public void setEndWith(String endWith) {
        this.endWith = endWith;
    }
}
