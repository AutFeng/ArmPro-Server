package armadillo.utils.axml.EditXml.editor;

import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.decode.StringBlock;
import armadillo.utils.axml.EditXml.utils.TypedValue;

import java.util.ArrayList;
import java.util.List;

public class MetaDataEditor extends BaseEditor<MetaDataEditor.Editorinfo> {

    public MetaDataEditor(AXMLDoc axmlDoc) {
        super(axmlDoc);
    }

    @Override
    public String getEditorName() {
        return NODE_META_DATA;
    }

    @Override
    protected void editor() {
        List<BXMLNode> nodes = findNode().getChildren();
        for (DataInfo info : editorInfo.dataInfos) {
            BTagNode.Attribute name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), NAME_SPACE, "name", TypedValue.TYPE_STRING);
            name.setString(info.name);
            BTagNode.Attribute value = new BTagNode.Attribute(axmlDoc.getmStringBlock(), NAME_SPACE, "value", TypedValue.TYPE_STRING);
            value.setString(info.value);
            BTagNode tagNode = new BTagNode(axmlDoc.getmStringBlock(), null, NODE_META_DATA);
            tagNode.addAttribute(name, value);
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
        return axmlDoc.getApplicationNode();
    }

    @Override
    protected void registStringBlock(StringBlock stringBlock) {
        editorInfo.dataInfos.removeIf(info -> stringBlock.containsString(info.name));
    }

    public static class Editorinfo {
        private final List<DataInfo> dataInfos = new ArrayList<>();

        public Editorinfo with(DataInfo dataInfo) {
            dataInfos.add(dataInfo);
            return this;
        }
    }

    public static class DataInfo {
        private final String name;
        private final String value;

        public DataInfo(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
