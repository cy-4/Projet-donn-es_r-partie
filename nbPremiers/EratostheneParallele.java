package linda.nbPremiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import linda.Linda;
import linda.shm.CentralizedLinda;
import linda.Tuple;

public class EratostheneParallele implements Eratosthene {

    @Override
    public List<Integer> getPremiers(int n) {
        Linda linda = new CentralizedLinda();
        boolean[] nombres = new boolean[n];
        int i = 2;
        while (i * i <= n) {
            if (! nombres[i-1]) {
                for (int j = i * i ; j <= n ; j += i) {
                    nombres[j-1] = true;
                }
            }
            i++;
        }
        int pas = n / 100000;
        int m = 0;
        while ((m + 1) * pas < n) {
            final int p = m;
            new Thread() {
                public void run() {
                    ArrayList<Integer> l = new ArrayList<>();
                    for (int k = p * pas ; k < (p+1) * pas ; k++) {
                        if (!nombres[k]) {
                            l.add(k+1);
                        }
                    }
                    linda.write(new Tuple(l));
                };
            }.start();
            m++;
        }
        final int p = m;
        new Thread() {
            public void run() {
                ArrayList<Integer> l = new ArrayList<>();
                for (int k = p * pas ; k < n ; k++) {
                    if (!nombres[k]) {
                        l.add(k-1);
                    }
                }
                linda.write(new Tuple(l));
            };
        }.start();
        ArrayList<Integer> finale = new ArrayList<>();
        for (Tuple t : linda.readAll(new Tuple(ArrayList.class))) {
            finale.addAll((Collection<Integer>) t.getFirst());
        }
        return finale;
    }


    public static void main(String[] args) {
        List<Integer> l = new EratostheneParallele().getPremiers(1000000);
        for (int k : l) {
            System.out.print("   " + k);
        }
    }

}