package xal.app.newmtv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import xal.ca.Channel;
import xal.extension.application.smf.AcceleratorWindow;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
import xal.smf.*;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.qualify.QualifierFactory;
import xal.smf.impl.qualify.TypeQualifier;

public class MTVWindow extends AcceleratorWindow {
	
	private static final long serialVersionUID = 1L;
	
	/** table of knob elements */
	protected AcceleratorSeq _sequence;
	protected Accelerator _acc;
	protected JTable _pvTable;
	protected JTextField filterField,jtfgroup,jtfperiod;
	protected KeyValueFilteredTableModel<PVRecord> _pvTableModel;
	protected Box view;
	protected List<String> pvnamelist =new ArrayList<String>();
	protected List<PVRecord> pvrecordlist=null;
	protected javax.swing.Timer timer;
	protected ButtonColumn buttonsColumn;
	
	public MTVWindow(MTVDocument aDocument) {
		super(aDocument);		
		setSize(800, 550);
		makeContent();
		_acc=((MTVDocument) this.document).getAccelerator();
		pvrecordlist=new ArrayList<PVRecord>();
	}

	public void setAccelerator(Accelerator accelerator) {
		setSequence(null);
	}

	public void setSequence(AcceleratorSeq sequence) {
		if (sequence == null) return;
		_sequence = sequence;
		pvrecordlist.clear();
		TypeQualifier typeQualifier = QualifierFactory.qualifierWithStatusAndTypes( true, Electromagnet.s_strType );       
		List<Electromagnet> mags = _sequence.getAllNodesWithQualifier( typeQualifier );
		List<String> spchannelnames=new ArrayList<String>(mags.size());
		List<String> rbchannelnames=new ArrayList<String>(mags.size());
		for(int i=0;i<mags.size();i++){
			final Electromagnet em = mags.get(i);
			String psname=em.getMainSupply().getId();
			String devicename=em.getId();
			String spchannelName=psname+":B_Set";          	
			spchannelnames.add(spchannelName);
			
			String rbchannelName=devicename+":B";           	
			rbchannelnames.add(rbchannelName);
			
			String tunner="KNOB";
			
			String type = em.getType();
		    String magtype=null;
		    if(psname.endsWith("C")){
			    magtype=type+"_MAIN";
		    }else if(psname.endsWith("TRIM")){
			    magtype=type+"_TRIM";
		    }else magtype=type;

			PVRecord record=new PVRecord(devicename, magtype,tunner);			
		    pvrecordlist.add(record);
		}
		
		List<ChannelWrapper> spwrappers=new ArrayList<ChannelWrapper>(spchannelnames.size());
		List<ChannelWrapper> rbwrappers=new ArrayList<ChannelWrapper>(spchannelnames.size());
	    for(int i=0;i< pvrecordlist.size();i++){
		    ChannelWrapper spchannelwrapper= new ChannelWrapper(spchannelnames.get(i));
		    ChannelWrapper rbchannelwrapper= new ChannelWrapper(rbchannelnames.get(i));
		    pvrecordlist.get(i).setSPChannel(spchannelwrapper);
		    pvrecordlist.get(i).setRBChannel(rbchannelwrapper);
		    spwrappers.add(spchannelwrapper);
		    rbwrappers.add(rbchannelwrapper);
	    }
	    Channel.flushIO();
   
        ConnectionChecker spconnectionChecker=new ConnectionChecker(spwrappers);
        ConnectionChecker rbconnectionChecker=new ConnectionChecker(rbwrappers);
	    spconnectionChecker.doCheck(3.);	
	    rbconnectionChecker.doCheck(3.);
	    
		_pvTableModel.setRecords(pvrecordlist);
		final int rowCount = _pvTableModel.getRowCount();
		if ( rowCount > 0 ) {
			_pvTableModel.fireTableRowsUpdated( 0, rowCount - 1 );
		}
	}

