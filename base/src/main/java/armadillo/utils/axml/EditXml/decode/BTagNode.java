package armadillo.utils.axml.EditXml.decode;


import armadillo.utils.axml.EditXml.io.ZInput;
import armadillo.utils.axml.EditXml.io.ZOutput;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import armadillo.utils.axml.Manifest_ids;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class BTagNode extends BXMLNode {
    private final int TAG_START = 0x00100102;
    private final int TAG_END = 0x00100103;

    private String RawNSUri;
    private String RawName;


    private short mRawAttrCount;
    private short mRawClassAttr;
    private short mRawIdAttr;
    private short mRawStyleAttr;
    private List<Attribute> mRawAttrs;

    public BTagNode() {
    }

    public BTagNode(StringBlock stringBlock, String ns, String name) {
        RawName = name;
        RawNSUri = ns;
        if (RawName != null)
            stringBlock.putString(RawName);
        if (RawNSUri != null)
            stringBlock.putString(RawNSUri);
        setmStringBlock(stringBlock);
    }

    public String getRawName() {
        return RawName;
    }

    public void checkStartTag(int tag) throws IOException {
        checkTag(TAG_START, tag);
    }

    public void checkEndTag(int tag) throws IOException {
        checkTag(TAG_END, tag);
    }

    public void readStart(ZInput reader) throws IOException {
        super.readStart(reader);
        int xffff_ffff = reader.readInt();
        RawNSUri = getmStringBlock().getStringFor(reader.readInt());
        RawName = getmStringBlock().getStringFor(reader.readInt());
        int x0014_0014 = reader.readInt();
        mRawAttrCount = reader.readShort();
        mRawIdAttr = reader.readShort();
        mRawClassAttr = reader.readShort();
        mRawStyleAttr = reader.readShort();
        if (mRawAttrCount > 0) {
            mRawAttrs = new ArrayList<>();
            int[] attrs = reader.readIntArray(mRawAttrCount * Attribute.SIZE);
            boolean isExtry = false;
            for (int i = 0; i < mRawAttrCount; i++) {
                Attribute attribute = new Attribute(getmStringBlock(), subArray(attrs, i * Attribute.SIZE, Attribute.SIZE));
                if (attribute.Name.equals("extractNativeLibs")) {
                    isExtry = true;
                    continue;
                }
                mRawAttrs.add(attribute);
            }
            if (isExtry)
                mRawAttrCount--;
        }
    }

    public void readEnd(ZInput reader) throws IOException {
        super.readEnd(reader);
        int xffff_ffff = reader.readInt();
        String ns_uri = getmStringBlock().getStringFor(reader.readInt());
        String name = getmStringBlock().getStringFor(reader.readInt());
        if (!name.equals(RawName))
            throw new IOException("Invalid end element");
        if (RawNSUri != null && !ns_uri.equals(RawNSUri))
            throw new IOException("Invalid end element");
    }

    private static final int INT_SIZE = 4;

    public void prepare() {
        int base_first = INT_SIZE * 9;
        mRawAttrCount = (short) (mRawAttrs == null ? 0 : mRawAttrs.size());
        int attrSize = mRawAttrs == null ? 0 : mRawAttrs.size() * Attribute.SIZE * INT_SIZE;
        mChunkSize.first = base_first + attrSize;
        mChunkSize.second = INT_SIZE * 6;
    }

    public void writeStart(ZOutput writer) throws IOException {
        writer.writeInt(TAG_START);
        super.writeStart(writer);
        writer.writeInt(0xFFFFFFFF);
        writer.writeInt(RawNSUri == null ? -1 : getmStringBlock().getStringMapping(RawNSUri));
        writer.writeInt(RawName == null ? -1 : getmStringBlock().getStringMapping(RawName));
        writer.writeInt(0x00140014);
        writer.writeShort(mRawAttrCount);
        writer.writeShort(mRawIdAttr);
        writer.writeShort(mRawClassAttr);
        writer.writeShort(mRawStyleAttr);
        if (mRawAttrCount > 0) {
            mRawAttrs.sort(Comparator.comparingInt(o -> o.mId));
            for (Attribute attr : mRawAttrs) {
                //写NS
                writer.writeInt(attr.NameSpace == null ? -1 : getmStringBlock().getStringMapping(attr.NameSpace));
                //写NS:XXX
                writer.writeInt(attr.Name == null ? -1 : getmStringBlock().getStringMapping(attr.Name));
                //写String
                if ((attr.mType >> 24) == TypedValue.TYPE_STRING)
                    writer.writeInt(attr.String == null ? -1 : getmStringBlock().getStringMapping(attr.String));
                else
                    writer.writeInt(-1);
                //写Type
                writer.writeInt(attr.mType);
                //写Value
                if ((attr.mType >> 24) == TypedValue.TYPE_STRING)
                    writer.writeInt(getmStringBlock().getStringMapping(attr.String));
                else
                    writer.writeInt(attr.mValue);
            }
        }
    }

    public void writeEnd(ZOutput writer) throws IOException {
        writer.writeInt(TAG_END);
        super.writeEnd(writer);
        writer.writeInt(0xFFFFFFFF);
        writer.writeInt(RawNSUri == null ? -1 : getmStringBlock().getStringMapping(RawNSUri));
        writer.writeInt(RawName == null ? -1 : getmStringBlock().getStringMapping(RawName));
    }

    public Attribute[] getAttribute() {
        if (mRawAttrs == null)
            return new Attribute[0];
        else
            return mRawAttrs.toArray(new Attribute[mRawAttrs.size()]);

    }

    public void setAttribute(Attribute attr) {
        if (mRawAttrs == null)
            mRawAttrs = new ArrayList<>();
        mRawAttrs.add(attr);
    }

    public void addAttribute(Attribute... attr) {
        if (mRawAttrs == null)
            mRawAttrs = new ArrayList<>();
        Collections.addAll(mRawAttrs, attr);
    }

    public void setAttribute(Attribute[] attr) {
        if (mRawAttrs == null)
            mRawAttrs = new ArrayList<>();
        mRawAttrs.clear();
        Collections.addAll(mRawAttrs, attr);
    }

    public void setAttributes(Attribute... attr) {
        if (mRawAttrs == null)
            mRawAttrs = new ArrayList<>();
        mRawAttrs.clear();
        Collections.addAll(mRawAttrs, attr);
    }

    public static class Attribute {
        public static final int SIZE = 5;
        private final StringBlock stringBlock;
        public int mType;
        public int mValue;
        public int mId;

        public String NameSpace;
        public String Name;
        public String String;
        public String Value;

        public Attribute(StringBlock stringBlock, String ns, String name, int type) {
            this.stringBlock = stringBlock;
            if (ns != null)
                stringBlock.putString(ns);
            if (name != null)
                stringBlock.putString(name);
            NameSpace = ns;
            Name = name;
            mId = Manifest_ids.getInstance().parseids(Name);
            mType = (type << 24) | 0x000008;
        }

        public void setString(String data) {
            if ((mType >> 24) != TypedValue.TYPE_STRING)
                mType = (TypedValue.TYPE_STRING << 24) | 0x000008;
            if (stringBlock.getStringMapping(data) == -1)
                stringBlock.putString(data);
            Value = data;
            String = data;
        }

        public void setValue(int type, int value) {
            mType = (type << 24) | 0x000008;
            mValue = value;
            Value = null;
            String = null;
        }

        public Attribute(StringBlock stringBlock, int[] raw) {
            this.stringBlock = stringBlock;
            NameSpace = stringBlock.getStringFor(raw[0]);
            Name = stringBlock.getStringFor(raw[1]);
            String = stringBlock.getStringFor(raw[2]);
            mType = raw[3];
            mValue = raw[4];
            Value = stringBlock.getStringFor(mValue);
            mId = Manifest_ids.getInstance().parseids(Name);
        }
    }

    private int[] subArray(int[] src, int start, int len) {
        if ((start + len) > src.length)
            throw new RuntimeException("OutOfArrayBound");
        int[] des = new int[len];
        System.arraycopy(src, start, des, 0, len);
        return des;
    }
}
