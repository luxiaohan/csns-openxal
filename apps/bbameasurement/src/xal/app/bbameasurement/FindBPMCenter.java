package xal.app.bbameasurement;

/*import gov.sns.ca.ConnectionException;
import gov.sns.ca.GetException;
import gov.sns.ca.PutException;
import gov.sns.tools.LinearFit;
import gov.sns.tools.plot.BasicGraphData;
import gov.sns.xal.smf.AcceleratorSeq;
import gov.sns.xal.smf.impl.BPM;
import gov.sns.xal.smf.impl.Dipole;
import gov.sns.xal.smf.impl.Quadrupole;
import gov.sns.xal.smf.impl.qualify.AndTypeQualifier;
import gov.sns.xal.smf.impl.qualify.MagnetType;
import gov.sns.xal.smf.impl.qualify.QualifierFactory;*/
import java.util.Iterator;
import java.util.List;

import xal.ca.*;
import xal.extension.fit.LinearFit;
import xal.extension.widgets.plot.BasicGraphData;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;

public class FindBPMCenter{
//public class FindBPMCenter implements ActionListener {
	private BBAWindow _bbaWindow;
	private AcceleratorSeq _sequence;
	
	private QuadResponse _quadresponse;
	private BPM _bpmmeasuring;
	protected BPM _bpmreference;
	protected Quadrupole _quadSupply;
	protected Dipole _upcorrctorsupply;
	protected Dipole _corrtuning;
	private double _initialCorrField = 0;
	private double _initialQuadField = 0;

	private int num_steps = 0;
	private double lowerLimit, upperLimit;
	private double[] x = null;
	private double[] y = null;
	double[] response_signal = null;
	private double[] xFit = null;
	private double[] yFit = null;
	double[] response_signalFit = null;

	// Thread measure = new Thread(new BBAMeasure(this));

	private Thread measure = null;
	
	private boolean _stop = false;

	public FindBPMCenter(BBAWindow bw) {
		_bbaWindow = bw;
		_sequence = _bbaWindow._sequence;

		_bpmmeasuring = _bbaWindow.getBPMMeasuring();
		_bpmreference = _bbaWindow.getBPMReference();
		_corrtuning = _bbaWindow.getCorrTuning();
		_upcorrctorsupply = _corrtuning;		
		_quadSupply = getnearestquad();		
		_quadresponse = new QuadResponse(this);
	}
	
	//Before measurement, some parameters must be setted.
	public void setMeasureParameters(int stepnum, double lower, double upper){
		num_steps = stepnum;
		lowerLimit = lower;
		upperLimit = upper;
	}
	
	public boolean getMeasureStatus(){
		return _stop;
	}
	
	public final Thread getThread(){
		return measure;
	}

