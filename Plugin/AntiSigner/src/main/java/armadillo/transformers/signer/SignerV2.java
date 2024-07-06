package armadillo.transformers.signer;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.editor.ContentProviderEditor;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;

public class SignerV2 extends OtherTransformer {
    @Override
    public void transform() throws Exception {
        String randon = nameFactory.randomName();
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AXMLDoc axmlDoc = new AXMLDoc();
        axmlDoc.parse(new ByteArrayInputStream(axml));

        ContentProviderEditor contentProviderEditor = new ContentProviderEditor(axmlDoc);
        contentProviderEditor.setEditorInfo(new ContentProviderEditor.Editorinfo()
                .with(new ContentProviderEditor.ProviderInfo("arm." + randon, randon, false, 19999999)));
        contentProviderEditor.commit();

        axmlDoc.build(outputStream);
        axmlDoc.release();
        getReplacerRes().put("AndroidManifest.xml", outputStream.toByteArray());

        byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/SignerV2.smali");
        JsonObject jsonObject = new JsonParser()
                .parse(configuration)
                .getAsJsonObject();
        String signer = jsonObject.has("app_signer") ? jsonObject.get("app_signer").getAsString() : ApkSignerUtils.getApkSignatureData(getZipFile());
        String body = new String(bytes)
                .replace("Larm/SignerV2;", String.format("Larm/%s;",randon))
                .replace("Signer Data", signer);
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
        MemoryDataStore dataStore = new MemoryDataStore();
        dexPool.writeTo(dataStore);
        getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
        dataStore.close();
    }

    @Override
    public String getResult() {
        return Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "signer.verify.tips"));
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

