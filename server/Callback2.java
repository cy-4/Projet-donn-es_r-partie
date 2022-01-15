package linda.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import linda.Callback;
import linda.Tuple;

public class Callback2 extends UnicastRemoteObject implements CallbackRemote {

    public Callback cb;

    public Callback2(Callback callback) throws RemoteException {
        this.cb = callback;
    }

    @Override
    public void call(Tuple t) throws RemoteException {
        this.cb.call(t);
    }
    
}
