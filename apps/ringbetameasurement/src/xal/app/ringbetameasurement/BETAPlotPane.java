/* BPMPlotPane.java
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on February 23, 2005, 10:25 AM
 */


package xal.app.ringbetameasurement;

import java.awt.Color;
// import java.awt.event.ActionListener;
// import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;

/*import gov.sns.tools.apputils.SimpleChartPopupMenu;
import gov.sns.tools.plot.BasicGraphData;
import gov.sns.tools.plot.FunctionGraphsJPanel;*/

public class BETAPlotPane extends JPanel {
	static final long serialVersionUID = 0;

	double[] myBpmArray, xArray,data;

	protected FunctionGraphsJPanel bpmPlot;

	private BasicGraphData bpmData,newdata;


	private int ind;

	/**
	 * BPM turn-by-turn data plot
	 * 
	 * @param bpmArray
	 *            BPM turn-by-turn array
	 * @param ind
	 *            Hori./Vert. indicator. Hori.=0, Vert.=1
	 */
	public BETAPlotPane(int ind) {
		this.ind = ind;

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(800, 150));
		bpmPlot = new FunctionGraphsJPanel();

		switch (ind) {

		case 0:
			bpmPlot.setName(" Horizontal Beta Plot");
			break;
		case 1:
			bpmPlot.setName(" Vertical Beta Plot");
			break;
		case 2:
			bpmPlot.setName(" Horizontal Beta Beat From Design");
			break;
		case 3:
			bpmPlot.setName(" Vertical Beta Beat From Design");
			break;
		case 4:
			bpmPlot.setName(" Horizontal Phase Plot");
			break;
		case 5:
			bpmPlot.setName(" Vertical Phase Plot");
			break;
		case 6:
			bpmPlot.setName("Hori. Phase Diff From Design");
			break;
		case 7:
			bpmPlot.setName("Vert. Phase Diff From Design");
			break;
		default:
			break;
		}

		if (ind == 0 || ind == 1)
			bpmPlot.setAxisNames("s (m)", "beta (m)");
		else if (ind == 2 || ind == 3)
			bpmPlot.setAxisNames("s (m)", "beta beat ");
		else if (ind == 4 || ind == 5)
			bpmPlot.setAxisNames("s (m)", "phase (rad)");
		else if (ind == 6 || ind == 7)
			bpmPlot.setAxisNames("s (m)", "phase error (deg)");

		bpmPlot.addMouseListener(new SimpleChartPopupMenu(bpmPlot));
		add(bpmPlot, BorderLayout.CENTER);
	}

	public void setDataArray(double[] bpmArray) {
		myBpmArray = bpmArray;
		if (myBpmArray != null) {
			xArray = new double[myBpmArray.length];
			for (int i = 0; i < myBpmArray.length; i++)
				xArray[i] = i + 1.;
		}
	}

	public void setDataArray(double[] xArray, double[] bpmArray) {
		this.xArray = xArray;
		myBpmArray = bpmArray;
	
	}

	public void setFittedData( double[] graphData) {
		
		data = graphData;
	}

	public void plot() {
	//	bpmPlot.removeAllGraphData();

		if (myBpmArray != null) {
			// for BPM data
			bpmData = new BasicGraphData();
			bpmData.setDrawLinesOn(true);
			
			// Do not add point by point but the entire arrays.  This will improve the performance a lot.
			//for (int i = 0; i < myBpmArray.length; i++) {
			//	bpmData.addPoint(xArray[i], myBpmArray[i]);
			//}
			
			bpmData.addPoint(xArray, myBpmArray);
			
			bpmPlot.addGraphData(bpmData);

			// for fitted curve
		//	if (ind == 0 || ind == 1) {
				// clean up old fitted curve first
			//	if (bpmPlot.getAllGraphData().size() > 1)
			//		bpmPlot.removeCurveData(1);
				if (data != null) {
					newdata = new BasicGraphData();
					newdata.setDrawPointsOn(true);
					newdata.setGraphColor(Color.RED);
					newdata.addPoint(xArray, data);
					bpmPlot.addGraphData(newdata);
				}
			//}
		}
	}

	// public void actionPerformed(ActionEvent ae) {
	// action for FFT

	// }
}
