package armadillo.model.tree;

import java.util.List;

public class TreeNode {
    private String name;
    private boolean isSingle;
    private List<Node> child;

    public TreeNode(String name) {
        this.name = name;
    }

    public TreeNode(String name, List<Node> child) {
        this.name = name;
        this.child = child;
    }

    public TreeNode(String name, boolean isSingle) {
        this.name = name;
        this.isSingle = isSingle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        isSingle = single;
    }

    public List<Node> getChild() {
        return child;
    }

    public void setChild(List<Node> child) {
        this.child = child;
    }


}
