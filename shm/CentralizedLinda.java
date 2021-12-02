package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.concurrent.Condition;
import java.util.concurrent.Lock;


/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    // TO BE COMPLETED

    List<Tuple> listTuple;
    boolean manipulee;
    private java.util.concurrent.locks.Lock mon_moniteur;
    private Condition Ep;
    private Condition Lp;
	
    public CentralizedLinda() {
        this.mon_moniteur =  new java.util.concurrent.locks.ReentrantLock();
        this.listTuple = new LinkedList<Tuple>();
        this.manipulee =  false;
        this.Lp = mon_moniteur.newCondition();
        this.Ep = mon_moniteur.newCondition();
        
    }

    public Tuple take(Tuple template){
        mon_moniteur.lock();
        while (!manipulee){
            Ep.await();
        }
        for (int i=0; i<listTuple.size(); i++){
            Tuple Tuplei = ListTuple.get(i);
            if (Tuplei.matches(template)){
                ListTuple.remove(i);
                return(Tuplei);
            }
        }
        mon_moniteur.unlock();   
        
    }

    public Tuple trytake(Tuple template){
        for (int i=0; i<listTuple.size(); i++){
            Tuple Tuplei = ListTuple.get(i);
            if (Tuplei.matches(template)){
                ListTuple.remove(i);
                return(Tuplei);
            }
        }   
        return(null);
    }

}
