package armadillo.transformers.hook;

import armadillo.Constant;
import armadillo.result.SignerInfo;
import armadillo.result.ignore;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.gson.*;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class HookSignerPro extends OtherTransformer {
    private final List<String> ignores = new ArrayList<>();
    private final String Js = "var _0x4f90=\"bFhmYkk= V09FU0k= bEZoR3o= d2Fybg== R2xleWg= Y29tcGlsZQ== dGFibGU= aWZhbU0= XihbXiBdKyggK1teIF0rKSspK1teIF19 cmV0dXJuIC8iICsgdGhpcyArICIv cmVwbGFjZQ== R2ZLYXc= Q3RnRWw= aW50 QWNuZGw= T1dFeHA= eVZtVGo= eFZhY0I= Y29uc3RydWN0b3I= Y215aVU= VnVUbUw= RlJVS3Y= VWhzTE8= 包名 dll5cVk= Z21tYW4= dFpxeUo= c2ZSRWI= bG9n dEZZWk0= UGZtdmg= cUt3eEQ= aW5kZXhPZg== UHpSUko= c3BsaXQ= a1NPRHk= UW1PWW4= WXVtdUU= bGliYy5zbw== cG9pbnRlcg== S09NbGM= SWVRamc= emFSZ3U= WWFMVUg= SWtuTmw= SXZkdFo= dHJhY2U= cmV0dXJuIChmdW5jdGlvbigpIA== Zm9xR2I= dWp4Y3M= Hook路径 dlBhVE0= YXBwbHk= YWRBZ3k= a3p1U0I= SVBURnY= ZnVOamo= SkNRRmw= ZEt0WVo= a2FycHM= alFhZW8= RG1CTkQ= YWNyRkU= VUljYkY= elBxZEQ= SXRHcWE= RHZMT1k= VEpZY3U= ZGF0YS9hcHA= a3JpZnE= ZGVidWc= Y29uc29sZQ== eXp4dUs= Q1hReHk= YWxsb2NVdGY4U3RyaW5n TG50SHE= VHNJS3Y= e30uY29uc3RydWN0b3IoInJldHVybiB0aGlzIikoICk= dGVzdA== WmxoUEg= RVBmdVQ= YXR0YWNo SFJDV1g= SnRKQlI= WE90R0M= ZXhjZXB0aW9u SmNoWUE= emV6THg= VHp6Q0M= cmVhZENTdHJpbmc= Y05mSmQ= V1NMS1o= ZkRaZFg= RG9zU3A= Q0txQ0s= SnlsZmE= T0Z5Vlc= SHRqbXk=\".split(\" \");\n" +
            "(function(a,k){var f=function(b){for(;--b;)a.push(a.shift())};(function(){var b={data:{key:\"cookie\",value:\"timeout\"},setCookie:function(d,c,e,h){h=h||{};c=c+\"=\"+e;e=0;for(var l=d.length;e<l;e++){l=d[e];c+=\"; \"+l;var m=d[l];d.push(m);l=d.length;!0!==m&&(c+=\"=\"+m)}h.cookie=c},removeCookie:function(){return\"dev\"},getCookie:function(d,c){d=d||function(e){return e};d=d(new RegExp(\"(?:^|; )\"+c.replace(/([.$?*|{}()[]\\/+^])/g,\"$1\")+\"=([^;]*)\"));(function(e,h){e(++h)})(f,k);return d?decodeURIComponent(d[1]):\n" +
            "void 0},updateCookie:function(){return/\\w+ *\\(\\) *{\\w+ *['|\"].+['|\"];? *}/.test(b.removeCookie.toString())}},g=b.updateCookie();g?g?b.getCookie(null,\"counter\"):b.removeCookie():b.setCookie([\"*\"],\"counter\",1)})()})(_0x4f90,289);\n" +
            "var _0x2cf0=function(a,k){a-=0;k=_0x4f90[a];void 0===_0x2cf0.PriHGt&&(function(){var b=function(){try{var g=Function('return (function() {}.constructor(\"return this\")( ));')()}catch(d){g=window}return g}();b.atob||(b.atob=function(g){g=String(g).replace(/=+$/,\"\");for(var d=\"\",c=0,e,h,l=0;h=g.charAt(l++);~h&&(e=c%4?64*e+h:h,c++%4)?d+=String.fromCharCode(255&e>>(-2*c&6)):0)h=\"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=\".indexOf(h);return d})}(),_0x2cf0.aRUOoU=function(b){b=atob(b);\n" +
            "for(var g=[],d=0,c=b.length;d<c;d++)g+=\"%\"+(\"00\"+b.charCodeAt(d).toString(16)).slice(-2);return decodeURIComponent(g)},_0x2cf0.TcbQoI={},_0x2cf0.PriHGt=!0);var f=_0x2cf0.TcbQoI[a];void 0===f?(f=function(b){this.lrUMQR=b;this.MaYHRt=[1,0,0];this.jXBVLY=function(){return\"newState\"};this.nXZVoi=\"\\\\w+ *\\\\(\\\\) *{\\\\w+ *\";this.JDoYHA=\"['|\\\"].+['|\\\"];? *}\"},f.prototype.mZxrno=function(){var b=(new RegExp(this.nXZVoi+this.JDoYHA)).test(this.jXBVLY.toString())?--this.MaYHRt[1]:--this.MaYHRt[0];return this.oJnzMK(b)},\n" +
            "f.prototype.oJnzMK=function(b){return~b?this.SPEoiW(this.lrUMQR):b},f.prototype.SPEoiW=function(b){for(var g=0,d=this.MaYHRt.length;g<d;g++)this.MaYHRt.push(Math.round(Math.random())),d=this.MaYHRt.length;return b(this.MaYHRt[0])},(new f(_0x2cf0)).mZxrno(),k=_0x2cf0.aRUOoU(k),_0x2cf0.TcbQoI[a]=k):k=f;return k},_0x304930=function(){var a={};a[_0x2cf0(\"0x45\")]=function(f,b){return f!==b};a[_0x2cf0(\"0x23\")]=function(f,b){return f!=b};a[_0x2cf0(\"0x7\")]=function(f,b){return f(b)};a.acrFE=function(f,b){return f!=\n" +
            "b};a.IeQjg=_0x2cf0(\"0x1c\");a[_0x2cf0(\"0x50\")]=function(f,b){return f!=b};a[_0x2cf0(\"0x51\")]=_0x2cf0(\"0x13\");a[_0x2cf0(\"0x15\")]=\"FSGfx\";var k=!0;return function(f,b){if(a[_0x2cf0(\"0x45\")](a.TsIKv,a.yVmTj)){var g=k?function(){if(b){var d=b[_0x2cf0(\"0x39\")](f,arguments);b=null;return d}}:function(){};k=!1;return g}g=args[0];a.zPqdD(g,void 0)&&a[_0x2cf0(\"0x23\")](g,null)&&(g=a[_0x2cf0(\"0x7\")](ptr,g)[_0x2cf0(\"0x5e\")](),is_hook=-1!=g.indexOf(\"data/app\")&&a[_0x2cf0(\"0x43\")](g[_0x2cf0(\"0x25\")](a[_0x2cf0(\"0x2e\")]),\n" +
            "-1)&&a[_0x2cf0(\"0x50\")](g.indexOf(\"base.apk\"),-1)?!0:!1)}}(),_0xc26807=_0x304930(this,function(){var a={};a[_0x2cf0(\"0x3c\")]=function(f,b){return f!==b};a[_0x2cf0(\"0x1e\")]=\"GuYya\";a[_0x2cf0(\"0x4e\")]='return /\" + this + \"/';a[_0x2cf0(\"0x40\")]=function(f){return f()};var k=function(){if(a.IPTFv(_0x2cf0(\"0x1\"),a[_0x2cf0(\"0x1e\")]))return!k.constructor(a.CXQxy)()[_0x2cf0(\"0xa\")](_0x2cf0(\"0xd\"))[_0x2cf0(\"0x53\")](_0xc26807);is_hook=!0};return a.karps(k)});_0xc26807();\n" +
            "var _0x7ca97f=function(){var a={};a[_0x2cf0(\"0x5c\")]=function(f,b){return f!==b};a[_0x2cf0(\"0x4a\")]=_0x2cf0(\"0x24\");var k=!0;return function(f,b){var g=k?function(){if(a[_0x2cf0(\"0x5c\")](a[_0x2cf0(\"0x4a\")],_0x2cf0(\"0x3f\"))){if(b){var d=b[_0x2cf0(\"0x39\")](f,arguments);b=null;return d}}else is_hook=!1}:function(){};k=!1;return g}}(),_0x1683f0=_0x7ca97f(this,function(){var a={};a[_0x2cf0(\"0x32\")]=function(e,h,l){return e(h,l)};a.fDZdX=_0x2cf0(\"0xe\");a[_0x2cf0(\"0x38\")]=_0x2cf0(\"0x3b\");a.xVacB=\"5|8|9|6|0|2|7|3|4|1\";\n" +
            "a[_0x2cf0(\"0x44\")]=function(e,h){return e===h};a.IknNl=_0x2cf0(\"0x4\");a[_0x2cf0(\"0x19\")]=function(e,h){return e(h)};a[_0x2cf0(\"0x5d\")]=function(e,h){return e+h};a[_0x2cf0(\"0x2f\")]=function(e,h){return e+h};a.kSODy=_0x2cf0(\"0x34\");a.kArRR='{}.constructor(\"return this\")( )';a[_0x2cf0(\"0x60\")]=function(e){return e()};a[_0x2cf0(\"0x14\")]=_0x2cf0(\"0x58\");a.cJtaH=function(e,h){return e!==h};a[_0x2cf0(\"0x2\")]=_0x2cf0(\"0x1a\");a[_0x2cf0(\"0x1b\")]=_0x2cf0(\"0x5f\");var k=function(){};try{if(a[_0x2cf0(\"0x44\")](a[_0x2cf0(\"0x31\")],\n" +
            "a[_0x2cf0(\"0x31\")])){var f=a[_0x2cf0(\"0x19\")](Function,a[_0x2cf0(\"0x5d\")](a[_0x2cf0(\"0x2f\")](a[_0x2cf0(\"0x28\")],a.kArRR),\");\"));var b=a[_0x2cf0(\"0x60\")](f)}else if(is_hook){var g=a[_0x2cf0(\"0x32\")](open,path,0);retval.replace(g)}}catch(e){if(a[_0x2cf0(\"0x14\")]!==a[_0x2cf0(\"0x14\")]){var d={};d[_0x2cf0(\"0x0\")]=a[_0x2cf0(\"0x61\")];var c=function(){return!c.constructor(d[_0x2cf0(\"0x0\")])()[_0x2cf0(\"0xa\")](_0x2cf0(\"0xd\")).test(_0xc26807)};return c()}b=window}if(b[_0x2cf0(\"0x4c\")])b[_0x2cf0(\"0x4c\")][_0x2cf0(\"0x21\")]=\n" +
            "k,b[_0x2cf0(\"0x4c\")][_0x2cf0(\"0x8\")]=k,b[_0x2cf0(\"0x4c\")][_0x2cf0(\"0x4b\")]=k,b[_0x2cf0(\"0x4c\")].info=k,b.console.error=k,b.console[_0x2cf0(\"0x5a\")]=k,b[_0x2cf0(\"0x4c\")][_0x2cf0(\"0xb\")]=k,b.console.trace=k;else if(a.cJtaH(a.Jylfa,a[_0x2cf0(\"0x1b\")]))b.console=function(e){if(a[_0x2cf0(\"0x38\")]!==a[_0x2cf0(\"0x38\")])return e=firstCall?function(){if(fn){var n=fn[_0x2cf0(\"0x39\")](context,arguments);fn=null;return n}}:function(){},firstCall=!1,e;for(var h=a[_0x2cf0(\"0x16\")][_0x2cf0(\"0x27\")](\"|\"),l=0;;){switch(h[l++]){case \"0\":m.info=\n" +
            "e;continue;case \"1\":return m;case \"2\":m.error=e;continue;case \"3\":m.table=e;continue;case \"4\":m[_0x2cf0(\"0x33\")]=e;continue;case \"5\":var m={};continue;case \"6\":m[_0x2cf0(\"0x4b\")]=e;continue;case \"7\":m.exception=e;continue;case \"8\":m.log=e;continue;case \"9\":m.warn=e;continue}break}}(k);else return!test[_0x2cf0(\"0x17\")](a[_0x2cf0(\"0x61\")])()[_0x2cf0(\"0xa\")](\"^([^ ]+( +[^ ]+)+)+[^ ]}\")[_0x2cf0(\"0x53\")](_0xc26807)});_0x1683f0();\n" +
            "setImmediate(function(){var a={};a[_0x2cf0(\"0x2d\")]=function(d,c){return d(c)};a[_0x2cf0(\"0x9\")]=function(d,c){return d+c};a[_0x2cf0(\"0xc\")]=_0x2cf0(\"0x34\");a[_0x2cf0(\"0x11\")]=function(d,c){return d!==c};a[_0x2cf0(\"0x46\")]=\"eWFwb\";a[_0x2cf0(\"0x42\")]=function(d,c){return d!==c};a[_0x2cf0(\"0x4d\")]=function(d,c){return d!=c};a[_0x2cf0(\"0x54\")]=function(d,c){return d!==c};a.JchYA=_0x2cf0(\"0x18\");a[_0x2cf0(\"0x47\")]=function(d,c){return d(c)};a[_0x2cf0(\"0x26\")]=_0x2cf0(\"0x49\");a[_0x2cf0(\"0x6\")]=\"base.apk\";\n" +
            "a[_0x2cf0(\"0x3d\")]=function(d,c){return d===c};a[_0x2cf0(\"0x41\")]=function(d,c){return d===c};a[_0x2cf0(\"0x55\")]=_0x2cf0(\"0x3a\");a[_0x2cf0(\"0x2a\")]=_0x2cf0(\"0x20\");a[_0x2cf0(\"0x36\")]=function(d,c){return d!==c};a[_0x2cf0(\"0x10\")]=_0x2cf0(\"0x57\");a[_0x2cf0(\"0x3\")]=_0x2cf0(\"0x5\");a[_0x2cf0(\"0x59\")]=function(d,c,e){return d(c,e)};a.tZqyJ=_0x2cf0(\"0x2b\");a[_0x2cf0(\"0x22\")]=\"open\";a[_0x2cf0(\"0x29\")]=\"int\";var k=Module.getExportByName(a[_0x2cf0(\"0x1f\")],a[_0x2cf0(\"0x22\")]),f=new NativeFunction(k,_0x2cf0(\"0x12\"),\n" +
            "[_0x2cf0(\"0x2c\"),a[_0x2cf0(\"0x29\")]]),b=Memory[_0x2cf0(\"0x4f\")](_0x2cf0(\"0x37\")),g=!1;Interceptor[_0x2cf0(\"0x56\")](Module.findExportByName(a[_0x2cf0(\"0x1f\")],a[_0x2cf0(\"0x22\")]),{onEnter:function(d){var c={awqih:function(h,l){return a.KOMlc(h,l)}};c[_0x2cf0(\"0x30\")]=function(h,l){return a.Gleyh(h,l)};c[_0x2cf0(\"0x3e\")]=a[_0x2cf0(\"0xc\")];c[_0x2cf0(\"0x48\")]=_0x2cf0(\"0x52\");if(a[_0x2cf0(\"0x11\")](\"kYrnS\",a[_0x2cf0(\"0x46\")])){var e=d[0];a[_0x2cf0(\"0x42\")](e,void 0)&&a.yzxuK(e,null)&&(a[_0x2cf0(\"0x54\")](\"JNkmi\",\n" +
            "a[_0x2cf0(\"0x5b\")])?(c=a[_0x2cf0(\"0x47\")](ptr,e)[_0x2cf0(\"0x5e\")](),a.yzxuK(c[_0x2cf0(\"0x25\")](a[_0x2cf0(\"0x26\")]),-1)&&-1!=c[_0x2cf0(\"0x25\")](_0x2cf0(\"0x1c\"))&&a[_0x2cf0(\"0x4d\")](c.indexOf(a[_0x2cf0(\"0x6\")]),-1)?g=!0:a[_0x2cf0(\"0x3d\")](_0x2cf0(\"0x1d\"),_0x2cf0(\"0x35\"))?that=window:g=!1):that=c.awqih(Function,c[_0x2cf0(\"0x30\")](c[_0x2cf0(\"0x3e\")],c[_0x2cf0(\"0x48\")])+\");\")())}else return c=fn[_0x2cf0(\"0x39\")](context,arguments),fn=null,c},onLeave:function(d){if(a[_0x2cf0(\"0x41\")](a.EPfuT,a[_0x2cf0(\"0x2a\")])){if(fn){var c=\n" +
            "fn[_0x2cf0(\"0x39\")](context,arguments);fn=null;return c}}else if(g)if(a[_0x2cf0(\"0x36\")](a[_0x2cf0(\"0x10\")],a.OFyVW))c=a.XOtGC(f,b,0),d[_0x2cf0(\"0xf\")](c);else return c=fn.apply(context,arguments),fn=null,c}})});";

    @Override
    public void transform() throws Exception {
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        JsonElement type_json = new JsonParser().parse(getConfiguration()).getAsJsonObject().get(Long.toString(33554432));
        File cacheLibs = null;
        File cacheCpp = null;
        JsonObject jsonObject = new JsonParser()
                .parse(configuration)
                .getAsJsonObject();
        String signer = jsonObject.has("app_signer") ? jsonObject.get("app_signer").getAsString() : ApkSignerUtils.getApkSignatureData(getZipFile());
        ManifestAppName appName = new ManifestAppName();
        ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), "arm.StubApp");
        if (type_json.getAsInt() == 6 || type_json.getAsInt() == 7) {
            AXMLDoc axmlDoc = new AXMLDoc();
            axmlDoc.parse(new ByteArrayInputStream(xmlMode.getData()));
            for (BXMLNode child : axmlDoc.getManifestNode().getChildren()) {
                BTagNode node = (BTagNode) child;
                if ("application".equals(node.getRawName())) {
                    BTagNode.Attribute[] attributes = node.getAttribute();
                    boolean flag = false;
                    for (BTagNode.Attribute attribute : attributes) {
                        if (attribute.Name.equals("debuggable")) {
                            flag = !flag;
                            attribute.setValue(TypedValue.TYPE_INT_BOOLEAN, 1);
                        }
                    }
                    if (flag)
                        node.setAttribute(attributes);
                    else {
                        List<BTagNode.Attribute> new_attr = new ArrayList<>(Arrays.asList(attributes));
                        BTagNode.Attribute debuggable = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "debuggable", TypedValue.TYPE_INT_BOOLEAN);
                        debuggable.setValue(TypedValue.TYPE_INT_BOOLEAN, 1);
                        new_attr.add(debuggable);
                        node.setAttribute(new_attr.toArray(new BTagNode.Attribute[0]));
                    }
                }
            }
            ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
            axmlDoc.build(xmlOut);
            axmlDoc.release();
            getReplacerRes().put("AndroidManifest.xml", xmlOut.toByteArray());
            xmlOut.close();
        } else
            getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
        switch (type_json.getAsInt()) {
            /**
             * frida
             */
            case 0:
            case 1: {
                String config = "{\"interaction\":{\"type\":\"script\",\"path\":\"/sdcard/Android/data/" + xmlMode.getPackageName() + "/files/arm.so\",\"on_change\":\"reload\"}}";
                byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/RemoveSignerPro.smali");
                String body = new String(bytes).replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                DexPool dexPool = new DexPool(Opcodes.getDefault());
                dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
                if (xmlMode.isCustomApplication() && xmlMode.getCustomApplicationName().equals("com.stub.StubApp")) {
                    Enumeration<? extends ZipEntry> entries = getZipFile().entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                            DexBackedDexFile dexFile =
                                    DexBackedDexFile.fromInputStream(
                                            Opcodes.getDefault(),
                                            new BufferedInputStream(getZipFile().getInputStream(zipEntry)));
                            for (DexBackedClassDef classDef : dexFile.getClasses())
                                dexPool.internClass(classDef);
                            ignores.add(zipEntry.getName());
                        }
                    }
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getReplacerRes().put("classes.dex", Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                } else {
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                }
                cacheCpp = new File(Constant.getCache(), SHAUtils.SHA1(getUuid()) + "-signer-cache");
                if (!cacheCpp.exists())
                    cacheCpp.mkdirs();
                ZipUtils.zipUncompress(new ZipInputStream(LoaderRes.getInstance().getStaticResAsStream("zip/frida_hook_signer.zip")), cacheCpp.getAbsolutePath());
                File cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "cpp" +
                        File.separator +
                        "output" +
                        File.separator +
                        "arm_StubApp.cpp");
                byte[] cpp_bytes = StreamUtil.readBytes(new FileInputStream(cpp_file));
                String cpp_body = new String(cpp_bytes)
                        .replace("Js Data",
                                Base64.getEncoder().encodeToString(
                                        Js.replace(
                                                "Hook路径",
                                                Base64.getEncoder().encodeToString(("/sdcard/Android/data/" + xmlMode.getPackageName() + "/files/base.apk").getBytes())
                                        ).replace(
                                                "包名",
                                                Base64.getEncoder().encodeToString(xmlMode.getPackageName().getBytes())
                                        ).getBytes()))
                        .replace("Signer Data", signer)
                        .replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                FileOutputStream outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String so_api = "APP_ABI := armeabi-v7a";
                if (getConfiguration().contains("so_framework")) {
                    StringBuilder builder = new StringBuilder();
                    JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                    for (JsonElement jsonElement : SO_API) {
                        if (jsonElement.getAsString().equals("x86_64"))
                            continue;
                        if (jsonElement.getAsString().equals("armeabi")) {
                            if (!builder.toString().contains("armeabi-v7a"))
                                builder.append("armeabi-v7a").append(" ");
                        } else
                            builder.append(jsonElement.getAsString()).append(" ");
                    }
                    so_api = "APP_ABI := " + builder.toString();
                }
                cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "Application.mk");
                cpp_body = new String(StreamUtil.readBytes(new FileInputStream(cpp_file))) + "\n" + so_api;
                outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String[] cmd;
                if (OsUtils.isOSLinux())
                    cmd = new String[]{"/www/basic/ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                else
                    cmd = new String[]{"cmd.exe",
                            "/c",
                            "ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                try {
                    if (exec(cmd)) {
                        cacheLibs = new File(cacheCpp, "libs");
                        if (!cacheLibs.exists() || !cacheLibs.isDirectory()) {
                            FileUtils.delete(cacheCpp);
                            throw new Exception("NDK Build failed....");
                        }
                        if (getConfiguration().contains("so_framework")) {
                            JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                            for (JsonElement jsonElement : SO_API) {
                                switch (jsonElement.getAsString()) {
                                    case "armeabi":
                                        getReplacerRes().put("lib/armeabi/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/armeabi/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/armeabi-v7a/libarm_loader.so"));
                                        getReplacerRes().put("lib/armeabi/libarm_hook.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/armeabi-v7a/libarm_hook.so"));
                                        getReplacerRes().put("lib/armeabi/libarm_hook.config.so", config.getBytes());
                                        break;
                                    case "armeabi-v7a":
                                        getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/armeabi-v7a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/armeabi-v7a/libarm_loader.so"));
                                        getReplacerRes().put("lib/armeabi-v7a/libarm_hook.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/armeabi-v7a/libarm_hook.so"));
                                        getReplacerRes().put("lib/armeabi-v7a/libarm_hook.config.so", config.getBytes());
                                        break;
                                    case "arm64-v8a":
                                        getReplacerRes().put("lib/arm64-v8a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "arm64-v8a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/arm64-v8a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/arm64-v8a/libarm_loader.so"));
                                        getReplacerRes().put("lib/arm64-v8a/libarm_hook.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/arm64-v8a/libarm_hook.so"));
                                        getReplacerRes().put("lib/arm64-v8a/libarm_hook.config.so", config.getBytes());
                                        break;
                                    case "x86":
                                        getReplacerRes().put("lib/x86/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "x86" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/x86/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/x86/libarm_loader.so"));
                                        getReplacerRes().put("lib/x86/libarm_hook.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/x86/libarm_hook.so"));
                                        getReplacerRes().put("lib/x86/libarm_hook.config.so", config.getBytes());
                                        break;
                                }
                            }
                        } else {
                            getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                            getReplacerRes().put("lib/armeabi-v7a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/armeabi-v7a/libarm_loader.so"));
                            getReplacerRes().put("lib/armeabi-v7a/libarm_hook.so", LoaderRes.getInstance().getStaticResAsBytes("so/frida/armeabi-v7a/libarm_hook.so"));
                            getReplacerRes().put("lib/armeabi-v7a/libarm_hook.config.so", config.getBytes());
                        }
                    } else {
                        FileUtils.delete(cacheCpp);
                        throw new Exception("NDK Build failed....");
                    }
                } catch (ThreadDeath threadDeath) {
                    if (Constant.isDevelopment())
                        logger.info("NDK编译/删除临时文件");
                    FileUtils.delete(cacheCpp);
                    throw new ThreadDeath();
                } finally {
                    FileUtils.delete(cacheCpp);
                }
            }
            break;
            /**
             * Sandhook
             */
            case 2:
            case 3: {
                byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/RemoveSignerPro.smali");
                String body = new String(bytes).replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                DexPool dexPool = new DexPool(Opcodes.getDefault());
                dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
                if (xmlMode.isCustomApplication() && xmlMode.getCustomApplicationName().equals("com.stub.StubApp")) {
                    Enumeration<? extends ZipEntry> entries = getZipFile().entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                            DexBackedDexFile dexFile =
                                    DexBackedDexFile.fromInputStream(
                                            Opcodes.getDefault(),
                                            new BufferedInputStream(getZipFile().getInputStream(zipEntry)));
                            for (DexBackedClassDef classDef : dexFile.getClasses())
                                dexPool.internClass(classDef);
                            ignores.add(zipEntry.getName());
                        }
                    }
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getReplacerRes().put("classes.dex", Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                } else {
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                }
                cacheCpp = new File(Constant.getCache(), SHAUtils.SHA1(getUuid()) + "-signer-cache");
                if (!cacheCpp.exists())
                    cacheCpp.mkdirs();
                ZipUtils.zipUncompress(new ZipInputStream(LoaderRes.getInstance().getStaticResAsStream("zip/sandhook_signer.zip")), cacheCpp.getAbsolutePath());
                File cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "cpp" +
                        File.separator +
                        "output" +
                        File.separator +
                        "arm_StubApp.cpp");
                byte[] cpp_bytes = StreamUtil.readBytes(new FileInputStream(cpp_file));
                String cpp_body = new String(cpp_bytes)
                        .replace("Signer Data", signer)
                        .replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                FileOutputStream outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String so_api = "APP_ABI := armeabi-v7a";
                if (getConfiguration().contains("so_framework")) {
                    StringBuilder builder = new StringBuilder();
                    JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                    for (JsonElement jsonElement : SO_API) {
                        if (jsonElement.getAsString().equals("x86") || jsonElement.getAsString().equals("x86_64"))
                            continue;
                        if (jsonElement.getAsString().equals("armeabi")) {
                            if (!builder.toString().contains("armeabi-v7a"))
                                builder.append("armeabi-v7a").append(" ");
                        } else
                            builder.append(jsonElement.getAsString()).append(" ");
                    }
                    so_api = "APP_ABI := " + builder.toString();
                }
                cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "Application.mk");
                cpp_body = new String(StreamUtil.readBytes(new FileInputStream(cpp_file))) + "\n" + so_api;
                outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String[] cmd;
                if (OsUtils.isOSLinux())
                    cmd = new String[]{"/www/basic/ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                else
                    cmd = new String[]{"cmd.exe",
                            "/c",
                            "ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                try {
                    if (exec(cmd)) {
                        cacheLibs = new File(cacheCpp, "libs");
                        if (!cacheLibs.exists() || !cacheLibs.isDirectory()) {
                            FileUtils.delete(cacheCpp);
                            throw new Exception("NDK Build failed....");
                        }
                        if (getConfiguration().contains("so_framework")) {
                            JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                            for (JsonElement jsonElement : SO_API) {
                                switch (jsonElement.getAsString()) {
                                    case "armeabi":
                                        getReplacerRes().put("lib/armeabi/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/armeabi/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libarm_loader.so"));
                                        break;
                                    case "armeabi-v7a":
                                        getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/armeabi-v7a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libarm_loader.so"));
                                        break;
                                    case "arm64-v8a":
                                        getReplacerRes().put("lib/arm64-v8a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "arm64-v8a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/arm64-v8a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/arm64-v8a/libarm_loader.so"));
                                        break;
                                }
                            }
                        } else {
                            getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                            getReplacerRes().put("lib/armeabi-v7a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libarm_loader.so"));
                        }
                    } else {
                        FileUtils.delete(cacheCpp);
                        throw new Exception("NDK Build failed....");
                    }
                } catch (ThreadDeath threadDeath) {
                    if (Constant.isDevelopment())
                        logger.info("NDK编译/删除临时文件");
                    FileUtils.delete(cacheCpp);
                    throw new ThreadDeath();
                } finally {
                    FileUtils.delete(cacheCpp);
                }
            }
            break;
            /**
             * sandhook xposed
             */
            case 4:
            case 5: {
                byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/SandHookSignerXposed.smali");
                String body = new String(bytes).replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                DexPool dexPool = new DexPool(Opcodes.getDefault());
                dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
                DexBackedDexFile sandhook_dex = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new BufferedInputStream(LoaderRes.getInstance().getStaticResAsStream("dex/sandhook_xposed.dex")));
                for (DexBackedClassDef dexClass : sandhook_dex.getClasses())
                    dexPool.internClass(dexClass);
                if (xmlMode.isCustomApplication() && xmlMode.getCustomApplicationName().equals("com.stub.StubApp")) {
                    Enumeration<? extends ZipEntry> entries = getZipFile().entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                            DexBackedDexFile dexFile =
                                    DexBackedDexFile.fromInputStream(
                                            Opcodes.getDefault(),
                                            new BufferedInputStream(getZipFile().getInputStream(zipEntry)));
                            for (DexBackedClassDef classDef : dexFile.getClasses())
                                dexPool.internClass(classDef);
                            ignores.add(zipEntry.getName());
                        }
                    }
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getReplacerRes().put("classes.dex", Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                } else {
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                }
                cacheCpp = new File(Constant.getCache(), SHAUtils.SHA1(getUuid()) + "-signer-cache");
                if (!cacheCpp.exists())
                    cacheCpp.mkdirs();
                ZipUtils.zipUncompress(new ZipInputStream(LoaderRes.getInstance().getStaticResAsStream("zip/sandhook_signer_xposed.zip")), cacheCpp.getAbsolutePath());
                File cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "cpp" +
                        File.separator +
                        "output" +
                        File.separator +
                        "arm_StubApp.cpp");
                byte[] cpp_bytes = StreamUtil.readBytes(new FileInputStream(cpp_file));
                String cpp_body = new String(cpp_bytes)
                        .replace("Signer Data", signer)
                        .replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                FileOutputStream outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String so_api = "APP_ABI := armeabi-v7a";
                if (getConfiguration().contains("so_framework")) {
                    StringBuilder builder = new StringBuilder();
                    JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                    for (JsonElement jsonElement : SO_API) {
                        if (jsonElement.getAsString().equals("x86") || jsonElement.getAsString().equals("x86_64"))
                            continue;
                        if (jsonElement.getAsString().equals("armeabi")) {
                            if (!builder.toString().contains("armeabi-v7a"))
                                builder.append("armeabi-v7a").append(" ");
                        } else
                            builder.append(jsonElement.getAsString()).append(" ");
                    }
                    so_api = "APP_ABI := " + builder.toString();
                }
                cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "Application.mk");
                cpp_body = new String(StreamUtil.readBytes(new FileInputStream(cpp_file))) + "\n" + so_api;
                outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String[] cmd;
                try {
                    if (OsUtils.isOSLinux())
                        cmd = new String[]{"/www/basic/ndk-build",
                                "-C",
                                cacheCpp + File.separator + "jni",
                                "-j",
                                "3"};
                    else
                        cmd = new String[]{"cmd.exe",
                                "/c",
                                "ndk-build",
                                "-C",
                                cacheCpp + File.separator + "jni",
                                "-j",
                                "3"};
                    if (exec(cmd)) {
                        cacheLibs = new File(cacheCpp, "libs");
                        if (!cacheLibs.exists() || !cacheLibs.isDirectory()) {
                            FileUtils.delete(cacheCpp);
                            throw new Exception("NDK Build failed....");
                        }
                        if (getConfiguration().contains("so_framework")) {
                            JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                            for (JsonElement jsonElement : SO_API) {
                                switch (jsonElement.getAsString()) {
                                    case "armeabi":
                                        getReplacerRes().put("lib/armeabi/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/armeabi/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libarm_loader.so"));
                                        getReplacerRes().put("lib/armeabi/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook-native.so"));
                                        getReplacerRes().put("lib/armeabi/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook.so"));
                                        break;
                                    case "armeabi-v7a":
                                        getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/armeabi-v7a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libarm_loader.so"));
                                        getReplacerRes().put("lib/armeabi-v7a/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook-native.so"));
                                        getReplacerRes().put("lib/armeabi-v7a/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook.so"));
                                        break;
                                    case "arm64-v8a":
                                        getReplacerRes().put("lib/arm64-v8a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "arm64-v8a" + File.separator + "libarm.so")));
                                        getReplacerRes().put("lib/arm64-v8a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/arm64-v8a/libarm_loader.so"));
                                        getReplacerRes().put("lib/arm64-v8a/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/arm64-v8a/libsandhook-native.so"));
                                        getReplacerRes().put("lib/arm64-v8a/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/arm64-v8a/libsandhook.so"));
                                        break;
                                }
                            }
                        } else {
                            getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                            getReplacerRes().put("lib/armeabi-v7a/libarm_loader.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libarm_loader.so"));
                            getReplacerRes().put("lib/armeabi-v7a/libsandhook-native.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook-native.so"));
                            getReplacerRes().put("lib/armeabi-v7a/libsandhook.so", LoaderRes.getInstance().getStaticResAsBytes("so/sandhook/armeabi-v7a/libsandhook.so"));
                        }
                    } else {
                        FileUtils.delete(cacheCpp);
                        throw new Exception("NDK Build failed....");
                    }
                } catch (ThreadDeath threadDeath) {
                    if (Constant.isDevelopment())
                        logger.info("NDK编译/删除临时文件");
                    FileUtils.delete(cacheCpp);
                    throw new ThreadDeath();
                } finally {
                    FileUtils.delete(cacheCpp);
                }
            }
            break;
            /**
             * JavaHook
             */
            case 6:
            case 7: {
                String soName = jsonObject.get("soName").getAsString();
                DexPool dexPool = new DexPool(Opcodes.getDefault());
                dexPool.internClass(SmaliUtils.assembleSmali(LoaderRes.getInstance().getStaticResAsBytes("smali/JavaHookSigner.smali")));
                if (xmlMode.isCustomApplication() && xmlMode.getCustomApplicationName().equals("com.stub.StubApp")) {
                    Enumeration<? extends ZipEntry> entries = getZipFile().entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                            DexBackedDexFile dexFile =
                                    DexBackedDexFile.fromInputStream(
                                            Opcodes.getDefault(),
                                            new BufferedInputStream(getZipFile().getInputStream(zipEntry)));
                            for (DexBackedClassDef classDef : dexFile.getClasses())
                                dexPool.internClass(classDef);
                            ignores.add(zipEntry.getName());
                        }
                    }
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getReplacerRes().put("classes.dex", Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                } else {
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                }
                cacheCpp = new File(Constant.getCache(), SHAUtils.SHA1(getUuid()) + "-signer-cache");
                if (!cacheCpp.exists())
                    cacheCpp.mkdirs();
                ZipUtils.zipUncompress(new ZipInputStream(LoaderRes.getInstance().getStaticResAsStream("zip/java_hook_signer.zip")), cacheCpp.getAbsolutePath());
                File cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "cpp" +
                        File.separator +
                        "output" +
                        File.separator +
                        "arm_StubApp.cpp");
                byte[] cpp_bytes = StreamUtil.readBytes(new FileInputStream(cpp_file));
                String cpp_body = new String(cpp_bytes)
                        .replace("### Signer Data ###", signer)
                        .replace("### SoName ###", soName)
                        .replace("### Src Application Name ###", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                FileOutputStream outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String so_api = "APP_ABI := armeabi-v7a";
                if (getConfiguration().contains("so_framework")) {
                    StringBuilder builder = new StringBuilder();
                    JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                    for (JsonElement jsonElement : SO_API) {
                        if (jsonElement.getAsString().equals("armeabi")) {
                            if (!builder.toString().contains("armeabi-v7a"))
                                builder.append("armeabi-v7a").append(" ");
                        } else
                            builder.append(jsonElement.getAsString()).append(" ");
                    }
                    so_api = "APP_ABI := " + builder.toString();
                }
                cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "Application.mk");
                cpp_body = new String(StreamUtil.readBytes(new FileInputStream(cpp_file))) + "\n" + so_api;
                outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String[] cmd;
                if (OsUtils.isOSLinux())
                    cmd = new String[]{"/www/basic/ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                else
                    cmd = new String[]{"cmd.exe",
                            "/c",
                            "ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                try {
                    if (exec(cmd)) {
                        cacheLibs = new File(cacheCpp, "libs");
                        if (!cacheLibs.exists() || !cacheLibs.isDirectory()) {
                            FileUtils.delete(cacheCpp);
                            throw new Exception("NDK Build failed....");
                        }
                        if (getConfiguration().contains("so_framework")) {
                            JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                            List<SignerInfo.Signer> signers = new ArrayList<>();
                            for (JsonElement jsonElement : SO_API) {
                                switch (jsonElement.getAsString()) {
                                    case "armeabi":
                                        if (type_json.getAsInt() == 7)
                                            getReplacerRes().put(String.format("lib/armeabi/%s", soName), StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
                                        else
                                            signers.add(new SignerInfo.Signer(String.format("lib/armeabi/%s", soName), ZipEntry.DEFLATED));
                                        getReplacerRes().put("lib/armeabi/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        break;
                                    case "armeabi-v7a":
                                        if (type_json.getAsInt() == 7)
                                            getReplacerRes().put(String.format("lib/armeabi-v7a/%s", soName), StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
                                        else
                                            signers.add(new SignerInfo.Signer(String.format("lib/armeabi-v7a/%s", soName), ZipEntry.DEFLATED));
                                        getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                                        break;
                                    case "arm64-v8a":
                                        if (type_json.getAsInt() == 7)
                                            getReplacerRes().put(String.format("lib/arm64-v8a/%s", soName), StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
                                        else
                                            signers.add(new SignerInfo.Signer(String.format("lib/arm64-v8a/%s", soName), ZipEntry.DEFLATED));
                                        getReplacerRes().put("lib/arm64-v8a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "arm64-v8a" + File.separator + "libarm.so")));
                                        break;
                                    case "x86":
                                        if (type_json.getAsInt() == 7)
                                            getReplacerRes().put(String.format("lib/x86/%s", soName), StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
                                        else
                                            signers.add(new SignerInfo.Signer(String.format("lib/x86/%s", soName), ZipEntry.DEFLATED));
                                        getReplacerRes().put("lib/x86/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "x86" + File.separator + "libarm.so")));
                                        break;
                                    case "x86_64":
                                        if (type_json.getAsInt() == 7)
                                            getReplacerRes().put(String.format("lib/x86_64/%s", soName), StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
                                        else
                                            signers.add(new SignerInfo.Signer(String.format("lib/x86_64/%s", soName), ZipEntry.DEFLATED));
                                        getReplacerRes().put("lib/x86_64/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "x86_64" + File.separator + "libarm.so")));
                                        break;
                                }
                            }
                            if (signers.size() > 0) {
                                getReplacerRes().put("Signer_mode", new Gson().toJson(new SignerInfo(signers)).getBytes());
                            }
                        } else {
                            getReplacerRes().put("lib/armeabi-v7a/libarm.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libarm.so")));
                            if (type_json.getAsInt() == 7)
                                getReplacerRes().put(String.format("lib/armeabi-v7a/%s", soName), StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
                            else {
                                SignerInfo signerInfo = new SignerInfo(Lists.newArrayList(new SignerInfo.Signer(String.format("lib/armeabi-v7a/%s", soName), ZipEntry.DEFLATED)));
                                getReplacerRes().put("Signer_mode", new Gson().toJson(signerInfo).getBytes());
                            }
                        }
                    } else {
                        FileUtils.delete(cacheCpp);
                        throw new Exception("NDK Build failed....");
                    }
                } catch (ThreadDeath threadDeath) {
                    if (Constant.isDevelopment())
                        logger.info("NDK编译/删除临时文件");
                    FileUtils.delete(cacheCpp);
                    throw new ThreadDeath();
                } finally {
                    FileUtils.delete(cacheCpp);
                }
            }
            break;
        }
        getReplacerRes().put("assets/arm_dex/dex文件存放路径.txt", "Dex文件过多。无法合并,放到该目录可动态加载".getBytes());
        switch (type_json.getAsInt()) {
            case 0:
            case 2:
            case 4: {
                SignerInfo signerInfo = new SignerInfo(Lists.newArrayList(new SignerInfo.Signer("assets/arm", ZipEntry.DEFLATED)));
                getReplacerRes().put("Signer_mode", new Gson().toJson(signerInfo).getBytes());
            }
            break;
            case 1:
            case 3:
            case 5: {
                getReplacerRes().put("assets/arm", StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
            }
            break;
        }
    }

    @Override
    public String getResult() {
        return Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "signer.pro.tips"));
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public List<String> getIgnores() {
        return ignores;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return 0;
    }
}
