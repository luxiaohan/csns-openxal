/**
 * A class to do checks on the connection status
 * of the PVs in the tables (both connections for monitors + value puts)
 *
 */

package xal.app.newmtv;

import java.text.*;
import java.util.*;
import java.lang.*;
import javax.swing.*;

public class ConnectionChecker implements Runnable {
	
    /** the thread this works in */
    private Thread thread;
	
	/** a list containing the channel names that are not connected */
    private ArrayList<String> notConnected;
    
	/** the time to wait before giving up on connections (sec)*/
    private double timeOut;
    
    /** the number of checks to make (calculated from timeOut */
    private int nTrys;
	
    /* minimum time to wait between channelConnection checks (sec) */
    static public final double dwellTime = 0.5;
	
    /** A flag indicating some channels are not connected */
    private boolean allOK = false;
	
    List<ChannelWrapper> _wrappers;
    /** return the array of not connected PV names from the last check */
    protected ArrayList<String> getNotconnectedPVs() { return notConnected;}
	
    public ConnectionChecker(List<ChannelWrapper> wrappers){
    	_wrappers=wrappers;
    }
    /** method to start a check 
     * @param to - timeout period for connection check (sec)
     */
    public void doCheck(double to) {
		System.out.println("Starting a check");
		// do connection checs in a seperate thread so we do not block other actions
		timeOut = to;
		nTrys = (int) (timeOut/dwellTime);
		thread = new Thread(this, "ConnectChecker");
		thread.start();
    }
	
    /* monitor connection status until timeout */
	
    public void run() {
		startConnectionStatus();
		int i=0;
		while (i< nTrys) {
			notConnected = getNotConnectedPVs();
			if(notConnected.size() == 0) {
				allOK = true;
				break;
			}
			else {
				try {
					Thread.sleep((int) (1000 * dwellTime));
				}
				catch (InterruptedException e) {}   
				i++;
			}
		}
		
		reportStatus();
    }
	
    /** Check the document tables monitor connections and
	 report if any are not connected */
	
    public void startConnectionStatus() {
	    
		allOK = false;	
    }
	
	
    /** Report the status of connections after checking is done */
    private void reportStatus() {
		String name;
		System.err.println("Connection Status Check at " + new Date());
		
		if(notConnected == null) {
			System.err.println("What the ..?, In ConnectionChecker, tried to updateConnectionStatus with a null notConnected map");
			return;
		}
		
		if (allOK) 
			System.err.println("All connections are OK");
		else {
			System.err.println("Some channels are not connected - see console output");
		    System.err.println("The following channels are not connected");		
		    Iterator<String> itr = notConnected.iterator();
		    while(itr.hasNext()) {
				name = itr.next();
				System.err.println(name);
		    }
		}
    }
    
    
    /** get a list of PVs that are not connected */
    private ArrayList<String> getNotConnectedPVs() {
	    
		ArrayList<String> badPVs = new ArrayList<String>();
        for(int i=0;i<_wrappers.size();i++){
        	ChannelWrapper rbChan=_wrappers.get(i);
        	if(rbChan != null && !rbChan.isConnected()){
				badPVs.add(rbChan.getId());
			}
        }		
		return badPVs;
    }	    
}

