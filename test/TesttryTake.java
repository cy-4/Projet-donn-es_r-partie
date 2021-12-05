package linda.test;

import linda.*;

public class TesttryTake {

    public static void main(String[] a) {
        final Linda linda = new linda.shm.CentralizedLinda();
        //              final Linda linda = new linda.server.LindaClient("//localhost:4000/MonServeur");
                
        for (int i = 1; i <= 4; i++) {
            final int j = i;
            new Thread() {  
                public void run() {
                    if ((j==3)||(j==4)) {
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Tuple motif = new Tuple(Integer.class, String.class);
                        Tuple res = linda.tryTake(motif);
                        System.out.println("("+j+") Resultat apres attente:" + res);

                    }
                    else{
                        Tuple motif = new Tuple(Integer.class, String.class);
                        Tuple res = linda.tryTake(motif);
                        System.out.println("("+j+") Resultat:" + res);
                    }
                }
            }.start();
        }
                
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Tuple t1 = new Tuple(4, 5);
                System.out.println("(0) write: " + t1);
                linda.write(t1);

                Tuple t2 = new Tuple("hello", 15);
                System.out.println("(0) write: " + t2);
                linda.write(t2);

                Tuple t3 = new Tuple(4, "foo");
                System.out.println("(0) write: " + t3);
                linda.write(t3);

                Tuple t4 = new Tuple(9, "loo");
                System.out.println("(0) write: " + t4);
                linda.write(t4);
                                
            }
        }.start();
                
    }
}

