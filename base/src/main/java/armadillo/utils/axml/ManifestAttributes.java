package armadillo.utils.axml;

import armadillo.utils.LoaderRes;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class ManifestAttributes {
    private static final String MANIFEST_ATTR_XML = "attrs_manifest.xml";
    private static ManifestAttributes instance;

    public static ManifestAttributes getInstance() {
        if (instance == null) {
            try {
                instance = new ManifestAttributes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private ManifestAttributes() {
        parseAll();
    }

    private void parseAll() {
        parse(loadXML());
    }

    private Document loadXML() {
        Document doc = null;
        try (InputStream xmlStream = LoaderRes.getInstance().getStaticResAsStream(MANIFEST_ATTR_XML)) {
            if (xmlStream == null)
                throw new Exception(MANIFEST_ATTR_XML + " not found in classpath");
            DocumentBuilder dBuilder = XmlSecurity.getSecureDbf().newDocumentBuilder();
            doc = dBuilder.parse(xmlStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    private void parse(Document doc) {
        NodeList nodeList = doc.getChildNodes();
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node node = nodeList.item(count);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.hasChildNodes()) {
                parseAttrList(node.getChildNodes());
            }
        }
    }

    private static final HashMap<String, Integer> TypeMap = new HashMap<>();
    private static final HashMap<String, String> ValueMap = new HashMap<>();

    private void parseAttrList(NodeList nodeList) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            switch (tempNode.getNodeName()) {
                case "attr":
                    if (tempNode.getAttributes().getNamedItem("format") != null) {
                        int Type;
                        switch (tempNode.getAttributes().getNamedItem("format").getNodeValue()) {
                            case "string":
                                Type = TypedValue.TYPE_STRING;
                                break;
                            case "reference":
                                Type = TypedValue.TYPE_REFERENCE;
                                break;
                            case "integer":
                                Type = TypedValue.TYPE_INT_DEC;
                                break;
                            case "boolean":
                                Type = TypedValue.TYPE_INT_BOOLEAN;
                                break;
                            case "reference|string":
                                Type = TypedValue.TYPE_STRING;
                                break;
                            case "string|integer|color|float|boolean":
                                Type = TypedValue.TYPE_STRING;
                                break;
                            default:
                                Type = TypedValue.TYPE_NULL;
                                break;
                        }
                        TypeMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), Type);
                    }
                    break;
                case "enum":
                case "flag":
                    if (tempNode.getAttributes().getNamedItem("value") != null) {
                        ValueMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), tempNode.getAttributes().getNamedItem("value").getNodeValue());
                    }
                    break;
            }
            if (tempNode.getNodeType() == Node.ELEMENT_NODE
                    && tempNode.hasAttributes()
                    && tempNode.hasChildNodes()) {
                parseAttrList(tempNode.getChildNodes());
            }
        }
    }


    public int decodeType(String key) {
        for (Map.Entry<String, Integer> entry : TypeMap.entrySet()) {
            if (entry.getKey().equals(key))
                return entry.getValue();
        }
        return TypedValue.TYPE_NULL;
    }

    public String decodeValue(String key) {
        for (Map.Entry<String, String> entry : ValueMap.entrySet()) {
            if (entry.getKey().equals(key))
                return entry.getValue();
        }
        return key;
    }
}
