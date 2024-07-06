package armadillo.transformers.hook;


import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.LoaderRes;
import armadillo.utils.SHAUtils;
import armadillo.utils.SmaliUtils;
import armadillo.utils.StreamUtil;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;

public class HookVer extends OtherTransformer {
    @Override
    public void transform() throws Exception {
        String randon = nameFactory.randomName();
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        ManifestAppName appName = new ManifestAppName();
        ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), "arm." + randon);
        getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());

        byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/Hook.smali");
        String body = new String(bytes).replace("Lcom/cloud/freehandle/OldApp;", String.format("Larm/%s;",randon));
        body = body.replace("SrcApplication", xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "");
        body = body.replace("Data Type", "1");
        body = body.replace("Signer Data", "");
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
        MemoryDataStore dataStore = new MemoryDataStore();
        dexPool.writeTo(dataStore);
        getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
        dataStore.close();
    }

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return 0;
    }
}

