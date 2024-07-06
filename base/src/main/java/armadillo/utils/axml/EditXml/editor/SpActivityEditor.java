package armadillo.utils.axml.EditXml.editor;


import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.decode.StringBlock;

import java.util.Iterator;
import java.util.List;

public class SpActivityEditor extends BaseEditor<SpActivityEditor.EditorInfo> {
    public String MainName;

    public SpActivityEditor(AXMLDoc doc) {
        super(doc);
    }

    @Override
    public String getEditorName() {
        return "activity";
    }

    private String ParseMain(AXMLDoc doc) {
        String Name = null;
        List<BXMLNode> children = doc.getApplicationNode().getChildren();
        Iterator<BXMLNode> iterator = children.iterator();
        while (iterator.hasNext()) {
            BTagNode n = (BTagNode) iterator.next();
            if (n.getRawName().equals("activity")) {
                for (BTagNode.Attribute attr : n.getAttribute()) {
                    if (attr.Name.equals("name"))
                        Name = attr.String;
                }
                if (n.getChildren() != null) {
                    for (BXMLNode tag : n.getChildren()) {
                        BTagNode i = (BTagNode) tag;
                        if (i.getRawName().equals("intent-filter")) {
                            if (i.getChildren() != null) {
                                for (BXMLNode k : i.getChildren()) {
                                    BTagNode action = (BTagNode) k;
                                    for (BTagNode.Attribute attr : action.getAttribute()) {
                                        if (attr.Name.equals("name")) {
                                            if ("android.intent.category.LAUNCHER".equals(attr.String))
                                                return Name;
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

            }
        }
        return null;
    }

    public void SetMainClass(String cla) {
        MainName = cla;
    }

    @Override
    protected void editor() {
        List<BXMLNode> children = findNode().getChildren();
        for (BXMLNode child : children) {
            BTagNode n = (BTagNode) child;
            if (n.getRawName().equals(getEditorName())) {
                for (BTagNode.Attribute a_attr : n.getAttribute()) {
                    if (a_attr.Name.equals("name") && a_attr.String.equals(MainName)) {
                        BTagNode a = new BTagNode(axmlDoc.getmStringBlock(), null, "activity");
                        for (BTagNode.Attribute attr : n.getAttribute()) {
                            BTagNode.Attribute k = new BTagNode.Attribute(axmlDoc.getmStringBlock(), attr.NameSpace, attr.Name, attr.mType);
                            if (attr.String != null)
                                k.setString(attr.String);
                            else
                                k.setValue(attr.mType >> 24, attr.mValue);
                            a.setAttribute(k);
                        }
                        children.add(a);
                        for (BTagNode.Attribute attr : n.getAttribute())
                            if (attr.Name.equals("name"))
                                attr.setString(editorInfo.Name);
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected BXMLNode findNode() {
        return axmlDoc.getApplicationNode();
    }

    @Override
    protected void registStringBlock(StringBlock block) {

    }

    public static class EditorInfo {
        public String Name;

        public EditorInfo(String name) {
            this.Name = name;
        }
    }
}
