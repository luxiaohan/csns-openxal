/*
 * Created on Mar 4, 2005
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 * 
 */
package xal.app.ringmeasurement;

import java.util.*;

import xal.extension.fit.lsm.Polynomial;

/**
 * For dispersion measurement at a BPM location.  This class takes an array of 
 * BPM measurements and perform line fit to get dispersion at this BPM location.
 * 
 * @author chu
 */
public class DispMeasurement {
	
	List myBPMs;
	double[] disp;                       //weiyy double disp[];
	double[][] data;
	double[][] eng;
	final int no_pts = 5;
	
	public DispMeasurement(List bpms) {
		myBPMs = bpms;
	}
	
	public void setBPMDataArray(double[][] data) {
		this.data = data;
		this.disp = new double[data.length]; // add by Weiyy
	}
	
	// ????
	public void setBPMDataArray(double[] data) {
		this.data = new double[1][];
		this.data[0] = data;
		this.disp = new double[data.length]; // add by Weiyy
	}
	
	public void setEnergyDataArray(double[][] data) {
		this.eng = data;
	}
	
	public void setEnergyDataArray(double[] data) {
		this.eng = new double[1][];
		this.eng[0] = data;
	}
	
	public double[] getDispersions() {
		// fit BPMs one-by-one
			for (int i=0; i<data.length; i++) {
				Polynomial p = new Polynomial(1);
				p.setData(eng[0], data[i]);                  // p.setData(eng[i], data[i]);   weiyy
				p.setParameter(0, 0.);
				p.setParameter(1, 0.);
				boolean res=p.fit();
				
				disp[i] = p.getParameter(1);
			}
		return disp;
	}
	
	public double getDispersion() {
	    return disp[0];
	}
	
}
