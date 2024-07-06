package armadillo.utils.axml.EditXml.editor;

public interface XEditor {
    String NAME_SPACE = "http://schemas.android.com/apk/res/android";
    String NODE_USER_PREMISSION = "uses-permission";
    String NODE_ContentProvider = "provider";
    String NODE_META_DATA = "meta-data";

    void commit();
}