	public void findbpmcenter(){
		if(num_steps <= 0 || _quadSupply == null) return;
			
		x = new double[num_steps];
		y = new double[num_steps];
		xFit = new double[num_steps];
		yFit = new double[num_steps];
		response_signal = new double[num_steps];
		
		//final double initialField = _upcorrctorsupply.getFieldSetting();
		/*
		 * final double liveLowerLimit = _upcorrctorsupply.getLowerFieldLimit();
		 * final double liveUpperLimit = _upcorrctorsupply.getUpperFieldLimit();
		 * final double lowerLimit = ( !Double.isNaN( liveLowerLimit ) &&
		 * liveLowerLimit > -10 ) ? liveLowerLimit : -0.01; final double
		 * upperLimit = ( !Double.isNaN( liveUpperLimit ) && liveUpperLimit < 10
		 * ) ? liveUpperLimit : 0.01;
		 */
		System.out.println(_upcorrctorsupply.getId() + " init field: " + _initialCorrField);
		LinearFit bbaFitx = new LinearFit();
		LinearFit bbaFity = new LinearFit();

		BasicGraphData tempdatax = new BasicGraphData();
		BasicGraphData tempdatay = new BasicGraphData();
		_bbaWindow.setGraphData(tempdatax, tempdatay);
		
		try {
			_initialCorrField = _upcorrctorsupply.getField();
			_initialQuadField = _quadSupply.getField();
			
			for (int step = 0; step < num_steps; step++) {
				if(_stop){
					measureEnd();
					return;
				}
				
				System.out.println("Corrector step: " + (step + 1));
				double trialField = lowerLimit + step * (upperLimit - lowerLimit) / (num_steps - 1);
				System.out.println("changing corrector field to: " + trialField);
				_upcorrctorsupply.setField(trialField);
				Thread.sleep(1000);
				
				double resresult = _quadresponse.measureResponse();
				if(resresult == Double.NaN){
					_bbaWindow.measureEnd();
					return;
				}
				response_signal[step] = resresult;
				//System.out.println("Reading reference BPM... ");

				if (_upcorrctorsupply.isHorizontal()) {
					// 水平测量
					x[step] = _bpmmeasuring.getXAvg();
					System.out.println("BPM Reading: " + x[step] + ", Response signal strength: " + response_signal[step]);
					// 一个一个点的显示在graph1中
					tempdatax.addPoint(response_signal[step], x[step]);

					bbaFitx.addSample(response_signal[step], x[step]);
				}
				else if (_upcorrctorsupply.isVertical()) {
					// 垂直测量
					y[step] = _bpmmeasuring.getYAvg();
					System.out.println("BPM Reading: " + y[step] + ", Response signal strength: " + response_signal[step]);
					// 一个一个点的显示在graph2中
					tempdatay.addPoint(response_signal[step], y[step]);

					bbaFity.addSample(response_signal[step], y[step]);
				}
			}

			_upcorrctorsupply.setField(_initialCorrField);
		} catch (ConnectionException | GetException | PutException
				| InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\nMeasure Result:");
		if (_upcorrctorsupply.isHorizontal()) {
			// 水平测量结果
			double offsetx = bbaFitx.getIntercept();
			double errox = bbaFitx.getCorrelationCoefficient();
			double slope = bbaFitx.getSlope();
			for (int i = 0; i < num_steps; i++) {
				double tempx = response_signal[i];
				xFit[i] = slope * tempx + offsetx;
			}
			System.out.println("Offset x: " + offsetx);
			System.out.println("Correlation coefficient: " + errox);
			
			BasicGraphData DataFitx = new BasicGraphData();
			DataFitx.addPoint(response_signal, xFit);
			_bbaWindow.setXResult(offsetx, DataFitx);
		}
		else if (_upcorrctorsupply.isVertical()) {
			// 垂直测量结果
			double offsety = bbaFity.getIntercept();
			double erroy = bbaFity.getCorrelationCoefficient();
			double slope = bbaFity.getSlope();
			for (int i = 0; i < num_steps; i++) {
				double tempy = response_signal[i];
				yFit[i] = slope * tempy + offsety;
			}
			System.out.println("Offset y: " + offsety);
			System.out.println("Correlation coefficient: " + erroy);
			
			BasicGraphData DataFity = new BasicGraphData();
			DataFity.addPoint(response_signal, yFit);
			_bbaWindow.setYResult(offsety, DataFity);
		}
		
		measureEnd();
	}

	private void measureEnd(){
		_bbaWindow.measureEnd();
	}
	
	public Quadrupole getnearestquad() {
		List avilabalequads = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(MagnetType.QUADRUPOLE).and(QualifierFactory.getStatusQualifier( true )));
		final Iterator quadIter = avilabalequads.iterator();
		double bpmPosition = _sequence.getPosition(_bpmmeasuring);
		double gap = _sequence.getLength();
		Quadrupole nearestquad = null;
		while (quadIter.hasNext()) {
			Quadrupole quad = (Quadrupole) quadIter.next();
			double quadPosition = _sequence.getPosition(quad);
			double quadbpmgap = Math.abs(quadPosition - bpmPosition);
			if ( quadbpmgap < gap) {
				gap = quadbpmgap;
				nearestquad = quad;
			}
		}
		if (nearestquad != null) {
			System.out.println("Quadrupole to vary: " + nearestquad);			
			return nearestquad;
		}else{
			System.out.println("There isn't quadrupole power supply.");
			return null;
		}	
	}

	public void startMeasure(){
		System.out.println("Start to measure the response to quadrupole " + _quadSupply.getId() + " :");
		try {
			//this.getnearestquad(_bpmmeasuring, _sequence);
		    //if (measure != null) measure.destroy();
		    _stop = false;
			measure = new Thread(new BBAMeasure(this));
			measure.start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public void cancelMeasure(){
		if (measure == null || _stop) return;
		System.out.println("Stopping the measurement......");
		try {
			_stop = true;
			//measure.stop();
			Thread.sleep(4000);
			
			// 为了防止在计算四级铁响应过程中程序突然停止工作导致四极铁k值未置回去，需要下面的操作。
			//double initialField = _quadSupply.getFieldSetting();
			System.out.println("corr initial value: " + _initialCorrField);
			_upcorrctorsupply.setField(_initialCorrField);
			System.out.println("quad initial value: " + _initialQuadField);
			QuadResponse.resetQuadSupply(_quadSupply, _initialQuadField);
			Thread.sleep(2000);
			System.out.println("The measurement is stopped!!!!!!");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
