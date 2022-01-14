package linda.shm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

public class CentralizedLinda implements Linda {
    
    private List<Tuple> listeTuples;
    private Map<Tuple, List<Callback>> callbackRead;
    private Map<Tuple, List<Callback>> callbackTake;
    private Semaphore mutex;

    public CentralizedLinda() {
        this.listeTuples = Collections.synchronizedList(new LinkedList<>());
        this.callbackRead = new HashMap<>();
        this.callbackTake = new HashMap<>();
        this.mutex = new Semaphore(0);
    }

    @Override
    public void write(Tuple t) {
        synchronized(listeTuples) {
            this.listeTuples.add(t);
        }
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
        if (this.mutex.getQueueLength()>0){
            this.mutex.release();
        }
    }

    @Override
    public Tuple take(Tuple template) {
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
    public Tuple read(Tuple template) {
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
    public Tuple tryTake(Tuple template) {
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
    public Tuple tryRead(Tuple template) {
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
    public Collection<Tuple> takeAll(Tuple template) {
        LinkedList<Tuple> l = new LinkedList<>();
        Tuple tuple = tryTake(template);
        while(tuple!=null){
            l.add(tuple);
            tuple = tryTake(template);
        }
        return l;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
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
    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback) {
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
    public void debug(String prefix) {
        // TODO Auto-generated method stub
        
    }

}
