/*
 * BetaPanel.java
 *
 * Copyright (c) 2013 IHEP
 * Beijing P.O.China
 * All rights reserved.
 *
 * Created on February 24, 2013, 10:45 AM
 */

package xal.app.ringbetameasurement;

import java.io.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.event.*;

import xal.ca.*;
import xal.extension.widgets.swing.DecimalField;
import xal.model.ModelException;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.service.pvlogger.LoggerSession;
import xal.service.pvlogger.MachineSnapshot;
import xal.service.pvlogger.PVLogger;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.impl.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.beam.calc.SimpleSimResultsAdaptor;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.ConnectionPreferenceController;
import Jama.*;

import java.text.ParseException;

/*import gov.sns.tools.apputils.EdgeLayout;
import gov.sns.xal.smf.impl.*;
import gov.sns.xal.smf.Ring;
import gov.sns.tools.swing.DecimalField;
import gov.sns.ca.*;
import gov.sns.tools.apputils.files.*;
import gov.sns.tools.pvlogger.*;
import gov.sns.tools.pvlogger.query.*;
import gov.sns.tools.database.*;
import gov.sns.xal.model.*;
import gov.sns.xal.model.probe.TransferMapProbe;
import gov.sns.xal.model.probe.ProbeFactory;
import gov.sns.xal.model.alg.TransferMapTracker;
import gov.sns.xal.model.scenario.Scenario;
import gov.sns.xal.model.probe.traj.TransferMapTrajectory;
import gov.sns.xal.model.probe.traj.TransferMapState;
import gov.sns.xal.model.pvlogger.PVLoggerDataSource;*/

/**
 * 
 * @author Weiyy
 */
