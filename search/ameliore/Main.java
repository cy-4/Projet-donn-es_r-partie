package linda.search.ameliore;
import java.rmi.RemoteException;

import linda.*;

public class Main {

    public static void main(String args[]) throws RemoteException {
    	if (args.length < 3) {
            System.err.println("linda.search.plusieurs_chercheurs.Main file nbchercheur words_search ");
            return;
    	}
        Linda lindamots = new linda.shm.CentralizedLinda();
        Linda lindacommunicaction = new linda.shm.CentralizedLinda();
        //Linda lindamots = new linda.server.LindaClient("//localhost:4000/MonServeur");
        //Linda lindacommunicaction = new linda.server.LindaClient("//localhost:4000/MonServeur2");

        // On crée autant de Manager que de mot à chercher
        for (int j=0; j<=(args.length-3); j++){
            Manager manager = new Manager(lindamots,lindacommunicaction,args[j+2],args[0],j);
            (new Thread(manager)).start();
        }
        
        // On crée autant de searcher qu'il y a spécifié dans la ligne de commande
        for (int i=0; i<Integer.valueOf(args[1]); i++){
            Searcher searcher = new Searcher(lindamots,lindacommunicaction,i,args.length-2);
            (new Thread(searcher)).start();
        }
        
    }
}
