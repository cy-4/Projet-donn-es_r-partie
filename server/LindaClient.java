package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

/** Client part of a client/server implementation of Linda.
 * It implements the Linda interface and propagates everything to the server it is connected to.
 * */
public class LindaClient extends UnicastRemoteObject implements Linda, CallbackRemote {
	
    private LindaReparti serveur;
    private Callback cb;
    private static final long serialVersionUID = 1L;

    /** Initializes the Linda implementation.
     *  @param serverURI the URI of the server, e.g. "rmi://localhost:4000/LindaServer" or "//localhost:4000/LindaServer".
     */
    public LindaClient(String serverURI) throws RemoteException {
        try {
            this.serveur = (LindaReparti) Naming.lookup(serverURI);
            System.out.println("Client connecté");
        } catch (MalformedURLException | RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Tuple t) {
        try {
            this.serveur.write(t);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tuple take(Tuple template) {
        try {
            return this.serveur.take(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Tuple read(Tuple template) {
        try {
            return this.serveur.read(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Tuple tryTake(Tuple template) {
        try {
            return this.serveur.tryTake(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) {
        try {
            return this.serveur.tryRead(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) {
        try {
            return this.serveur.takeAll(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        try {
            return this.serveur.readAll(template);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
        try {
            this.cb = callback;
            this.serveur.eventRegister(mode, timing, template, this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void debug(String prefix) {
        try {
            this.serveur.debug(prefix);
        } catch (RemoteException e) {
            e.printStackTrace();
        }        
    }

    @Override
    public void call(Tuple t) throws RemoteException {
        this.cb.call(t);
    }


}
