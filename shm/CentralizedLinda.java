package linda.shm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.Semaphore;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

public class CentralizedLinda implements Linda {
    
    // Liste où on stocke les tuple
    private List<Tuple> listeTuples;

    // Liste où on stocke les templates et les callbacks associés à ces derniers
    private Map<Tuple, List<Callback>> callbackRead;
    private Map<Tuple, List<Callback>> callbackTake;

    // Pour réveiller les take et read une fois qu'on à write
    private Semaphore mutex;

    public CentralizedLinda() {
        // Pour permettre une exclusion mutuelle simple
        this.listeTuples = Collections.synchronizedList(new LinkedList<>());
        this.callbackRead = Collections.synchronizedMap(new HashMap<>());
        this.callbackTake = Collections.synchronizedMap(new HashMap<>());
        this.mutex = new Semaphore(0);
    }

    @Override
    public void write(Tuple t) {
        synchronized(this.listeTuples) {
            // On ajoute le tuple à la Liste
            this.listeTuples.add(t);
        }
        // On appelle les Callbacks associé au template du Tuple t
        Set<Tuple> lt;
        List<Callback> lcb;
        synchronized(this.callbackRead) {
            lt = this.callbackRead.keySet();
        }
        for (Tuple template : lt) {
            // Le tuple correspond au template
            if (t.matches(template)){
                synchronized(this.callbackRead) {
                    lcb = this.callbackRead.get(template);
                }
                // On récupére les Callbacks
                for (Callback cb : lcb) {
                    // On les supprime de callbackRead
                    synchronized(this.callbackRead) {
                        this.callbackRead.get(template).remove(cb);
                    }
                    // On les appelle avec le tuple t
                    cb.call(t);
                }
            }
        }
        // Pareil qu'avant mais avec callbackTake
        synchronized(this.callbackTake) {
            lt = this.callbackTake.keySet();
        }
        for (Tuple template : lt) {
            if (t.matches(template)){
                synchronized(this.callbackTake) {
                    lcb = this.callbackTake.get(template);                   
                }
                // On vérifie qu'il y bien bien des Callback dans le template
                if (!lcb.isEmpty()){
                    // On prend le premier Callback
                    Callback cb = lcb.get(0);
                    // On enleve le t qu'on vient de rajouter
                    take(t);
                    // On le supprime de la liste des Callback
                    synchronized(this.callbackTake) {
                        this.callbackTake.get(template).remove(cb);
                    }
                    // On l'appelle avec t
                    cb.call(t);
                    break;
                }
            }

        }
        // Si ya des lecteurs et des takes en attend, 
        // on en libére un pour vérifier si ce tuple ajouté correspond à ce qu'il cherhce
        if (this.mutex.hasQueuedThreads()) {
            this.mutex.release();
        }
    }

    @Override
    public Tuple take(Tuple template) {
        while (true) {
            // On regarde si un tuple correspond à notre template
            synchronized(listeTuples) {
                for (Tuple t : this.listeTuples) {
                    if (t.matches(template)) {
                        // On libérer un take ou un read s'il y en a en attente
                        if (this.mutex.hasQueuedThreads()) {
                            this.mutex.release();
                        }
                        // On le supprime de l'espace des tuples
                        this.listeTuples.remove(t);
                        return t;
                    }
                }
            }
            try {
                if (this.mutex.hasQueuedThreads()) {
                    this.mutex.release();
                    // On met un sleep, pour qu'il évite de reprendre le mutex qu'il a libéré
                    // ce qui entrainerai une boucle infinie
                    try {
                        Thread.sleep(10);
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
        // Pareil que pour take, sauf on ne supprime pas le tuple de l'espace
        while (true) {
            synchronized(listeTuples) {
                for (Tuple t : this.listeTuples) {
                    if (t.matches(template)) {
                        if (this.mutex.hasQueuedThreads()) {
                            this.mutex.release();
                        }
                        return t;
                    }
                }
            }
            try {
                if (this.mutex.hasQueuedThreads()) {
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
        // Pareil que take sauf qu'ici on enléve le while(true)
        // permettant d'enlever la bloquance
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
        // Pareil que tryRead, sauf qu'on enleve pas le tuple
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
        // Tant qu'on trouve un tuple correspondant au template,
        // on l'enleve avec le tryTake et on l'ajoute à l
        Tuple tuple = tryTake(template);
        while(tuple!=null){
            l.add(tuple);
            tuple = tryTake(template);
        }
        return l;
    }

    @Override
    public Collection<Tuple> readAll(Tuple template) {
        // Tant qu'on trouve un tuple correspondant au template,
        // on l'ajoute à l
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
                    // On le rajoute à le liste de callbackRead
                    synchronized(this.callbackRead) {    
                        // Cas où le template n'a pas encore été crée dans la liste
                        if (this.callbackRead.get(template) == null) {
                            // On rajoute la nouvelle liste de Callback associé au template à callbackRead
                            List<Callback> l = new LinkedList<>();
                            l.add(callback);
                            this.callbackRead.put(template, l);
                        } else {
                            // On ajoute le callback à la liste associé au template
                            this.callbackRead.get(template).add(callback);
                        }
                    }
                }
            } else {
                // Meme principe que précédemment mais avec la liste callbackTake
                Tuple t = this.tryTake(template);
                if (t != null) {
                    callback.call(t);
                } else {
                    synchronized(this.callbackTake) {  
                        if (this.callbackTake.get(template) == null) {
                            List<Callback> l = new LinkedList<>();
                            l.add(callback);
                            this.callbackTake.put(template, l);
                        } else {
                            this.callbackTake.get(template).add(callback);
                        }
                    }
                }
            }
        } else {
            // Meme principe que précédemment sauf qu'on passe directement à l'enregistrement du Callback
            if (mode == eventMode.READ) {
                synchronized(this.callbackRead) {  
                    if (this.callbackRead.get(template) == null) {
                        List<Callback> l = new LinkedList<>();
                        l.add(callback);
                        this.callbackRead.put(template, l);
                    } else {
                        this.callbackRead.get(template).add(callback);
                    }
                }
            } else {
                synchronized(this.callbackTake) {  
                    if (this.callbackTake.get(template) == null) {
                        List<Callback> l = new LinkedList<>();
                        l.add(callback);
                        this.callbackTake.put(template, l);
                    } else {
                        this.callbackTake.get(template).add(callback);
                    }
                }
            }
        }
    }

    @Override
    public void debug(String prefix) {
        System.out.println(prefix);        
    }

}
