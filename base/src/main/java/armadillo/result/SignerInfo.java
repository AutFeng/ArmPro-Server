package armadillo.result;

import java.util.List;

public class SignerInfo {
    private List<Signer> signer;

    public static class Signer {
        private String path;
        private int ZipMethod;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getZipMethod() {
            return ZipMethod;
        }

        public void setZipMethod(int zipMethod) {
            ZipMethod = zipMethod;
        }

        public Signer(String path, int zipMethod) {
            this.path = path;
            ZipMethod = zipMethod;
        }
    }

    public List<Signer> getSigner() {
        return signer;
    }

    public void setSigner(List<Signer> signer) {
        this.signer = signer;
    }

    public SignerInfo(List<Signer> signer) {
        this.signer = signer;
    }
}
