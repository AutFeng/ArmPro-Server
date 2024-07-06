package armadillo.utils.axml.EditXml.decode;



import armadillo.utils.axml.EditXml.io.ZInput;
import armadillo.utils.axml.EditXml.io.ZOutput;
import armadillo.utils.axml.Manifest_ids;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AXMLDoc {
    private final int MAGIC_NUMBER = 0X00080003;
    private final int CHUNK_STRING_BLOCK = 0X001C0001;
    private final int CHUNK_RESOURCE_ID = 0X00080180;
    private final int CHUNK_XML_TREE = 0X00100100;
    private final String MANIFEST = "manifest";
    private final String APPLICATION = "application";
    private int mDocSize;
    private StringBlock mStringBlock;
    private ResBlock mResBlock;
    private BXMLTree mXMLTree;
    private InputStream is;

    public BXMLTree getBXMLTree() {
        return mXMLTree;
    }

    public BXMLNode getManifestNode() {
        List<BXMLNode> children = mXMLTree.getRoot().getChildren();
        for (BXMLNode node : children) {
            if (MANIFEST.equals(((BTagNode) node).getRawName())) {
                return node;
            }
        }
        return null;
    }

    public BXMLNode getApplicationNode() {
        BXMLNode manifest = getManifestNode();
        if (manifest == null) {
            return null;
        }
        for (BXMLNode node : manifest.getChildren()) {
            if (APPLICATION.equals(((BTagNode) node).getRawName())) {
                return node;
            }
        }
        return null;
    }

    public void build(OutputStream os) throws IOException {
        try(ZOutput writer = new ZOutput(os)){
            mStringBlock.prepare();
            List<Integer> attr_id = new ArrayList<>();
            for (String string : mStringBlock.getmStrings()) {
                int id = Manifest_ids.getInstance().parseids(string);
                if (id == -1)
                    break;
                attr_id.add(id);
            }
            int[] id_attr = new int[attr_id.size()];
            for (int i = 0; i < attr_id.size(); i++)
                id_attr[i] = attr_id.get(i);
            mResBlock.setmRawResIds(id_attr);
            mResBlock.prepare();
            mXMLTree.prepare();
            int base = 8;
            mDocSize = base + mStringBlock.getSize() + mResBlock.getSize() + mXMLTree.getSize();
            writer.writeInt(MAGIC_NUMBER);
            writer.writeInt(mDocSize);
            mStringBlock.write(writer);
            mResBlock.write(writer);
            mXMLTree.write(writer);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e);
        } finally {
            os.close();
        }
    }

    public void parse(InputStream is) throws Exception {
        this.is = is;
        ZInput reader = new ZInput(is);
        if (reader.readInt() != MAGIC_NUMBER)
            throw new Exception("Not valid AXML format");
        mDocSize = reader.readInt();
        int chunkType = reader.readInt();
        //解析字符串常量池
        if (chunkType == CHUNK_STRING_BLOCK)
            parseStringBlock(reader);
        chunkType = reader.readInt();
        //解析资源ID
        if (chunkType == CHUNK_RESOURCE_ID)
            parseResourceBlock(reader);
        chunkType = reader.readInt();
        //解析节点
        if (chunkType == CHUNK_XML_TREE)
            parseXMLTree(reader);

    }

    public void release() {
        try {
            if (is != null)
                is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseStringBlock(ZInput reader) throws Exception {
        mStringBlock = new StringBlock();
        mStringBlock.read(reader);
    }

    private void parseResourceBlock(ZInput reader) throws IOException {
        mResBlock = new ResBlock();
        mResBlock.read(reader);
        mStringBlock.setmResBlock(mResBlock);
    }

    private void parseXMLTree(ZInput reader) throws Exception {
        mXMLTree = new BXMLTree(mStringBlock,mResBlock);
        mXMLTree.read(reader);
    }

    public StringBlock getmStringBlock() {
        return mStringBlock;
    }

    public ResBlock getmResBlock() {
        return mResBlock;
    }
}
