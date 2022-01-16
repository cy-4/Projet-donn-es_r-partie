package linda.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import linda.Tuple;

// Classe qui permet au client/serveur de s'échanger des CallBacks
public interface CallbackRemote extends Remote {
        public void call(Tuple t) throws RemoteException;
}
