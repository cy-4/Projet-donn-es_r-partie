package linda.server;
import linda.server.LindaServer;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

public class CreateServer {
    public static void main(String[] args) {
        try {
           LindaServer serveur = new LindaServer(args[0]);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while(true) {
            
        }        
    }
}
