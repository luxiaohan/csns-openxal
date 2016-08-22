package xal.app.newmtv;

/*import gov.sns.ca.Channel;
import gov.sns.ca.ConnectionException;
import gov.sns.ca.GetException;*/

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import xal.ca.*;
import xal.extension.widgets.swing.Wheelswitch;

//import com.cosylab.gui.components.Wheelswitch;

public class TunnerDialog extends JDialog{
	
	protected Box _objectivesContainer;	
	protected JLabel magnetname,lastrecord, upperLabel,lowerLabel;
	protected double lastfield;
	protected Number upperLimit, lowerLimit; 
	protected Wheelswitch pvWheel;
	protected JButton applyButton,restoreButton, memorizeButton;
	MTVWindow mywindow;
	List<PVRecord> pvrecordlist=null;
	int selectedrow;
	Channel setpointchannel=null;
	
	public TunnerDialog(MTVWindow window,int row){
		super( window, "Magnet Tunner", true );
		mywindow=window;		
		setSize(420, 300);		
		initial(row);						
		makeContentView();
		setResizable( false );	
		handleWindowEvents();	
	}
	
	private void initial(int row) {
		pvrecordlist=new ArrayList<PVRecord>(mywindow.pvrecordlist);
		//System.out.println("pvrecordlist size: "+pvrecordlist.size());
		selectedrow=row;
		setpointchannel=pvrecordlist.get(selectedrow).getSPChannel().getChannel();
		//System.out.println("setpointchannel: "+setpointchannel.isConnected());
		if(setpointchannel.isConnected()){
			lastfield=Double.parseDouble(pvrecordlist.get(selectedrow).getSetpointValue());
			setLimits(setpointchannel);
		}
	}

	public void setLimits(Channel channel){
		if (channel!=null){
			try {
				upperLimit =channel.upperControlLimit();
				lowerLimit = channel.lowerControlLimit();
			} catch (ConnectionException | GetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}		
	}
	
	/**
	 * Handle window events.
	 */
	protected void handleWindowEvents() {
		addWindowListener( new WindowAdapter() {
			public void windowClosing( final WindowEvent event ) {
				System.out.println( "config window closing..." );
				mywindow.buttonsColumn.editButton.setSelected(false);	
				mywindow.buttonsColumn.editButton.setBackground(UIManager.getColor("Button.background"));
			}
		});
	}

	private void makeContentView() {
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		mainView.add( Box.createVerticalStrut( 15 ) );
		mainView.add( makeDeviceView() );
		mainView.add( Box.createVerticalStrut( 15 ) );
		mainView.add( makeWheelswitchView() );
		mainView.add( Box.createVerticalStrut( 15 ) );
		mainView.add( makeLimitView() );
		mainView.add( Box.createVerticalStrut( 15 ) );
		mainView.add( makeButtonView() );
		mainView.add( Box.createVerticalStrut( 15 ) );
		
	}

	private Component makeButtonView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		restoreButton = new JButton("Restore");
		restoreButton.setToolTipText( "Restore Original Value" );		
		restoreButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				pvWheel.setValue(lastfield);
				pvrecordlist.get(selectedrow).setSetpointValue(lastfield);
			}
		});	
		view.add(restoreButton);
		view.add( Box.createHorizontalStrut( 20 ) );
		
		memorizeButton  = new JButton("Memorize");
		memorizeButton.setToolTipText( "Memorize Value as Original" );
		memorizeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed (java.awt.event.ActionEvent evt) {
					pvrecordlist.get(selectedrow).setmemButtonAct();//liyong add on 2016-1-4
					pvrecordlist.get(selectedrow).addValueChangeListener();
					double val = pvWheel.getValue();
					lastfield=val;
					lastrecord.setText("last record = "+lastfield);
				}
		});
		
		view.add(memorizeButton);
		view.add( Box.createHorizontalStrut( 60 ) );
		
		applyButton = new JButton( "Apply" );
		applyButton.setToolTipText( "Apply the selected devices to control." );
		applyButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				double val = pvWheel.getValue();
				//System.out.println("val: "+val);
				//System.out.print("upperLimit: "+upperLimit.doubleValue());
				if(val<upperLimit.doubleValue()& val>lowerLimit.doubleValue()){
				    pvrecordlist.get(selectedrow).setClickedAct();//liyong add on 2016-1-4
				    pvrecordlist.get(selectedrow).addValueChangeListener();//liyong add on 2016-1-4
				    pvrecordlist.get(selectedrow).setSetpointValue(val);
				}else{
					String message="The value you applied is out of the limit!";
					String aTitle="Erro Message";
					JOptionPane.showMessageDialog( TunnerDialog.this, message, aTitle, JOptionPane.WARNING_MESSAGE );									
				}
			}
		});
		view.add(applyButton);
		return view;
	}

	private Component makeLimitView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		upperLabel = new JLabel("upper  lim =" + upperLimit);
		lowerLabel = new JLabel("lower  lim = "+ lowerLimit);
		view.add(upperLabel);
		view.add( Box.createHorizontalStrut( 20 ) );
		view.add(lowerLabel);
		return view;
	}

	private Component makeWheelswitchView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		pvWheel = new Wheelswitch();
		pvWheel.setFormat("+###.#####");
		
		//if(pvrecordlist.get(selectedrow).getSPChannel()!=null){
			pvWheel.setValue(lastfield);
		//}
		view.add( Box.createHorizontalStrut( 20 ) );
		view.add(pvWheel);
		view.add( Box.createHorizontalStrut( 20 ) );
		return view;
	}

	private Component makeDeviceView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		magnetname = new JLabel("magnet  name = "+pvrecordlist.get(selectedrow).getDeviceName());
		
		lastrecord = new JLabel("last record = "+lastfield);
		lastrecord.setForeground(Color.RED);
		view.add(magnetname);
		view.add( Box.createHorizontalStrut( 20 ) );
		view.add(lastrecord);
		return view;
	}
}
