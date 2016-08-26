package xal.app.wireanalysis;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.border.*;

import xal.app.wireanalysis.phasespaceanalysis.MyPhasePlaneEllipse;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.hint.InitialDelta;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.tools.apputils.files.RecentFileTracker;
import Jama.Matrix;

public class EmAnalysisPanel extends JPanel {
	protected GenDocument doc;
	protected JLabel jlfilename, jldirection, jlpercentage;
	protected JLabel jlalpha = new JLabel(" Alpha:");
	protected JLabel jlbeta = new JLabel(" Beta: ");
	protected JLabel jlgamma = new JLabel(" Gamma:");
	protected JLabel jlemittance = new JLabel(
			" Erms:                                 ");
	protected JLabel jlemittancenor = new JLabel(" En:");
	protected DoubleInputTextField alphaLocal_Text = new DoubleInputTextField(8);
	protected DoubleInputTextField betaLocal_Text = new DoubleInputTextField(8);
	protected DoubleInputTextField emtLocal_Text = new DoubleInputTextField(8);
	protected JLabel jlalpha_g = new JLabel(" Alpha_g:              ");
	protected JLabel jlbeta_g = new JLabel(" Beta_g: ");
	protected JLabel jlgamma_g = new JLabel(" Gamma_g:");
	protected JLabel jlemittance_g = new JLabel(" Erms_g:");
	protected JLabel jlemittancenor_g = new JLabel(" En_g:");

	protected JSpinner threshStart_Spinner;
	protected JSpinner threshStep_Spinner;
	protected JSpinner threshStop_Spinner;

	protected JLabel fitTime_Label;
	protected JSpinner fitTime_Spinner;
	protected JTextField jtfbackground, jtffirst, jtfsecond, jtfindexf,
			jtfindext, jtfx0, jtfxp0;
	protected JCheckBox jcbfirst, jcbsecond;
	protected FunctionGraphsJPanel signalpanel, GP_ep;
	private ColorSurfaceData emittance3D = Data3DFactory.getData3D(1, 1,
			"smooth");
	private LocalColorGenerator colorGen_ep = new LocalColorGenerator();
	private MyPhasePlaneEllipse myphasePlaneEllipseRMS = new MyPhasePlaneEllipse();
	private MyPhasePlaneEllipse myphasePlaneEllipseGauss = new MyPhasePlaneEllipse();
	private int emScrResX = 200;
	private int emScrResY = 200;
	private int emSizeX = 40;
	private int emSizeY = 40;
	protected double[] x = null, xp = null, value = null, initialvalue = null,
			finalvalue = null;
	protected double x_center, xp_center;
	protected double alpha = 0, beta = 0, gamma = 0, emittance = 0,
			emittancebetagamma = 0;
	protected double alpha_g = 0, beta_g = 0, gamma_g = 0, emittance_g = 0,
			emittancebetagamma_g = 0;
	protected double percentage = 0.0, absmaxvalue = 0, maxvalue = 0;
	protected double xrange, xprange, betagamma;
	protected Boolean firstmethod, secondmethod;
	protected Matrix Mx, My;
	private GaussianDensity gaussianDensity = new GaussianDensity();
	//scorer and solver for fitting
	private GaussScorer scorer = new GaussScorer();
	private Solver solver;
	private Problem problem;
	
	public EmAnalysisPanel(GenDocument aDocument) {
		doc = aDocument;
		makeComponents();
	}

	/** Make the content view */
	private void makeComponents() {
		final Box mainview = new Box(BoxLayout.Y_AXIS);
		Box plotview = new Box(BoxLayout.Y_AXIS);
		plotview.add(getplotview());
		plotview.add(Box.createVerticalStrut(20));
		plotview.add(getbackgroundview());

		Box Analysisview = new Box(BoxLayout.X_AXIS);
		Analysisview.add(getscanview());
		Analysisview.add(Box.createHorizontalStrut(10));
		Analysisview.add(getrmsemittanceview());
		Analysisview.add(Box.createHorizontalStrut(10));
		Analysisview.add(getgaussfittingview());

		mainview.add(Box.createVerticalStrut(20));
		mainview.add(getsearchview());
		mainview.add(Box.createVerticalStrut(20));
		mainview.add(plotview);
		mainview.add(Box.createVerticalStrut(30));
		mainview.add(Analysisview);
		this.add(mainview);
	}

