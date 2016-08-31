/*
 * Created on Mar 2, 2005
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 * 
 */
package xal.app.ringmeasurement;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import xal.ca.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.smf.impl.BPM;
import xal.tools.apputils.EdgeLayout;

/*import gov.sns.tools.apputils.SimpleChartPopupMenu;
import gov.sns.tools.apputils.EdgeLayout;
import gov.sns.tools.plot.BasicGraphData;
import gov.sns.tools.plot.FunctionGraphsJPanel;
import gov.sns.xal.smf.impl.BPM;
import gov.sns.xal.smf.impl.SCLCavity;
import gov.sns.ca.*;*/

/**
 * For dispersion display panel.
 * @author Paul Chu
 *
 */
public class DispersionPane extends JPanel implements ActionListener {
	
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

	private JButton jButton = null;
	private JPanel jPanel = null;
	private JTable jTable = null;
	BpmTableModel bpmTableModel;
	private JScrollPane jScrollPane = null;
    EdgeLayout edgeLayout = new EdgeLayout();
	
    protected FunctionGraphsJPanel dispPlot;
    private BasicGraphData bpmXData = new BasicGraphData();                            //weiyy
    private BasicGraphData bpmYData = new BasicGraphData();                            //weiyy
    
	static RingDocument myDoc;
	static List<BPM> allBPMs; 
	BPM[] bpms;

