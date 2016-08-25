package xal.app.bbameasurement;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import xal.ca.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import Jama.Matrix;

public class NewBBAPanel extends JPanel{
	
	protected AcceleratorSeq _sequence;
	protected JProgressBar progressBar;
	final static int MAX_PROGRESS = 100;
	JSpinner fractionSpinner;
	protected Timer timer;
	protected Timer mytimer;
	protected JButton  jbBBAMea,jbMeaStop,jbSVDCal;
	protected JTextField jtfx,jtfy,jtfxp,jtfyp;
	private FunctionGraphsJPanel graphbpm = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel graphqoffset = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel graphbpmoffset = new FunctionGraphsJPanel();
	private BasicGraphData bpmXData = new BasicGraphData();                           
	private BasicGraphData bpmYData = new BasicGraphData(); 
	private BasicGraphData quadoffsetXData = new BasicGraphData();                           
	private BasicGraphData quadoffsetYData = new BasicGraphData();
	private BasicGraphData bpmoffsetXData = new BasicGraphData();                           
	private BasicGraphData bpmoffsetYData = new BasicGraphData();
	//JTable quadoffsetTable=new JTable();
	//JTable bpmoffsetTable=new JTable();
	protected QuadOffsetTableModel _quadtableModel;
	protected BPMOffsetTableModel _bpmtableModel;
	
	protected List<Quadrupole> _AVAILABLE_Quads;
	protected List<BPM> _AVAILABLE_BPMs;
	
	SVDCal svdcal;
	Matrix Ax, Ay, Mx,My;
	//MatrixCal matcal;
	//MatrixQuad matquad;
	
	
	
	public NewBBAPanel() {
		_AVAILABLE_Quads = new ArrayList<Quadrupole>();
		_AVAILABLE_BPMs=new ArrayList<BPM>();
		setSize(1300, 600);
		makeContent();
	}

	private void makeContent() {
		final Box view = new Box( BoxLayout.X_AXIS );		
		view.add(makeActionPanel());
		view.add( Box.createHorizontalStrut( 40 ) );
		view.add(makePlotPanel());
		this.add(view);	
	}

	private Component makePlotPanel() {
		JPanel graphPanel = new JPanel();
		graphPanel.setLayout(new GridLayout(3, 1, 20, 20));
		graphPanel.add(graphbpm);
		graphPanel.add(graphqoffset);
		graphPanel.add(graphbpmoffset);
		graphPanel.setPreferredSize(new Dimension(560, 700));//
		// *************for plot 1*****************
		// Make header visible, and add some text
		graphbpm.setName("BPM");
		graphbpm.setAxisNames("BPM position(m)", "BPM Reading (mm)");

		// Make legend visible
		graphbpm.setLegendVisible(true);
	    graphbpm.setLegendButtonVisible(true);
		graphbpm.addMouseListener(new SimpleChartPopupMenu(graphbpm));

		// **************for plot 2**************
		// Make header visible, and add some text
		graphqoffset.setName("Quad offset");
		graphqoffset.setAxisNames("Quad Index", "Quad offset(mm)");

		// Make legend visible
		graphqoffset.setLegendVisible(true);
		graphqoffset.setLegendButtonVisible(true);
		graphqoffset.addMouseListener(new SimpleChartPopupMenu(graphqoffset));
		
		// **************for plot 3**************
		// Make header visible, and add some text
		graphbpmoffset.setName("BPM offset");
		graphbpmoffset.setAxisNames("BPM Index", "BPM offset(mm)");

		// Make legend visible
		graphbpmoffset.setLegendVisible(true);
		graphbpmoffset.setLegendButtonVisible(true);
		graphbpmoffset.addMouseListener(new SimpleChartPopupMenu(graphbpmoffset));

		
		return graphPanel;
	}

	private Component makeActionPanel() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		Box meaview =new Box( BoxLayout.X_AXIS );
		meaview.setBorder(BorderFactory.createTitledBorder("NewBBA Measure"));
		jbBBAMea =new JButton("Start NewBBA");
		jbBBAMea.setToolTipText( "Start to Measure" );
		progressBar=new JProgressBar();
		progressBar.setStringPainted(true);
		jbMeaStop=new JButton("Stop");
		//jbsaveMea= new JButton("Save");
		
