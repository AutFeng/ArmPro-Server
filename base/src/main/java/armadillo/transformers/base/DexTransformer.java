package armadillo.transformers.base;

import org.apache.log4j.Logger;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class DexTransformer extends BaseTransformer {
    protected String configuration;
    protected HashMap<String, byte[]> ReplacerRes;
    protected HashSet<byte[]> Add_Classdex;
    protected byte[] data;
    protected DexBackedDexFile dexBackedDexFile;
    protected ZipFile zipFile;

    public final void init(String configuration, HashMap<String, byte[]> replacerRes, HashSet<byte[]> add_Classdex, byte[] data, DexBackedDexFile dexBackedDexFile, ZipFile zipFile) {
        this.configuration = configuration;
        this.ReplacerRes = replacerRes;
        this.Add_Classdex = add_Classdex;
        this.data = data;
        this.dexBackedDexFile = dexBackedDexFile;
        this.zipFile = zipFile;
    }

    protected String getConfiguration() {
        return configuration;
    }

    protected HashMap<String, byte[]> getReplacerRes() {
        return ReplacerRes;
    }

    protected HashSet<byte[]> getAdd_Classdex() {
        return Add_Classdex;
    }

    protected byte[] getData() {
        return data;
    }

    protected DexBackedDexFile getDexBackedDexFile() {
        return dexBackedDexFile;
    }

    protected ZipFile getZipFile() {
        return zipFile;
    }
}