	/** Make the gauss fitting view */
	private Component getgaussfittingview() {
		Box gaussfittingview = new Box(BoxLayout.X_AXIS);
		gaussfittingview.setBorder(BorderFactory
				.createTitledBorder("GAUSS FITTING"));
		Box gausssettingview = new Box(BoxLayout.Y_AXIS);

		Border etchedBorder = BorderFactory.createEtchedBorder();
		TitledBorder localEmtParamBborder = null;

		JLabel alphaLocal_Label = new JLabel("Alpha", JLabel.CENTER);
		JLabel betaLocal_Label = new JLabel("Beta", JLabel.CENTER);
		JLabel emtLocal_Label = new JLabel("Emittance_n", JLabel.CENTER);
		JLabel emtBounding_Label = new JLabel(" fitting region in [%]",
				JLabel.LEFT);
		JLabel fitTime_Label = new JLabel(" fitting time [sec]", JLabel.LEFT);
		fitTime_Spinner = new JSpinner(new SpinnerNumberModel(10, 1, 60, 1));
		// calculation buttons
		JButton gaussfit_Button = new JButton("START GAUSS FITTING");

		JPanel emtParPanel = new JPanel(new GridLayout(2, 3, 1, 1));
		localEmtParamBborder = BorderFactory.createTitledBorder(etchedBorder,
				"initial fitting parameters");
		emtParPanel.setBorder(localEmtParamBborder);
		emtParPanel.add(alphaLocal_Label);
		emtParPanel.add(betaLocal_Label);
		emtParPanel.add(emtLocal_Label);
		emtParPanel.add(alphaLocal_Text);
		emtParPanel.add(betaLocal_Text);
		emtParPanel.add(emtLocal_Text);

		JPanel gaussresultPanel = new JPanel(new GridLayout(5, 2, 2, 2));
		gaussresultPanel.add(jlalpha_g);
		gaussresultPanel.add(jlbeta_g);
		gaussresultPanel.add(jlgamma_g);
		gaussresultPanel.add(jlemittance_g);
		gaussresultPanel.add(jlemittancenor_g);
		gaussresultPanel.setBorder(etchedBorder);

		JPanel fitParPanel_0 = new JPanel(new GridLayout(1, 2, 1, 1));
		fitParPanel_0.add(fitTime_Spinner);
		fitParPanel_0.add(fitTime_Label);

		JPanel fitParPanel = new JPanel(new BorderLayout());
		fitParPanel.add(fitParPanel_0, BorderLayout.WEST);
		fitParPanel.setBorder(etchedBorder);

		JPanel calcFitPanel = new JPanel(
				new FlowLayout(FlowLayout.CENTER, 3, 3));
		calcFitPanel.add(gaussfit_Button);

		gaussfit_Button.setForeground(Color.blue.darker());

		gaussfit_Button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				gaussfitting();
			}

		});
		gausssettingview.add(emtParPanel);
		gausssettingview.add(fitParPanel);
		gausssettingview.add(calcFitPanel);

		gaussfittingview.add(gausssettingview);
		gaussfittingview.add(gaussresultPanel);

		return gaussfittingview;
	}

	/** Make the threshold scan view */
	private Component getscanview() {
		Box scanview = new Box(BoxLayout.Y_AXIS);
		scanview.setBorder(BorderFactory
				.createTitledBorder("THRESHOLD SCAN [%]"));
		JLabel threshStart_Label = new JLabel("Start", JLabel.CENTER);
		JLabel threshStep_Label = new JLabel("Step", JLabel.CENTER);
		JLabel threshStop_Label = new JLabel("Stop", JLabel.CENTER);
		threshStart_Spinner = new JSpinner(new SpinnerNumberModel(-10.0, -100.,
				100., 1.0));
		threshStep_Spinner = new JSpinner(new SpinnerNumberModel(0.2, 0.1,
				25.0, 0.1));
		threshStop_Spinner = new JSpinner(new SpinnerNumberModel(10.0, -100.,
				100., 1.0));
		JButton plotGraphs_Button = new JButton("PLOT GRAPHS");
		plotGraphs_Button.setForeground(Color.blue.darker());
		// threshold scan control parameter panel
		JPanel thresholdSubPanel_0 = new JPanel(new GridLayout(2, 3, 1, 1));
		Border etchedBorder = BorderFactory.createEtchedBorder();
		thresholdSubPanel_0.setBorder(etchedBorder);
		thresholdSubPanel_0.add(threshStart_Label);
		thresholdSubPanel_0.add(threshStep_Label);
		thresholdSubPanel_0.add(threshStop_Label);
		thresholdSubPanel_0.add(threshStart_Spinner);
		thresholdSubPanel_0.add(threshStep_Spinner);
		thresholdSubPanel_0.add(threshStop_Spinner);
		thresholdSubPanel_0.add(plotGraphs_Button);

		JPanel thresholdSubPanel_1 = new JPanel();
		thresholdSubPanel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		thresholdSubPanel_1.add(plotGraphs_Button);

		plotGraphs_Button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				calculateGraphs();
			}

		});

		scanview.add(Box.createVerticalStrut(10));
		scanview.add(thresholdSubPanel_0);
		scanview.add(Box.createVerticalStrut(10));
		scanview.add(thresholdSubPanel_1);

		return scanview;
	}

	/** Make the search file view */
	private Component getsearchview() {
		JButton jbaddfile = new JButton("Open new EM file");
		jlfilename = new JLabel(
				"file name: null                                       ");
		jldirection = new JLabel("Direction: null");

		jbaddfile.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				openEMfile();
			}

		});

		Box searchview = new Box(BoxLayout.X_AXIS);
		searchview.add(jbaddfile);
		searchview.add(Box.createHorizontalStrut(150));
		searchview.add(jlfilename);
		searchview.add(Box.createHorizontalStrut(150));
		searchview.add(jldirection);
		return searchview;
	}

	/** Make the two plots view */
	private Component getplotview() {
		final Box view = new Box(BoxLayout.X_AXIS);
		// *************for plot 1*****************
		signalpanel = new FunctionGraphsJPanel();
		signalpanel.setPreferredSize(new Dimension(400, 400));
		signalpanel.setName("Signal Distribution");
		signalpanel.setBackground(Color.white);
		signalpanel.setAxisNames("Singal Index", "Signal record");

		// *************for plot 2*****************
		GP_ep = new FunctionGraphsJPanel();
		GP_ep.setPreferredSize(new Dimension(400, 400));
		GP_ep.setOffScreenImageDrawing(true);
		GP_ep.setGraphBackGroundColor(Color.black);
		GP_ep.setGridLinesVisibleX(false);
		GP_ep.setGridLinesVisibleY(false);

		GP_ep.setName("Emittance Contour Plot");
		GP_ep.setAxisNames("x (y), [mm]", "xp (yp), [mrad]");

		emittance3D.setColorGenerator(colorGen_ep);
		emittance3D.setScreenResolution(emScrResX, emScrResY);
		emittance3D.setSize(emSizeX, emSizeY);
		// emittance3D.setMinMaxX(-20, 50);
		// emittance3D.setMinMaxY(-60, 10);
		emittance3D.setMinMaxX(-10, 10);
		emittance3D.setMinMaxY(-10, 10);
		GP_ep.setColorSurfaceData(emittance3D);

		view.add(signalpanel);
		view.add(Box.createHorizontalStrut(50));
		view.add(GP_ep);
		view.add(Box.createHorizontalStrut(50));
		return view;

	}

	/** Make the background calculation view */
	private Component getbackgroundview() {
		Box view = new Box(BoxLayout.X_AXIS);
		JLabel jlindexf = new JLabel("Index-F: ");
		JLabel jlindext = new JLabel("Index-T: ");
		jtfindexf = new JTextField(4);
		jtfindext = new JTextField(4);
		jtfindexf.setText("1");
		jtfindext.setText("11");

		JButton jbcalback = new JButton("Cal-Background");
		jbcalback.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				backgroundcal();
			}
		});

		JLabel jlbackground = new JLabel("Background: ");
		jtfbackground = new JTextField(8);
		jtfbackground.setForeground(Color.red);
		jtfbackground.setHorizontalAlignment(JTextField.CENTER);
		jtfbackground.setMaximumSize(jtfbackground.getPreferredSize());

		jlpercentage = new JLabel("Fraction:                  ");
		jlpercentage.setToolTipText("background-value/max-value");

		JLabel jlx0 = new JLabel("x0 (y0): ");
		JLabel jlxp0 = new JLabel("xp0 (yp0): ");
		jtfx0 = new JTextField(8);
		jtfx0.setMaximumSize(jtfx0.getPreferredSize());
		jtfxp0 = new JTextField(8);
		jtfxp0.setMaximumSize(jtfxp0.getPreferredSize());

		view.add(jlindexf);
		view.add(jtfindexf);
		view.add(Box.createHorizontalStrut(5));
		view.add(jlindext);
		view.add(jtfindext);
		view.add(Box.createHorizontalStrut(10));
		view.add(jbcalback);

		view.add(Box.createHorizontalStrut(10));
		view.add(jlbackground);
		view.add(jtfbackground);
		view.add(Box.createHorizontalStrut(10));
		view.add(jlpercentage);
		view.add(Box.createHorizontalStrut(20));
		view.add(jlx0);
		view.add(Box.createHorizontalStrut(10));
		view.add(jtfx0);
		view.add(Box.createHorizontalStrut(10));
		view.add(jlxp0);
		view.add(Box.createHorizontalStrut(10));
		view.add(jtfxp0);
		return view;
	}

	/** Make the SET THRESHOLD & RMS EMITTANCE view */
	private Component getrmsemittanceview() {
		final Box view = new Box(BoxLayout.X_AXIS);
		final Box methodview = new Box(BoxLayout.Y_AXIS);
		view.setBorder(BorderFactory
				.createTitledBorder("SET THRESHOLD & RMS EMITTANCE"));

		jcbfirst = new JCheckBox("1:Background ");
		jcbsecond = new JCheckBox("2:Threshold[%]");

		jtffirst = new JTextField(5);
		jtffirst.setMaximumSize(jtffirst.getPreferredSize());

		jtfsecond = new JTextField(5);
		jtfsecond.setMaximumSize(jtfsecond.getPreferredSize());
		jtfsecond.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				if (jtfsecond.getText() == null)
					percentage = 0.0;
				else
					percentage = Double.parseDouble(jtfsecond.getText());
			}
		});

		// JLabel jlsecond = new JLabel("%");
		JButton jbmea = new JButton("RMS CALCULATION");
		jbmea.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				System.out.println("++++++++++++++++++++");
				if (jcbsecond.isSelected() && !jtfsecond.getText().isEmpty())
					percentage = Double.parseDouble(jtfsecond.getText());
				else
					percentage = 0.0;
				calEmittance(percentage);
				setEllipsecenter(x_center, xp_center);
				plottwiss();
				plotRMSEllipse();
				setgaussfitting(alpha, beta, emittance);
			}

		});

		jlalpha = new JLabel(" Alpha:");
		jlbeta = new JLabel(" Beta: ");
		jlgamma = new JLabel(" Gamma:");
		jlemittance = new JLabel(" Erms:                     ");
		jlemittancenor = new JLabel(" En:");

		JPanel methodPanel_0 = new JPanel(new GridLayout(2, 1, 1, 1));
		methodPanel_0.add(jcbfirst);
		methodPanel_0.add(jcbsecond);

		JPanel methodPanel_1 = new JPanel(new GridLayout(2, 1, 1, 30));
		methodPanel_1.add(jtffirst);
		methodPanel_1.add(jtfsecond);

		Border etchedBorder = BorderFactory.createEtchedBorder();
		JPanel methodPanel = new JPanel(new BorderLayout());
		methodPanel.add(methodPanel_0, BorderLayout.WEST);
		methodPanel.add(methodPanel_1, BorderLayout.CENTER);
		methodPanel.setBorder(etchedBorder);

		JPanel calcRMSPanel = new JPanel(
				new FlowLayout(FlowLayout.CENTER, 3, 3));
		calcRMSPanel.add(jbmea);

		jbmea.setForeground(Color.blue.darker());

		JPanel resultPanel = new JPanel(new GridLayout(5, 2, 2, 2));
		resultPanel.add(jlalpha);
		resultPanel.add(jlbeta);
		resultPanel.add(jlgamma);
		resultPanel.add(jlemittance);
		resultPanel.add(jlemittancenor);
		resultPanel.setBorder(etchedBorder);

		methodview.add(Box.createVerticalStrut(10));
		methodview.add(methodPanel);
		methodview.add(calcRMSPanel);

		view.add(methodview);
		view.add(resultPanel);

		return view;
	}

	/** Make the twiss parameters view */
	private Component gettwissview() {
		final Box view = new Box(BoxLayout.Y_AXIS);
		view.setBorder(BorderFactory.createTitledBorder("result"));

		view.add(Box.createVerticalStrut(10));
		view.add(jlalpha);
		view.add(Box.createVerticalStrut(10));
		view.add(jlbeta);
		view.add(Box.createVerticalStrut(10));
		view.add(jlgamma);
		view.add(Box.createVerticalStrut(10));
		view.add(jlemittance);
		view.add(Box.createVerticalStrut(10));
		view.add(jlemittancenor);
		return view;
	}

	/** Calculates all graphs data - fraction, emittance, alpha, beta, gamma */
	private void calculateGraphs() {
		openScanDialog();
	}

	private void openScanDialog() {
		JDialog scanDialog = new JDialog();
		final FunctionGraphsJPanel scanpanel = new FunctionGraphsJPanel();
		JPanel buttonPanel = new JPanel(new GridLayout(1, 5));

		final BasicGraphData[] gdArr = new BasicGraphData[5];
		BasicGraphData gdFrac = new BasicGraphData();
		BasicGraphData gdEmt = new BasicGraphData();
		BasicGraphData gdAlpha = new BasicGraphData();
		BasicGraphData gdBeta = new BasicGraphData();
		BasicGraphData gdGamma = new BasicGraphData();

		final JRadioButton[] buttonArr = new JRadioButton[5];
		JRadioButton frac_Button = new JRadioButton(" fraction ", true);
		JRadioButton emt_Button = new JRadioButton(" emittance ", false);
		JRadioButton alpha_Button = new JRadioButton(" alpha ", false);
		JRadioButton beta_Button = new JRadioButton(" beta ", false);
		JRadioButton gamma_Button = new JRadioButton(" gamma ", false);
		ButtonGroup buttonGroup = new ButtonGroup();

		gdArr[0] = gdFrac;
		gdArr[1] = gdEmt;
		gdArr[2] = gdAlpha;
		gdArr[3] = gdBeta;
		gdArr[4] = gdGamma;

		buttonArr[0] = frac_Button;
		buttonArr[1] = emt_Button;
		buttonArr[2] = alpha_Button;
		buttonArr[3] = beta_Button;
		buttonArr[4] = gamma_Button;

		buttonPanel.add(frac_Button);
		buttonPanel.add(emt_Button);
		buttonPanel.add(alpha_Button);
		buttonPanel.add(beta_Button);
		buttonPanel.add(gamma_Button);

		buttonGroup.add(frac_Button);
		buttonGroup.add(emt_Button);
		buttonGroup.add(alpha_Button);
		buttonGroup.add(beta_Button);
		buttonGroup.add(gamma_Button);

		int dataIndex = 0;
		Color[] colorArr = { Color.black, Color.red, Color.blue, Color.cyan,
				Color.magenta };

		ActionListener radioButtonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JRadioButton source = (JRadioButton) e.getSource();
				int ind = -1;
				for (int i = 0; i < buttonArr.length; i++) {
					if (source == buttonArr[i]) {
						ind = i;
					}
				}
				if (ind < 0) {
					return;
				}
				setDataIndex(ind, buttonArr, gdArr, scanpanel);
			}
		};

		for (int i = 0; i < colorArr.length; i++) {
			gdArr[i].setGraphColor(colorArr[i]);
			gdArr[i].setDrawPointsOn(false);
			gdArr[i].removeAllPoints();
			gdArr[i].setGraphProperty(scanpanel.getLegendKeyString(),
					buttonArr[i].getText());
			gdArr[i].setLineThick(2);
			gdArr[i].setImmediateContainerUpdate(false);
			buttonArr[i].setForeground(colorArr[i]);
			buttonArr[i].addActionListener(radioButtonListener);
		}

		double start = ((Double) threshStart_Spinner.getValue()).doubleValue();
		double step = ((Double) threshStep_Spinner.getValue()).doubleValue();
		double stop = ((Double) threshStop_Spinner.getValue()).doubleValue();

		double[] resArr = null;
		int nPoint = 0;

		for (double thresh = start; thresh < stop; thresh += step) {
			resArr = calEmittance(thresh);
			for (int j = 0; j < gdArr.length; j++) {
				gdArr[j].addPoint(thresh, resArr[j]);
				nPoint++;
			}
		}

		scanpanel.addGraphData(gdArr[dataIndex]);
		scanDialog.add(scanpanel, BorderLayout.CENTER);
		scanDialog.add(buttonPanel, BorderLayout.NORTH);
		scanDialog.setLocationRelativeTo(doc.getMainWindow());
		scanDialog.setSize(600, 500);
		scanDialog.setVisible(true);
		scanDialog.setResizable(false);
	}

	/**
	 * Sets the new data index. See above for index meaning
	 * 
	 * @param dataIndex_new
	 *            The new new data index
	 */
	private void setDataIndex(int dataIndex_new, JRadioButton[] buttonArr,
			BasicGraphData[] gdArr, FunctionGraphsJPanel scanpanel) {
		String[] graphNames = { "Fraction of the beam",
				"Emittance of the beam", "Alpha parameter [a.u.]",
				"Beta parameter [mm mrad]", "Gamma parameter [mrad/mm]" };

		String[] xAxisNames = { "threshold [%]", "threshold [%]",
				"threshold [%]", "threshold [%]", "threshold [%]" };

		String[] yAxisNames = { "fraction [%]", "emittance [mm mrad]",
				"alpha [ ]", "beta [mm mrad]", "gamma [mrad/mm]" };

		// set pressed button
		buttonArr[dataIndex_new].setSelected(true);

		// set graph panel decoration
		scanpanel.removeAllGraphData();
		scanpanel.addGraphData(gdArr[dataIndex_new]);
		scanpanel.setAxisNameX(xAxisNames[dataIndex_new]);
		scanpanel.setAxisNameY(yAxisNames[dataIndex_new]);
		scanpanel.setName(graphNames[dataIndex_new]);
		scanpanel.addDraggedVerLinesListener(null);
		scanpanel.clearZoomStack();
		scanpanel.refreshGraphJPanel();
	}

	/*
	 * calculate background
	 */
	protected void backgroundcal() {
		int indexfrom = Integer.parseInt(jtfindexf.getText());
		int indexto = Integer.parseInt(jtfindext.getText());
		int interval = indexto - indexfrom + 1;

		NumberFormat nf;
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		NumberFormat nf1 = NumberFormat.getNumberInstance();
		nf1.setMaximumFractionDigits(2);
		nf1.setMinimumFractionDigits(2);
		double backsum = 0;

		for (int k = indexfrom - 1; k < indexto; k++) {
			double v = value[k];
			backsum += v;
		}
		double backgroundvalue = 0;
		backgroundvalue = backsum / interval;
		jtfbackground.setText(nf.format(backgroundvalue));
		jtffirst.setText(nf.format(backgroundvalue));

		double absmax = 0, max = 0;
		for (int i = 0; i < x.length; i++) {
			if (absmax < Math.abs(value[i])) {
				absmax = Math.abs(value[i]);
				max = value[i];
			}
		}
		absmaxvalue = absmax;
		maxvalue = max;
		System.out.println("backgroundvalue: " + backgroundvalue);
		System.out.println("absmaxvalue: " + absmaxvalue);
		System.out.println("maxvalue: " + maxvalue);

		double percentage = (backgroundvalue / maxvalue) * 100;
		jlpercentage.setText("Fraction: " + nf1.format(percentage) + " %");
	}

	private void rangevalcal() {
		double xmax = 0, xpmax = 0;
		for (int i = 0; i < x.length; i++) {
			if (xmax < Math.abs(x[i]))
				xmax = Math.abs(x[i]);
			if (xpmax < Math.abs(xp[i]))
				xpmax = Math.abs(xp[i]);
		}
		xpmax = xpmax * 1000;
		xrange = (int) xmax + 1;
		xprange = (int) xpmax + 1;
	}

	/*
	 * calculate emittance
	 */
	protected double[] calEmittance(double threshold) {
		double firstvalue = 0;
		if (jcbfirst.isSelected())
			firstvalue = Double.parseDouble(jtffirst.getText());
		finalvalue = new double[x.length];
		double[] proportion = new double[x.length];
		double[] squarex = new double[x.length];
		double[] squarexp = new double[x.length];
		double[] xxp = new double[x.length];
		double sum = 0, sum_total = 0;

		// *****new value without background******//
		double max = 0;
		initialvalue = new double[x.length];
		double[] newvalue = new double[x.length];
		/*
		 * for (int i = 0; i < x.length; i++) { newvalue[i] = Math.abs(value[i]
		 * - firstvalue); if (max < newvalue[i]) max = newvalue[i]; }
		 */
		// ************liyong modify on 4.19*************
		if (maxvalue > 0) {
			for (int i = 0; i < x.length; i++) {
				initialvalue[i] = value[i];
				newvalue[i] = value[i] - firstvalue;
				if (max < newvalue[i])
					max = newvalue[i];
			}
		} else {
			for (int i = 0; i < x.length; i++) {
				initialvalue[i] = -value[i];
				newvalue[i] = -(value[i] - firstvalue);
				if (max < newvalue[i])
					max = newvalue[i];
			}
		}
		// System.out.println("max: "+max);
		double cut = max * threshold / 100;
		// *******final value with percentage*********//
		for (int i = 0; i < x.length; i++) {
			if (newvalue[i] < cut)
				finalvalue[i] = 0;
			else
				finalvalue[i] = newvalue[i];
			sum += finalvalue[i];
			sum_total += newvalue[i];
		}
		// System.out.println(" sum: "+ sum);

		sum = Math.abs(sum);
		sum_total = Math.abs(sum_total);

		// *****************************//
		double x0 = 0, xp0 = 0;
		for (int i = 0; i < x.length; i++) {
			proportion[i] = finalvalue[i] / sum;
			x0 += x[i] * proportion[i];
			xp0 += xp[i] * proportion[i];
		}
		// System.out.println("x0: "+x0);
		// System.out.println("xp0: "+xp0);
		// ********************************//

		for (int i = 0; i < x.length; i++) {
			squarex[i] = (x[i] - x0) * (x[i] - x0) * proportion[i];
			squarexp[i] = (xp[i] - xp0) * (xp[i] - xp0) * proportion[i];
			xxp[i] = (x[i] - x0) * (xp[i] - x0) * proportion[i];
		}

		// *******************************************//
		double squarexvalue = 0, squarexpvalue = 0, xxpvalue = 0;
		for (int i = 0; i < x.length; i++) {
			squarexvalue += squarex[i];
			squarexpvalue += squarexp[i];
			xxpvalue += xxp[i];
		}

		// System.out.println("squarex: " + squarexvalue);
		// System.out.println("squarexp: " + squarexpvalue);
		// System.out.println("xxp: " + xxpvalue);

		// **********************Erms=[<x*x><xp*xp>-<x*xp><x*xp>]^0.5****************//
		// **********************En=beta*gama*Erms****************//
		// **********************alpha=-<x*xp>/Erms****************//
		// **********************beta=<x*x>/Erms****************//
		// **********************gama=<xp*xp>/Erms****************//
		emittance = Math.sqrt(squarexvalue * squarexpvalue - xxpvalue
				* xxpvalue) * 1000;
		alpha = -1 * xxpvalue / emittance * 1000;
		beta = squarexvalue / emittance;
		gamma = 1000000 * squarexpvalue / emittance;

		emittancebetagamma = betagamma * emittance;
		x_center = x0;
		xp_center = xp0;

		double[] result = new double[5];

		result[0] = 100.0 * sum / sum_total;
		result[1] = emittance;
		result[2] = alpha;
		result[3] = beta;
		result[4] = gamma;
		return result;
	}

	private void setEllipsecenter(double x0, double xp0) {
		NumberFormat nf;
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		jtfx0.setText(nf.format(x_center));
		jtfxp0.setText(nf.format(xp_center * 1000));
	}

	private void setgaussfitting(double alpha2, double beta2,
			double emittance2) {
		alphaLocal_Text.setValue(alpha2);
		betaLocal_Text.setValue(beta2);
		emtLocal_Text.setValue(emittance2);
	}

	/*
	 * get raw data from the selected file
	 */
	public void getDataFromFile() {
		List<String> datainformations = new ArrayList<String>();
		RecentFileTracker _savedFileTracker = new RecentFileTracker(1,
				this.getClass(), "recent_saved_file");
		String currentDirectory = _savedFileTracker.getRecentFolderPath();
		JFileChooser fileChooser = new JFileChooser(currentDirectory);
		int status = fileChooser.showOpenDialog(this);
		if (status == JFileChooser.APPROVE_OPTION) {
			_savedFileTracker.cacheURL(fileChooser.getSelectedFile());
			File file = fileChooser.getSelectedFile();
			String filename = file.getName();
			// ******************************//
			jlfilename.setText("file name: " + filename);
			if (filename.contains("EMX"))
				jldirection.setText("Direction: X");
			else
				jldirection.setText("Direction: Y");
			// ******************************//
			try {
				String encoding = "GBK";
				int i = 0;
				int cutoff = 0;
				Scanner scanner = new Scanner(new FileInputStream(file),
						encoding);
				while (scanner.hasNext()) {
					String world = scanner.next();
					i++;
					if (world.equals("X'")) {
						cutoff = i;
					}
					if (i > cutoff + 1 && cutoff > 0) {
						datainformations.add(world);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			if (filename.contains("LEBT")) {
				betagamma = 0.01032;
			} else if (filename.contains("MEBT")) {
				betagamma = 0.08033;
			}

			int group = datainformations.size() / 143;
			System.out.println("group: " + group);

			x = new double[group];
			xp = new double[group];
			value = new double[group];
			TreeMap<Double, List<Double>> mapdata = new TreeMap<Double, List<Double>>();
			for (int i = 0; i < group; i++) {
				List<Double> listdata = new ArrayList<Double>();
				for (int j = 0; j < 143; j++) {
					listdata.add(Double.parseDouble(datainformations.get(143
							* i + j)));
				}
				mapdata.put((double) (i), listdata);
			}
			for (int i = 0; i < mapdata.size(); i++) {
				x[i] = mapdata.get((double) (i)).get(0);
				xp[i] = mapdata.get((double) (i)).get(1);
				double temp = 0;
				double sum = 0;
				for (int j = 3; j < 143; j++) {
					temp = mapdata.get((double) (i)).get(j);
					sum += temp;
				}
				value[i] = sum / 140;

			}
			// *********************/
			backgroundcal();
			jcbfirst.setSelected(true);
			rangevalcal();
		}
	}

	public void plotgausstwiss() {
		NumberFormat nf;
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);

		jlalpha_g.setText(" Alpha:  " + nf.format(alpha_g));
		jlbeta_g.setText(" Beta:  " + nf.format(beta_g));
		jlgamma_g.setText(" Gamma:  " + nf.format(gamma_g));
		jlemittance_g.setText(" Erms:  " + nf.format(emittance_g));
		jlemittance_g.setForeground(Color.red);
		jlemittancenor_g.setText(" En:  " + nf.format(emittancebetagamma_g));

	}

	private void plottwiss() {
		NumberFormat nf;
		nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);

		jlalpha.setText(" Alpha:  " + nf.format(alpha));
		jlbeta.setText(" Beta:  " + nf.format(beta) + "  (m)");
		jlgamma.setText(" Gamma:  " + nf.format(gamma) + " (1/m)");
		jlemittance
				.setText(" Erms:  " + nf.format(emittance) + ("  (mm.mrad)"));
		jlemittance.setForeground(Color.red);
		jlemittancenor.setText(" En:  " + nf.format(emittancebetagamma)
				+ ("  (mm.mrad)"));
	}

	/*
	 * plot left picture
	 */
	private void plotSingal() {
		signalpanel.removeAllGraphData();
		BasicGraphData signalData = new BasicGraphData();
		double[] indexarry = new double[value.length];
		for (int i = 0; i < value.length; i++) {
			indexarry[i] = i + 1;
		}
		signalData.addPoint(indexarry, value);
		signalpanel.addGraphData(signalData);
	}

	/*
	 * plot right picture
	 */
	public void plotEmittance() {
		GP_ep.removeAllGraphData();
		GP_ep.removeAllCurveData();
		emittance3D.setZero();

		emittance3D.setMinMaxX(-xrange, xrange);
		emittance3D.setMinMaxY(-xprange, xprange);

		if (absmaxvalue / maxvalue == 1) {
			for (int i = 0; i < x.length; i++) {
				emittance3D.addValue(x[i], 1000 * xp[i], value[i] * 10000);
			}
		} else if (absmaxvalue / maxvalue == -1) {
			for (int i = 0; i < x.length; i++) {
				emittance3D.addValue(x[i], 1000 * xp[i], -value[i] * 10000);
			}
		}

		GP_ep.setColorSurfaceData(emittance3D);
		GP_ep.refreshGraphJPanel();
	}

	/**
	 * Plots the ellipse on the phasespace graph.
	 */
	private void plotRMSEllipse() {
		// FunctionGraphsJPanel GP_ep =
		// (FunctionGraphsJPanel)getParamsHashMap().get("EMITTANCE_3D_PLOT");
		GP_ep.removeAllCurveData();
		myphasePlaneEllipseRMS.getCurveData().setColor(Color.BLACK);
		myphasePlaneEllipseRMS.getCurveData().setLineWidth(2);
		myphasePlaneEllipseRMS.getCurveData().clear();
		myphasePlaneEllipseRMS.setEmtAlphaBeta(emittance, alpha, beta);
		myphasePlaneEllipseRMS.calcCurvePoints(x_center, 1000 * xp_center);
		GP_ep.addCurveData(myphasePlaneEllipseRMS.getCurveData());

		MyPhasePlaneEllipse myphasePlaneEllipseRMS3 = new MyPhasePlaneEllipse();
		myphasePlaneEllipseRMS3.getCurveData().setColor(Color.white);
		myphasePlaneEllipseRMS3.getCurveData().setLineWidth(2);
		myphasePlaneEllipseRMS3.getCurveData().clear();
		myphasePlaneEllipseRMS3.setEmtAlphaBeta(3 * emittance, alpha, beta);
		myphasePlaneEllipseRMS3.calcCurvePoints(x_center, 1000 * xp_center);
		GP_ep.addCurveData(myphasePlaneEllipseRMS3.getCurveData());

		GP_ep.refreshGraphJPanel();
	}

	/**
	 * Plots the ellipse on the phasespace graph.
	 */
	private void plotGaussEllipse(double xc_g, double yc_g) {
		// if(GP_ep.getAllCurveData().size()>2)
		// GP_ep.getCurveData(2).clear();
		myphasePlaneEllipseGauss.getCurveData().setColor(Color.red);
		myphasePlaneEllipseGauss.getCurveData().setLineWidth(2);
		myphasePlaneEllipseGauss.getCurveData().clear();
		myphasePlaneEllipseGauss.setEmtAlphaBeta(emittance_g, alpha_g, beta_g);
		myphasePlaneEllipseGauss.calcCurvePoints(xc_g, yc_g);
		GP_ep.addCurveData(myphasePlaneEllipseGauss.getCurveData());
	}

	/*
	 * open EM file
	 */
	protected void openEMfile() {
		getDataFromFile();
		plotDisplay();
	}

	private void plotDisplay() {
		plotSingal();
		plotEmittance();

	}
	

	/** 
	 * Generate a new problem.
	 * @param gaussianDensity density from which to get the initial problem parameters
	 * @param scorer the scorer to which to assign the variables
	 * @return the new problem
	 */
	private Problem makeProblem( final GaussianDensity gaussianDensity, final GaussScorer scorer ) {
		final Problem problem = ProblemFactory.getInverseSquareMinimizerProblem( new ArrayList<>(), scorer, 0.1 );

		final InitialDelta initialDeltaHint = new InitialDelta();
		problem.addHint( initialDeltaHint );

		final Variable emtVariable = new Variable( "emt", gaussianDensity.getEmt(), 0.0, Double.MAX_VALUE );
		problem.addVariable( emtVariable );
		initialDeltaHint.addInitialDelta( emtVariable, 0.01 );

		final Variable alphaVariable = new Variable( "alpha", gaussianDensity.getAlpha(), -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( alphaVariable );
		initialDeltaHint.addInitialDelta( alphaVariable, 0.05 );

		final Variable betaVariable = new Variable( "beta", gaussianDensity.getBeta(), 0.0, Double.MAX_VALUE );
		problem.addVariable( betaVariable );
		initialDeltaHint.addInitialDelta( betaVariable, 0.05 );

		//correction of the possible non-normalization in the experimental data
		final Variable maxValVariable = new Variable( "maxVal", gaussianDensity.getMaxVal(), -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( maxValVariable );
		initialDeltaHint.addInitialDelta( maxValVariable, 0.01 );
		
		//correction of the possible non-normalization in the experimental data
		final Variable z0ValVariable = new Variable( "z0Val", gaussianDensity.getZ0Val(), -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( z0ValVariable );
		initialDeltaHint.addInitialDelta( z0ValVariable, 0.01 );
		
		//correction of the possible non-normalization in the experimental data
		final Variable xcValVariable = new Variable( "xcVal", gaussianDensity.getXC(), -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( xcValVariable );
		initialDeltaHint.addInitialDelta( xcValVariable, 0.01 );
		
		//correction of the possible non-normalization in the experimental data
		final Variable ycValVariable = new Variable( "ycVal", gaussianDensity.getYC(), -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( ycValVariable );
		initialDeltaHint.addInitialDelta( ycValVariable, 0.01 );

		// set the variables on the scorer
		scorer.setVariables( emtVariable, alphaVariable, betaVariable, maxValVariable, z0ValVariable,xcValVariable,ycValVariable);

		return problem;
	}
	
	/*
	 * Calculate rms emittance by gauss fitting
	 */

	public void gaussfitting() {
		//set initial values
		gaussianDensity.setEmtAlphaBeta( emtLocal_Text.getValue(),alphaLocal_Text.getValue(),
				betaLocal_Text.getValue(),x_center, 1000*xp_center);

		scorer.init(gaussianDensity, x, xp, initialvalue);
		// scorer and solver for fitting
		final double maxSolveTime = ((Integer)fitTime_Spinner.getValue()).doubleValue();
		solver = new Solver( new SimplexSearchAlgorithm(),  SolveStopperFactory.minMaxTimeSatisfactionStopper( 0.5, maxSolveTime, 0.99 ));
		problem = makeProblem( gaussianDensity, scorer );

		//perform fitting
		solver.solve( problem );
		/*final ScoreBoard scoreBoard = solver.getScoreBoard();
		final Trial bestSolution = scoreBoard.getBestSolution();
		scorer.applyTrialPoint( bestSolution.getTrialPoint() );*/

		System.out.println("===RESULTS of GAUSSIAN EMITTANCE FITTING===");
		System.out.println( solver.getScoreBoard() );
		
		double xc_gauss = 0, yc_gauss = 0;
		emittance_g = gaussianDensity.getEmt();
		alpha_g = gaussianDensity.getAlpha();
		beta_g = gaussianDensity.getBeta();
		xc_gauss= gaussianDensity.getXC();
		yc_gauss= gaussianDensity.getYC();

		gamma_g = (alpha_g * alpha_g + 1) / beta_g;
		emittancebetagamma_g = emittance_g * betagamma;

		plotgausstwiss();
		plotGaussEllipse(xc_gauss, yc_gauss);
	}

}

/**
 * This class calculates a Gaussian phase space density.
 */
class GaussianDensity {

	private double emt;
	private double alpha;
	private double beta;
	private double maxVal;
	private double z0Val;
	private double xcVal;
	private double ycVal;
	
	/**
	 * Constructor for the GaussianDensity object
	 */
	GaussianDensity() {
		this.emt = 0.1;
		this.alpha = 1.0;
		this.beta = 2.0;
		this.maxVal = 1.0;
		this.z0Val=0;
		this.xcVal=0;
		this.ycVal=0;
	}

	/**
	 * Returns the emittance parameter of the Gaussian phase space density
	 * 
	 * @return The emittance parameter of the Gaussian phase space density
	 */
	double getEmt() {
		return emt;
	}

	/**
	 * Returns the alpha parameter of the Gaussian phase space density
	 * 
	 * @return The alpha parameter of the Gaussian phase space density
	 */
	double getAlpha() {
		return alpha;
	}

	/**
	 * Returns the beta parameter of the Gaussian phase space density

	 * @return The beta parameter of the Gaussian phase space density
	 */
	double getBeta() {
		return beta;
	}
	
	/** get the max val */
	double getMaxVal() {
		return maxVal;
	}
	

	public double getZ0Val() {
		return z0Val;
	}

	double getXC() {
		return xcVal;
	}

	double getYC() {
		return ycVal;
	}


	/**
	 * Sets the parameters of the Gaussian density with maxVal set to 1.0
	 * @param  emt    The emittance parameter of the Gaussian phase space density
	 * @param  alpha  The alpha parameter of the Gaussian phase space density
	 * @param  beta   The beta parameter of the Gaussian phase space density
	 * @param  xcVal  The horizontal center position parameter of the Gaussian phase space density
	 * @param  ycVal  The vertical center position parameter of the Gaussian phase space density
	 */
	
	public void setEmtAlphaBeta(final double emt, final double alpha, final double beta,
				 final double xcVal, final double ycVal  ) {
		setEmtAlphaBetaMaxVal( emt, alpha, beta, 1.0, 0.0, xcVal, ycVal );		
	}


	/**
	 * Sets the parameters of the Gaussian density
	 * @param  emt    The emittance parameter of the Gaussian phase space density
	 * @param  alpha  The alpha parameter of the Gaussian phase space density
	 * @param  beta   The beta parameter of the Gaussian phase space density
	 * @param  maxVal Correction of the possible non-normalization in the experimental data
	 * @param  z0val  The background parameter of the Gaussian phase space density
	 * @param  xcVal  The horizontal center position parameter of the Gaussian phase space density
	 * @param  ycVal  The vertical center position parameter of the Gaussian phase space density
	 */
	void setEmtAlphaBetaMaxVal( final double emt, final double alpha, final double beta, final double maxVal, 
			final double z0Val, final double xcVal, final double ycVal) {
		this.emt = emt;
		this.alpha = alpha;
		this.beta = beta;
		this.maxVal = maxVal;
		this.z0Val = z0Val;
		this.xcVal = xcVal;
		this.ycVal = ycVal;
				
	}
	
	/**
	 * Returns the Gaussian density value at particular point of the phase space plane
	 * @param  x  The coordinate on the phase space plane
	 * @param  xp The momentum on the phase space plane
	 * @return The density value
	 */
	double getDensity(double x, double xp) {

		double val = alpha * (x - xcVal) + beta * (xp - ycVal);
		val = val * val + (x - xcVal) * (x - xcVal);
		val = Math.exp(-val / (2 * beta * emt));
		val *= maxVal;
		val += z0Val;
		return val;
	}
}

/**
 * This is an implementation of the Scorer interface for our fitting
 */
class GaussScorer implements Scorer {

	private MyPhasePlaneEllipse phasePlaneEllipse = null;
	// Gaussian phase space density
	private GaussianDensity gaussianDensity = null;
	double[] xvalue = null, xpvalue = null, zvalue = null;
	private double z_max = 0.;
	// problem variables
	private Variable emtVariable;
	private Variable alphaVariable;
	private Variable betaVariable;
	private Variable maxValVariable;
	private Variable z0ValVariable;
	private Variable xcValVariable;
	private Variable ycValVariable;

	

	void setVariables(Variable emtVariable, Variable alphaVariable,Variable betaVariable, 
			Variable maxValVariable,Variable z0ValVariable, Variable xcValVariable,Variable ycValVariable) {
		this.emtVariable = emtVariable;
		this.alphaVariable = alphaVariable;
		this.betaVariable = betaVariable;
		this.maxValVariable = maxValVariable;
		this.z0ValVariable = z0ValVariable;
		this.xcValVariable = xcValVariable;
		this.ycValVariable = ycValVariable;
		
	}

	/**
	 * This method initializes all data for fitting. It should be called before
	 * optimization starts
	 * @param gaussianDensity The theoretical phase space density
	 */
	void init(GaussianDensity gaussianDensity, double[] x, double[] xp,double[] zvalue) {
		this.gaussianDensity = gaussianDensity;
		this.xvalue = x;
		this.xpvalue = xp;
		this.zvalue = zvalue;

		double max = 0;
		for (int i = 0; i < zvalue.length; i++) {
			if (max < zvalue[i]) {
				max = zvalue[i];
			}
		}
		z_max = max;
	}
	
	/** apply the trial point to configure the model */
	void applyTrialPoint( final TrialPoint trialPoint ) {
		final double emt = trialPoint.getValue( emtVariable );
		final double alpha = trialPoint.getValue( alphaVariable );
		final double beta = trialPoint.getValue( betaVariable );
		final double maxVal = trialPoint.getValue( maxValVariable );
		final double z0Val = trialPoint.getValue( z0ValVariable );
		final double xcVal = trialPoint.getValue( xcValVariable );
		final double ycVal = trialPoint.getValue( ycValVariable );
		gaussianDensity.setEmtAlphaBetaMaxVal( emt, alpha, beta,z0Val, maxVal,xcVal,ycVal);
	}

	@Override
	public double score(Trial trial, List<Variable> variables) {
		double sum2 = 0.;
		double x = 0.;
		double xp = 0.;
		double val_exp = 0.;
		double val_th = 0.;

		applyTrialPoint( trial.getTrialPoint() );


		for (int i = 0; i < zvalue.length; i++) {
			x = xvalue[i];
			xp = xpvalue[i] * 1000;
			val_exp = zvalue[i] / z_max;
			val_th = gaussianDensity.getDensity(x, xp);
			sum2 += (val_th - val_exp) * (val_th - val_exp);
		}

		return sum2;
	}

}
