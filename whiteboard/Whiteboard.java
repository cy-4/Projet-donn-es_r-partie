/*
** @author philippe.queinnec@enseeiht.fr
** Inspired by IBM TSpaces exemples.
**
**/

package linda.whiteboard;

import java.rmi.RemoteException;

public class Whiteboard {

    /*** main **
     ** Run the whiteboard as an application.
     **
     ** @param args - command line arguments
     * @throws RemoteException
     */
    public static void main(String args[]) throws RemoteException {
    	if (args.length != 1) {
    		System.err.println("Whiteboard serverURI.");
    		return;
    	}
        WhiteboardModel model = new WhiteboardModel();
        WhiteboardView view = new WhiteboardView(model);
        model.setView(view);
        model.start(new linda.server.LindaClient(args[0]));
    }
}