public class BetaPanel extends JPanel implements ConnectionListener,
		ActionListener {
	static final long serialVersionUID = 0;

	RingDocument myDoc;

	EdgeLayout edgeLayout = new EdgeLayout();

	JTable bpmTable, quadTable;

	ArrayList<BPM> allBPMs;

	ArrayList<Integer> badBPMs = new ArrayList<Integer>();

	JPanel bpmPane = new JPanel();

//	private BetaMeasurement[] betaMeasurement;

	JScrollPane bpmChooserPane;

	JTabbedPane plotDisplayPane,plotBetaPane;

	JPanel betaPlotPane = new JPanel();
	JPanel betabeatPlotPane = new JPanel();
	JPanel phasePlotPane = new JPanel();

	JPanel posPlotPanex = new JPanel();
	JPanel posPlotPaney = new JPanel();


	JPanel phaseDiffPlotPane = new JPanel();

	BpmTableModel bpmTableModel;



	private String selectedBPM = "";

	// private JTextField selectedBPMName = new JTextField(20);


	private JDialog configDialog = new JDialog();

	BPMPlotPane xBpmPlotPane, yBpmPlotPane; 

	BETAPlotPane xPhDiffPlotPane, yPhDiffPlotPane,xPhasePlotPane, yPhasePlotPane;
	BETAPlotPane xbetaPlotPane, ybetaPlotPane,xbetabeatPlotPane, ybetabeatPlotPane;


	double[] xPhase, yPhase, xPhaseDiff, yPhaseDiff, xDiffPlot, yDiffPlot,
			posArray, goodPosArry,xModelPhase,yModelPhase,xModelBeta,yModelBeta,
			xbeta,ybeta,xbetabeat,ybetabeat;

	protected DecimalField df6, df7;

	NumberFormat numberFormat = NumberFormat.getNumberInstance();

	int maxTime = 100;

	int fftSize = 1024;                                               //weiyy

	int len = 1024;                                                   //weiyy

	protected JComboBox fftConf;
	
	JProgressBar progBar;

	
	/** List of the monitors */
	final Vector<Monitor> mons = new Vector<Monitor>();
	
	/** for data dump file */
	private RecentFileTracker _datFileTracker;

	File datFile;

	// private PVLoggerForm pvlogger;
	private LoggerSession loggerSession, loggerSession1;

	private MachineSnapshot snapshot, snapshot1;

	protected long pvLoggerId, pvLoggerId1;

	/** Timestamp when a scan was started */
	protected Date startTime;

	InputPVTableCell setPVCell[], rbPVCell[];

	HashMap<MagnetMainSupply, Double> designMap;

	
	
	// get track of "good" BPMs
	ArrayList<String> goodBPMs;
	
    /** for on/off line mode */
    private boolean isOnline = true;
    	
	private long bpmPVLogId = 0;
	private long defPVLogId = 0;
	private BetaMeasurement betaMeasurement;


	public BetaPanel(RingDocument doc) {

		myDoc = doc;
		_datFileTracker = new RecentFileTracker(1, this.getClass(),
				"recent_saved_file");

		// initialize PVLogger
		try {
			PVLogger pvLogger = null;
			final ConnectionDictionary defaultDictionary = ConnectionDictionary.defaultDictionary();
			if ( defaultDictionary != null && defaultDictionary.hasRequiredInfo() ) {
				pvLogger = new PVLogger( defaultDictionary );
			}
			else {
				ConnectionPreferenceController.displayPathPreferenceSelector();
				final ConnectionDictionary dictionary = ConnectionDictionary.defaultDictionary();
				if ( dictionary != null && dictionary.hasRequiredInfo() ) {
					pvLogger = new PVLogger( dictionary );
				}
			}
			
			if ( pvLogger != null ) {
				loggerSession = pvLogger.requestLoggerSession( "default" );			
				loggerSession1 = pvLogger.requestLoggerSession( "Ring BPM Test" );
			}		
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}

	protected void initTables(ArrayList<BPM> bpms) {
		allBPMs = bpms;
//		betaMeasurement = new BetaMeasurement[allBPMs.size()];	2.27
		xPhase = new double[allBPMs.size()];
		yPhase = new double[allBPMs.size()];
//		xPhaseDiff = new double[allBPMs.size()];
//		yPhaseDiff = new double[allBPMs.size()];
//		xDiffPlot = new double[allBPMs.size()];
//		yDiffPlot = new double[allBPMs.size()];
		posArray = new double[allBPMs.size()];

		this.setSize(960, 850);

		setLayout(edgeLayout);
		String[] bpmColumnNames = { "BPM", "XBeta", "XPhase", "YBeta",
				"YPhase", "Ignore" };
		bpmTableModel = new BpmTableModel(allBPMs, bpmColumnNames, this);


		EdgeLayout edgeLayout1 = new EdgeLayout();
		bpmPane.setLayout(edgeLayout1);
		JLabel label = new JLabel("Select a BPM to view the data");
		edgeLayout.setConstraints(label, 0, 0, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(label);

		// bpmTableModel.setValueAt(new Boolean(true), 0, 5);
		// setSelectedBPM(bpmTableModel.getRowName(0));

		bpmTable = new JTable(bpmTableModel);
		bpmTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		
		bpmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = bpmTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
					// do nothing
				} else {
					int selectedRow = lsm.getMinSelectionIndex();
					setSelectedBPM(((BPM) allBPMs.get(selectedRow)).getId());
					if (!badBPMs.contains(new Integer(selectedRow))) {
	//					plotBPMData(selectedRow);
					}
				}
			}
		});
           
		bpmChooserPane = new JScrollPane(bpmTable);

		bpmChooserPane.setPreferredSize(new Dimension(450, 300));
		bpmChooserPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		edgeLayout1.setConstraints(bpmChooserPane, 20, 0, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		bpmPane.add(bpmChooserPane);
		
		JPanel selection = new JPanel();
		selection.setLayout(new GridLayout(1, 5));
		selection.setPreferredSize(new Dimension(400, 30));
		// selection.add(selectedBPMName);
		JButton calculate = new JButton("Fit Beta");
		calculate.setPreferredSize(new Dimension(180, 10));
		calculate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				System.out.println("App mode is " + isOnline);			
					betaByMIA();					
				// save to PV Logger
				if (isOnline) {
					snapshot = loggerSession.takeSnapshot();
					snapshot1 = loggerSession1.takeSnapshot();
					startTime = new Date();
				}
			
			}
		});
		JButton config = new JButton("Config");
		config.setActionCommand("configuration");
		config.setPreferredSize(new Dimension(60, 10));
		config.addActionListener(this);
		selection.add(config);
		JLabel dummy = new JLabel("");
		dummy.setPreferredSize(new Dimension(10, 10));
		//selection.add(dummy);		
		selection.add(calculate);
		JLabel dummy1 = new JLabel("");
		dummy1.setPreferredSize(new Dimension(10, 10));
		//selection.add(dummy1);
		configDialog.setBounds(300, 300, 330, 300);
		configDialog.setTitle("Config. fit/FFT parameters...");
		numberFormat.setMaximumFractionDigits(6);


		JPanel paramConf = new JPanel();
		// paramConf.setLayout(new GridLayout(6,1));
		JLabel fitFunction = new JLabel(
				"Fit function: A*exp(-c*x) * sin(2PI*(w*x + b)) + d");
		JPanel ampPane = new JPanel();
		ampPane.setLayout(new GridLayout(1, 2));

		/*
		 * JLabel label1 = new JLabel("A = "); df1 = new DecimalField(A, 9,
		 * numberFormat); ampPane.add(label1); ampPane.add(df1);
		 * paramConf.add(ampPane); JPanel expPane = new JPanel();
		 * expPane.setLayout(new GridLayout(1, 2)); JLabel label2 = new
		 * JLabel("c = "); df2 = new DecimalField(c, 9, numberFormat);
		 * expPane.add(label2); expPane.add(df2); paramConf.add(expPane); JPanel
		 * tunePane = new JPanel(); tunePane.setLayout(new GridLayout(1, 2));
		 * JLabel label3 = new JLabel("w = "); df3 = new DecimalField(w, 9,
		 * numberFormat); tunePane.add(label3); tunePane.add(df3);
		 * paramConf.add(tunePane); JPanel phiPane = new JPanel();
		 * phiPane.setLayout(new GridLayout(1, 2)); JLabel label4 = new
		 * JLabel("b = "); df4 = new DecimalField(b, 9, numberFormat);
		 * phiPane.add(label4); phiPane.add(df4); paramConf.add(phiPane); JPanel
		 * offsetPane = new JPanel(); offsetPane.setLayout(new GridLayout(1,
		 * 2)); JLabel label5 = new JLabel("d = "); df5 = new DecimalField(d, 9,
		 * numberFormat); offsetPane.add(label5); offsetPane.add(df5);
		 * paramConf.add(offsetPane);
		 */
		JPanel maxTimePane = new JPanel();
		maxTimePane.setLayout(new GridLayout(1, 2));
		JLabel label6 = new JLabel("Max. no of iterations: ");
		df6 = new DecimalField(maxTime, 9, numberFormat);
		maxTimePane.add(label6);
		maxTimePane.add(df6);
		paramConf.add(maxTimePane);
		JPanel fitLengthPane = new JPanel();
		fitLengthPane.setLayout(new GridLayout(1, 2));
		JLabel label7 = new JLabel("fit up to turn number:");
		numberFormat.setMaximumFractionDigits(0);
		df7 = new DecimalField(len, 4, numberFormat);
		fitLengthPane.add(label7);
		fitLengthPane.add(df7);
		paramConf.add(fitLengthPane);

		JPanel fftPane = new JPanel();
		fftPane.setLayout(new GridLayout(1, 2));
		JLabel label8 = new JLabel("FFT array size: ");
		String[] fftChoice = { "32", "64", "128", "256","1024" };
		fftConf = new JComboBox(fftChoice);
		fftConf.setSelectedIndex(4);
		fftConf.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				if (((String) (((JComboBox) evt.getSource()).getSelectedItem()))
						.equals("32")) {
					fftSize = 32;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("64")) {
					fftSize = 64;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("128")) {
					fftSize = 128;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("256")) {
					fftSize = 256;
				} else if (((String) (((JComboBox) evt.getSource())
						.getSelectedItem())).equals("1024")) {
					fftSize = 1024;
				}
			}
		});
		fftConf.setPreferredSize(new Dimension(30, 18));
		fftPane.add(label8);
		fftPane.add(fftConf);
		paramConf.add(fftPane);

		JPanel paramConfBtn = new JPanel();
		EdgeLayout edgeLayout3 = new EdgeLayout();
		paramConfBtn.setLayout(edgeLayout3);
		JButton done = new JButton("OK");
		done.setActionCommand("paramsSet");
		done.addActionListener(this);
		edgeLayout3.setConstraints(done, 0, 50, 0, 0, EdgeLayout.LEFT_BOTTOM,
				EdgeLayout.NO_GROWTH);
		paramConfBtn.add(done);
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancelConf");
		cancel.addActionListener(this);
		edgeLayout3.setConstraints(cancel, 0, 170, 0, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		paramConfBtn.add(cancel);
		configDialog.getContentPane().setLayout(new BorderLayout());
		configDialog.getContentPane().add(fitFunction, BorderLayout.NORTH);
		configDialog.getContentPane().add(paramConf, BorderLayout.CENTER);
		configDialog.getContentPane().add(paramConfBtn, BorderLayout.SOUTH);

		
		
		JButton dumpData = new JButton("Save");
		dumpData.setPreferredSize(new Dimension(60, 10));
		dumpData.setActionCommand("dumpData");
		dumpData.addActionListener(this);
		dumpData.setEnabled(false);
		selection.add(dumpData);

		edgeLayout.setConstraints(selection, 280, 500, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(selection);
		// selectedBPMName.setText(selectedBPM);

		edgeLayout.setConstraints(bpmPane, 10, 10, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		add(bpmPane);

		// show results
		plotDisplayPane = new JTabbedPane();
		plotDisplayPane.setPreferredSize(new Dimension(430, 260));
		plotDisplayPane.addTab("Posx", posPlotPanex);
		plotDisplayPane.addTab("Posy", posPlotPaney);
		edgeLayout.setConstraints(plotDisplayPane, 0, 480, 0, 0,
				EdgeLayout.TOP, EdgeLayout.NO_GROWTH);

		xBpmPlotPane = new BPMPlotPane(0);
		EdgeLayout el1 = new EdgeLayout();
		posPlotPanex.setLayout(el1);
		el1.setConstraints(xBpmPlotPane, 10, 20, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		posPlotPanex.add(xBpmPlotPane);
		yBpmPlotPane = new BPMPlotPane(1);
		posPlotPaney.add(yBpmPlotPane);
	


		// edgeLayout.setConstraints(posPlotPane, 15, 55, 0, 0, EdgeLayout.TOP,
		// EdgeLayout.NO_GROWTH);
		add(plotDisplayPane);

		for (int i = 0; i < allBPMs.size(); i++) {
			bpmTableModel.addRowName(((BPM) allBPMs.get(i)).getId(), i);
			bpmTableModel.setValueAt("0", i, 1);
			bpmTableModel.setValueAt("0", i, 2);
			bpmTableModel.setValueAt("0", i, 3);
			bpmTableModel.setValueAt("0", i, 4);
			bpmTableModel.setValueAt(new Boolean(false), i, 5);
		}
		
		// show results
		plotBetaPane = new JTabbedPane();
		plotBetaPane.setPreferredSize(new Dimension(900, 350));
		plotBetaPane.addTab("Beta", betaPlotPane);
		plotBetaPane.addTab("Beta beat", betabeatPlotPane);
		plotBetaPane.addTab("Phase", phasePlotPane);
		plotBetaPane.addTab("phase diff.", phaseDiffPlotPane);
		edgeLayout.setConstraints(plotBetaPane, 350, 0, 0, 0,
				EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
		
	//	EdgeLayout el2 = new EdgeLayout();
	//	phasePlotPane.setLayout(el2);
		xPhasePlotPane = new BETAPlotPane(4);
	//	el2.setConstraints(xPhasePlotPane, 20, 20, 0, 0, EdgeLayout.TOP,
	//			EdgeLayout.NO_GROWTH);
		phasePlotPane.add(xPhasePlotPane);
		yPhasePlotPane = new BETAPlotPane(5);
	//	el2.setConstraints(yPhasePlotPane, 180, 20, 0, 0, EdgeLayout.TOP,
	//			EdgeLayout.NO_GROWTH);
		phasePlotPane.add(yPhasePlotPane);
		
		xbetaPlotPane = new BETAPlotPane(0);
		betaPlotPane.add(xbetaPlotPane);
		ybetaPlotPane = new BETAPlotPane(1);
		betaPlotPane.add(ybetaPlotPane);
		
		xbetabeatPlotPane = new BETAPlotPane(2);
		betabeatPlotPane.add(xbetabeatPlotPane);
		ybetabeatPlotPane = new BETAPlotPane(3);
		betabeatPlotPane.add(ybetabeatPlotPane);
		
		xPhDiffPlotPane = new BETAPlotPane(6);
		phaseDiffPlotPane.add(xPhDiffPlotPane);
		yPhDiffPlotPane = new BETAPlotPane(7);
		phaseDiffPlotPane.add(yPhDiffPlotPane);

		
		add(plotBetaPane);
		
		
		
		TransferMapProbe myProbe = ProbeFactory.getTransferMapProbe(myDoc
				.getSelectedSequence(), new TransferMapTracker());
		Scenario scenario;

		try {
			scenario = Scenario.newScenarioFor(myDoc.getSelectedSequence());
			scenario.setProbe(myProbe);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			scenario.resetProbe();
			scenario.resync();
			scenario.run();

			//TransferMapTrajectory traj = (TransferMapTrajectory) scenario
			//		.getTrajectory();
			Trajectory traj = scenario.getTrajectory();

	
			
		

			
			xModelPhase = new double[allBPMs.size()];
			yModelPhase = new double[allBPMs.size()];
			xModelBeta = new double[allBPMs.size()];
			yModelBeta = new double[allBPMs.size()];
			
			System.out.println("xModelPhase" );
			 SimpleSimResultsAdaptor    cmpCalcEngine = new SimpleSimResultsAdaptor( traj );
				// get the 1st good BPM betatron phase as the reference
				TransferMapState state0 = (TransferMapState) traj
						.stateForElement(allBPMs.get(0).getId());
				//double xModelPhase0 = state0.getBetatronPhase().getx();
				//double yModelPhase0 = state0.getBetatronPhase().gety();
				double xModelPhase0 =cmpCalcEngine.computeBetatronPhase(state0).getx();
				double yModelPhase0 =cmpCalcEngine.computeBetatronPhase(state0).gety();
				for (int i=0; i<allBPMs.size(); i++) {
					// xPhaseDiff[i] = (xPhase[i] - xPhase[0]);
					// yPhaseDiff[i] = (yPhase[i] - yPhase[0]);
					posArray[i] = myDoc.getSelectedSequence().getPosition(allBPMs.get(i));		
				// get model BPM phase difference
				TransferMapState state = (TransferMapState) traj
				.stateForElement(allBPMs.get(i).getId());
				/*xModelPhase[i] = state.getBetatronPhase().getx()
				- xModelPhase0;
				yModelPhase[i] = state.getBetatronPhase().gety()
				- yModelPhase0;*/
				
				xModelPhase[i] = cmpCalcEngine.computeBetatronPhase(state).getx()-xModelPhase0;;				
				yModelPhase[i] = cmpCalcEngine.computeBetatronPhase(state).gety()-yModelPhase0;
				
				if (xModelPhase[i] < 0.)
					xModelPhase[i] = xModelPhase[i] + 2. * Math.PI;
				if (yModelPhase[i] < 0.)
					yModelPhase[i] = yModelPhase[i] + 2. * Math.PI;
				
				// calculate diff between measured difference and model
				// predicted difference
				
				//xModelBeta[i]=state.getTwiss()[0].getBeta();
				xModelBeta[i]=cmpCalcEngine.computeTwissParameters( state )[0].getBeta();
				
				System.out.println("xModelbeta"+i+"="+xModelBeta[i]);
				//yModelBeta[i]=state.getTwiss()[1].getBeta();
				yModelBeta[i]=cmpCalcEngine.computeTwissParameters( state )[1].getBeta();
				
			}
			

			xPhasePlotPane.setDataArray(posArray, xModelPhase);
//			xPhasePlotPane.setDataArray(goodPosArry, xPhaseDiff);
			xPhasePlotPane.plot();
			yPhasePlotPane.setDataArray(posArray, yModelPhase);
//			yPhasePlotPane.setDataArray(goodPosArry, yPhaseDiff);
			yPhasePlotPane.plot();
			
			xbetaPlotPane.setDataArray(posArray, xModelBeta);
//			xPhasePlotPane.setDataArray(goodPosArry, xPhaseDiff);
			xbetaPlotPane.plot();
			ybetaPlotPane.setDataArray(posArray, yModelBeta);
//			yPhasePlotPane.setDataArray(goodPosArry, yPhaseDiff);
			ybetaPlotPane.plot();
		

		} catch (ModelException e) {
			System.out.println(e);
		}
		
	}

	

	
	

	protected void plotBPMData(int ind) {
		
		
	}

	public void actionPerformed(ActionEvent ev) {
		// pop-up dialog for changing fit/FFT parameters
		if (ev.getActionCommand().equals("configuration")) {
			configDialog.setVisible(true);
		} else if (ev.getActionCommand().equals("paramsSet")) {
			/*
			 * A = df1.getValue(); c = df2.getValue(); w = df3.getValue(); b =
			 * df4.getValue(); d = df5.getValue();
			 */maxTime = Math.round((int) df6.getValue());
			len = Math.round((int) df7.getValue());
			configDialog.setVisible(false);
		} else if (ev.getActionCommand().equals("cancelConf")) {
			configDialog.setVisible(false);
	
			
	
		} else if (ev.getActionCommand().equals("dumpData")) {
			String currentDirectory = _datFileTracker.getRecentFolderPath();

			JFileChooser fileChooser = new JFileChooser(currentDirectory);

			int status = fileChooser.showSaveDialog(this);
			if (status == JFileChooser.APPROVE_OPTION) {
				_datFileTracker.cacheURL(fileChooser.getSelectedFile());
				File file = fileChooser.getSelectedFile();

				try {
					FileWriter fileWriter = new FileWriter(file);
					NumberFormat nf = NumberFormat.getNumberInstance();
					nf.setMaximumFractionDigits(5);
					nf.setMinimumFractionDigits(5);

					// write BPM data
					fileWriter.write("BPM_Id\t\t\t" + "s\t\t" + "xTune\t"
							+ "xPhase\t" + "yTune\t" + "yPhase" + "\n");

					// numberFormat.setMaximumFractionDigits(6);

		

					String comments = startTime.toString();
					comments = comments + "\n"
							+ "For Ring Measurement Application";
					snapshot.setComment(comments);
					snapshot1.setComment(comments);
					loggerSession.publishSnapshot(snapshot);
					loggerSession1.publishSnapshot(snapshot1);
					pvLoggerId = snapshot.getId();
					pvLoggerId1 = snapshot1.getId();

					fileWriter.write("PVLoggerID = " + pvLoggerId
							+ "\tPVLoggerId = " + pvLoggerId1 + "\n");

					fileWriter.close();

				} catch (IOException ie) {
					JFrame frame = new JFrame();
					JOptionPane.showMessageDialog(frame, "Cannot open the file"
							+ file.getName() + "for writing", "Warning!",
							JOptionPane.PLAIN_MESSAGE);

					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				}
			}
		}

	}
	
	
	private void betaByMIA() {
		 goodBPMs = new ArrayList<String>();
		 HashMap<String, double[][]> bpmMap = new HashMap<String, double[][]>();
		 
		 double[][] datax = new double[32][1024];
		 double[][] datay = new double[32][1024];
		 double[][][] BPMData= new double[2][32][1024];
		 if (!isOnline) {
						
				 try{
				 // FileReader fr = new FileReader("./src/csns/apps/ringbetameasurement/tbtdatax.txt");
				  FileReader fr = new FileReader("./apps/ringbetameasurement/src/xal/app/ringbetameasurement/tbtdatax.txt");
				  BufferedReader br = new BufferedReader(fr);
				  Scanner sc = new Scanner(br).useDelimiter("[ ]*,+[ ]*|\\s*;+\\s*|\\s+");
				
				 //FileReader fs = new FileReader("./src/csns/apps/ringbetameasurement/tbtdatay.txt");
				 FileReader fs = new FileReader("./apps/ringbetameasurement/src/xal/app/ringbetameasurement/tbtdatay.txt");
				 BufferedReader bs = new BufferedReader(fs);
				 Scanner sd = new Scanner(bs).useDelimiter("[ ]*,+[ ]*|\\s*;+\\s*|\\s+");
					  for (int i = 0; i < 1024; i++) {
						  for (int j = 0; j<32; j++){
							  datax[j][i]= sc.nextDouble()*1000;	
							  datay[j][i]= sd.nextDouble()*1000;
						  }
					  }
			
				
				  fr.close();
				  fs.close();
				 double[][] data = new double[2][1024];
			
					for (int i = 0; i < allBPMs.size(); i++) {
						
						BPM theBPM = (BPM) allBPMs.get(i);	
						
						data[0]=datax[i];
						data[1]=datay[i];
				
						bpmMap.put(theBPM.getId(),data.clone());
					
			  		}
		
				
				 }catch(FileNotFoundException e){
					 System.out.println("Open file error.");
					 e.printStackTrace();
				 }catch(IOException ioe){
					 System.out.println("Close file error.");
					 ioe.printStackTrace();
				 }			
				 
				} else {
			//		connectAll();
				}
		 
		    betaMeasurement = new BetaMeasurement();
			if (!isOnline) {
				for (int i = 0; i < allBPMs.size(); i++) {
					
					// BPM theBPM = (BPM) (myDoc.getSelectedSequence()
					// .getNodeWithId(selectedBPM));

			    BPM theBPM = (BPM) allBPMs.get(i);
				BPMData[0][i]=bpmMap.get(theBPM.getId())[0];
			    BPMData[1][i]=bpmMap.get(theBPM.getId())[1];
				}
				betaMeasurement.setBPMData(BPMData);
			}
			
			Thread thread = new Thread(betaMeasurement);
				thread.start();
			try{
				thread.join();
			     xbeta=betaMeasurement.getXBeta();
			     ybeta=betaMeasurement.getYBeta();
			     xbetabeat= new double[xbeta.length];
			     ybetabeat= new double[ybeta.length];
			    double sum=0;
		       	double sumsquare=0;
			     for(int i=0;i<xbeta.length;i++){
				     sum=sum+xbeta[i]/xModelBeta[i];
				     sumsquare=sumsquare+(xbeta[i]/xModelBeta[i])*xbeta[i]/xModelBeta[i];
			     }
			     for(int i=0;i<xbeta.length;i++){
				     xbeta[i]=xbeta[i]*sum/sumsquare;
				     xbetabeat[i]=xbeta[i]/xModelBeta[i];
			     }
			     sum=0;
			     sumsquare=0;
				 for(int i=0;i<ybeta.length;i++){
					 sum=sum+ybeta[i]/yModelBeta[i];
					 sumsquare=sumsquare+(ybeta[i]/yModelBeta[i])*ybeta[i]/yModelBeta[i];
			     }
				 for(int i=0;i<ybeta.length;i++){
					 ybeta[i]=ybeta[i]*sum/sumsquare;
					 ybetabeat[i]=ybeta[i]/yModelBeta[i];
				  }
			     
			} catch (InterruptedException ie) {
				System.out.println("beta calculation for normally!");
			}
			  
			numberFormat.setMaximumFractionDigits(4);
			for (int i = 0; i < allBPMs.size(); i++) {
		    	
			    bpmTableModel.setValueAt(numberFormat.format(xbeta[i]), i, 1);
		     	bpmTableModel.setValueAt(numberFormat.format(ybeta[i]), i, 3);
			
			}
			
			xbetaPlotPane.setDataArray(posArray, xModelBeta);
			xbetaPlotPane.setFittedData(xbeta);
			xbetaPlotPane.plot();
			ybetaPlotPane.setDataArray(posArray, yModelBeta);
			ybetaPlotPane.setFittedData(ybeta);
			ybetaPlotPane.plot();
			xbetabeatPlotPane.setDataArray(posArray, xbetabeat);
			xbetabeatPlotPane.plot();
			ybetabeatPlotPane.setDataArray(posArray, ybetabeat);
			ybetabeatPlotPane.plot();
	}

	protected void setSelectedBPM(String theBPM) {
		selectedBPM = theBPM;
		System.out.println("Selected BPM = " + selectedBPM);
	}

	protected String getSelectedBPM() {
		return selectedBPM;
	}

	/** ConnectionListener interface */
	public void connectionMade(Channel aChannel) {
		connectMons(aChannel);
	}

	/** ConnectionListener interface */
	public void connectionDropped(Channel aChannel) {
	}

	/** internal method to connect the monitors */
	private void connectMons(Channel p_chan) {
	
	}

	/** get the list of table cells monitoring the prescibed channel */


    protected void setAppMode(boolean isOn) {
        isOnline = isOn;
    }
    
}
