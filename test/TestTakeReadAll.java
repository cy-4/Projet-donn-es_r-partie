package linda.test;

import linda.shm.*;
import linda.*;
import java.util.Collection;

public class TestTakeReadAll {

    public static void main(String[] a) {
                
        final Linda linda = new linda.shm.CentralizedLinda();
        // final Linda linda = new linda.server.LindaClient("//localhost:4000/aaa");
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(7, 5);
                System.out.println("(1) write: " + t1);
                linda.write(t1);

                Tuple motif = new Tuple(Integer.class, Integer.class);
                Collection<Tuple> res = linda.readAll(motif);
                for (Tuple t : res) {
                    System.out.println("(1) Resultat readAll1:" + t);
                }
                res = linda.takeAll(motif);
                for (Tuple t : res) {
                    System.out.println("(1) Resultat takeAll:" + t);
                }
                res = linda.readAll(motif);
                for (Tuple t : res) {
                    System.out.println("(1) Resultat readAll2:" + t);
                }
            }
        }.start();
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(2) write: " + t1);
                linda.write(t1);

                Tuple t11 = new Tuple(4, 6);
                System.out.println("(2) write: " + t11);
                linda.write(t11);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(2) write: " + t2);
                linda.write(t2);

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(2) write: " + t3);
                linda.write(t3);
                                
            }
        }.start();
                
    }
}
