package armadillo.utils.axml.EditXml.editor;


import armadillo.utils.axml.AutoXml.ManifestParse;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import armadillo.utils.axml.ManifestAttributes;
import armadillo.utils.axml.Manifest_ids;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoEditor {
    public static byte[] AutoXml(byte[] bytes, byte[] data) throws Exception {
        ManifestAttributes attr = ManifestAttributes.getInstance();
        Manifest_ids ids = Manifest_ids.getInstance();
        AXMLDoc axmlDoc = new AXMLDoc();
        axmlDoc.parse(new ByteArrayInputStream(bytes));
        XmlPullParserFactory f = XmlPullParserFactory.newInstance();
        f.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        XmlPullParser p = f.newPullParser();
        p.setInput(new ByteArrayInputStream(data), "utf-8");
        Map<Integer, BTagNode> root_node = null;
        String root_name = null;
        labie1:
        for (int i = p.getEventType(); i != XmlPullParser.END_DOCUMENT; i = p.next()) {
            switch (i) {
                case XmlPullParser.START_TAG:
                    if (p.getName().equals("manifest")) continue;
                    if (p.getName().equals("uses-sdk")) {
                        int minSdkVersion = 0;
                        int targetSdkVersion = 0;
                        for (int i1 = 0; i1 < p.getAttributeCount(); i1++) {
                            if (p.getAttributeName(i1).equals("minSdkVersion")) {
                                minSdkVersion = Integer.parseInt(p.getAttributeValue(i1));
                            } else if (p.getAttributeName(i1).equals("targetSdkVersion")) {
                                targetSdkVersion = Integer.parseInt(p.getAttributeValue(i1));
                            }
                        }
                        if (minSdkVersion > 0 || targetSdkVersion > 0) {
                            for (BXMLNode child : axmlDoc.getManifestNode().getChildren()) {
                                BTagNode node = (BTagNode) child;
                                if ("uses-sdk".equals(node.getRawName())) {
                                    BTagNode.Attribute[] attributes = node.getAttribute();
                                    for (BTagNode.Attribute attribute : attributes) {
                                        if (attribute.Name.equals("minSdkVersion") && minSdkVersion > 0)
                                            attribute.setValue(TypedValue.TYPE_INT_DEC, minSdkVersion);
                                        else if (attribute.Name.equals("targetSdkVersion") && targetSdkVersion > 0)
                                            attribute.setValue(TypedValue.TYPE_INT_DEC, targetSdkVersion);
                                    }
                                    node.setAttribute(attributes);
                                }
                            }
                        }
                    }
                    List<BTagNode.Attribute> attributeList = new ArrayList<>();
                    for (int in = 0; in < p.getAttributeCount(); in++) {
                        String name = p.getAttributeName(in);
                        String value = p.getAttributeValue(in);
                        if ("replace".equals(name) && "true".equals(value)) {
                            for (int k = 0; k < p.getAttributeCount(); k++)
                                if ("name".equals(p.getAttributeName(k))) {
                                    String main_class = ManifestParse.parseMainActivity(new ByteArrayInputStream(bytes));
                                    if (main_class == null)
                                        throw new Exception("Main Class Not Find");
                                    SpActivityEditor spActivityEditor = new SpActivityEditor(axmlDoc);
                                    spActivityEditor.SetMainClass(main_class);
                                    spActivityEditor.setEditorInfo(new SpActivityEditor.EditorInfo(p.getAttributeValue(k)));
                                    spActivityEditor.commit();
                                    MetaDataEditor metaDataEditor = new MetaDataEditor(axmlDoc);
                                    metaDataEditor.setEditorInfo(new MetaDataEditor.Editorinfo().with(new MetaDataEditor.DataInfo("arm", main_class)));
                                    metaDataEditor.commit();
                                }
                            continue labie1;
                        }
                        int Type = attr.decodeType(name);
                        BTagNode.Attribute attribute = new BTagNode.Attribute(axmlDoc.getmStringBlock(),
                                "http://schemas.android.com/apk/res/android",
                                name,
                                Type);
                        switch (Type) {
                            case TypedValue.TYPE_REFERENCE:
                                if (value.startsWith("@")) {
                                    try {
                                        Integer.parseInt(value.substring(1), 16);
                                        attribute.setValue(TypedValue.TYPE_REFERENCE, Integer.parseInt(value.substring(1), 16));
                                    } catch (Exception e) {
                                        if (value.startsWith("@android:style")) {
                                            int v = ids.parseids(value.split("/")[1]);
                                            if (v == -1)
                                                v = ids.parseStyleids(value.split("/")[1]);
                                            attribute.setValue(TypedValue.TYPE_REFERENCE, v);
                                        }
                                    }
                                }
                                break;
                            case TypedValue.TYPE_STRING:
                                if (p.getName().equals("provider") && name.equals("authorities"))
                                    value = ManifestParse.parseManifestPackageName(new ByteArrayInputStream(bytes)) + value;
                                if (value.startsWith("@"))
                                    attribute.setValue(TypedValue.TYPE_REFERENCE, Integer.parseInt(value.substring(1), 16));
                                else
                                    attribute.setString(value);
                                break;
                            case TypedValue.TYPE_INT_BOOLEAN:
                                attribute.setValue(TypedValue.TYPE_INT_BOOLEAN, value.equals("true") ? 1 : 0);
                                break;
                            case TypedValue.TYPE_NULL:
                                if (value.contains("|")) {
                                    int v = 0;
                                    for (String s : value.split("\\|")) {
                                        String buff = attr.decodeValue(s);
                                        if (buff.startsWith("0x")) {
                                            v = v | Integer.parseInt(buff.substring(2));
                                        }
                                    }
                                    attribute.setValue(TypedValue.TYPE_INT_HEX, v);
                                } else {
                                    String v = attr.decodeValue(value);
                                    if (v.startsWith("0x"))
                                        attribute.setValue(TypedValue.TYPE_INT_HEX, Integer.parseInt(v.substring(2), 16));
                                    else
                                        attribute.setValue(TypedValue.TYPE_INT_DEC, Integer.parseInt(v));
                                }
                                break;
                            case TypedValue.TYPE_INT_DEC:
                                attribute.setValue(TypedValue.TYPE_INT_DEC, Integer.parseInt(attr.decodeValue(value)));
                                break;
                        }
                        attributeList.add(attribute);
                    }
                    if (root_node == null) {
                        root_name = p.getName();
                        root_node = new HashMap<>();
                        BTagNode node = new BTagNode(axmlDoc.getmStringBlock(), null, p.getName());
                        for (BTagNode.Attribute a : attributeList)
                            node.setAttribute(a);
                        root_node.put(p.getDepth(), node);
                    } else {
                        BTagNode node = new BTagNode(axmlDoc.getmStringBlock(), null, p.getName());
                        for (BTagNode.Attribute a : attributeList)
                            node.setAttribute(a);
                        root_node.get(p.getDepth() - 1).addChild(node);
                        root_node.put(p.getDepth(), node);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if (p.getName().equals(root_name)) {
                        switch (p.getName()) {
                            case "uses-permission":
                            case "compatible-screens":
                            case "permission":
                            case "permission-group":
                            case "supports-gl-texture":
                            case "permission-tree":
                            case "uses-split":
                            case "uses-permission-sdk-23":
                            case "uses-feature":
                            case "uses-configuration":
                            case "instrumentation": {
                                boolean flag = false;
                                for (int k = 0; k < axmlDoc.getManifestNode().getChildren().size(); k++) {
                                    BTagNode tagNode = (BTagNode) axmlDoc.getManifestNode().getChildren().get(k);
                                    if (tagNode.getRawName().equals(p.getName()) && !flag) {
                                        axmlDoc.getManifestNode().getChildren().add(k, root_node.get(p.getDepth()));
                                        root_name = null;
                                        root_node.clear();
                                        root_node = null;
                                        flag = true;
                                    }
                                }
                                if (!flag) {
                                    axmlDoc.getManifestNode().getChildren().add(root_node.get(p.getDepth()));
                                    root_name = null;
                                    root_node.clear();
                                    root_node = null;
                                }
                            }
                            break;
                            case "activity":
                            case "service":
                            case "meta-data":
                            case "receiver":
                            case "provider":
                            case "activity-alias":
                            case "uses-library": {
                                boolean flag = false;
                                for (int k = 0; k < axmlDoc.getApplicationNode().getChildren().size(); k++) {
                                    BTagNode tagNode = (BTagNode) axmlDoc.getApplicationNode().getChildren().get(k);
                                    if (tagNode.getRawName().equals(p.getName()) && !flag) {
                                        axmlDoc.getApplicationNode().getChildren().add(k, root_node.get(p.getDepth()));
                                        root_name = null;
                                        root_node.clear();
                                        root_node = null;
                                        flag = true;
                                    }
                                }
                                if (!flag) {
                                    axmlDoc.getApplicationNode().getChildren().add(root_node.get(p.getDepth()));
                                    root_name = null;
                                    root_node.clear();
                                    root_node = null;
                                }
                            }
                            break;
                            case "application":
                                for (BTagNode.Attribute attribute : root_node.get(p.getDepth()).getAttribute())
                                    ((BTagNode) axmlDoc.getApplicationNode()).setAttribute(attribute);
                                root_name = null;
                                root_node.clear();
                                root_node = null;
                                break;
                        }
                    }
                    break;
            }
        }
        ByteArrayOutputStream Xml_Out = new ByteArrayOutputStream();
        axmlDoc.build(Xml_Out);
        axmlDoc.release();
        return Xml_Out.toByteArray();
    }
}
