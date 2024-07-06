package xml;

import org.jf.dexlib2.iface.ClassDef;

import java.text.Collator;
import java.util.*;

public class Tree {
    private final List<TreeNode> treeNodes = new ArrayList<>();
    private final String TAG = Tree.class.getSimpleName();

    public Tree(List<HashSet<ClassDef>> classDefs) {
        for (int i = 0; i < classDefs.size(); i++) {
            TreeNode root = new TreeNode("Classes" + (i + 1));
            treeNodes.add(root);
            for (ClassDef classDef : classDefs.get(i)) {
                String class_type = classDef.getType();
                class_type = class_type.substring(1, class_type.length() - 1);
                String[] split = class_type.split("/");
                TreeNode tree = root;
                for (int index = 0; index < split.length; index++) {
                    TreeNode treeNode = new TreeNode(split[index], index + 1 == split.length);
                    if (index == 0)
                        treeNode = tree.addNode(treeNode);
                    else {
                        if (tree.getName().equals(split[index - 1])) {
                            treeNode = tree.addNode(treeNode);
                        }
                    }
                    tree = treeNode;
                }
            }
            Sort(root);
        }
        System.out.println("ssss");
    }

    private void Sort(TreeNode treeNode) {
        if (treeNode.isChild()) {
            Comparator comparator = Collator.getInstance(Locale.ENGLISH);
            treeNode.getChild().sort((o1, o2) -> {
                if (o1.isChild()) return 1;
                if (o2.isChild()) return 1;
                return comparator.compare(o1.getName(), o2.getName());
            });
            for (TreeNode node : treeNode.getChild()) {
                if (node.isChild())
                    Sort(node);
            }
        }
    }
    /**
     * for (ClassDef classDef : classDefs.get(i)) {
     *                 String class_type = classDef.getType();
     *                 class_type = class_type.substring(1, class_type.length() - 1);
     *                 String[] split = class_type.split("/");
     *                 TreeNode tree = root;
     *                 for (int index = 0; index < split.length; index++) {
     *                     TreeNode treeNode = new TreeNode(split[index], index + 1 == split.length);
     *                     if (tree.getChild() != null) {
     *                         if (index > 0) {
     *                             int key = tree.getChild().indexOf(new TreeNode(split[index - 1]));
     *                             if (key > -1) {
     *                                 tree = tree.getChild().get(key);
     *                                 tree.addNode(treeNode);
     *                             }
     *                         } else
     *                             tree.addNode(treeNode);
     *                     } else
     *                         tree.addNode(treeNode);
     *                 }
     *             }
     */

}
