package armadillo.utils.axml.EditXml.editor;

import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.decode.StringBlock;
import armadillo.utils.axml.EditXml.utils.TypedValue;

import java.util.ArrayList;
import java.util.List;

public class PermissionEditor extends BaseEditor<PermissionEditor.EditorInfo> {
    public PermissionEditor(AXMLDoc axmlDoc) {
        super(axmlDoc);
    }

    @Override
    public String getEditorName() {
        return NODE_USER_PREMISSION;
    }

    @Override
    protected void editor() {
        List<BXMLNode> nodes = findNode().getChildren();
        for (PermissionInfo info : editorInfo.permissionInfos) {
            BTagNode.Attribute attribute = new BTagNode.Attribute(axmlDoc.getmStringBlock(), NAME_SPACE, "name", TypedValue.TYPE_STRING);
            attribute.setString(info.name);
            BTagNode tagNode = new BTagNode(axmlDoc.getmStringBlock(),null, NODE_USER_PREMISSION);
            tagNode.setAttribute(attribute);
            boolean flag = false;
            for (int i = 0; i < nodes.size(); i++) {
                if (((BTagNode) nodes.get(i)).getRawName().equals(getEditorName())) {
                    nodes.add(i, tagNode);
                    flag = true;
                    break;
                }
            }
            if (!flag)
                nodes.add(tagNode);
        }
    }

    @Override
    protected BXMLNode findNode() {
        return axmlDoc.getManifestNode();
    }

    @Override
    protected void registStringBlock(StringBlock stringBlock) {
        editorInfo.permissionInfos.removeIf(info -> stringBlock.containsString(info.name));
    }

    public static class EditorInfo {
        private final List<PermissionInfo> permissionInfos = new ArrayList<>();

        public EditorInfo with(PermissionInfo info) {
            permissionInfos.add(info);
            return this;
        }
    }

    public static class PermissionInfo {
        private final String name;

        public PermissionInfo(String name) {
            this.name = name;
        }
    }
}
