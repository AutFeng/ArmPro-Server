package armadillo.utils.axml.EditXml.editor;

import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.decode.StringBlock;

public abstract class BaseEditor<T> implements XEditor {
    protected AXMLDoc axmlDoc;

    protected T editorInfo;

    public void setEditorInfo(T editorInfo) {
        this.editorInfo = editorInfo;
    }

    public BaseEditor(AXMLDoc axmlDoc) {
        this.axmlDoc = axmlDoc;
    }

    @Override
    public void commit() {
        if (editorInfo != null) {
            registStringBlock(axmlDoc.getmStringBlock());
            editor();
        }
    }

    public abstract String getEditorName();

    protected abstract void editor();

    protected abstract BXMLNode findNode();

    protected abstract void registStringBlock(StringBlock stringBlock);
}
