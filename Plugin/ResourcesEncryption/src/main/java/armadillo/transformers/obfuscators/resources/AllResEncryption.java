package armadillo.transformers.obfuscators.resources;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AllResEncryption extends OtherTransformer {
    @Override
    public void transform() throws Exception {
        String randon = SHAUtils.SHA1(System.currentTimeMillis() + UUID.randomUUID().toString());
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        ManifestAppName appName = new ManifestAppName();
        ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), "armadillo." + randon);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        Enumeration<? extends ZipEntry> enumeration = getZipFile().entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.isDirectory()
                    || zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith("dex")
                    || zipEntry.getName().startsWith("lib/")
                    || zipEntry.getName().startsWith("META-INF/"))
                continue;
            ZipUtils.addZipEntry(zipOutputStream,new ZipEntry(zipEntry.getName()),StreamUtil.readBytes(getZipFile().getInputStream(zipEntry)));
        }
        zipOutputStream.close();
        getReplacerRes().put("Armadillo.rez", FullAssetsUtils.encrypt(outputStream.toByteArray(), "Armadillo"));
        getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
        byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/loadRes.smali");
        String body = new String(bytes).replace("LArmadillo/loadResApp;", "Larmadillo/" + randon + ";");
        body = body.replace("原入口", xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "");
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
