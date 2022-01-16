package linda.search.ameliore;

import linda.*;
import java.util.Arrays;
import java.util.UUID;

public class Searcher implements Runnable {

    private Linda lindamots,lindacommunicaction;
    private int nbchercheur;
    private int nbqueries;

    public Searcher(Linda lindamots,Linda lindacommunicaction,int nbchercheur,int nbqueries) {
        this.lindamots = lindamots;
        this.lindacommunicaction = lindacommunicaction;
        this.nbchercheur = nbchercheur +1;
        this.nbqueries = nbqueries;
    }

    public void run() {
        System.out.println("Searcher "+nbchercheur+" ready to do a search");
        for (int i=0; i<nbqueries; i++){
            Tuple treq = lindacommunicaction.read(new Tuple(Code.Request, i, String.class));     
            int numqueries = (Integer) treq.get(1);
            String req = (String) treq.get(2);
            Tuple tv;
            System.out.println("Searcher "+nbchercheur+" looking for: " + req);
            int dist = 15; // arbitraire
            int nbmotlut = 0;
            Boolean requete_tjr_en_cours = true; 
            while ((tv = lindamots.tryTake(new Tuple(Code.Value, String.class))) != null & (dist!=0) & requete_tjr_en_cours) {
                nbmotlut++;
                String val = (String) tv.get(1);
                dist = getLevenshteinDistance(req, val);
                if (dist < 10) { // arbitrary
                    lindamots.write(new Tuple(Code.Result, numqueries, val, dist));
                }
                if ((nbmotlut)%10==0){
                    requete_tjr_en_cours = lindacommunicaction.tryRead(new Tuple(Code.Request, numqueries, req))!=null;
                }
            }
            if (requete_tjr_en_cours){
                lindacommunicaction.write(new Tuple(Code.Searcher, "done", numqueries));
            }
        }
    }
    
    /*****************************************************************/

    /* Levenshtein distance is rather slow */
    /* Copied from https://www.baeldung.com/java-levenshtein-distance */
    static int getLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1] 
                                   + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)), 
                                   dp[i - 1][j] + 1, 
                                   dp[i][j - 1] + 1);
                }
            }
        }
        return dp[x.length()][y.length()];
    }

    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

}

