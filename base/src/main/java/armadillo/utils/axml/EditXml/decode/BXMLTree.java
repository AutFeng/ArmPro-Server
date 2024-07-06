package armadillo.utils.axml.EditXml.decode;


import armadillo.utils.axml.EditXml.io.ZInput;
import armadillo.utils.axml.EditXml.io.ZOutput;
import armadillo.utils.axml.EditXml.utils.Pair;

import java.io.IOException;
import java.util.Stack;


public class BXMLTree implements IAXMLSerialize {
    private final int NS_START = 0x00100100;
    private final int NS_END = 0x00100101;
    private final int NODE_START = 0x00100102;
    private final int NODE_END = 0x00100103;

    private final Stack<BXMLNode> mVisitor = new Stack<>();
    private final BNSNode mRoot = new BNSNode();
    private int mSize;
    private final StringBlock mStringBlock;
    private final ResBlock mResBlock;

    public BXMLTree(StringBlock mStringBlock, ResBlock mResBlock) {
        this.mStringBlock = mStringBlock;
        this.mResBlock = mResBlock;
        mRoot.setmResBlock(mResBlock);
        mRoot.setmStringBlock(mStringBlock);
    }

    public void prepare() {
        mSize = 0;
        prepare(mRoot);
    }

    private void write(BXMLNode node, ZOutput writer) throws IOException {
        node.writeStart(writer);
        if (node.hasChild()) {
            for (BXMLNode child : node.getChildren())
                write(child, writer);
        }
        node.writeEnd(writer);
    }

    private void prepare(BXMLNode node) {
        node.prepare();
        Pair<Integer, Integer> p = node.getSize();
        mSize += p.first + p.second;
        if (node.hasChild()) {
            for (BXMLNode child : node.getChildren()) {
                prepare(child);
            }
        }
    }

    public int getSize() {
        return mSize;
    }

    public BXMLNode getRoot() {
        return mRoot;
    }


    @Override
    public int getType() {
        return 0;
    }

    @Override
    public void setSize(int size) {
    }

    @Override
    public void setType(int type) {
    }

    @Override
    public void read(ZInput reader) throws IOException {
        mRoot.checkStartTag(NS_START);
        mVisitor.push(mRoot);
        mRoot.readStart(reader);
        while (reader.available() > 0) {
            int chunkType = reader.readInt();
            switch (chunkType) {
                case NS_START: {
                    mRoot.checkStartTag(NS_START);
                    mRoot.readStart(reader);
                }
                break;
                case NODE_START: {
                    BTagNode node = new BTagNode();
                    node.setmResBlock(mResBlock);
                    node.setmStringBlock(mStringBlock);
                    node.checkStartTag(NODE_START);
                    BXMLNode parent = mVisitor.peek();
                    parent.addChild(node);
                    mVisitor.push(node);
                    node.readStart(reader);
                }
                break;
                case NODE_END: {
                    BTagNode node = (BTagNode) mVisitor.pop();
                    node.checkEndTag(NODE_END);
                    node.readEnd(reader);
                }
                break;
                case NS_END:
                    mRoot.checkEndTag(chunkType);
                    mRoot.readEnd(reader);
                    break;
            }
        }
        if (!mRoot.equals(mVisitor.pop()))
            throw new IOException("doc has invalid end");
    }

    @Override
    public void write(ZOutput writer) throws IOException {
        write(mRoot, writer);
    }
}
