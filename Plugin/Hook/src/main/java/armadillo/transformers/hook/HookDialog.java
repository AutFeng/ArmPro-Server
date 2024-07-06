package armadillo.transformers.hook;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.LoaderRes;
import armadillo.utils.SHAUtils;
import armadillo.utils.StreamUtil;
import armadillo.utils.ZipUtils;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.editor.ContentProviderEditor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;

public class HookDialog extends OtherTransformer {
    @Override
    public void transform() throws Exception {
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AXMLDoc axmlDoc = new AXMLDoc();
        axmlDoc.parse(new ByteArrayInputStream(axml));
        ContentProviderEditor contentProviderEditor = new ContentProviderEditor(axmlDoc);
        contentProviderEditor.setEditorInfo(new ContentProviderEditor.Editorinfo()
                .with(new ContentProviderEditor.ProviderInfo("arm.DialogHook", SHAUtils.SHA1(UUID.randomUUID().toString()), false, 19999999)));
        contentProviderEditor.commit();
        axmlDoc.build(outputStream);
        axmlDoc.release();
        getReplacerRes().put("AndroidManifest.xml", outputStream.toByteArray());
        getAdd_Classdex().add(LoaderRes.getInstance().getStaticResAsBytes("dex/dialog_hook.dex"));
        if (getConfiguration().contains("so_framework")) {
            JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
            for (JsonElement jsonElement : SO_API) {
                switch (jsonElement.getAsString()) {
                    case "armeabi":
                        getReplacerRes().put("lib/armeabi/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook-native.so"));
                        getReplacerRes().put("lib/armeabi/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook.so"));
                        break;
                    case "armeabi-v7a":
                        getReplacerRes().put("lib/armeabi-v7a/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook-native.so"));
                        getReplacerRes().put("lib/armeabi-v7a/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook.so"));
                        break;
                    case "arm64-v8a":
                        getReplacerRes().put("lib/arm64-v8a/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/arm64-v8a/libsandhook-native.so"));
                        getReplacerRes().put("lib/arm64-v8a/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/arm64-v8a/libsandhook.so"));
                        break;
                }
            }
        } else {
            getReplacerRes().put("lib/armeabi-v7a/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook-native.so"));
            getReplacerRes().put("lib/armeabi-v7a/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook.so"));
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
