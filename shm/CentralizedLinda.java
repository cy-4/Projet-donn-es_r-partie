package linda.shm;

import linda.Callback;
import linda.Linda;
import linda.Tuple;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.LinkedList;
import java.util.List;
import java.util.Collection;



/** Shared memory implementation of Linda. */
public class CentralizedLinda implements Linda {

    // TO BE COMPLETED

    private List<Tuple> listTuple;
    private java.util.concurrent.locks.Lock mon_moniteur;
    private Condition Ep;
    private Condition Lp;
    private Condition newTuple;
    private boolean manipulee;
    private int nbLecteur;
    private int nbLecteurAttente;
    private int nbLectures;
    private int seuilEquite;
    
	
    public CentralizedLinda() {
        this.mon_moniteur =  new java.util.concurrent.locks.ReentrantLock();
        this.listTuple = new LinkedList<Tuple>();
        this.Lp = mon_moniteur.newCondition();
        this.Ep = mon_moniteur.newCondition();
        this.newTuple = mon_moniteur.newCondition();
	    this.nbLecteur = 0;
        this.manipulee = false;
	    this.nbLecteurAttente = 0;
	    this.nbLectures = 0;
	    this.seuilEquite = 10;
        
    }

    public void write(Tuple t) {
        mon_moniteur.lock();
        while(this.nbLecteur != 0 || (this.nbLecteurAttente != 0 && this.nbLectures < this.seuilEquite) || manipulee) {
            try{
                System.out.println("attente");
                Ep.await();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        this.manipulee =  true;
        this.listTuple.add(t);
        
        this.nbLectures = 0;
        this.manipulee = false;
        this.newTuple.signal();
        if (this.nbLecteurAttente == 0) {
    		Ep.signal();
    	} else {
    		Lp.signal();
    	}
        mon_moniteur.unlock();
    }

    public Tuple take(Tuple template){
        while(true) {
            mon_moniteur.lock();
            while (this.nbLecteur != 0 || (this.nbLecteurAttente != 0 && this.nbLectures < this.seuilEquite) || this.manipulee) {
                try{
                    Ep.await();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            this.manipulee = true ; 
            for (int i=0; i<this.listTuple.size(); i++) {
                Tuple tuplei = this.listTuple.get(i);
                if (tuplei.matches(template)) {
                    this.listTuple.remove(i);
                    this.nbLectures = 0;
                    this.manipulee = false;
                    if (this.nbLecteurAttente == 0) {
                        Ep.signal();
                    } else {
                        Lp.signal();
                    }
                    mon_moniteur.unlock();
                    return(tuplei);
                }
            }
            this.manipulee = false ;
            if (this.nbLecteurAttente == 0) {
                Ep.signal();
            } else {
                Lp.signal();
            }
            mon_moniteur.unlock();
        } 
        
    }

    public Tuple read(Tuple template){
        while(true) {
            this.mon_moniteur.lock();
            while (this.manipulee || this.nbLectures > this.seuilEquite) {
                this.nbLecteurAttente ++;
                try{
                    this.Lp.await();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                this.nbLecteurAttente --;
            }
            this.nbLectures ++;
            this.nbLecteur ++;
            for (int i=0; i<this.listTuple.size(); i++) {
                Tuple tuplei = this.listTuple.get(i);
                if (tuplei.matches(template)) {
                    this.nbLecteur --;
                    if (this.nbLecteurAttente == 0) {
                        Ep.signal();
                    } else {
                        Lp.signal();
                    }
                    mon_moniteur.unlock();
                    return(tuplei);
                }
            }
            this.nbLecteur --;
            if (this.nbLecteurAttente == 0) {
                Ep.signal();
            } else {
                Lp.signal();
            }
            this.mon_moniteur.unlock();
        }
    }

    public Tuple tryTake(Tuple template) {
        this.mon_moniteur.lock();
        while (this.nbLecteur != 0 || (this.nbLecteurAttente != 0 && this.nbLectures < this.seuilEquite) || manipulee) {
            try{
                Ep.await();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        this.manipulee = true;
        for (int i=0; i<this.listTuple.size(); i++) {
            Tuple tuplei = this.listTuple.get(i);
            if (tuplei.matches(template)){
                this.listTuple.remove(i);
                this.nbLectures = 0;
                this.manipulee =  false;
                if (this.nbLecteurAttente == 0) {
                    Ep.signal();
                } else {
                    Lp.signal();
                }
                mon_moniteur.unlock();
                return(tuplei);
            }
        }
        this.nbLectures = 0;
        this.manipulee = false;
        if (this.nbLecteurAttente == 0) {
            Ep.signal();
        } else {
            Lp.signal();
        }
        mon_moniteur.unlock();
        return(null);
    }

    public Tuple tryRead(Tuple template){
        this.mon_moniteur.lock();
        while (this.manipulee || this.nbLectures > this.seuilEquite) {
            this.nbLecteurAttente ++;
            try{
                this.Lp.await();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            this.nbLecteurAttente --;
        }
        this.nbLectures ++;
        this.nbLecteur ++;
        for (int i=0; i<this.listTuple.size(); i++) {
            Tuple tuplei = this.listTuple.get(i);
            if (tuplei.matches(template)) {
                this.nbLecteur --;
                if (this.nbLecteurAttente == 0) {
                    Ep.signal();
                } else {
                    Lp.signal();
                }
                mon_moniteur.unlock();
                return(tuplei);
            }
        }
        this.nbLecteur --;
        if (this.nbLecteurAttente == 0) {
            Ep.signal();
        } else {
            Lp.signal();
        }
        this.mon_moniteur.unlock();
        return(null);

    }

    public Collection<Tuple> takeAll(Tuple template){
        LinkedList<Tuple> resultat = new LinkedList<Tuple>();
        LinkedList<Tuple> copie = (LinkedList<Tuple>) ((LinkedList<Tuple>) this.listTuple).clone();
        this.mon_moniteur.lock();
        while (this.nbLecteur != 0 || (this.nbLecteurAttente != 0 && this.nbLectures < this.seuilEquite) || manipulee) {
            try{
                Ep.await();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        this.manipulee = true;
        int c = 0;
        for (int i=0; i<copie.size() ; i++) {
            Tuple tuplei = copie.get(i);
            if (tuplei.matches(template)) {
                this.listTuple.remove(i-c);
                resultat.add(tuplei);
                c++;
            }
        }
        this.nbLectures = 0;
        this.manipulee = false;
        if (this.nbLecteurAttente == 0) {
            Ep.signal();
        } else {
            Lp.signal();
        }
        mon_moniteur.unlock();
        return resultat;
    }

    public Collection<Tuple> readAll(Tuple template){
        LinkedList<Tuple> resultat = new LinkedList<Tuple>();
        this.mon_moniteur.lock();
        while (this.nbLecteur != 0 || (this.nbLecteurAttente != 0 && this.nbLectures < this.seuilEquite) || manipulee) {
            try{
                Ep.await();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        this.manipulee = true;
        for (int i=0; i< this.listTuple.size() ; i++) {
            Tuple tuplei = this.listTuple.get(i);
            if (tuplei.matches(template)) {
                resultat.add(tuplei);
            }
        }
        this.nbLectures = 0;
        this.manipulee = false;
        if (this.nbLecteurAttente == 0) {
            Ep.signal();
        } else {
            Lp.signal();
        }
        mon_moniteur.unlock();
        return resultat;
    }

    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback){
        if (timing==eventTiming.IMMEDIATE){
            this.mon_moniteur.lock();
            if (mode==eventMode.READ){
                while(this.manipulee || this.nbLectures > this.seuilEquite){
                    this.nbLecteurAttente ++;
                    try{
                        this.Lp.await();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    this.nbLecteurAttente --;
 
                }
                this.nbLecteur ++;
                for (int i=0; i<this.listTuple.size(); i++) {
                    Tuple tuplei = this.listTuple.get(i);
                    if (tuplei.matches(template)) {
                        this.nbLecteur --;
                        if (this.nbLecteurAttente == 0) {
                            Ep.signal();
                        } else {
                            Lp.signal();
                        }
                        mon_moniteur.unlock();
                        callback.call(tuplei);
                        return;
                    }
                }
                this.nbLecteur --;
                if (this.nbLecteurAttente == 0) {
                    Ep.signal();
                } else {
                    Lp.signal();
                }
                this.mon_moniteur.unlock();
            }
            else{
                while (this.nbLecteur != 0 || (this.nbLecteurAttente != 0 && this.nbLectures < this.seuilEquite) || this.manipulee) {
                    try{
                        Ep.await();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                this.manipulee = true ; 
                for (int i=0; i<this.listTuple.size(); i++) {
                    Tuple tuplei = this.listTuple.get(i);
                    if (tuplei.matches(template)) {
                        this.listTuple.remove(i);
                        this.nbLectures = 0;
                        this.manipulee = false;
                        if (this.nbLecteurAttente == 0) {
                            Ep.signal();
                        } else {
                            Lp.signal();
                        }
                        mon_moniteur.unlock();
                        callback.call(tuplei);
                        return;
                    }
                }
                this.manipulee = false ;
                if (this.nbLecteurAttente == 0) {
                    Ep.signal();
                } else {
                    Lp.signal();
                }
                mon_moniteur.unlock();
                }         
        }
        if (mode==eventMode.READ){
            while(true){
                this.mon_moniteur.lock();
                try{
                    newTuple.await();
                }
                catch (Exception e){
                    e.printStackTrace();
                }     
                while (this.manipulee || this.nbLectures > this.seuilEquite) {
                    this.nbLecteurAttente ++;
                    try{
                        this.Lp.await();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    this.nbLecteurAttente --;
                }
                this.nbLectures ++;
                this.nbLecteur ++;
                for (int i=0; i<this.listTuple.size(); i++) {
                    Tuple tuplei = this.listTuple.get(i);
                    if (tuplei.matches(template)) {
                        this.nbLecteur --;
                        if (this.nbLecteurAttente == 0) {
                            Ep.signal();
                        } else {
                            Lp.signal();
                        }
                        mon_moniteur.unlock();
                        callback.call(tuplei);
                        return;
                    }
                }
                this.nbLecteur --;
                if (this.nbLecteurAttente == 0) {
                    Ep.signal();
                } else {
                    Lp.signal();
                }
                this.mon_moniteur.unlock();
            }
        }
        else{
            while(true){
                this.mon_moniteur.lock();
                try{
                    System.out.println("je suis bloqué");
                    newTuple.await();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println("je suis sortie");
                mon_moniteur.lock();
                while (this.nbLecteur != 0 || (this.nbLecteurAttente != 0 && this.nbLectures < this.seuilEquite) || this.manipulee) {
                    try{
                        Ep.await();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                this.manipulee = true ; 
                for (int i=0; i<this.listTuple.size(); i++) {
                    Tuple tuplei = this.listTuple.get(i);
                    if (tuplei.matches(template)) {
                        this.listTuple.remove(i);
                        this.nbLectures = 0;
                        this.manipulee = false;
                        if (this.nbLecteurAttente == 0) {
                            Ep.signal();
                        } else {
                            Lp.signal();
                        }
                        mon_moniteur.unlock();
                        callback.call(tuplei);
                        return;
                    }
                }
                this.manipulee = false ;
                if (this.nbLecteurAttente == 0) {
                    Ep.signal();
                } else {
                    Lp.signal();
                }
                mon_moniteur.unlock();
                }
        }
    }

    public void debug(String prefix){
        System.out.println(prefix);
    }
}
