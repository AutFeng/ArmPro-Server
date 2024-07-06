package armadillo.transformers.base;

import armadillo.model.SysUser;
import org.apache.log4j.Logger;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public abstract class OtherTransformer extends BaseTransformer {
    protected String configuration;
    protected HashMap<String, byte[]> ReplacerRes;
    protected HashSet<byte[]> Add_Classdex;
    protected ZipFile zipFile;
    protected SysUser sysUser;
    protected String uuid;

    public final void init(String configuration, HashMap<String, byte[]> replacerRes, HashSet<byte[]> add_Classdex, ZipFile zipFile, SysUser sysUser, String uuid) {
        this.configuration = configuration;
        this.ReplacerRes = replacerRes;
        this.Add_Classdex = add_Classdex;
        this.zipFile = zipFile;
        this.sysUser = sysUser;
        this.uuid = uuid;
    }


    protected String getConfiguration() {
        return configuration;
    }

    protected HashSet<byte[]> getAdd_Classdex() {
        return Add_Classdex;
    }

    protected HashMap<String, byte[]> getReplacerRes() {
        return ReplacerRes;
    }

    protected ZipFile getZipFile() {
        return zipFile;
    }

    protected SysUser getSysUser() {
        return sysUser;
    }

    protected String getUuid() {
        return uuid;
    }
}
