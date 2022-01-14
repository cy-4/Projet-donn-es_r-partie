package linda.server;

import linda.Callback;
import linda.Tuple;

public class Callback2 implements CallbackRemote {

    public Callback cb;

    public Callback2(Callback cb) {
        this.cb = cb;
    }

    @Override
    public void call(Tuple t) {
    }
    
}
