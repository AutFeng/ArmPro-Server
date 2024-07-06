package xml;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String name;
    private boolean isChoose = false;
    private boolean isExpand = false;
    private boolean isClass = false;
    private TreeNode parent;
    private List<TreeNode> child;

    public TreeNode(String name, boolean isClass) {
        this.name = name;
        this.isClass = isClass;
    }

    public TreeNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChoose() {
        return isChoose;
    }

    public void setChoose(boolean choose) {
        isChoose = choose;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
        if (!expand) {
            if (child != null)
                for (TreeNode node : child) {
                    node.setExpand(false);
                }
        }
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public List<TreeNode> getChild() {
        return child;
    }

    public void setChild(List<TreeNode> child) {
        this.child = child;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isChild() {
        return child != null;
    }

    public TreeNode addNode(TreeNode node) {
        if (child == null)
            child = new ArrayList<>();
        node.setParent(this);
        if (!child.contains(node))
            child.add(node);
        else
            return child.get(child.indexOf(node));
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeNode) {
            if (this.getName().equals(((TreeNode) obj).getName()))
                return true;
            else
                return false;
        }
        return false;
    }
}
