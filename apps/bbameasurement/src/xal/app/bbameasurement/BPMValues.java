package xal.app.bbameasurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import xal.ca.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import Jama.Matrix;


public class BPMValues {
	AcceleratorSeq _sequence;
	int _step;
	double[][] bpmx;
	double[][] bpmy;
	protected double tempratio;
	boolean _status=false;
	protected Quadrupole currentquad;
	protected double initialField;
	
	public BPMValues(AcceleratorSeq sequence) {
		_sequence = sequence;
	}

	public double[][] getBpmx() {
		return bpmx;
	}

	public double[][] getBpmy() {
		return bpmy;
	}
	
	public double getFractionComplete() {
		return tempratio;
	}
	

	public double getTempratio() {
		return tempratio;
	}

	public void setTempratio(double tempratio) {
		this.tempratio = tempratio;
	}

	public Quadrupole getCurrentquad() {
		return currentquad;
	}

	public double getInitialField() {
		return initialField;
	}

	public void getBPMValue() {
		List<AcceleratorNode> bpms = _sequence
				.getAllNodesWithQualifier(new AndTypeQualifier().and(
						BPM.s_strType).and(
						QualifierFactory.getStatusQualifier(true)));
		List<AcceleratorNode> quads = _sequence
				.getAllNodesWithQualifier(new AndTypeQualifier().and(
						Quadrupole.s_strType).and(
						QualifierFactory.getStatusQualifier(true)));

		int NUM_STEPS = _step;
		int numquads = quads.size();
		int totalstep = NUM_STEPS*numquads;
		int step = 0;
		
		bpmx = new double[NUM_STEPS*numquads][(bpms.size())];
		bpmy = new double[NUM_STEPS*numquads][(bpms.size())];
		HashMap<String, Double> tempx = new HashMap<String, Double>();

		try {
			for (int i = 0; i < numquads; i++) {
				if(getStatus()) return;
				//tempratio = (double)(i)/numquads;//liyong add
				currentquad = (Quadrupole) quads.get(i);				
				initialField = currentquad.getField();
				System.out.println("quad name: " +quads.get(i).getId());
				Electromagnet quadmagnet = (Electromagnet) quads.get(i);
				//final double initialField = quadmagnet.getDfltField();
				double TRIAL_FIELD_EXCURSION = initialField * 0.1;
				double FIELD_STEP = 2 * TRIAL_FIELD_EXCURSION / (NUM_STEPS - 1);
				double startField = initialField - TRIAL_FIELD_EXCURSION;
				for (int stepIndex = 0; stepIndex < NUM_STEPS; stepIndex++) {
					if(getStatus()) return;
					tempratio = (double)(step)/totalstep;//liyong add
					step++;
					System.out.println("step: "+step);
					final double trialField = startField + stepIndex * FIELD_STEP;
					System.out.println("step index: "+stepIndex+"   trialField: " + trialField);
					try {
						//((Quadrupole) quads.get(i)).getMainSupply().getAndConnectChannel(MagnetMainSupply.FIELD_SET_HANDLE).putVal(trialField);
						((Quadrupole) quads.get(i)).setField(trialField);
					} catch (ConnectionException | PutException
							| NoSuchChannelException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					for (int j = 0; j < bpms.size(); j++) {
						if(getStatus()) return;
						BPM node = (BPM) bpms.get(j);
						try {
							//System.out.println("step: "+step);
							//bpmx[i][j] = node.getXAvg();
							bpmx[step-1][j] = node.getXAvg();
							bpmy[step-1][j] = node.getYAvg();
							/*try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}*/
						//	System.out.print("bpmx: " + bpmx[step-1][j]);						
						} catch (ConnectionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (GetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
								
				}
				try {
					//((Quadrupole) quads.get(i)).getMainSupply().getAndConnectChannel(MagnetMainSupply.FIELD_SET_HANDLE).putVal(initialField);
					((Quadrupole) quads.get(i)).setField(initialField);
				} catch (ConnectionException | PutException
						| NoSuchChannelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} catch (ConnectionException | GetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
    public void setStatus(boolean _stop) {
	    _status=_stop;
	
    }

    public boolean getStatus(){
	   return _status;
    }

	public void setStep(int value) {
		_step=value;		
	}

}
