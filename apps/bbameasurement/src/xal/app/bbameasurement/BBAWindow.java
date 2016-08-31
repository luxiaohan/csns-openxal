package xal.app.bbameasurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

import xal.extension.application.smf.AcceleratorWindow;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;

public class BBAWindow extends AcceleratorWindow implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JLabel jlBPMSelected, jlBPMDownstreamSelected;
	private JLabel jlUpstreamCorrSelected;
	
	private JLabel jlCorrTuningNo, jlCorrLowerLimit, jlCorrUpperLimit;
	private JTextField jtfCorrTuningNo, jtfCorrLowerLimit, jtfCorrUpLimit;
	
	//分别为测量点数；校正子的下限；上限；
	
	//Components display measure result
	//offsetx值;offsety值
	private JLabel jlBPMName, jlOffsetX, jlOffsetY;
	private JTextField jtfSelectedBPM, jtfOffsetX, jtfOffsetY;
	
	// jb1,jb2分别为Start和 Cancel;
	private JButton jbStart, jbCancel, jbClear;
	private JComboBox<BPM> jcbAllBPMs, jcbDownstreamBPMs;
	private JComboBox<Dipole> jcbUpstreamCorrs;

	private FunctionGraphsJPanel graphx = new FunctionGraphsJPanel();
	private FunctionGraphsJPanel graphy = new FunctionGraphsJPanel();
	
	protected NewBBAPanel newbbapanel;

	protected AcceleratorSeq _sequence;
	private List<BPM> AVAILABLE_BPMs;
	private List<Dipole> AVAILABLE_Correctors;

	private BPM SelectedBPM;
	private String lastMeasuredBPM = "";
	
	private FindBPMCenter findBPMCenter;

	/** Creates a new instance of MainWindow */
	public BBAWindow(BBADocument aDocument) {
		super(aDocument);
		
		AVAILABLE_BPMs = new ArrayList<BPM>();
		AVAILABLE_Correctors = new ArrayList<Dipole>();
		setSize(1200, 1000);
		makeContent();
		
		setSequence(((BBADocument) this.document).getSelectedSequence());
	}

	public void setAccelerator(Accelerator accelerator) {
		setSequence(null);
	}

	public void setSequence(AcceleratorSeq sequence) {
		if (sequence == null) return;

		_sequence = sequence;
		
		//
		loadCorrectors();
		loadBPMs();
		newbbapanel.setSequence(sequence);
	}

    public void loadCorrectors() {
		//final List allCorrectors = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and( MagnetType.DIPOLE ).and( QualifierFactory.getStatusQualifier( true ) ));
		
		// 只选择校正子，排除偏转磁铁
		List<HDipoleCorr> allHCorrectors = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(HDipoleCorr.s_strType).and( QualifierFactory.getStatusQualifier( true ) ));
		List<VDipoleCorr> allVCorrectors = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(VDipoleCorr.s_strType).and( QualifierFactory.getStatusQualifier( true ) ));

		AVAILABLE_Correctors.clear();
		AVAILABLE_Correctors.addAll(allHCorrectors);
		AVAILABLE_Correctors.addAll(allVCorrectors);
		
		Collections.sort(AVAILABLE_Correctors, new Comparator<Dipole>(){
			@Override
			public int compare(Dipole dipole1, Dipole dipole2) {
				double delta = _sequence.getPosition(dipole1) - _sequence.getPosition(dipole2);
				if(delta < 0) return -1;
				else if(delta > 0) return 1;
				return 0;
			}
		});
	}

	public void loadBPMs() {
		if(_sequence == null) AVAILABLE_BPMs.clear();
		else{
			List<BPM> bpms = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(BPM.s_strType).and(QualifierFactory.getStatusQualifier(true)));
			
			AVAILABLE_BPMs.clear();
			AVAILABLE_BPMs.addAll(bpms);
			
			jcbAllBPMs.removeAllItems();
			for(BPM bpm : AVAILABLE_BPMs) jcbAllBPMs.addItem(bpm);
		}
	}

	public void getDownStreamBPMs(BPM selectedbpm) {
		if(selectedbpm == null) return;

		jcbDownstreamBPMs.removeAllItems();
		for (BPM bpm : AVAILABLE_BPMs){
			if (_sequence.getPosition(bpm) > _sequence.getPosition(selectedbpm)){
				jcbDownstreamBPMs.addItem(bpm);
			}
		}
		
		if(jcbDownstreamBPMs.getItemCount() > 0) jcbDownstreamBPMs.setSelectedIndex(0);
	}

	public void getUpStreamCorrs(BPM selectedbpm) {
		if(selectedbpm == null) return;

		double bpmPosition = _sequence.getPosition(selectedbpm);
		
		jcbUpstreamCorrs.removeAllItems();
		for (Dipole correct : AVAILABLE_Correctors) {
			if(_sequence.getPosition(correct) < bpmPosition) jcbUpstreamCorrs.addItem(correct);
		}
	}
	
	public BPM getBPMMeasuring(){
		return (BPM) jcbAllBPMs.getSelectedItem();
	}
	
	public BPM getBPMReference(){
		return (BPM) jcbDownstreamBPMs.getSelectedItem();
	}
	
	public Dipole getCorrTuning(){
		return (Dipole) jcbUpstreamCorrs.getSelectedItem();
	}

	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {
		JPanel bbapanel= new JPanel();
		bbapanel.setSize(1100, 900);
		//显示测量结果曲线
		JPanel graphPanel = new JPanel();
		graphPanel.setLayout(new GridLayout(2, 1, 20, 20));
		graphPanel.add(graphx);
		graphPanel.add(graphy);
		graphPanel.setPreferredSize(new Dimension(700, 700));//

		//操作界面
		JPanel operationPanel = new JPanel();
		operationPanel.setLayout(new GridLayout(2, 1, 0, 20));
		
		JPanel selectPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		operationPanel.add(selectPanel);
		operationPanel.add(buttonPanel);
		
		//显示测量结果数据
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		bbapanel.add(graphPanel, BorderLayout.CENTER);
		bbapanel.add(operationPanel, BorderLayout.EAST);
		bbapanel.add(resultPanel, BorderLayout.SOUTH);
		
		jlBPMSelected = new JLabel("Name of BPM :", JLabel.CENTER);
		jlBPMDownstreamSelected = new JLabel("Downstream BPM :", JLabel.CENTER);
		jlUpstreamCorrSelected = new JLabel("Upstream Corrector :", JLabel.CENTER);
		jlCorrTuningNo = new JLabel("No. of points :", JLabel.CENTER);
		jlCorrLowerLimit = new JLabel("Corr Lower Limit :", JLabel.CENTER);
		jlCorrUpperLimit = new JLabel("Corr Upper Limit :", JLabel.CENTER);
		jlOffsetX = new JLabel("Offset X :", JLabel.CENTER);
		jlOffsetY = new JLabel("Offset Y :", JLabel.CENTER);
		
		jlBPMName = new JLabel("BPM Name :");
		jtfSelectedBPM = new JTextField(20);
		

		jcbAllBPMs = new JComboBox<BPM>(AVAILABLE_BPMs.toArray(new BPM[0]));
		jcbDownstreamBPMs = new JComboBox<BPM>(AVAILABLE_BPMs.toArray(new BPM[0]));
		jcbUpstreamCorrs = new JComboBox<Dipole>(AVAILABLE_Correctors.toArray(new Dipole[0]));

		jtfCorrTuningNo = new JTextField(5);
		jtfCorrLowerLimit = new JTextField(5);
		jtfCorrUpLimit = new JTextField(5);
		jtfOffsetX = new JTextField(20);
		jtfOffsetY = new JTextField(20);

		jtfCorrTuningNo.setFont(new Font("黑体", Font.PLAIN, 16));// 设置字体样式
		jtfCorrTuningNo.setForeground(Color.red);// 设置字体颜色
		jtfCorrTuningNo.setHorizontalAlignment(JTextField.CENTER);// 设置字体居中

		jtfCorrLowerLimit.setFont(new Font("黑体", Font.PLAIN, 16));// 设置字体样式
		jtfCorrLowerLimit.setForeground(Color.red);// 设置字体颜色
		jtfCorrLowerLimit.setHorizontalAlignment(JTextField.CENTER);// 设置字体居中

		jtfCorrUpLimit.setFont(new Font("黑体", Font.PLAIN, 16));// 设置字体样式
		jtfCorrUpLimit.setForeground(Color.red);// 设置字体颜色
		jtfCorrUpLimit.setHorizontalAlignment(JTextField.CENTER);// 设置字体居中

		jtfOffsetX.setFont(new Font("黑体", Font.PLAIN, 14));// 设置字体样式
		jtfOffsetX.setForeground(Color.red);// 设置字体颜色
		jtfOffsetX.setHorizontalAlignment(JTextField.CENTER);// 设置字体居中

		jtfOffsetY.setFont(new Font("黑体", Font.PLAIN, 14));// 设置字体样式
		jtfOffsetY.setForeground(Color.red);// 设置字体颜色
		jtfOffsetY.setHorizontalAlignment(JTextField.CENTER);// 设置字体居中

		selectPanel.setLayout(new GridLayout(6, 2, 10, 15));
		selectPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
		selectPanel.setPreferredSize(new Dimension(340, 340));

		selectPanel.add(jlBPMSelected);
		selectPanel.add(jcbAllBPMs);
		selectPanel.add(jlBPMDownstreamSelected);
		selectPanel.add(jcbDownstreamBPMs);
		selectPanel.add(jlUpstreamCorrSelected);
		selectPanel.add(jcbUpstreamCorrs);
		selectPanel.add(jlCorrTuningNo);
		selectPanel.add(jtfCorrTuningNo);
		selectPanel.add(jlCorrLowerLimit);
		selectPanel.add(jtfCorrLowerLimit);
		selectPanel.add(jlCorrUpperLimit);
		selectPanel.add(jtfCorrUpLimit);
		
		//selectPanel.add(startButton);
		//selectPanel.add(cancelButton);
		//selectPanel.add(clearButton);
		//jp2.add(jlbtemp1);
		//jp2.add(jlbtemp2);

		jbStart = new JButton("Start");
		jbStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		jbCancel = new JButton("Cancel");
		jbCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		jbClear = new JButton("Clear");
		jbClear.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		buttonPanel.setPreferredSize(new Dimension(340, 50));
		buttonPanel.add(jbStart);
		buttonPanel.add(jbCancel);
		buttonPanel.add(jbClear);
		
		resultPanel.add(jlBPMName);
		resultPanel.add(jtfSelectedBPM);
		resultPanel.add(jlOffsetX);
		resultPanel.add(jtfOffsetX);
		resultPanel.add(jlOffsetY);
		resultPanel.add(jtfOffsetY);

		resultPanel.setBorder(BorderFactory.createTitledBorder("BBA Result"));


		// *************for plot 1*****************
		// Make header visible, and add some text
		graphx.setName("BBA: X");
		graphx.setBackground(Color.white);
		graphx.setAxisNames("Quad Response", "BPM Reading (mm)");

		// Make legend visible
		graphx.setLegendVisible(true);
		graphx.setLegendButtonVisible(true);
		graphx.addMouseListener(new SimpleChartPopupMenu(graphx));

		// **************for plot 2**************
		// Make header visible, and add some text
		graphy.setName("BBA: Y");
		graphy.setBackground(Color.white);
		graphy.setAxisNames("Quad Response", "BPM Reading (mm)");

		// Make legend visible
		graphy.setLegendVisible(true);
		graphy.setLegendButtonVisible(true);
		graphy.addMouseListener(new SimpleChartPopupMenu(graphy));

		// ********************注册监听*****************
		jcbAllBPMs.addActionListener(this);
		jcbAllBPMs.setActionCommand("BPMSelected");

		jcbDownstreamBPMs.addActionListener(this);
		jcbDownstreamBPMs.setActionCommand("DownstreamBPMSelected");

		jcbUpstreamCorrs.addActionListener(this);
		jcbUpstreamCorrs.setActionCommand("UpstreamCorrSelected");

		jtfCorrTuningNo.addActionListener(this);
		jtfCorrTuningNo.setActionCommand("CorrTuningNo");

		jtfCorrLowerLimit.addActionListener(this);
		jtfCorrLowerLimit.setActionCommand("LOWERLIMIT");

		jtfCorrUpLimit.addActionListener(this);
		jtfCorrUpLimit.setActionCommand("UPLIMIT");

		jbStart.setActionCommand("start");
		jbStart.addActionListener(this);
		
		jbCancel.setActionCommand("cancel");
		jbCancel.addActionListener(this);
		
		jbClear.setActionCommand("clear");
		jbClear.addActionListener(this);
		//*************
		newbbapanel=new NewBBAPanel();
	   // JTabbedPane configuration
		JTabbedPane  jtpBBA=new JTabbedPane();			
		jtpBBA.add("BBA", bbapanel);	
		jtpBBA.add("NewBBA", newbbapanel);
		getContentPane().add(jtpBBA);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("BPMSelected")) {
			SelectedBPM = (BPM) jcbAllBPMs.getSelectedItem();
			this.getDownStreamBPMs(SelectedBPM);
			this.getUpStreamCorrs(SelectedBPM);
		}
		/*
		else if (e.getActionCommand().equals("DownstreamBPMSelected")) {
			 System.out.println(jcbDownstreamBPMs.getSelectedItem());
		}
		else if (e.getActionCommand().equals("UpstreamCorrSelected")) {
			System.out.println(jcbUpstreamCorrs.getSelectedItem());
		}
		else if (e.getActionCommand().equals("CorrTuningNo")) {
			System.out.println(jtfCorrTuningNo.getText());
		}
		else if (e.getActionCommand().equals("LOWERLIMIT")) {
			System.out.println(jtfCorrLowerLimit.getText());
		}
		else if (e.getActionCommand().equals("UPLIMIT")) {
			System.out.println(jtfCorrUpLimit.getText());
		}
		*/
		else if(e.getActionCommand().equals("start")){
			int num_steps;
			double lowerLimit;
			double upperLimit;
			try{
				num_steps = Integer.parseInt(jtfCorrTuningNo.getText());
				lowerLimit = Double.parseDouble(jtfCorrLowerLimit.getText());
				upperLimit = Double.parseDouble(jtfCorrUpLimit.getText());
			} catch(NumberFormatException exception){
				System.out.println("Measurement setting is error. Please reset the parameter!");
				return;
			}
			
			//新测量的BPM如果与上次测量的不是同一个BPM，则清除原来的测量结果
			if(!lastMeasuredBPM.equals(SelectedBPM.getId())) clearResult();
			
			System.out.println("==================================");
			System.out.println("Begin to measure the BPM offset of " + SelectedBPM.getId() + " ......");
			System.out.println("Reference BPM: " + jcbDownstreamBPMs.getSelectedItem());
			System.out.println("Corrector to vary: " + ((Dipole) jcbUpstreamCorrs.getSelectedItem()).getId()
					+ ", Lower limit: " + lowerLimit + ", upper limit: " + upperLimit);
			
			findBPMCenter = new FindBPMCenter(this);
			findBPMCenter.setMeasureParameters(num_steps, lowerLimit, upperLimit);
			jbStart.setEnabled(false);
			findBPMCenter.startMeasure();
		}
		else if(e.getActionCommand().equals("cancel")) {
			if(findBPMCenter == null) return;
			if(!findBPMCenter.getThread().isAlive()) return;
			findBPMCenter.cancelMeasure();
			jbStart.setEnabled(true);
			lastMeasuredBPM = "";
		}
		else if(e.getActionCommand().equals("clear")){
			if(jbStart.isEnabled()) clearResult();
		}
	}
	
	public void measureEnd(){
		jbStart.setEnabled(true);
		lastMeasuredBPM = SelectedBPM.getId();
		System.out.println("==================================");
	}
	
	public void setGraphData(BasicGraphData datax, BasicGraphData datay){
		graphx.addGraphData(datax);
		graphx.setLegendKeyString("BBA-X");
		datax.setGraphProperty(graphx.getLegendKeyString(), "measurement X");
		
		graphy.addGraphData(datay);
		graphy.setLegendKeyString("BBA-Y");
		datay.setGraphProperty(graphy.getLegendKeyString(), "measurement Y");
	}
	
	//清除测量结果
	public void clearResult(){
		graphx.removeAllGraphData();
		graphy.removeAllGraphData();
		
		jtfSelectedBPM.setText(SelectedBPM.getId());
		jtfOffsetX.setText("");
		jtfOffsetY.setText("");
	}
	
	//设置水平测量结果
	public void setXResult(double offsetx, BasicGraphData DataFitx){
		jtfOffsetX.setText(String.valueOf(offsetx));
		
		DataFitx.setGraphColor(Color.red);
		DataFitx.setGraphProperty(graphx.getLegendKeyString(), "Fit X");
		if(graphx.getNumberOfInstanceOfGraphData() >= 3) graphx.removeGraphData(graphx.getNumberOfInstanceOfGraphData() - 2);
		graphx.addGraphData(DataFitx);
	}
	
	//设置垂直测量结果
	public void setYResult(double offsety, BasicGraphData DataFity){
		jtfOffsetY.setText(String.valueOf(offsety));
		
		DataFity.setGraphColor(Color.red);
		DataFity.setGraphProperty(graphy.getLegendKeyString(), "Fit Y");
		if(graphy.getNumberOfInstanceOfGraphData() >= 3) graphy.removeGraphData(graphy.getNumberOfInstanceOfGraphData() - 2);
		graphy.addGraphData(DataFity);
	}
}