	private void makeContent() {
		 view = new Box( BoxLayout.Y_AXIS );
		 //_pvTableModel =new KeyValueFilteredTableModel<PVRecord>(new ArrayList<PVRecord>(),"deviceName","deviceType","setpointValue","liveReadbackValue","readbackErro","tunner");	
		 _pvTableModel =new KeyValueFilteredTableModel<PVRecord>(new ArrayList<PVRecord>(),"deviceName","deviceType","tunner","setpointValue","liveReadbackValue","readbackErro");	
		 _pvTableModel.setColumnName( "deviceName", "Device_Name" );
		 _pvTableModel.setColumnName( "deviceType", "Device_Type" );
		 _pvTableModel.setColumnName( "setpointValue", "B_Set" );
		 _pvTableModel.setColumnName( "liveReadbackValue", "B_ReadBack" );
		 _pvTableModel.setColumnName( "readbackErro", "ReadBack_Erro" );
		_pvTableModel.setColumnName( "tunner", "Tunner" );
		_pvTableModel.setColumnEditable("tunner",true);		
		
		filterField = new JTextField(2147483647);
		_pvTableModel.setInputFilterComponent( filterField );
		filterField.setMaximumSize(filterField.getPreferredSize() );		
		_pvTable=new JTable();
		_pvTable.setRowHeight(30);
		_pvTable.setModel(_pvTableModel);
		//_pvTable.getColumnModel().getColumn(2).setPreferredWidth(2);

		buttonsColumn = new ButtonColumn(this,_pvTable, 2);
		_pvTable.setRowSelectionAllowed(false);
		
		_pvTable.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
				    int row=_pvTable.rowAtPoint(e.getPoint());
				    int col=_pvTable.columnAtPoint(e.getPoint());
				    if(col==2){					
					   // System.out.println("mywindow=================");
					  //  System.out.println("selectedrow: " + row);
					  //  System.out.println("name: " +pvrecordlist.get(row).getDeviceName() );
					    buttonsColumn.editButton.setSelected(true);
					    buttonsColumn.showTunnerDialog(row);
				}
				}
			}
		});
		
		ActionListener taskPerformer = new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
				tableTask();
		    }
		};	
	    timer = new javax.swing.Timer(1000, taskPerformer);
		timer.start();
		       
		final TableCellRenderer defaultRenderer =_pvTable.getDefaultRenderer(Object.class);;
		TableCellRenderer newRenderer = new TableCellRenderer(){
			 public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
				 boolean hasFocus, int row, int column){				 
			 			Component component = defaultRenderer.getTableCellRendererComponent(table,value,isSelected,
							hasFocus,row,column);
			 			String temp=(String)(table.getValueAt(row, 5));
						if(column == 3){								
							PVRecord record=pvrecordlist.get(row);
							if(record.isValueChanged()){
								component.setForeground(Color.blue);
							}
							else{
								component.setForeground(Color.black);
							}
						}
						else if(column == 5){
							if( !temp.contains("�")){
								if(Math.abs(Double.parseDouble(temp))>0.001){
									component.setForeground(Color.red);
								}										    		
							}
						  
					    }else  component.setForeground(Color.black);
						return component;
				 }
		};
		_pvTable.setDefaultRenderer(Object.class,newRenderer); 
		_pvTable.setDefaultRenderer(PVRecord.class,newRenderer);
		
		view.add(getSearchView());
		view.add( new JScrollPane( _pvTable ) );	
		this.getContentPane().add(view);
	}
	
	/** the action to perform when the tables need updating */
	protected void tableTask() {
		if( _pvTable != null ) {
			final AbstractTableModel tableModel = (AbstractTableModel)_pvTable.getModel();
			final int rowCount = tableModel.getRowCount();
			final int comCount = tableModel.getColumnCount();
			/*if ( rowCount > 0 ) {
				tableModel.fireTableRowsUpdated( 0, rowCount - 1 );				
			}*/
			if ( rowCount > 0 ) {
				for(int row=0;row<rowCount;row++ ){
					for(int column=0;column<comCount-1;column++){
						tableModel.fireTableCellUpdated(row, column);
						tableModel.fireTableDataChanged();
					}
				}				
			}
			else {
				tableModel.fireTableDataChanged();
			}
		}
	}
	private Component getSearchView() {
		Box view =new Box(BoxLayout.X_AXIS );
		JLabel jlSearch = new JLabel("Filter: ");	
		view.add(jlSearch);
		view.add(filterField);
		return view;
	} 
}


