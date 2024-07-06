package armadillo.utils.axml.EditXml.decode;

import armadillo.utils.axml.EditXml.io.ZInput;
import armadillo.utils.axml.EditXml.io.ZOutput;

import java.io.IOException;

public interface IAXMLSerialize {
	public int getSize();
	public int getType();

	public void setSize(int size);
	public void setType(int type);

	public void read(ZInput reader) throws IOException;
	public void write(ZOutput writer) throws IOException;
}
