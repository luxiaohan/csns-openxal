package xal.app.newmtv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import xal.ca.*;

public class PVRecord {	
	private String DeviceName;
	private Double Position ;
	private String DeviceType;
	//private String PowerSupplyName ;//liyong add
    /** the readback Channel */
    protected ChannelWrapper _setpointChannel;
    protected ChannelWrapper _readbackChannel;
	static NumberFormat nf;
	//private  String Tunner;
	private  String Tunner;
	
	private boolean wasClicked = false;
	private boolean isChanged = false;
	private volatile boolean memButtonAct = false;
	
	static{
    	nf= NumberFormat.getNumberInstance();
    	nf.setMaximumFractionDigits(5);
    	nf.setMinimumFractionDigits(5);
	}
    
    public PVRecord(String name,String type,String tunner){
    	DeviceName=name;
    	DeviceType=type;
    	_setpointChannel=null;
    	_readbackChannel=null; 
    	Tunner=tunner;
    }
    
    public String getDeviceName() {
		return DeviceName;
	}


	public void setDeviceName(String deviceName) {
		DeviceName = deviceName;
	}

	public String getDeviceType() {
		return DeviceType;
	}


	public void setDeviceType(String deviceType) {
		DeviceType = deviceType;
	}
	
	/** set the rb channel */
    public void setSPChannel( ChannelWrapper channel ) { 
    	_setpointChannel = channel; }
    
    /** get the rb channel */
    public ChannelWrapper getSPChannel() { return _setpointChannel; }
    
 	/** Get the latest live readback value */
    public void setSetpointValue(double val) { 
 		if( _setpointChannel != null ){
 			try {
 	        	_setpointChannel.getChannel().putVal(val);
 		    } catch (ConnectionException e) {
 			// TODO Auto-generated catch block
 			    e.printStackTrace();
 		    } catch (PutException e) {
 			// TODO Auto-generated catch block
 			    e.printStackTrace();
 		    }
 		}					
 	}
 	
    
 	/** Get the latest live readback value */
     public String getSetpointValue() {
  		return _setpointChannel != null ? nf.format(_setpointChannel.doubleValue()) : null;
  		
  	}
      
	/** set the rb channel */
    public void setRBChannel( ChannelWrapper channel ) { _readbackChannel = channel; }
		
    /** get the rb channel */
    public ChannelWrapper getRBChannel() { return _readbackChannel; }
    
 	/** Get the latest live readback value */
     public String getLiveReadbackValue() {
  		return _readbackChannel != null ? nf.format(_readbackChannel.doubleValue()) : null;
  	}
      
  	/** Get the latest live readback value */
     public String getReadbackErro() {
  		return _readbackChannel != null ? nf.format(_setpointChannel.doubleValue()-_readbackChannel.doubleValue()): null;
  	}

	public String getTunner() {
		return Tunner;
	}

	public void setTunner(String tunner) {
		Tunner = tunner;
	}
	
	//==================================

	public void setmemButtonAct() {
		memButtonAct=true;		
	}
	
	/*public boolean getmemButtonAct(){
		return memButtonAct;
	}*/

	public void setClickedAct() {
		wasClicked=true;		
	}
	
	/*public boolean getClickedAct(){
		return wasClicked;
	}*/
	
	public void addValueChangeListener(){
		//ActionListener valueChangeListener = new ActionListener(){
			//public void actionPerformed(ActionEvent evnt){
				if(wasClicked){
					if(memButtonAct != true){
						isChanged = true;
					}
					else{
						memButtonAct = false;
						isChanged = false;
					}
				}
			//}
	//	};
		/*if(_setpointChannel!= null){
			_setpointChannel.setValueChangeListener(valueChangeListener);
		}*/
	}
	
	/** returns true is the cell was clkicked */
	public boolean wasClicked(){
		return wasClicked;
	}
	
	/** returns true if the value has been changed */
	public boolean isValueChanged(){
		return isChanged;
	}
	
}
