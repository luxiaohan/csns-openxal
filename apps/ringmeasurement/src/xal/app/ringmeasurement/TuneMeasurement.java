/*
 * TuneMeasurement.java
 *
 * Created on February 15, 2005, 3:32 PM
 */

package xal.app.ringmeasurement;

import JSci.maths.Complex;
import JSci.maths.FourierMath;
import JSci.maths.analysis.Cosine;
import xal.ca.*;
import xal.extension.fit.DampedSinusoidFit;
import xal.extension.widgets.plot.BasicGraphData;
import xal.smf.impl.BPM;

/*import gov.sns.xal.smf.impl.BPM;
import gov.sns.ca.*;
import gov.sns.tools.plot.BasicGraphData;
import gov.sns.tools.fit.lsm.Cosine;*/

/**
 * 
 * @author Paul Chu
 */
public class TuneMeasurement implements Runnable {

	double xTune, yTune, dxTune, dyTune;

	double xPhase, yPhase;

	double[] xArray, yArray;

	double[] xTransform, yTransform;

	BPM theBPM;

	boolean isHorizontal = true;

	boolean isTuneFromFit = true;

	BasicGraphData xgraphData = new BasicGraphData();

	BasicGraphData ygraphData = new BasicGraphData();

	int fftArraySize = 64;

	double A, c, w, b, d;

	int maxTime, len;

	int tuneInd = 0;

	long pvLogId = 0;

	boolean isOnline = true;

	double[][] bpmData;

	/** Creates a new instance of TuneMeasurement */
	public TuneMeasurement() {
		isOnline = true;
	}
	
	protected void setBPMData(double[][] data) {
		isOnline = false;
		bpmData = data;		
	}

	public void setBPM(BPM bpm) {
		theBPM = bpm;
	}

	protected void setFFTArraySize(int size) {
		fftArraySize = size;
	}

	/**
	 * @return horizontal tune
	 */
	public void calcXTune() {
		isHorizontal = true;
		// use live data
		if (isOnline) {
			try {
				xArray = theBPM.getXTBT();
			
				if (isTuneFromFit)
				
					xTune = tuneFromFit(xArray);
				
				else
			
					xTune = tuneFromFFT(xArray);
					
			} catch (ConnectionException ce) {
				System.out.println("Cannot connect to " + theBPM.getId()
						+ "'s X turn-by-turn PV");
				xTune = 0.;
			} catch (GetException ge) {
				System.out.println("Error occurred when trying to get "
						+ theBPM.getId() + "'s X turn-by-turn array");
				xTune = 0.;
			}
		} 
		// use data from PV Logger
		else {
			xArray = bpmData[0];
	
			if (isTuneFromFit)
				xTune = tuneFromFit(xArray);
			else
				xTune = tuneFromFFT(xArray);
		}

	}

	public double getXTuneError() {
		return dxTune;
	}

	public double getXPhase() {
		return xPhase;
	}

	/**
	 * @return vertical tune
	 */
	public void calcYTune() {
		isHorizontal = false;
		// use live data
		if (isOnline) {
		try {
			yArray = theBPM.getYTBT();
			if (isTuneFromFit)
				yTune = tuneFromFit(yArray);
			else
				yTune = tuneFromFFT(yArray);
		} catch (ConnectionException ce) {
			System.out.println("Cannot connect to " + theBPM.getId()
					+ "'s Y turn-by-turn PV");
			yTune = 0.;
		} catch (GetException ge) {
			System.out.println("Error occurred when trying to get "
					+ theBPM.getId() + "'s Y turn-by-turn array");
			yTune = 0.;
		}
		} 
		// use data from PV Logger
		else {
			yArray = bpmData[1];
			if (isTuneFromFit)
				yTune = tuneFromFit(yArray);
			else
				yTune = tuneFromFFT(yArray);			
		}

	}

	public double getYTuneError() {
		return dyTune;
	}

	public double getYPhase() {
		return yPhase;
	}

	public double[] getXArray() {
		return xArray;
	}

	public double[] getYArray() {
		return yArray;
	}

	public void setTuneFromFit(boolean tuneFromFit) {
		isTuneFromFit = tuneFromFit;
	}

	public void setXFittedData(BasicGraphData data) {
		xgraphData = data;
	}

	public BasicGraphData getXFittedData() {
		return xgraphData;
	}

	public void setYFittedData(BasicGraphData data) {
		ygraphData = data;
	}

	public BasicGraphData getYFittedData() {
		return ygraphData;
	}

	public double[] getXTransform() {
		if (xTransform != null)
			return xTransform;
		else {
			System.out
					.println("no FFT for horizontal, please call getXTune() first.");
			return new double[0];
		}
	}

	public double[] getYTransform() {
		if (yTransform != null)
			return yTransform;
		else {
			System.out
					.println("no FFT for vertical, please call getYTune() first.");
			return new double[0];
		}
	}

	private double tuneFromFFT(double[] array) {
		double tune = 0.;
		// truncate the array to less than 256 (default 64)
		double[] array1 = new double[fftArraySize];
		System.arraycopy(array, 0, array1, 0, fftArraySize);

		Complex[] fft = FourierMath.transform(array1);
		final int fft_count = fft.length;
		double[] transform = new double[fft_count];
	
		for (int index = 0; index < fft_count; index++) {
			transform[index] = fft[index].mod();
		
		}

		if (isHorizontal) {
			xTransform = transform;
		} else {
			yTransform = transform;
		}

		// find the maximum of the transformed array
		double tmp = 0.;
		//for (int i = 0; i < fft_count / 2; i++) {                   by weiyy
		for (int i = fft_count / 2; i < fft_count ; i++) {
			if (tmp < transform[i]) {
				tmp = transform[i];
				tune = i;
			}
		}
		return tune / fft_count;
	}

