package linda.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import linda.Callback;
import linda.Tuple;
import linda.Linda.eventMode;
import linda.Linda.eventTiming;

public class LindaServer extends UnicastRemoteObject implements LindaReparti {
    
    private List<Tuple> listeTuples;
    private Map<Tuple, List<Callback>> callbackRead;
    private Map<Tuple, List<Callback>> callbackTake;
    private Semaphore mutex;

    public LindaServer(String serveurUrl) throws RemoteException, MalformedURLException {
        LocateRegistry.createRegistry(4000);
        Naming.rebind(serveurUrl, this);
        this.callbackRead = new HashMap<>();
        this.callbackTake = new HashMap<>();
        this.listeTuples = Collections.synchronizedList(new LinkedList<>());
        this.mutex = new Semaphore(0);
    }


    @Override
    public void write(Tuple t) throws RemoteException {
        synchronized(listeTuples) {
            this.listeTuples.add(t);
            for (Tuple template : this.callbackRead.keySet()) {
                if (t.matches(template)){
                    for (Callback cb : this.callbackRead.get(template)) {
                        Tuple t2 = this.tryRead(template);
                        if (t2 != null) {
                            cb.call(t2);
                            this.callbackRead.get(template).remove(cb);
                        }
                    }
                }
            }
            for (Tuple template : this.callbackTake.keySet()) {
                if (t.matches(template)){
                    for (Callback cb : this.callbackTake.get(template)) {
                        Tuple t2 = this.tryTake(template);
                        if (t2 != null) {
                            cb.call(t2);
                            this.callbackTake.get(template).remove(cb);
                        }
                    }
                }
            }
        }
        this.mutex.release();
    }

    @Override
    public Tuple take(Tuple template) throws RemoteException{
        while (true) {
            synchronized(listeTuples) {
                for (Tuple t : this.listeTuples) {
                    if (t.matches(template)) {
                        if (this.mutex.getQueueLength()>0){
                            this.mutex.release();
                        }
                        this.listeTuples.remove(t);
                        return t;
                    }
                }
            }
            try {
                if (this.mutex.getQueueLength()>0){
                    this.mutex.release();
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.mutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Tuple read(Tuple template) throws RemoteException {
        while (true) {
            synchronized(listeTuples) {
                for (Tuple t : this.listeTuples) {
                    if (t.matches(template)) {
                        if (this.mutex.getQueueLength()>0){
                            this.mutex.release();
                        }
                        return t;
                    }
                }
            }
            try {
                if (this.mutex.getQueueLength()>0){
                    this.mutex.release();
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                this.mutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Tuple tryTake(Tuple template) throws RemoteException {
        synchronized(listeTuples) {
            for (Tuple t : this.listeTuples) {
                if (t.matches(template)) {
                    this.listeTuples.remove(t);
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public Tuple tryRead(Tuple template) throws RemoteException {
        synchronized(listeTuples) {
            for (Tuple t : this.listeTuples) {
                if (t.matches(template)) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<Tuple> takeAll(Tuple template) throws RemoteException{
        LinkedList<Tuple> l = new LinkedList<>();
        Tuple tuple = tryTake(template);
        while(tuple!=null){
            l.add(tuple);
            tuple = tryTake(template);
        }
        return l;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) throws RemoteException {
        LinkedList<Tuple> l = new LinkedList<>();
        synchronized(listeTuples) {
            for (Tuple t : this.listeTuples) {
                if (t.matches(template)) {
                    l.add(t);
                }
            }
        }
        return l;
    }

    @Override
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) throws RemoteException {
        if (timing == eventTiming.IMMEDIATE) {
            if (mode == eventMode.READ) {
                Tuple t = this.tryRead(template);
                if (t != null) {
                    callback.call(t);
                } else {
                    this.callbackRead.get(template).add(callback);
                }
            } else {
                Tuple t = this.tryTake(template);
                if (t != null) {
                    callback.call(t);
                } else {
                    this.callbackTake.get(template).add(callback);
                }
            }
        } else {
            if (mode == eventMode.READ) {
                this.callbackRead.get(template).add(callback);
            } else {
                this.callbackTake.get(template).add(callback);
            }
        }
    }

    @Override
    public void debug(String prefix) throws RemoteException {
        // TODO Auto-generated method stub
        
    }


    public static void main(String[] args) throws RemoteException {
        try {
            new LindaServer(args[0]);
        } catch (RemoteException | MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (true) {
        }
    }
}
