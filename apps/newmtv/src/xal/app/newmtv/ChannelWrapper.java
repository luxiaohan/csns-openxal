/**
 * A Wrapper class around the Channel object to provide functionality for 
 * callback value storage. Only works for double value channels now.
 * @version   1.1
 * provisions are in (but commented out) to make this a callback get 
 * archetecture, rather than a monitored system.l
 * @author    J. Galambos
 */

package xal.app.newmtv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import xal.ca.*;

public class ChannelWrapper implements IEventSinkValue, ConnectionListener {
	/** The channel object */
	//private Channel channel;
	protected Channel channel;
	
	/** the monitor for this channel */
	protected Monitor monitor;
	
	/** latest record */
	protected ChannelRecord _latestRecord;
	
	private boolean isChanged = false;
	
	private ActionListener valueChangeListener = null;
	private ActionEvent valueChangeEvent = null;
	

	
	/** primary constructor */
	public ChannelWrapper( final Channel chan ) { 
		_latestRecord = null;
		channel = chan;
		channel.addConnectionListener( this );
		channel.requestConnection();
		valueChangeEvent = new ActionEvent(this,0,"change");
	}
	
	
	/** the constructor */
	public ChannelWrapper( final String name ) { 
		this( ChannelFactory.defaultFactory().getChannel( name ) );
	}
	
	
	/** whether this channel is connected */
	protected boolean isConnected() { 
		return channel.isConnected();
	}
	
	/** sets an ActionListener for a value change*/
	public void setValueChangeListener(ActionListener valueChangeListener){
		this.valueChangeListener = valueChangeListener;
	}
	
	/** return the channel */
	protected Channel getChannel() { return channel; }
	
	
	/** get the value as a double */
	public double doubleValue() {
		return _latestRecord != null ? _latestRecord.doubleValue() : Double.NaN;
	}
	
	
	/** get the value as string */
	public String stringValue() {
		return _latestRecord != null ? _latestRecord.stringValue() : "";
	}
	
	
	/** the name of the Channel */
	protected String getId() { return channel.getId(); }
	
	
	/** make a monitor connection to a channel */
	private void makeMonitor() {
		try {
		    monitor = channel.addMonitorValue( this, Monitor.VALUE );	
		}
		catch(ConnectionException exc) {}
		catch(MonitorException exc) {}
	}
	
	
	/** The Connection Listener interface */
	public void connectionMade(Channel chan) {
		if (monitor == null) makeMonitor();
	}
	
	
	/** fire a callback fetch of the value */
	protected void fireGetCallback() {
		try {
			channel.getValueCallback( this );
		}
		catch (Exception ex) {
		}		
	}
	
	
	/** ConnectionListener interface */
	public void connectionDropped(Channel aChannel) {
		System.out.println("Channel dropped " + aChannel.channelName() );
		_latestRecord = null;
	}

	/** interface method for IEventSinkVal */
	public void eventValue(ChannelRecord newRecord, Channel chan) {
		_latestRecord = newRecord;
		if(valueChangeListener != null){
			valueChangeListener.actionPerformed(valueChangeEvent);
		}
	}
}