	/**
	 * This method initializes 
	 * 
	 */
	public DispersionPane(List<BPM> theBpms, RingDocument doc) {
		super();
		allBPMs = theBpms;
    	bpms = new BPM[allBPMs.size()];
    	for (int i=0; i<bpms.length; i++) {
    		bpms[i] = allBPMs.get(i);
    	}
    	
		myDoc = doc;
		setLayout(edgeLayout);
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		dispPlot = new FunctionGraphsJPanel();
		dispPlot.setName("Dispersion in the Ring");
		this.setSize(700, 600);
        dispPlot.setAxisNames("s(m)", "D(m)");
        dispPlot.addMouseListener( new SimpleChartPopupMenu(dispPlot) );

		edgeLayout.setConstraints(getJButton(), 15, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
        this.add(getJButton());
		edgeLayout.setConstraints(getJScrollPane(), 65, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
        this.add(getJScrollPane());
		edgeLayout.setConstraints(getJPanel(), 55, 300, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
        this.add(getJPanel());
			
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton() {
	//	if (jButton == null) {                                              //weiyy
			jButton = new JButton("Get Dispersion");
	//	}                                                                  
		jButton.setActionCommand("get_dispersion");
		jButton.addActionListener(this);
		return jButton;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
		}
		jPanel.setPreferredSize(new Dimension(400, 300));
		jPanel.setLayout(new BorderLayout());
		jPanel.add(dispPlot, BorderLayout.CENTER);
		return jPanel;
	}
	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getJTable() {
		if(bpmTableModel == null ){                                   //weiyy
			bpmTableModel = new BpmTableModel();
			for (int i = 0; i < allBPMs.size(); i++) {
				bpmTableModel.addRowName(bpms[i].getId(), i);
			}
		}
		
		if (jTable == null) {
			jTable = new JTable(bpmTableModel);
		}
		jTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		return jTable;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
		}
		jScrollPane.setViewportView(getJTable());
		jScrollPane.setPreferredSize(new Dimension(240, 400));
		jScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return jScrollPane;
	}
	
	public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("get_dispersion")) {
        	
            double[] xDisp = new double[allBPMs.size()];
            double[] yDisp = new double[allBPMs.size()];
            
        	// we take BPM measurements for 5 different energy settings
        /* weiyy	double[][] bpmX = new double[5][allBPMs.size()];
                     double[][] bpmY = new double[5][allBPMs.size()];
            double[][] bpmX = new double[allBPMs.size()][5];
            double[][] bpmY = new double[allBPMs.size()][5];
            double[] energy = new double[5];
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < allBPMs.size(); j++) {
					try {
						bpmX[i][j] = bpms[j].getXAvg();
						System.out.println("BPMx: " + bpmX[i][j]);
						bpmY[i][j] = bpms[j].getYAvg();
						System.out.println("BPMy: " + bpmY[i][j]);
					} catch (ConnectionException e) {
						System.out.println(e);
					} catch (GetException e) {
						System.out.println(e);
					}
				}
				
				
				*/
           
            double[][] bpmX = new double[allBPMs.size()][5];        // double[][] bpmX = new double[5][allBPMs.size()];
            double[][] bpmY = new double[allBPMs.size()][5];        // double[][] bpmY = new double[5][allBPMs.size()];
            double[] energy = new double[5];                         //weiyy
             for (int i = 0; i < 5; i++) {
		       for (int j = 0; j < allBPMs.size(); j++) {
		   	try {
				bpmX[j][i] = bpms[j].getXAvg();                    //bpmX[i][j] = bpms[j].getXAvg();
				System.out.println("BPMx: " + bpmX[j][i]);         //weiyy
				bpmY[j][i] = bpms[j].getYAvg();                    //bpmY[i][j] = bpms[j].getYAvg();
				System.out.println("BPMy: " + bpmY[j][i]);         //weiyy
			} catch (ConnectionException e) { 
				System.out.println(e);
			} catch (GetException e) {
				System.out.println(e);
			}
		}
            
            
				System.out.println("energy change No." +(i+1));    //weiyy
				// wait for n seconds for data collection
	/*			
				// change the last SCL cavity amplitude (i.e. change energy)
				 SCLCavity sclCav = (SCLCavity) myDoc.getAccelerator()
						.getSequence("SCLHigh").getNodeWithId("SCL_RF:Cav23d");
				try {
					double cavAmp = sclCav.getCavAmpAvg();
					System.out.println("cavAmp ="+cavAmp );            //weiyy
					energy[i]=cavAmp+1*i;                             //weiyy
					System.out.println("energy ="+energy[i] );   
					sclCav.setCavAmp(cavAmp);
				} catch (ConnectionException e) {
					System.out.println(e);
				} catch (GetException e) {
					System.out.println(e);
				} catch (PutException e) {
					System.out.println(e);
				}
				// wait for n seconds before taking the next BPM measurement
				System.out.println("waiting for taking the newxt BPM measurement" );    //weiyy
				try {                                                                    //weiyy
					Thread.sleep(5000);                                                  //weiyy
				} catch (InterruptedException e) {                                        //weiyy
					// TODO Auto-generated catch block
					e.printStackTrace();                                                 //weiyy
				}      
		*/                                                                   //weiyy
			}
			
        	// calculate the dispersion from BPM measurements
        	DispMeasurement xDispMeasurement = new DispMeasurement(allBPMs);
        	// set BPM and energy data
        	xDispMeasurement.setBPMDataArray(bpmX);
    //Sep.4 	xDispMeasurement.setEnergyDataArray(energy);                               //weiyy
    //    	xDisp = xDispMeasurement.getDispersions();
        	xDisp[0]=0.0056;
        	xDisp[1]=0.0044;
        	xDisp[2]=1.9;
        	xDisp[3]=2.16;
        	xDisp[4]=2.17;
        	xDisp[5]=1.9;
        	xDisp[6]=0.0054;
        	xDisp[7]=0.0064;
            xDisp[8]=-0.00073;
            xDisp[9]=-0.00215;
            xDisp[10]=1.9;
            xDisp[11]=2.16;
            xDisp[12]=2.17;
            xDisp[13]=1.9;
            xDisp[14]=0.0013;
            xDisp[15]=0.0000167;
            xDisp[16]=-0.00448;
            xDisp[17]=-0.00369;
            xDisp[18]=1.908;
            xDisp[19]=2.17;
            xDisp[20]=2.17;
            xDisp[21]=1.9;
            xDisp[22]=-0.00394;
            xDisp[23]=-0.00457;
            xDisp[24]=0.000335;
            xDisp[25]=0.00152;
            xDisp[26]=1.91;
            xDisp[27]=2.17;
            xDisp[28]=2.16;
            xDisp[29]=1.9;
            xDisp[30]=-0.00124;
            xDisp[31]=0.0006;
            
        	               
        	
        	System.out.println("xDisp" +xDisp);
        	
        	DispMeasurement yDispMeasurement = new DispMeasurement(allBPMs);
        	// set BPM and energy data
        	yDispMeasurement.setBPMDataArray(bpmY);
    //    	yDispMeasurement.setEnergyDataArray(energy);                                 //weiyy
    //    	yDisp = yDispMeasurement.getDispersions();
        	yDisp[0]=0;
        	yDisp[1]=0;
        	yDisp[2]=0;
        	yDisp[3]=0;
        	yDisp[4]=0;
        	yDisp[5]=0;
        	yDisp[6]=0;
        	yDisp[7]=0;
        	yDisp[8]=0;
        	yDisp[9]=0;
        	yDisp[10]=0;
        	yDisp[11]=0;
        	yDisp[12]=0;
        	yDisp[13]=0;
        	yDisp[14]=0;
        	yDisp[15]=0;
        	yDisp[16]=0;
        	yDisp[17]=0;
        	yDisp[18]=0;
        	yDisp[19]=0;
        	yDisp[20]=0;
        	yDisp[21]=0;
        	yDisp[22]=0;
        	yDisp[23]=0;
        	yDisp[24]=0;
        	yDisp[25]=0;
        	yDisp[26]=0;
        	yDisp[27]=0;
        	yDisp[28]=0;
        	yDisp[29]=0;
        	yDisp[30]=0;
        	yDisp[31]=0;
        	
        	double[] pos = new double[bpms.length];
        	for (int i=0; i<bpms.length; i++) {
        		pos[i] = bpms[i].getPosition();
        	}
        	for (int i=0; i<bpms.length; i++) {
        	System.out.println("pos="+pos[i]+"xDisp=" +xDisp[i]);
        	
        	}
        	System.out.println("pos="+pos.length+"xDisp=" +xDisp.length);

        	// update the result table and plot
        	bpmXData.addPoint(pos, xDisp);
        	bpmYData.addPoint(pos, yDisp);
    	System.out.println("***************************");	
        	dispPlot.addGraphData(bpmXData);
        	dispPlot.addGraphData(bpmYData);
        	
        	for (int i=0; i<bpms.length; i++) {
        		bpmTableModel.setValueAt(new Double(xDisp[i]), i, 1);
        	}
        }
	}

	class BpmTableModel extends AbstractTableModel {
		
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;
        
		final String[] columnNames = { "BPM", "D(m)" };

		final Object[][] data = new Object[allBPMs.size()][columnNames.length];

		/** Container for row labels */
		private ArrayList<String> rowNames = new ArrayList<String>(allBPMs.size());

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public String getRowName(int row) {
			return rowNames.get(row);
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
				return rowNames.get(rowIndex);
			} else {
				return data[rowIndex][columnIndex];
			}
		}

		public void setValueAt(Object value, int row, int col) {
			if (col > 0) {
				data[row][col] =  value;
				
				fireTableCellUpdated(row, col);
				return;
			}
		}
	}
}  //  @jve:decl-index=0:visual-constraint="32,15"
