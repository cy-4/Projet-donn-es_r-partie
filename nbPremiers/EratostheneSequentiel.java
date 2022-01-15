package linda.nbPremiers;

import java.util.ArrayList;
import java.util.List;

public class EratostheneSequentiel implements Eratosthene {

    @Override
    public List<Integer> getPremiers(int n) {
        ArrayList<Integer> premiers = new ArrayList<>();
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
        for (int k = 1 ; k < n ; k++) {
            if (! nombres[k]) {
                premiers.add(k + 1);
            }
        }
        return premiers;
    }

    public static void main(String[] args) {
        List<Integer> l = new EratostheneSequentiel().getPremiers(1000000);
        for (int k : l) {
            System.out.print("   " + k);
        }
    }
    
}
