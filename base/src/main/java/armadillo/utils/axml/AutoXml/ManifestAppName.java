package armadillo.utils.axml.AutoXml;


import armadillo.utils.axml.AutoXml.xml.decode.AXmlDecoder;
import armadillo.utils.axml.AutoXml.xml.decode.AXmlResourceParser;
import armadillo.utils.axml.AutoXml.xml.decode.XmlPullParser;
import armadillo.utils.axml.EditXml.utils.TypedValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ManifestAppName {
    public XmlMode parseManifest(InputStream is, String Name) throws IOException {
        XmlMode xmlMode = new XmlMode();
        AXmlDecoder axml = AXmlDecoder.decode(is);
        AXmlResourceParser parser = new AXmlResourceParser();
        parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);
        boolean success = false;
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG)
                continue;
            if (parser.getName().equals("manifest")) {
                int size = parser.getAttributeCount();
                for (int i = 0; i < size; ++i) {
                    if (parser.getAttributeName(i).equals("package")) {
                        xmlMode.setPackageName(parser.getAttributeValue(i));
                    }
                }
            } else if (parser.getName().equals("application")) {
                int size = parser.getAttributeCount();
                for (int i = 0; i < size; ++i) {
                    if (parser.getAttributeNameResource(i) == 0x01010003) {
                        xmlMode.setCustomApplication(true);
                        String attributeValue = parser.getAttributeValue(i);
                        if (attributeValue.startsWith("."))
                            attributeValue = xmlMode.getPackageName() + attributeValue;
                        else if (!attributeValue.contains("."))
                            attributeValue = xmlMode.getPackageName() + "." + attributeValue;
                        xmlMode.setCustomApplicationName(attributeValue);
                        int index = axml.mTableStrings.getSize();
                        byte[] data = axml.getData();
                        int off = parser.currentAttributeStart + 20 * i;
                        off += 8;
                        ManifestParse.writeInt(data, off, index);
                        off += 8;
                        ManifestParse.writeInt(data, off, index);
                    }
                    if (parser.getAttributeValueType(i) == TypedValue.TYPE_REFERENCE)
                        xmlMode.getRes_id().add(parser.getAttributeResourceValue(i, -1));
                }
                if (!xmlMode.isCustomApplication()) {
                    int off = parser.currentAttributeStart;
                    byte[] data = axml.getData();
                    byte[] newData = new byte[data.length + 20];
                    System.arraycopy(data, 0, newData, 0, off);
                    System.arraycopy(data, off, newData, off + 20, data.length - off);

                    // chunkSize
                    int chunkSize = ManifestParse.readInt(newData, off - 32);
                    ManifestParse.writeInt(newData, off - 32, chunkSize + 20);
                    // attributeCount
                    ManifestParse.writeInt(newData, off - 8, size + 1);

                    int idIndex = parser.findResourceID(0x01010003);
                    if (idIndex == -1)
                        throw new IOException("idIndex == -1");

                    boolean isMax = true;
                    for (int i = 0; i < size; ++i) {
                        int id = parser.getAttributeNameResource(i);
                        if (id > 0x01010003) {
                            isMax = false;
                            if (i != 0) {
                                System.arraycopy(newData, off + 20, newData, off, 20 * i);
                                off += 20 * i;
                            }
                            break;
                        }
                    }
                    if (isMax) {
                        System.arraycopy(newData, off + 20, newData, off, 20 * size);
                        off += 20 * size;
                    }

                    ManifestParse.writeInt(newData, off, axml.mTableStrings.find("http://schemas.android.com/apk/res/android"));
                    ManifestParse.writeInt(newData, off + 4, idIndex);
                    ManifestParse.writeInt(newData, off + 8, axml.mTableStrings.getSize());
                    ManifestParse.writeInt(newData, off + 12, 0x03000008);
                    ManifestParse.writeInt(newData, off + 16, axml.mTableStrings.getSize());
                    axml.setData(newData);
                }
                success = true;
            } else {
                int size = parser.getAttributeCount();
                for (int i = 0; i < size; ++i) {
                    if (parser.getAttributeValueType(i) == TypedValue.TYPE_REFERENCE)
                        xmlMode.getRes_id().add(parser.getAttributeResourceValue(i, -1));
                }
            }
        }
        if (!success)
            throw new IOException();
        ArrayList<String> list = new ArrayList<>(axml.mTableStrings.getSize());
        axml.mTableStrings.getStrings(list);
        list.add(Name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        axml.write(list, baos);
        xmlMode.setData(baos.toByteArray());
        return xmlMode;
    }

    public static class XmlMode {
        public boolean customApplication = false;
        public String customApplicationName;
        public String packageName;
        public byte[] data;
        public List<Integer> res_id = new ArrayList<>();

        public List<Integer> getRes_id() {
            return res_id;
        }

        public void setRes_id(List<Integer> res_id) {
            this.res_id = res_id;
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public boolean isCustomApplication() {
            return customApplication;
        }

        public void setCustomApplication(boolean customApplication) {
            this.customApplication = customApplication;
        }

        public String getCustomApplicationName() {
            return customApplicationName;
        }

        public void setCustomApplicationName(String customApplicationName) {
            this.customApplicationName = customApplicationName;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }
}
