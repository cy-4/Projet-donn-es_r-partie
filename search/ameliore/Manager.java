package linda.search.ameliore;

import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import linda.*;

public class Manager implements Runnable {

    private Linda lindamots,lindacommunicaction;

    private UUID reqUUID;
    private String search;
    private int bestvalue = Integer.MAX_VALUE; // lower is better
    private String bestresult;

    public Manager(Linda lindamots,Linda lindacommunicaction, String search) {
        this.lindamots = lindamots;
        this.lindacommunicaction = lindacommunicaction;
        this.search = search;
    }

    private void addSearch(String search) {
        this.search = search;
        this.reqUUID = UUID.randomUUID();
        System.out.println("Search " + this.reqUUID + " for " + this.search);
        lindamots.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, this.reqUUID, String.class, Integer.class), new CbGetResult());
        lindacommunicaction.write(new Tuple(Code.Request, this.reqUUID, this.search));
    }

    private void waitForEndSearch() {
        lindacommunicaction.take(new Tuple(Code.Searcher, "done", this.reqUUID));
        lindacommunicaction.take(new Tuple(Code.Request, this.reqUUID, String.class)); // remove query
        System.out.println("query done");
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
            lindamots.eventRegister(Linda.eventMode.TAKE, Linda.eventTiming.IMMEDIATE, new Tuple(Code.Result, reqUUID, String.class, Integer.class), this);
        }
    }

    public void run() {
        this.addSearch(search);
        this.waitForEndSearch();
    }
}
