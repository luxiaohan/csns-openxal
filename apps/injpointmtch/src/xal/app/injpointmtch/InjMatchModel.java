/**
 * 
 */
package xal.app.injpointmtch;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.mathworks.engine.MatlabEngine;

/**
 * The Injection Point Match Model to handle the match algorithms
 * @author luxiaohan
 *
 */
public class InjMatchModel {
	
	final private Future<MatlabEngine> MATLAB_ENGINE;
	
	
	
	/**
	 * Constructor
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public InjMatchModel()  {
		
		MATLAB_ENGINE = connectToMatlab();
		 
		
	}
	
	
	/**
	 * Connect to Matlab and return the matlab engine
	 * @return eng  the matlab engine
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private Future<MatlabEngine> connectToMatlab() {
		
		Future<MatlabEngine> eng = null;
		
        Future<String[]> engines = MatlabEngine.findMatlabAsync();
        try {
			if ( engines.get().length == 0 ) {
				
				 String[] options = {}; // we can set startup options for MATLAB here.
				eng = MatlabEngine.startMatlabAsync( options );
			}
			else {
			    eng = MatlabEngine.connectMatlabAsync( engines.get()[0] );
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return eng;

	}
	
	/**get matlab engine*/
	public Future<MatlabEngine> getMatlabEngine() {
		return MATLAB_ENGINE;
	}
	
	public void getdata() {
		//MATLAB_ENGINE.get().eval("");
	}
	
	
	

}
