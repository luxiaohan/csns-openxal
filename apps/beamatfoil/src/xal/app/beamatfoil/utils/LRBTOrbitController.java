package xal.app.beamatfoil.utils;

import java.net.*;
import java.io.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.*;

import javax.swing.tree.DefaultTreeModel;

import xal.extension.scan.UpdatingEventController;
import xal.extension.widgets.swing.DoubleInputTextField;
import xal.smf.*;
import xal.smf.impl.Electromagnet;
import xal.tools.xml.XmlDataAdaptor;


/**
 *  This controller includes two panels for hor. and vert. positions and angle
 *  control.
 *
 *@author     shishlo
 *@adjusted by qiujing@mail.ihep.ac.cn
 */
public class LRBTOrbitController {

	//main panel
	private JPanel lrbtOrbMainPanel = new JPanel();

	//Updating controller
	UpdatingEventController updatingController = null;
	
	LRBTOrbitCorrector lrbtOrbCorrH = null; 
	LRBTOrbitCorrector lrbtOrbCorrV = null; 
	
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	/**
	 *  Constructor for the LRBTOrbitController object
	 *
	 *@param  updatingController_in  The Parameter
	 */
	public LRBTOrbitController(UpdatingEventController updatingController_in) {

		updatingController = updatingController_in;
		
		lrbtOrbCorrH = new LRBTOrbitCorrector("HORIZONTAL - LRBT Beam at Foil");
		lrbtOrbCorrV = new LRBTOrbitCorrector("VERTICAL - LRBT Beam at Foil");
		lrbtOrbCorrH.setMessageText(getMessageText());
		lrbtOrbCorrV.setMessageText(getMessageText());

		lrbtOrbMainPanel.setLayout(new GridLayout(2, 1, 1, 1));
		lrbtOrbMainPanel.add(lrbtOrbCorrH.getPanel());
		lrbtOrbMainPanel.add(lrbtOrbCorrV.getPanel());
	}

	/**
	 *  Returns the panel attribute of the LRBTOrbitController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return lrbtOrbMainPanel;
	}

	/**
	 *  Sets the accelerator sequence
	 *
	 *@param  accSeq  The new accelSeq value
	 */
	public void setAccelSeq(AcceleratorSeq accSeq) {
		java.util.List<AcceleratorNode> accNodes = accSeq.getNodesOfType(Electromagnet.s_strType);
		java.util.Iterator<AcceleratorNode>  itr =  accNodes.iterator();
		while(itr.hasNext()){
			Electromagnet emg = (Electromagnet) itr.next();
			if(emg.getStatus()){
				emg.setUseFieldReadback(false); 
			}
		}
		lrbtOrbCorrH.setAccelSeq(accSeq,0);
		lrbtOrbCorrV.setAccelSeq(accSeq,1);
	}
	
	
	/**
	 *  Description of the Method
	 */
	public void update() {
	}

	
	/**
	 *  Returns the sign for hor. correctors
	 */	
	 public DoubleInputTextField getSignXText(){
	  return lrbtOrbCorrH.getSignText();
	 }
	 
	/**
	 *  Returns the sign for ver. correctors
	 */	
	 public DoubleInputTextField getSignYText(){
	  return lrbtOrbCorrV.getSignText();
	 }
	
	/**
	 *  Sets the fontForAll attribute of the LRBTOrbitController object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
		lrbtOrbCorrH.setFontForAll(fnt);
		lrbtOrbCorrV.setFontForAll(fnt); 
	}


	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void dumpData(XmlDataAdaptor da) {
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void readData(XmlDataAdaptor da) {
	}

	/**
	 *  Returns the messageText attribute of the LRBTOrbitController object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}

}