	private double tuneFromFit(double[] array) {
		double tune = 0.;
		Cosine cosFit = new Cosine();

		double[] dataToFit = new double[len];

		System.arraycopy(array, 0, dataToFit, 0, len);

		double[] iarr = new double[dataToFit.length];
		for (int i = 0; i < dataToFit.length; i++) {
			iarr[i] = (new Integer(i)).doubleValue();
		}

		DampedSinusoidFit fitter = new DampedSinusoidFit(iarr);
		
		/*cosFit.setData(iarr, dataToFit);
		cosFit.fitParameter(Cosine.TUNE, true);
		cosFit.fitParameter(Cosine.PHASE, true);
		cosFit.fitParameter(Cosine.SLOPE, true);
		cosFit.fitParameter(Cosine.AMP, true);
		cosFit.fitParameter(Cosine.OFFSET, false);*/

		int iterations = 1;
		//boolean result = cosFit.guessAndFit(iterations);

		/*
		 * cosFit.setParameter(Cosine.TUNE, w);
		 * cosFit.setParameter(Cosine.PHASE, b);
		 * cosFit.setParameter(Cosine.SLOPE, c); cosFit.setParameter(Cosine.AMP,
		 * A); cosFit.setParameter(Cosine.OFFSET, d);
		 */
		/*cosFit.setParameter(Cosine.TUNE, cosFit.getParameter(Cosine.TUNE));
		cosFit.setParameter(Cosine.PHASE, cosFit.getParameter(Cosine.PHASE));
		cosFit.setParameter(Cosine.SLOPE, cosFit.getParameter(Cosine.SLOPE));
		cosFit.setParameter(Cosine.AMP, cosFit.getParameter(Cosine.AMP));
		cosFit.setParameter(Cosine.OFFSET, cosFit.getParameter(Cosine.OFFSET));*/

		iterations = maxTime;
	//	result = cosFit.fit();
	//	if (result)
		//	tune = cosFit.getParameter(Cosine.TUNE);

		if (isHorizontal) {
			//dxTune = cosFit.getParameterError(Cosine.TUNE);
			dxTune = (Math.sqrt(fitter.getInitialFrequencyVariance()));
			//xPhase = cosFit.getParameter(Cosine.PHASE);
			xPhase = fitter.getInitialCosineLikePhase();
		} else {
			//dyTune = cosFit.getParameterError(Cosine.TUNE);
			dyTune = (Math.sqrt(fitter.getInitialFrequencyVariance()));
			//yPhase = cosFit.getParameter(Cosine.PHASE);
			yPhase = fitter.getInitialCosineLikePhase();
		}
		/*
		 * SinusoidalFit sinFit = new SinusoidalFit(array);
		 * sinFit.setFitParameters(A, c, w, b, d, maxTime, len); sinFit.fit();
		 * tune = sinFit.getTune(); if (isHorizontal)
		 * setXFittedData(sinFit.getFittedData()); else
		 * setYFittedData(sinFit.getFittedData());
		 */

		// prepare for result plot
		BasicGraphData graphData = new BasicGraphData();
		double[] x = new double[1000];
		double[] y = new double[1000];
		double del = len / 1000.;
		/*System.out.println("A = " + cosFit.getParameter(Cosine.AMP));
		System.out.println("c = " + cosFit.getParameter(Cosine.SLOPE));
		System.out.println("w = " + cosFit.getParameter(Cosine.TUNE));
		System.out.println("b = " + cosFit.getParameter(Cosine.PHASE));
		System.out.println("d = " + cosFit.getParameter(Cosine.OFFSET));*/
		System.out.println("A = " + fitter.getInitialAmplitude());
		System.out.println("c = " + -fitter.getInitialGrowthRate());
		System.out.println("w = " + fitter.getInitialFrequency());
		System.out.println("b = " + fitter.getInitialCosineLikePhase());
		System.out.println("d = " + fitter.getInitialOffset());

		for (int i = 0; i < x.length; i++) {
			x[i] = i * del;
			/*y[i] = cosFit.getParameter(Cosine.AMP)
					* Math.exp(-1. * cosFit.getParameter(Cosine.SLOPE) * x[i])
					* Math
							.sin(2.
									* Math.PI
									* (tune * x[i] + cosFit
											.getParameter(Cosine.PHASE)))
					+ cosFit.getParameter(Cosine.OFFSET);*/
			y[i] = fitter.getInitialAmplitude() * Math.exp(-1. * -fitter.getInitialGrowthRate() *
					x[i]) * Math.sin(2. * Math.PI * (tune * x[i] + fitter.getInitialCosineLikePhase()))
					+ fitter.getInitialOffset();
		}
		
		graphData.addPoint(x, y);

		if (isHorizontal)
			setXFittedData(graphData);
		else
			setYFittedData(graphData);

		return tune;
	}

	// protected void setFitParameters(double A, double c, double w, double b,
	// double d, double maxTime, int len) {
	protected void setFitParameters(int maxTime, int len) {
		/*
		 * this.A = A; this.c = c; this.w = w; this.b = b; this.d = d;
		 */this.maxTime = maxTime;
		this.len = len;
	}

	public void run() {
		calcXTune();
		calcYTune();
	}

	protected double getXTune() {
		return xTune;
	}

	protected double getYTune() {
		return yTune;
	}

}
