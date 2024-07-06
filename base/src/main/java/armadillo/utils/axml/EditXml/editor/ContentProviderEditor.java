package armadillo.utils.axml.EditXml.editor;

import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.decode.StringBlock;
import armadillo.utils.axml.EditXml.utils.TypedValue;

import java.util.ArrayList;
import java.util.List;

public class ContentProviderEditor extends BaseEditor<ContentProviderEditor.Editorinfo> {

    public ContentProviderEditor(AXMLDoc axmlDoc) {
        super(axmlDoc);
    }

    @Override
    public String getEditorName() {
        return NODE_ContentProvider;
    }

    @Override
    protected void editor() {
        List<BXMLNode> nodes = findNode().getChildren();
        for (ProviderInfo info : editorInfo.providerInfos) {
            BTagNode tagNode = new BTagNode(axmlDoc.getmStringBlock(),null, "provider");
            tagNode.setmStringBlock(axmlDoc.getmStringBlock());
            tagNode.setmResBlock(axmlDoc.getmResBlock());
            List<BTagNode.Attribute> attributes = new ArrayList<>();
            {
                try {
                    BTagNode.Attribute attribute_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(),NAME_SPACE, "name", TypedValue.TYPE_STRING);
                    BTagNode.Attribute attribute_authorities = new BTagNode.Attribute(axmlDoc.getmStringBlock(),NAME_SPACE, "authorities", TypedValue.TYPE_STRING);
                    BTagNode.Attribute attribute_exported = new BTagNode.Attribute(axmlDoc.getmStringBlock(),NAME_SPACE, "exported", TypedValue.TYPE_INT_BOOLEAN);
                    BTagNode.Attribute attribute_initOrder = new BTagNode.Attribute(axmlDoc.getmStringBlock(),NAME_SPACE, "initOrder", TypedValue.TYPE_INT_DEC);

                    attribute_name.setString(info.name);
                    attributes.add(attribute_name);

                    attribute_authorities.setString(info.authorities);
                    attributes.add(attribute_authorities);

                    attribute_exported.setValue(TypedValue.TYPE_INT_BOOLEAN, info.exported ? 1 : 0);
                    attributes.add(attribute_exported);

                    attribute_initOrder.setValue(TypedValue.TYPE_INT_DEC, info.initOrder);
                    attributes.add(attribute_initOrder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tagNode.setAttribute(attributes.toArray(new BTagNode.Attribute[attributes.size()]));
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
        editorInfo.providerInfos.removeIf(info -> stringBlock.containsString(info.name));
    }

    public static class Editorinfo {
        private final List<ProviderInfo> providerInfos = new ArrayList<>();

        public Editorinfo with(ProviderInfo providerInfo) {
            providerInfos.add(providerInfo);
            return this;
        }
    }

    public static class ProviderInfo {
        private final String name;
        private final String authorities;
        private final boolean exported;
        private final int initOrder;

        public ProviderInfo(String name, String authorities, boolean exported, int initOrder) {
            this.name = name;
            this.authorities = authorities;
            this.exported = exported;
            this.initOrder = initOrder;
        }
    }
}
