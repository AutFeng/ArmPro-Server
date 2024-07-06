package armadillo.utils.axml.EditXml.decode;


import armadillo.utils.axml.EditXml.io.ZInput;
import armadillo.utils.axml.EditXml.io.ZOutput;
import armadillo.utils.axml.EditXml.utils.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public abstract class BXMLNode {
    public Pair<Integer, Integer> mChunkSize = new Pair<>();
    public Pair<Integer, Integer> mLineNumber = new Pair<>();
    private StringBlock mStringBlock;
    private ResBlock mResBlock;

    public BXMLNode() {
        mLineNumber.first = 0;
        mLineNumber.second = 0;
        mChunkSize.first = 0;
        mChunkSize.second = 0;
    }

    private List<BXMLNode> mChild;

    public void checkTag(int expect, int value) throws IOException {
        if (value != expect) {
            throw new IOException("Can't read current node");
        }
    }

    public void readStart(ZInput reader) throws IOException {
        mChunkSize.first = reader.readInt();
        mLineNumber.first = reader.readInt();
    }

    public void readEnd(ZInput reader) throws IOException {
        mChunkSize.second = reader.readInt();
        mLineNumber.second = reader.readInt();
    }

    public void writeStart(ZOutput writer) throws IOException {
        writer.writeInt(mChunkSize.first);
        writer.writeInt(mLineNumber.first);
    }

    public void writeEnd(ZOutput writer) throws IOException {
        writer.writeInt(mChunkSize.second);
        writer.writeInt(mLineNumber.second);
    }

    public boolean hasChild() {
        return (mChild != null && !mChild.isEmpty());
    }

    public List<BXMLNode> getChildren() {
        return mChild;
    }

    public void addChild(BXMLNode node) {
        if (mChild == null) mChild = new ArrayList<>();
        if (node != null) {
            mChild.add(node);
        }
    }

    public void addChild(BXMLNode... node) {
        if (mChild == null) mChild = new ArrayList<>();
        mChild.addAll(Arrays.asList(node));
    }

    public abstract void prepare();

    public Pair<Integer, Integer> getSize() {
        return mChunkSize;
    }

    public Pair<Integer, Integer> getLineNumber() {
        return mLineNumber;
    }

    public StringBlock getmStringBlock() {
        return mStringBlock;
    }

    public void setmStringBlock(StringBlock mStringBlock) {
        this.mStringBlock = mStringBlock;
    }

    public ResBlock getmResBlock() {
        return mResBlock;
    }

    public void setmResBlock(ResBlock mResBlock) {
        this.mResBlock = mResBlock;
    }
}
