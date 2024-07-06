package armadillo.transformers.jiagu;

import armadillo.result.ignore;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.LoaderRes;
import armadillo.utils.SmaliUtils;
import armadillo.utils.StreamUtil;
import armadillo.utils.ZipUtils;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import com.alibaba.fastjson.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.zip.ZipEntry;

public class JiaGuV1 extends OtherTransformer {
    private final List<String> ignores = new ArrayList<>();

    @Override
    public void transform() throws Exception {
        Enumeration<? extends ZipEntry> entries = getZipFile().entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex") && !zipEntry.isDirectory()) {
                byte[] bytes = StreamUtil.readBytes(getZipFile().getInputStream(zipEntry));
                for (int i = 0; i < bytes.length; i++)
                    bytes[i] = (byte) (~bytes[i] & 0x00ff);
                getReplacerRes().put("assets/" + zipEntry.getName(), bytes);
                ignores.add(zipEntry.getName());
            }
        }
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        ManifestAppName appName = new ManifestAppName();
        ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), "arm.StubApp");
        getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
        byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/StubApp.smali");
        String body = new String(bytes).replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
        MemoryDataStore dataStore = new MemoryDataStore();
        dexPool.writeTo(dataStore);
        getReplacerRes().put("classes.dex", Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
        dataStore.close();
        if (getConfiguration().contains("so_framework")) {
            JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
            for (JsonElement jsonElement : SO_API) {
                switch (jsonElement.getAsString()) {
                    case "armeabi":
                        getReplacerRes().put("lib/armeabi/libarm_protect.so", LoaderRes.getInstance().getStaticResAsBytes("so/protect/armeabi-v7a/libarm_protect.so"));
                        break;
                    case "armeabi-v7a":
                        getReplacerRes().put("lib/armeabi-v7a/libarm_protect.so", LoaderRes.getInstance().getStaticResAsBytes("so/protect/armeabi-v7a/libarm_protect.so"));
                        break;
                    case "arm64-v8a":
                        getReplacerRes().put("lib/arm64-v8a/libarm_protect.so", LoaderRes.getInstance().getStaticResAsBytes("so/protect/arm64-v8a/libarm_protect.so"));
                        break;
                    case "x86":
                        getReplacerRes().put("lib/x86/libarm_protect.so", LoaderRes.getInstance().getStaticResAsBytes("so/protect/x86/libarm_protect.so"));
                        break;
                    case "x86_64":
                        getReplacerRes().put("lib/x86_64/libarm_protect.so", LoaderRes.getInstance().getStaticResAsBytes("so/protect/x86_64/libarm_protect.so"));
                        break;
                }
            }
        } else
            getReplacerRes().put("lib/armeabi-v7a/libarm_protect.so", LoaderRes.getInstance().getStaticResAsBytes("so/protect/armeabi-v7a/libarm_protect.so"));
    }

    @Override
    public List<String> getIgnores() {
        return ignores;
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
