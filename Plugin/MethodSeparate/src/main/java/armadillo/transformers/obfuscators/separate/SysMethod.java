package armadillo.transformers.obfuscators.separate;

import armadillo.utils.LoaderRes;
import armadillo.utils.axml.XmlSecurity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.util.HashSet;

public class SysMethod {
    private static final String MANIFEST_ATTR_XML = "api-versions.xml";
    private static SysMethod instance;
    private final HashSet<String> Methods = new HashSet<>();

    public static SysMethod getInstance() {
        if (instance == null) {
            try {
                instance = new SysMethod();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private SysMethod() {
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
            if (tempNode.getNodeName().equals("class")) {
                String classdef = tempNode.getAttributes().getNamedItem("name").getNodeValue();
                Methods.add(classdef);
            }
            if (tempNode.getNodeType() == Node.ELEMENT_NODE
                    && tempNode.hasAttributes()
                    && tempNode.hasChildNodes()) {
                parseAttrList(tempNode.getChildNodes());
            }
        }
    }

    public boolean isSet(String classdef) {
        return Methods.contains(classdef.substring(1, classdef.length() - 1));
    }
}
