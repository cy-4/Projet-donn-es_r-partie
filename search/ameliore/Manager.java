package linda.search.ameliore;

import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import linda.*;

public class Manager implements Runnable {

    private Linda lindamots,lindacommunicaction;

    private String search;
    private int bestvalue = Integer.MAX_VALUE; // lower is better
    private String bestresult;
    private String pathname;
    private int numqueries;
    

    public Manager(Linda lindamots,Linda lindacommunicaction, String search, String pathname,int numqueries) {
        this.lindamots = lindamots;
        this.lindacommunicaction = lindacommunicaction;
        this.search = search;
        this.pathname = pathname;
        this.numqueries = numqueries;
    }

    private void addSearch(String search) {
        this.search = search;
        System.out.println("Manager " + this.numqueries + " querie for " + this.search);
        lindamots.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result,this.numqueries, String.class, Integer.class), new CbGetResult());
        lindacommunicaction.write(new Tuple(Code.Request, this.numqueries, this.search));
    }

    private void waitForEndSearch() {
        lindacommunicaction.take(new Tuple(Code.Searcher, "done", this.numqueries));
        lindacommunicaction.take(new Tuple(Code.Request, this.numqueries, String.class)); // remove query
        lindacommunicaction.write(new Tuple(Code.Finished, this.numqueries)); // remove query
        System.out.println("query done");
    }

    private void loadData(String pathname) {
        try (Stream<String> stream = Files.lines(Paths.get(pathname))) {
            stream.limit(10000).forEach(s -> lindamots.write(new Tuple(Code.Value, s.trim())));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private class CbGetResult implements linda.Callback {
        public void call(Tuple t) {  // [ Result, ?UUID, ?String, ?Integer ]
            String s = (String) t.get(2);
            Integer v = (Integer) t.get(3);
            if (v < bestvalue) {
                bestvalue = v;
                bestresult = s;
                System.out.println("New best (" + bestvalue + "): \"" + bestresult + "\"");
            }
            lindamots.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result,numqueries, String.class, Integer.class), this);
        }
    }

    public void run() {
        // Tous les Managers attends la fin du précédent pour commencer à écrire puis demander leur queries
        if (numqueries!=0){
            // On attend l'information de la fin du précédent
            lindacommunicaction.read(new Tuple(Code.Finished, this.numqueries-1));
            try {
                // On attend un peu pour s'assurer que les autres searcher squitte la recherche de la queries précédentes
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.loadData(pathname);
        this.addSearch(search);
        this.waitForEndSearch();
    }
}
