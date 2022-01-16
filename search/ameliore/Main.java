package linda.search.ameliore;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import linda.*;

public class Main {

    public static void main(String args[]) {
    	if (args.length < 3) {
            System.err.println("linda.search.plusieurs_chercheurs.Main file nbchercheur word_search ");
            return;
    	}
        Linda lindamots = new linda.shm.CentralizedLinda();
        Linda lindacommunicaction = new linda.shm.CentralizedLinda();

        for (int j=0; j<=(args.length-3); j++){
            Manager manager = new Manager(lindamots,lindacommunicaction,args[j+2],args[0],j);
            (new Thread(manager)).start();
        }
               
        for (int i=0; i<Integer.valueOf(args[1]); i++){
            Searcher searcher = new Searcher(lindamots,lindacommunicaction,i,args.length-2);
            (new Thread(searcher)).start();
        }
        
    }
}