		meaview.add(new JLabel("Quad changing number: "));
		fractionSpinner = new JSpinner( new SpinnerNumberModel( 2, 2, 7, 1 ) );
		meaview.add( Box.createHorizontalStrut( 10 ) );
		meaview.add(fractionSpinner);		
		meaview.add( Box.createHorizontalStrut( 10 ) );
		meaview.add(jbBBAMea);
	    

		meaview.add( Box.createHorizontalStrut( 10 ) );
		meaview.add(progressBar);
		meaview.add( Box.createHorizontalStrut( 10 ) );
		meaview.add(jbMeaStop);
		meaview.add( Box.createHorizontalStrut( 10 ) );
		//meaview.add(jbsaveMea);
		
		Box svdview =new Box( BoxLayout.X_AXIS );
		jbSVDCal=new JButton("SVDCal");
		svdview.add(jbSVDCal);
		
		
		view.add(meaview);
		view.add( Box.createVerticalStrut(20));
		view.add(svdview);
		view.add( Box.createVerticalStrut(50));
		view.add(gettableview());
		
	    jbBBAMea.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _sequence != null ) {
					System.out.println("==START TO MEATURE==");
					jbBBAMea.setEnabled(false);
					jbSVDCal.setEnabled(false);
					//jbsaveMea.setEnabled(false);
					int step=(int) fractionSpinner.getValue();
					svdcal.setStep(step);
					/*BPMValues bpmvalues=new BPMValues(_sequence,step);
					MatrixCal matcal =new MatrixCal(_sequence,step);
					MatrixQuad matquad=new MatrixQuad(_sequence,step);
					svdcal=new SVDCal(this, matcal,matquad,bpmvalues,step,_AVAILABLE_Quads.size(),_AVAILABLE_BPMs.size());	*/
					svdcal.getSVDRight();
					startResponseTimer();
					svdcal.startMeasure();
					//svdcal.getSVDRight();
				   // svdcal.getSVDLeft();						
				}else{
				    System.out.println("Please select sequence before measurement!");  
				}
			}
		});
	    	    
		jbMeaStop.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				System.out.println("Stop measuring newBBA");
				if(svdcal == null) return;
				if(!svdcal.getThread().isAlive()) return;
				svdcal.stopMeasure();
				//jbBBAMea.setEnabled(true);
			}
		});
	    
	    jbSVDCal.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if(svdcal!=null){
				    Ax=svdcal.getMatSVDRightx();
				    Ay=svdcal.getMatSVDRighty();
				    Mx=svdcal.getMatSVDLeftx();
				    My=svdcal.getMatSVDLefty();
					svdcal.getsvd(Ax,Mx,Ay,My);
					setResult(svdcal.getInitialx(),svdcal.getBpmoffsetx(),svdcal.getQuadoffsetx(),svdcal.getInitialy(),svdcal.getBpmoffsety(),svdcal.getQuadoffsety());
				}
			}	
		});
		
		return view;
	}
	/** Start the progress timer. */
	protected void startResponseTimer() {
	    mytimer = new Timer( 1000, new ActionListener() {
				public void actionPerformed( final ActionEvent event ) {
					updateProgress();
				}
			});
			
	    mytimer.setRepeats( true );
	   mytimer.start();
	}
	
	/**
	 * Update the feedback to reflect the flattening progress.
	 */
	protected void updateProgress() {		
		progressBar.setValue( (int) ( MAX_PROGRESS * svdcal.bpmvalues.getFractionComplete() ) );
	}
	
	public void measureEnd(){
		progressBar.setValue(100);
		mytimer.stop();
		jbBBAMea.setEnabled(true);
		jbSVDCal.setEnabled(true);
		//jbsaveMea.setEnabled(true);
	}


	//设置测量结果
	protected void setResult(double[] initialx, double[] bpmoffsetx,double[] quadoffsetx,double[] initialy, double[] bpmoffsety,double[] quadoffsety) {
		// set TextField
		jtfx.setText(String.valueOf(initialx[0]));
		jtfxp.setText(String.valueOf(initialx[1]));
		jtfy.setText(String.valueOf(initialy[0]));
		jtfyp.setText(String.valueOf(initialy[1]));
		//set Table
		setQuadsOffsetTable(quadoffsetx,quadoffsety);
		setBPMsOffsetTable(bpmoffsetx,bpmoffsety);
		//set graph
		quadoffsetplotDisplay(quadoffsetx,quadoffsety);
		bpmoffsetplotDisplay(bpmoffsetx,bpmoffsety);
		
		
	}
	
	private void bpmoffsetplotDisplay(double[] bpmoffsetx, double[] bpmoffsety) {
		double [] bpmindex=new double[_AVAILABLE_BPMs.size()];
		for(int i=0;i<_AVAILABLE_BPMs.size();i++){			
			bpmindex[i]=i+1;
		}
		bpmoffsetXData.setGraphColor(Color.RED);
		bpmoffsetYData.setGraphColor(Color.BLACK);
		bpmoffsetXData.setGraphProperty(graphbpmoffset.getLegendKeyString(), "bpmoffset_x");
		bpmoffsetYData.setGraphProperty(graphbpmoffset.getLegendKeyString(), "bpmoffset_y");
	    graphbpmoffset.removeAllGraphData();
	    bpmoffsetXData.addPoint(bpmindex, bpmoffsetx);
	    bpmoffsetYData.addPoint(bpmindex, bpmoffsety);	    		
	    graphbpmoffset.addGraphData(bpmoffsetXData);
	    graphbpmoffset.addGraphData(bpmoffsetYData);	
		
	}

	private void quadoffsetplotDisplay(double[] quadoffsetx, double[] quadoffsety) {
		double [] quadindex=new double[_AVAILABLE_Quads.size()];
		for(int i=0;i<_AVAILABLE_Quads.size();i++){
			quadindex[i]=i+1; 
		}
		quadoffsetXData.setGraphColor(Color.RED);
		quadoffsetYData.setGraphColor(Color.BLACK);
	    quadoffsetXData.setGraphProperty(graphqoffset.getLegendKeyString(), "quadoffset_x");
	    quadoffsetYData.setGraphProperty(graphqoffset.getLegendKeyString(), "quadoffset_y");
	    graphqoffset.removeAllGraphData();
	    quadoffsetXData.addPoint(quadindex, quadoffsetx);
	    quadoffsetYData.addPoint(quadindex, quadoffsety);	    		
	    graphqoffset.addGraphData(quadoffsetXData);
	    graphqoffset.addGraphData(quadoffsetYData);		
	}

	public void updateChartWithPeriod(final List<BPM> bpms, final double period ) {
		final long msecPeriod = Math.round( 1000 * period );
		java.util.Timer _chartTimer = new java.util.Timer();
		_chartTimer.schedule( newChartUpdateTask(bpms, msecPeriod ), msecPeriod, msecPeriod );
	}
	
	/** make a timer task to update the chart */
	public TimerTask newChartUpdateTask(final List<BPM> bpms, final long period ) {
		return new TimerTask() {
			public void run() {
				try {
					SwingUtilities.invokeAndWait( newChartUpdater(bpms) );
					Thread.sleep( period );	// make sure we rest for at least the specified period
				}
				catch ( Exception exception ) {
					System.err.println( "Exception updating the chart..." );
					exception.printStackTrace();
				}
			}
		};
	}
	
	/** make a runnable to update the chart */
	public Runnable newChartUpdater(final List<BPM> bpms) {
		return new Runnable() {
			public void run() {
				updateBpmChart(bpms);
			}
		};
	}	
		
	public void updateBpmChart(List<BPM> bpms) {
		double [] tempbpmpos =new double[bpms.size()];
		double [] tempbpmx =new double[bpms.size()];
		double [] tempbpmy =new double[bpms.size()];
		for (int i=0;i<bpms.size();i++){
			tempbpmpos[i]= bpms.get(i).getPosition();
			//Channel bpmXAvgChannel = bpms.get(i).getChannel( BPM.X_AVG_HANDLE );
			//Channel bpmYAvgChannel = bpms.get(i).getChannel( BPM.Y_AVG_HANDLE ); 
			//if(bpmXAvgChannel.isConnected() && bpmYAvgChannel.isConnected()){
			    try {
				    tempbpmx[i]=bpms.get(i).getXAvg();
				    tempbpmy[i]=bpms.get(i).getYAvg();
			    } catch (ConnectionException | GetException e) {
				// TODO Auto-generated catch block
				 e.printStackTrace();
			    }
		   // }
		}
		graphbpm.removeAllGraphData();
		bpmXData.setGraphColor(Color.RED);
		bpmYData.setGraphColor(Color.BLACK);
		bpmXData.setGraphProperty( graphbpm.getLegendKeyString(), "xAvg" );
		bpmYData.setGraphProperty( graphbpm.getLegendKeyString(), "yAvg" );
		bpmXData.addPoint(tempbpmpos, tempbpmx);
		bpmYData.addPoint(tempbpmpos, tempbpmy);
		graphbpm.addGraphData(bpmXData);
		graphbpm.addGraphData(bpmYData);
	}


	private void setQuadsOffsetTable(double[] quadoffsetx, double[] quadoffsety) {
		Object[][] quaddata = new Object[_AVAILABLE_Quads.size()][3];
		_quadtableModel.setMode(quaddata);
		for (int i = 0; i < _AVAILABLE_Quads.size(); i++) {						
			_quadtableModel.addRowName(_AVAILABLE_Quads.get(i).getId(), i);
			_quadtableModel.setValueAt(quadoffsetx[i], i, 1);
			_quadtableModel.setValueAt(quadoffsety[i], i, 2);				
		}
		_quadtableModel.fireTableDataChanged();		
	}
	
	private void setBPMsOffsetTable(double[] bpmoffsetx, double[] bpmoffsety) {
		Object[][] bpmdata = new Object[_AVAILABLE_BPMs.size()][3];
		_bpmtableModel.setMode(bpmdata);
		for (int i = 0; i < _AVAILABLE_BPMs.size(); i++) {						
			_bpmtableModel.addRowName(_AVAILABLE_BPMs.get(i).getId(), i);
			_bpmtableModel.setValueAt(-bpmoffsetx[i], i, 1);
			_bpmtableModel.setValueAt(-bpmoffsety[i], i, 2);				
		}
		_bpmtableModel.fireTableDataChanged();			
	}


	private Component gettableview() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		final Box tableview = new Box( BoxLayout.X_AXIS );
		tableview.setBorder(BorderFactory.createTitledBorder("Q+BPM offset"));

		_quadtableModel = new QuadOffsetTableModel(null);		
		JTable quadoffsetTable = new JTable(_quadtableModel);
		quadoffsetTable.getColumnModel().getColumn(0).setPreferredWidth(130);
		
		JScrollPane quadscrollpane =new JScrollPane(quadoffsetTable);
		quadoffsetTable.setPreferredScrollableViewportSize(quadoffsetTable.getPreferredSize());
		quadscrollpane.setPreferredSize(new Dimension(180, 500));
		quadscrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		_bpmtableModel = new BPMOffsetTableModel(null);
		JTable bpmoffsetTable=new JTable(_bpmtableModel);
		bpmoffsetTable.getColumnModel().getColumn(0).setPreferredWidth(170);
		JScrollPane bpmscrollpane =new JScrollPane(bpmoffsetTable);
		bpmoffsetTable.setPreferredScrollableViewportSize(bpmoffsetTable.getPreferredSize());
		bpmscrollpane.setPreferredSize(new Dimension(180, 500));
		bpmscrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		tableview.add(quadscrollpane);
		tableview.add(bpmscrollpane);
		
		final Box initialview = new Box( BoxLayout.X_AXIS );
		initialview.setBorder(BorderFactory.createTitledBorder("Initial Result"));
		jtfx=new JTextField(6);
		jtfy=new JTextField(6);
		jtfxp=new JTextField(6);
		jtfyp=new JTextField(6);
		initialview.add(new JLabel("x(mm): "));
		initialview.add(jtfx);
		initialview.add(new JLabel("xp(mrad): "));
		initialview.add(jtfxp);
		initialview.add(new JLabel("y(mm): "));
		initialview.add(jtfy);
		initialview.add(new JLabel("yp(mrad): "));
		initialview.add(jtfyp);
		
		view.add(tableview);
		view.add( Box.createVerticalStrut(20));
		view.add(initialview);
		return view;
	}

	public void setSequence(AcceleratorSeq sequence) {
		_sequence=sequence;	
		if(_sequence!=null){
		    List bpms = sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(BPM.s_strType).and(
							QualifierFactory.getStatusQualifier(true)));
		    _AVAILABLE_BPMs.clear();
		    _AVAILABLE_BPMs.addAll(bpms);
		
		    List quads = sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(Quadrupole.s_strType).and(
							QualifierFactory.getStatusQualifier(true)));
		    _AVAILABLE_Quads.clear();
		    _AVAILABLE_Quads.addAll(quads);
		   updateChartWithPeriod( _AVAILABLE_BPMs, 1.0 );
		
		    BPMValues bpmvalues=new BPMValues(_sequence);
		    MatrixCal matcal =new MatrixCal(_sequence);
		    MatrixQuad matquad=new MatrixQuad(_sequence);
		    svdcal=new SVDCal(this, matcal,matquad,bpmvalues,_AVAILABLE_Quads.size(),_AVAILABLE_BPMs.size());	
		}
	}
	
	protected class QuadOffsetTableModel extends AbstractTableModel {

		final String[] columnNames = { "QuadName", "qx(mm)" ,"qy(mm)"};
		 Object[][] data ;

		/** Container for row labels */
		
	    private ArrayList rowNames = new ArrayList(_AVAILABLE_Quads.size());
	    
		public QuadOffsetTableModel(Object[][] data1) {
			setMode(data1);
		}

		private void setMode(Object[][] data1) {			
			data=data1;		
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return ( data != null ) ? data.length : 0;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public String getRowName(int row) {
			return (String) rowNames.get(row);
		}

		public boolean isCellEditable(int row, int col) {
				return false;
		}

		/** method to add a row name */
		public void addRowName(String name, int row) {
			rowNames.add(row, name);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {			
			if (columnIndex == 0) {
				return (String) rowNames.get(rowIndex);
			} else 
				return data[rowIndex][columnIndex];
		}

		public void setValueAt(Object value, int row, int col) {
			if (col > 0) {				
				data[row][col] = (Object) value;
				fireTableCellUpdated(row, col);
				return;
			}
		}
	  }
	
	protected class BPMOffsetTableModel extends AbstractTableModel {

		final String[] columnNames = { "BPMName", "bx(mm)" ,"by(mm)"};
		 Object[][] data ;

		/** Container for row labels */
		
	    private ArrayList rowNames = new ArrayList(_AVAILABLE_BPMs.size());
	    
		public BPMOffsetTableModel(Object[][] data1) {
			setMode(data1);
		}

		private void setMode(Object[][] data1) {			
			data=data1;		
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return ( data != null ) ? data.length : 0;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public String getRowName(int row) {
			return (String) rowNames.get(row);
		}

		public boolean isCellEditable(int row, int col) {
				return false;
		}

		/** method to add a row name */
		public void addRowName(String name, int row) {
			rowNames.add(row, name);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {			
			if (columnIndex == 0) {
				return (String) rowNames.get(rowIndex);
			} else 
				return data[rowIndex][columnIndex];

		}

		public void setValueAt(Object value, int row, int col) {
			if (col > 0) {				
				data[row][col] = (Object) value;
				fireTableCellUpdated(row, col);
				return;
			}
		}
	  }
	
	
	
	

}
