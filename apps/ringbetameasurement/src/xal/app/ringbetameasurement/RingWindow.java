/*
 * RingWindow.java
 *
 * Copyright (c) 2013 IHEP
 * Beijing CHINA
 * All rights reserved.
 *
 * Created on February 26, 2013, 11:48 AM
 */

package xal.app.ringbetameasurement;

import java.util.*;

import javax.swing.*;

import xal.extension.application.smf.AcceleratorWindow;
import xal.smf.*;
import xal.smf.impl.BPM;

/**
 * 
 * @author Weiyy
 */
public class RingWindow extends AcceleratorWindow {

	static final long serialVersionUID = 0;

	private RingDocument myDocument;

	protected JTabbedPane tabbedPane;

	protected BetaPanel betaPanel;
	
	JPanel betaPane = new JPanel();

	/** Creates a new instance of RingWindow */
	public RingWindow(RingDocument aDocument) {
		super(aDocument);
		myDocument = aDocument;
		setSize(1000, 900);
		makeContent();
	}

	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Beta", betaPane);
		getContentPane().add(tabbedPane);
		if (myDocument.getSelectedSequence() != null) {
			setBetaPanel(myDocument.getSelectedSequence());
		
		}
	}


    
	protected void setBetaPanel(AcceleratorSeq seq) {
		betaPane.removeAll();
		List<AcceleratorNode> bpms = seq.getAllNodesOfType("BPM");
		bpms = AcceleratorSeq.filterNodesByStatus(bpms, true);
		Iterator <AcceleratorNode> bpmIt = bpms.iterator();
		ArrayList<BPM> bpmLst = new ArrayList<BPM>();
		while(bpmIt.hasNext()) {
			bpmLst.add((BPM)bpmIt.next());
		}
				
		betaPanel = new BetaPanel(myDocument);
		betaPane.add(betaPanel);
		betaPanel.initTables(bpmLst);
		
	}
	
	
	
	
	protected String getSelectedBPM() {
		return betaPanel.getSelectedBPM();
	}

	protected void setSelectedBPM(String BPM) {
		betaPanel.setSelectedBPM(BPM);
	}

	protected BetaPanel getBetaPanel() {
		return betaPanel;
	}
}
