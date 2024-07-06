package armadillo.utils.axml;


import armadillo.utils.LoaderRes;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Manifest_ids {
    private static final String MANIFEST_ATTR_XML = "manifest_ids.xml";
    private static Manifest_ids instance;
    private static final HashMap<String, Integer> IdsMap = new HashMap<>();
    private static final HashMap<String, Integer> StyleIdsMap = new HashMap<>();

    public static Manifest_ids getInstance() {
        if (instance == null) {
            try {
                instance = new Manifest_ids();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private Manifest_ids() {
        parseAll();
    }

    //解析全部Ids
    private void parseAll() {
        parse(loadXML());
    }

    private Document loadXML() {
        Document doc = null;
        try (InputStream xmlStream = LoaderRes.getInstance().getStaticResAsStream(MANIFEST_ATTR_XML)) {
            if (xmlStream == null) {
                throw new Exception(MANIFEST_ATTR_XML + " not found in classpath");
            }
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

    private void parseAttrList(NodeList nodeList) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeName().equals("public")) {
                if (tempNode.getAttributes().getNamedItem("type") != null && tempNode.getAttributes().getNamedItem("type").getNodeValue().equals("attr")) {
                    String ids = tempNode.getAttributes().getNamedItem("id").getNodeValue();
                    IdsMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), Integer.parseInt(ids.substring(2), 16));
                } else if (tempNode.getAttributes().getNamedItem("type") != null && tempNode.getAttributes().getNamedItem("type").getNodeValue().equals("style")) {
                    String ids = tempNode.getAttributes().getNamedItem("id").getNodeValue();
                    StyleIdsMap.put(tempNode.getAttributes().getNamedItem("name").getNodeValue(), Integer.parseInt(ids.substring(2), 16));
                }
            }
            if (tempNode.getNodeType() == Node.ELEMENT_NODE
                    && tempNode.hasAttributes()
                    && tempNode.hasChildNodes()) {
                parseAttrList(tempNode.getChildNodes());
            }
        }
    }

    public int parseids(String key) {
        for (Map.Entry<String, Integer> entry : IdsMap.entrySet()) {
            if (entry.getKey().equals(key))
                return entry.getValue();
        }
        return -1;
    }

    public int parseStyleids(String key) {
        for (Map.Entry<String, Integer> entry : StyleIdsMap.entrySet()) {
            if (entry.getKey().equals(key))
                return entry.getValue();
        }
        return -1;
    }
}
