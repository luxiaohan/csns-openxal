package xal.app.bbameasurement;


import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class SVDCal {
	
	NewBBAPanel _newbbapanel;
	MatrixCal matcal;
	MatrixQuad matquad;
	BPMValues bpmvalues;
	int equNumber;
	int step;
	int colnumber;
	static int bpmnumber;
	static int quadnumber;
	static double quadoffsetx[],quadoffsety[];
	static double bpmoffsetx[],bpmoffsety[];
	static double initialx[],initialy[];
	Matrix matSVDLeftx,matSVDLefty,matSVDRightx,matSVDRighty;
	TreeMap<Double, List<Matrix>> mapmatcalx;
	TreeMap<Double, List<Matrix>> mapmatcaly;
	private Thread measureThread = null;
	private boolean _stop = false;

	public SVDCal(NewBBAPanel newbbapanel,MatrixCal _matcal,MatrixQuad _matquad,BPMValues _bpmvalues,int _quadnumber,int _bpmnumber) {
		_newbbapanel=newbbapanel;
		matcal=_matcal;
		matquad=_matquad;
		bpmvalues=_bpmvalues;		
		bpmnumber=_bpmnumber;
		quadnumber=_quadnumber;		
		quadoffsetx=new double[quadnumber];
		quadoffsety=new double[quadnumber];
		bpmoffsetx=new double[bpmnumber];
		bpmoffsety=new double[bpmnumber];
		initialx=new double[2];
		initialy=new double[2];
	}
	


	public void setStep(int value) {
		step = value*quadnumber;
		equNumber=step*bpmnumber;
		colnumber=quadnumber+bpmnumber+2;
		matSVDLeftx = new Matrix(equNumber,1);
		matSVDLefty = new Matrix(equNumber,1);
		matSVDRightx = new Matrix(equNumber,colnumber);
		matSVDRighty = new Matrix(equNumber,colnumber);
		matcal.setStep(value);
		matquad.setStep(value);
		bpmvalues.setStep(value);		
	}

	public Matrix getMatSVDLeftx() {
		return matSVDLeftx;
	}

	public Matrix getMatSVDLefty() {
		return matSVDLefty;
	}

	public Matrix getMatSVDRightx() {
		return matSVDRightx;
	}

	public Matrix getMatSVDRighty() {
		return matSVDRighty;
	}

	public  double[] getQuadoffsetx() {
		return quadoffsetx;
	}

	public  double[] getBpmoffsetx() {
		return bpmoffsetx;
	}

	public double[] getInitialx() {
		return initialx;
	}
	
	public  double[] getQuadoffsety() {
		return quadoffsety;
	}

	public  double[] getBpmoffsety() {
		return bpmoffsety;
	}

	public  double[] getInitialy() {
		return initialy;
	}

	public void getSVDLeft(){
		if(_stop){			
			newBBAmeasureEnd();
			return;
		}
		bpmvalues.getBPMValue();		
		double[][] arrybpmvaluesx=bpmvalues.getBpmx();
		double[][] arrybpmvaluesy=bpmvalues.getBpmy();
		int k=0;
		for(int i=0;i<step;i++){			
			for(int j=0;j<bpmnumber;j++){
				k++;
				double bpmx=arrybpmvaluesx[i][j];
				double bpmy=arrybpmvaluesy[i][j];
				double bpmtobeginx=mapmatcalx.get((double)i).get(j).get(0, 2)*1000;
				double bpmtobeginy=mapmatcaly.get((double)i).get(j).get(0, 2)*1000;
				//System.out.println("bpmtobeginx: "+bpmtobeginx);
				double svdleftx=bpmx-bpmtobeginx;
				double svdlefty=bpmy-bpmtobeginy;
				matSVDLeftx.set(k-1, 0, svdleftx);
				matSVDLefty.set(k-1, 0, svdlefty);
			}		
		}				
		newBBAmeasureEnd();
	}
	
	private void newBBAmeasureEnd() {
		_newbbapanel.measureEnd();		
	}
	
	public void getSVDRight(){
		matcal.getMatBPMCal();
		mapmatcalx=matcal.getListmatbpmx();
		mapmatcaly=matcal.getListmatbpmy();
		Matrix matIdentity = (Matrix.identity(bpmnumber, bpmnumber)).times(-1);
		int k=0;
		
		int tempmatv[]=new int[bpmnumber];
		for (int qn=0;qn<bpmnumber;qn++){
			tempmatv[qn]=qn+quadnumber;
		}
		matquad.getlistMatrixQuad();
		TreeMap<Double, TreeMap<Double, List<Double>>> listMarixQuadx=matquad.getListmapquadbpmx();
		TreeMap<Double, TreeMap<Double, List<Double>>> listMarixQuady=matquad.getListmapquadbpmy();
		//TreeMap<Double, List<Matrix>> mapmatcal=matcal.getMatBPMCal();
		for(int i=0;i<step;i++){
			int tempmath[]=new int[bpmnumber];
			for(int j=0;j<bpmnumber;j++){
				k++;
				tempmath[j]=k-1;
                for(int index=0;index<colnumber;index++){  
                	if(index<quadnumber){
                		matSVDRightx.set(k-1, index, listMarixQuadx.get((double)i).get((double)j).get(index));
                		matSVDRighty.set(k-1, index, listMarixQuady.get((double)i).get((double)j).get(index));
                	}else if(index>=quadnumber+bpmnumber){
                		matSVDRightx.set(k-1, index, mapmatcalx.get((double)i).get(j).get(0, index-quadnumber-bpmnumber));
                		matSVDRighty.set(k-1, index, mapmatcaly.get((double)i).get(j).get(0, index-quadnumber-bpmnumber));
                	}
		        }
	       }	   
			matSVDRightx.setMatrix(tempmath, tempmatv, matIdentity);
			matSVDRighty.setMatrix(tempmath, tempmatv, matIdentity);
		}		
		//matSVDRight.print(9, 6);
    }
	
	public void getsvd(Matrix Ax,Matrix Mx,Matrix Ay,Matrix My) {
        SingularValueDecomposition sx = Ax.svd();
        SingularValueDecomposition sy = Ay.svd();
        Matrix Ux = sx.getU();
        Matrix Sx = sx.getS();
        Matrix Vx = sx.getV();
        
        Matrix Uy = sy.getU();
        Matrix Sy = sy.getS();
        Matrix Vy = sy.getV();
        
       double[] sigvalx= sx.getSingularValues();
       double[] sigvaly= sy.getSingularValues();
       double[] sigvalinvx=new double[sigvalx.length];
       double[] sigvalinvy=new double[sigvaly.length];
       
       for(int i=0;i<sigvalx.length;i++){
      	 System.out.println("sigvalx:"+sigvalx[i]);
      	 if(Math.abs(sigvalx[i])<1.e-3)sigvalinvx[i]=0;
      	 else sigvalinvx[i]=1/sigvalx[i];
       }
       
       for(int i=0;i<sigvaly.length;i++){
        	 System.out.println("sigvaly:"+sigvaly[i]);
        	 if(Math.abs(sigvaly[i])<1.e-3)sigvalinvy[i]=0;
        	 else sigvalinvy[i]=1/sigvaly[i];
       }
       
       Matrix InvSx=new Matrix(Sx.getRowDimension(),Sx.getColumnDimension());
       Matrix InvSy=new Matrix(Sy.getRowDimension(),Sy.getColumnDimension());
       
       for(int i=0;i<Sx.getRowDimension();i++){
      	 for(int j=0;j<Sx.getColumnDimension();j++){
      		 if(i==j){
      			 InvSx.set(i, j, sigvalinvx[i]);
      		 }else InvSx.set(i, j, 0);
      	 }
       }
       
       for(int i=0;i<Sy.getRowDimension();i++){
        	 for(int j=0;j<Sy.getColumnDimension();j++){
        		 if(i==j){
        			 InvSy.set(i, j, sigvalinvy[i]);
        		 }else InvSy.set(i, j, 0);
        	 }
         }
       
     //  InvS.print(9, 6);
       
       System.out.println("svdInvA = V * S' * U'");
       System.out.println();
       System.out.print("svdInvA = ");
       Matrix svdInvAx=Vx.times(InvSx).times(Ux.transpose());
       svdInvAx.print(9,6);
       
       Matrix svdInvAy=Vy.times(InvSy).times(Uy.transpose());
       svdInvAy.print(9,6);
       
       System.out.print("X = ");
       Matrix X= svdInvAx.times(Mx);
       Matrix Y= svdInvAy.times(My);
       X.print(9,6);
       System.out.print("Y = ");
       Y.print(9,6);
       DecimalFormat df = new DecimalFormat("0.00000");
       for(int i=0;i<quadnumber;i++){
    	   System.out.println("Q offset x: "+X.get(i, 0));
    	   System.out.println("Q offset y: "+Y.get(i, 0));    	   
    	   quadoffsetx[i]=Double.parseDouble(df.format(X.get(i, 0)));
    	   quadoffsety[i]=Double.parseDouble(df.format(Y.get(i, 0)));
    	   
       }
       for(int i=quadnumber;i<quadnumber+bpmnumber;i++){
    	   System.out.println("BPM offset x: "+X.get(i, 0));
    	   System.out.println("BPM offset y: "+Y.get(i, 0));
    	   bpmoffsetx[i-quadnumber]=Double.parseDouble(df.format(X.get(i, 0)));
    	   bpmoffsety[i-quadnumber]=Double.parseDouble(df.format(Y.get(i, 0)));
       }       
       for(int i=quadnumber+bpmnumber;i<quadnumber+bpmnumber+2;i++){
    	   System.out.println("initial value x: "+X.get(i, 0));
    	   System.out.println("initial value y: "+Y.get(i, 0));
    	   initialx[i-quadnumber-bpmnumber]=Double.parseDouble(df.format(X.get(i, 0)));
    	   initialy[i-quadnumber-bpmnumber]=Double.parseDouble(df.format(Y.get(i, 0)));
       }
		
	}

	public final Thread getThread(){
		return measureThread;
	}
	
	public void startMeasure() {
		try {			
			 _stop = false;
			 bpmvalues.setStatus(_stop);
			measureThread = new Thread(new NewBBAMeasure(this));
			measureThread.start();
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	public boolean getMeasureStatus(){
		return _stop;
	}

	public void stopMeasure() {		
		if (measureThread == null || _stop) return;
		try {
			_stop = true;
			bpmvalues.setStatus(_stop);
			Thread.sleep(1000);		
			System.out.println("corr initial value: " +bpmvalues.getCurrentquad());
			bpmvalues.getCurrentquad().setField(bpmvalues.getInitialField());
			Thread.sleep(1000);
			System.out.println("The measurement is stopped!!!!!!");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}

class NewBBAMeasure implements Runnable {
	SVDCal _svdcal;
	
	public NewBBAMeasure(SVDCal svdcal){
		_svdcal=svdcal;
	}

	@Override
	public void run(){
        synchronized(this){
        	try {
        		_svdcal.getSVDLeft();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

}
