package armadillo.utils.axml.EditXml.decode;

import armadillo.utils.axml.EditXml.io.ZInput;
import armadillo.utils.axml.EditXml.io.ZOutput;

import java.io.IOException;

public class ResBlock implements IAXMLSerialize {
    private static final int TAG = 0x00080180;
    private int mChunkSize;

    public void setmRawResIds(int[] mRawResIds) {
        this.mRawResIds = mRawResIds;
    }

    public boolean IsId(int id) {
        for (int i : mRawResIds) {
            if (i == id)
                return true;
        }
        return false;
    }

    private int[] mRawResIds;


    private final int INT_SIZE = 4;

    public void prepare() {
        int base = 2 * INT_SIZE;
        int resSize = mRawResIds == null ? 0 : mRawResIds.length * INT_SIZE;
        mChunkSize = base + resSize;
    }


    public int[] getResourceIds() {
        return mRawResIds;
    }

    public int getResourceIdAt(int index) {
        return mRawResIds[index];
    }

    @Override
    public int getSize() {
        return mChunkSize;
    }

    @Override
    public int getType() {
        return TAG;
    }

    @Override
    public void setSize(int size) {
    }

    @Override
    public void setType(int type) {
    }

    @Override
    public void read(ZInput reader) throws IOException {
        mChunkSize = reader.readInt();
        if (mChunkSize < 8 || (mChunkSize % 4) != 0)
            throw new IOException("Invalid resource ids size (" + mChunkSize + ").");
        mRawResIds = reader.readIntArray(mChunkSize / 4 - 2);
    }

    @Override
    public void write(ZOutput writer) throws IOException {
        writer.writeInt(TAG);
        writer.writeInt(mChunkSize);
        if (mRawResIds != null) {
            for (int id : mRawResIds) {
                writer.writeInt(id);
            }
        }
    }
}
