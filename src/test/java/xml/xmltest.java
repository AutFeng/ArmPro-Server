package xml;

import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class xmltest {
    @Test
    public void test() throws Exception {
        File file = new File("C:\\Users\\Administrator\\Desktop\\aaaaa.xml");
        AXMLDoc axmlDoc = new AXMLDoc();
        //axmlDoc.parse(new ByteArrayInputStream(AutoEditor.AutoXml(StreamUtil.readBytes(new FileInputStream(file)), StreamUtil.readBytes(new FileInputStream("C:\\Users\\Administrator\\Desktop\\test.txt")))));
        axmlDoc.parse(new FileInputStream(file));
//        ContentProviderEditor contentProviderEditor = new ContentProviderEditor(axmlDoc);
//        contentProviderEditor.setEditorInfo(new ContentProviderEditor.Editorinfo().with(new ContentProviderEditor.ProviderInfo("aaa.aa", "aaa", false, 199999)));
//        contentProviderEditor.commit();
        FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\AndroidManifest.xml");
        axmlDoc.build(outputStream);
        axmlDoc.release();
    }

    @Test
    public void sss() {
        System.out.println(Integer.toHexString(((TypedValue.TYPE_INT_HEX << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_ATTRIBUTE << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_DIMENSION << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_FIRST_COLOR_INT << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_FIRST_INT << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_FLOAT << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_FRACTION << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_INT_BOOLEAN << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_INT_COLOR_ARGB4 << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_INT_COLOR_ARGB8 << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_INT_COLOR_RGB8 << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_INT_COLOR_RGB4 << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_INT_DEC << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_LAST_COLOR_INT << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_LAST_INT << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_REFERENCE << 24) + 8)));
        System.out.println(Integer.toHexString(((TypedValue.TYPE_STRING << 24) + 8)));
    }
}
