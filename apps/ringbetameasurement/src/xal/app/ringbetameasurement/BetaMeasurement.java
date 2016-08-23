
package xal.app.ringbetameasurement;

import java.lang.reflect.Array;


import xal.tools.statistics.*;
import Jama.*;
import JSci.maths.*;
/**
 * 
 * @author Weiyy
 */
public class BetaMeasurement implements Runnable{
	
	boolean isOnline = true;

	Matrix xmatrix,ymatrix;
	SingularValueDecomposition svd;
	
	boolean isHorizontal = true;
	
	double[] xBeta,yBeta;
	double[][][] bpmData;
	

	public BetaMeasurement() {
//		isOnline = true;
	}
	public double[] getXBeta() {
		return xBeta;
	}
	public double[] getYBeta() {
		return yBeta;
	}

	public void calcXBeta() {
		isHorizontal = true;
		// use live data
		if (isOnline) {
	/*		try {
				xmatrix = theBPM.getXTBT();
			
				catch (ConnectionException ce) {
				System.out.println("Cannot connect to " + theBPM.getId()
						+ "'s X turn-by-turn PV");
			
			} catch (GetException ge) {
				System.out.println("Error occurred when trying to get "
						+ theBPM.getId() + "'s X turn-by-turn array");
			}
	*/	} 
		// use data from PV Logger
		else {
			xmatrix = new Matrix(bpmData[0]);			
		}
		xBeta = MIA(xmatrix.transpose());
		

	}
	public void calcYBeta() {
		isHorizontal = true;
		// use live data
		if (isOnline) {
	/*		try {
				xmatrix = theBPM.getXTBT();
			
				catch (ConnectionException ce) {
				System.out.println("Cannot connect to " + theBPM.getId()
						+ "'s X turn-by-turn PV");
			
			} catch (GetException ge) {
				System.out.println("Error occurred when trying to get "
						+ theBPM.getId() + "'s X turn-by-turn array");
			}
	*/	} 
		// use data from PV Logger
		else {
			ymatrix = new Matrix(bpmData[1]);			
		}
		yBeta = MIA(ymatrix.transpose());
		

	}
private double[] MIA(Matrix matrix){
    int P,M,sign;
    Matrix U,V,matrixcopy;
    double[] S,beta;
    double Vnew,Unew;
	P=matrix.getRowDimension();
    M=matrix.getColumnDimension(); 
    System.out.println("P ="+P );
    System.out.println("M ="+M );
    double[][] array=matrix.getArrayCopy();
    ArrayStatistics stat = new ArrayStatistics(M);
    for (int i=0;i<P;i++){
    	stat.addSample(Array.get(array,i));
    }
 //   System.out.println("stat.length ="+Array.getLength(stat.getMean()));
    Matrix meanmatrix=Matrix.identity(P, 1).times(new Matrix((double[])stat.getMean(),1));
    matrix.minusEquals(meanmatrix); 
    matrix.timesEquals(1/Math.sqrt(P*M));
    SingularValueDecomposition svd = new SingularValueDecomposition(matrix);    
    U = svd.getU();
    V = svd.getV();
    S = svd.getSingularValues();
    double[][] Varray=V.getArrayCopy();
 //   System.out.println("S.length ="+S.length );
    stat = new ArrayStatistics(S.length);
    for(int i=0;i<S.length;i++){     	
    	stat.addSample(Array.get(Varray,i));
    }
   // 	System.out.println("mean="+Array.getDouble(stat.getMean(),0));
    for(int i=0;i<S.length;i++){   
     	if (Array.getDouble(stat.getMean(),i)<0){     	
     		for(int j=0;j<V.getRowDimension();j++){
     			Vnew=V.get(j,i)*-1;
     			V.set(j, i, Vnew);
     		}
     		for(int j=0;j<U.getRowDimension();j++){
     			Unew=U.get(j,i)*-1;
     			U.set(j, i,Unew);
     		}
     	} 	
    
    }
        beta=new double[V.getRowDimension()];
    	for (int i=0;i<V.getRowDimension();i++){
    		beta[i]=(V.get(i, 0)*S[0])*(V.get(i, 0)*S[0])+ 
    		          (V.get(i, 1)*S[1])*(V.get(i, 1)*S[1]);
    	    System.out.println("beta  "+i+"=  "+beta[i] );	 
    	}
    	return beta;
    	
    }

	
protected void setBPMData(double[][][] data) {
	isOnline = false;	
	bpmData = data;		
}

public void run() {
	calcXBeta();
	calcYBeta();
}

}