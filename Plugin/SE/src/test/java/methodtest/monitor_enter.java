package methodtest;

public class monitor_enter {
    public void ss(){
        String s = "";
        synchronized (s){
            System.exit(0);
        }
    }
}
