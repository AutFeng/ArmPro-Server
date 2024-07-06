package armadillo.utils.axml.EditXml.decode;

import armadillo.utils.axml.EditXml.io.ZInput;
import armadillo.utils.axml.EditXml.io.ZOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;

public class BNSNode extends BXMLNode {
    private final int TAG_START = 0x00100100;
    private final int TAG_END = 0x00100101;
    private final LinkedHashMap<String, String> ns = new LinkedHashMap<>();

    public void checkStartTag(int tag) throws IOException {
        checkTag(TAG_START, tag);
    }

    public void checkEndTag(int tag) throws IOException {
        checkTag(TAG_END, tag);
    }

    public void readStart(ZInput reader) throws IOException {
        super.readStart(reader);
        //FFFFFFFF
        reader.readInt();
        //NS
        String Prefix = getmStringBlock().getStringFor(reader.readInt());
        //Uri
        String Uri = getmStringBlock().getStringFor(reader.readInt());
        ns.put(Prefix, Uri);
    }

    public void readEnd(ZInput reader) throws IOException {
        super.readEnd(reader);
        //FFFFFFFF
        reader.readInt();
        //NS
        String prefix = getmStringBlock().getStringFor(reader.readInt());
        //Uri
        String uri = getmStringBlock().getStringFor(reader.readInt());
        if (!ns.containsKey(prefix) || !ns.containsValue(uri))
            throw new IOException("Invalid end element");
    }

    public void prepare() {
    }

    public void writeStart(ZOutput writer) throws IOException {
        for (Map.Entry<String, String> entry : ns.entrySet()) {
            //写Node头
            writer.writeInt(TAG_START);
            //写大小
            super.writeStart(writer);
            //写-1
            writer.writeInt(0xFFFFFFFF);
            //写Prefix字符串下标
            writer.writeInt(getmStringBlock().getStringMapping(entry.getKey()));
            //写Url字符串下标
            writer.writeInt(getmStringBlock().getStringMapping(entry.getValue()));
        }
    }

    public void writeEnd(ZOutput writer) throws IOException {
        ListIterator<Map.Entry<String, String>> i = new ArrayList<>(ns.entrySet()).listIterator(ns.size());
        while (i.hasPrevious()) {
            Map.Entry<String, String> entry = i.previous();
            //写Node头
            writer.writeInt(TAG_END);
            //写大小
            super.writeStart(writer);
            //写-1
            writer.writeInt(0xFFFFFFFF);
            //写Prefix字符串下标
            writer.writeInt(getmStringBlock().getStringMapping(entry.getKey()));
            //写Url字符串下标
            writer.writeInt(getmStringBlock().getStringMapping(entry.getValue()));
        }
    }
}
