package xal.app.bbameasurement;

import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.fit.LinearFit;
import xal.smf.impl.*;

public class QuadResponse {
	private FindBPMCenter _findbpmcenter;
	private Quadrupole _quadsupply;
	private BPM _referencebpm;
	private double responsesignal;
	private Dipole _corrtuning;
	
	private final int quadTuningNo = 5;
	

	public QuadResponse(FindBPMCenter findbpmcenter){
		_findbpmcenter = findbpmcenter;
		
		_quadsupply = _findbpmcenter._quadSupply;
		_referencebpm = _findbpmcenter._bpmreference;
		_corrtuning = _findbpmcenter._corrtuning;
	}
	
	public double measureResponse(){
	   try{
		   double initialField = _quadsupply.getFieldSetting();
		   
		   final double liveLowerLimit = _quadsupply.getMainSupply().lowerFieldLimit();
		   final double liveUpperLimit = _quadsupply.getMainSupply().upperFieldLimit();
		   final double currentValue = _quadsupply.getField();
		   final double lowerLimit = ( !Double.isNaN( liveLowerLimit ) ) ? liveLowerLimit : currentValue - 0.02;
		   final double upperLimit = ( !Double.isNaN( liveUpperLimit )) ? liveUpperLimit : currentValue + 0.02;
		   
	       LinearFit quadFit = new LinearFit();
		   for(int step=0; step<quadTuningNo; step++){
			   //判断是否中断测量
			   if(_findbpmcenter.getMeasureStatus()) return Double.NaN;

			   System.out.println("Quadrupole step: " +step);
			   double trialField  =lowerLimit + step * ( upperLimit - lowerLimit ) / ( quadTuningNo - 1 );
			   System.out.println("changing quadrupole field to: " +trialField );
			   _quadsupply.setField( trialField );
			   Thread.sleep( 2000 );
			   if(_corrtuning.isHorizontal()){
			       double xAvg=_referencebpm.getXAvg();
			       // if (ampAvg = null )  continue;
			       quadFit.addSample( trialField, xAvg);
			   }
			   else if(_corrtuning.isVertical()){
			       double yAvg=_referencebpm.getYAvg();
			       // if (ampAvg = null )  continue;
			       quadFit.addSample( trialField, yAvg);
			   }
		   }
		   //响应在KL较小的时候近似为线性
		   responsesignal = quadFit.getSlope();
		   resetQuadSupply( _quadsupply, initialField );
		   Thread.sleep( 2000 );
	   }catch( Exception exception ) {
			Logger.global.log( Level.SEVERE, "Exception updating the response matrix.", exception );
			exception.printStackTrace();
			throw new RuntimeException( "Online Model calibration exception.", exception );
	  	}
	   
	   return responsesignal;
	}
	
	public static void resetQuadSupply(Quadrupole supply, double initialField) {
		try {
			supply.setField( initialField );			
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
	public double getResponsesignal() {
		return responsesignal;
	}
}