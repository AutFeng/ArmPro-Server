package armadillo.transformers.hook;

import armadillo.Constant;
import armadillo.result.SignerInfo;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;

public class HookSignerCnfix extends OtherTransformer {
    @Override
    public void transform() throws Exception {
        String radom = SHAUtils.SHA1(UUID.randomUUID().toString()).substring(0, 8);
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        ManifestAppName appName = new ManifestAppName();
        ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), "arm." + radom);
        getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
        JsonElement type_json = new JsonParser().parse(getConfiguration()).getAsJsonObject().get(Long.toString(268435456));
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/CnfixSigner.smali");
        JsonObject jsonObject = new JsonParser()
                .parse(configuration)
                .getAsJsonObject();
        String signer = jsonObject.has("app_signer") ? jsonObject.get("app_signer").getAsString() : ApkSignerUtils.getApkSignatureData(getZipFile());
        String body = new String(bytes)
                .replace("Src Application", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"))
                .replace("Larm/HookSigner;", "Larm/" + radom + ";")
                .replace("Signer Data", signer)
                .replace("false", type_json.getAsInt() == 0 ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
        MemoryDataStore dataStore = new MemoryDataStore();
        dexPool.writeTo(dataStore);
        getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
        dataStore.close();
        switch (type_json.getAsInt()) {
            case 0:
                SignerInfo signerInfo = new SignerInfo(Lists.newArrayList(new SignerInfo.Signer("assets/arm", ZipEntry.DEFLATED)));
                getReplacerRes().put("Signer_mode", new Gson().toJson(signerInfo).getBytes());
                break;
            case 1:
                getReplacerRes().put("assets/arm", StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
                break;
        }
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
