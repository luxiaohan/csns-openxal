package xal.app.bbameasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import xal.ca.*;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.*;
import xal.sim.scenario.*;
import xal.sim.sync.SynchronizationException;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.tools.beam.PhaseMatrix;
import Jama.Matrix;
/*import gov.sns.ca.ConnectionException;
import gov.sns.ca.GetException;
import gov.sns.tools.beam.PhaseMatrix;
import gov.sns.xal.model.ModelException;
import gov.sns.xal.model.alg.EnvelopeTracker;
import gov.sns.xal.model.probe.EnvelopeProbe;
import gov.sns.xal.model.probe.ProbeFactory;
import gov.sns.xal.model.probe.traj.EnvelopeProbeState;
import gov.sns.xal.model.probe.traj.Trajectory;
import gov.sns.xal.model.scenario.Scenario;
import gov.sns.xal.model.sync.SynchronizationException;
import gov.sns.xal.smf.AcceleratorNode;
import gov.sns.xal.smf.AcceleratorSeq;
import gov.sns.xal.smf.impl.BPM;
import gov.sns.xal.smf.impl.Electromagnet;
import gov.sns.xal.smf.impl.HDipoleCorr;
import gov.sns.xal.smf.impl.Quadrupole;
import gov.sns.xal.smf.impl.qualify.AndTypeQualifier;
import gov.sns.xal.smf.impl.qualify.QualifierFactory;
import gov.sns.xal.smf.proxy.ElectromagnetPropertyAccessor;*/

public class MatrixCal {
	AcceleratorSeq _sequence;
	int _step;
	Trajectory traj;
	EnvelopeProbe probe;
	Scenario scenario = null;
	List<AcceleratorNode> bpms;
	List<AcceleratorNode> hcorr;
	List<AcceleratorNode> quads;
	String beginID;
	TreeMap<Double, List<Matrix>> listmatbpmx;
	TreeMap<Double, List<Matrix>> listmatbpmy;
	
	public MatrixCal(AcceleratorSeq sequence) {
		_sequence = sequence;	
		initial();
		listmatbpmx = new TreeMap<Double, List<Matrix>>();
		listmatbpmy = new TreeMap<Double, List<Matrix>>();
	}
	
	
	public TreeMap<Double, List<Matrix>> getListmatbpmx() {
		return listmatbpmx;
	}

	public TreeMap<Double, List<Matrix>> getListmatbpmy() {
		return listmatbpmy;
	}

	public void initial() {
		bpms = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(
				BPM.s_strType).and(QualifierFactory.getStatusQualifier(true)));

		hcorr = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(
				HDipoleCorr.s_strType).and(
				QualifierFactory.getStatusQualifier(true)));
		quads = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(
				Quadrupole.s_strType).and(
				QualifierFactory.getStatusQualifier(true)));
		probe = ProbeFactory.getEnvelopeProbe(_sequence, new EnvelopeTracker());
		try {
			scenario = Scenario.newScenarioFor(_sequence);
			//probe.reset();	
			scenario.setProbe(probe);
			//scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
			scenario.resync();
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PhaseMatrix getTransferMatrix(Trajectory traj,final String strFromElement, final String strToElement) {

		final EnvelopeProbeState stateFrom = (EnvelopeProbeState) traj
				.stateForElement(strFromElement);

		final EnvelopeProbeState stateTo = (EnvelopeProbeState) traj
				.stateForElement(strToElement);
		
	//	System.out.print(" ,"+stateTo.phaseMean().getx()*1000);
		
		final PhaseMatrix matFrom = ((EnvelopeProbeState) stateFrom)
				.getResponseMatrix();

		final PhaseMatrix matTo = ((EnvelopeProbeState) stateTo)
				.getResponseMatrix();
		// System.out.println("matFrom:" + matFrom);
		// System.out.println("matTo:" + matTo);
		final PhaseMatrix matFromInv = matFrom.inverse();
		final PhaseMatrix matXfer = matTo.times(matFromInv);
		// System.out.println("matXfer:" + matXfer);
		return matXfer;
	}

	public void getMatBPMCal() {
		String beginElement ="Begin_Of_"+_sequence.getEntranceID().toString();
		int NUM_STEPS = _step;
		//System.out.println("NUM_STEPS: "+NUM_STEPS);
		int numquads = quads.size();
		int step = 0;
		try {
			for (int quadIndex = 0; quadIndex < numquads; quadIndex++) {
				//System.out.println("quad: "+quads.get(quadIndex).getId());						
				//scenario.setProbe(probe);
				//scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
				Electromagnet quadmagnet = (Electromagnet) quads.get(quadIndex);
				//final double initialField = quadmagnet.getDfltField();
				final double initialField = quadmagnet.getField();
				//System.out.println("initialField: "+initialField);
				double TRIAL_FIELD_EXCURSION = initialField * 0.1;
				double FIELD_STEP = 2 * TRIAL_FIELD_EXCURSION / (NUM_STEPS - 1);
				double startField = initialField - TRIAL_FIELD_EXCURSION;
				for (int stepIndex = 0; stepIndex < NUM_STEPS; stepIndex++) {
				//	System.out.println("stepIndex: "+stepIndex);
					step++;					
					final double trialField = startField + stepIndex * FIELD_STEP;
				//	System.out.println("trialField: " + trialField);
					scenario.setModelInput(quadmagnet,ElectromagnetPropertyAccessor.PROPERTY_FIELD,trialField);
					probe.reset();
					scenario.resyncFromCache();
					scenario.run();
					traj = scenario.getProbe().getTrajectory();

			     
				   // System.out.println("*******************");
				    List<Matrix> matbpmx = new ArrayList<Matrix>(bpms.size());
				    List<Matrix> matbpmy = new ArrayList<Matrix>(bpms.size());
				    for (int j = 0; j < bpms.size(); j++) {
					        BPM node = (BPM) bpms.get(j);
					        PhaseMatrix tempx = this.getTransferMatrix(traj, beginElement,node.getId());					        				        
					       // System.out.println("PhaseMatrix tempx "+tempx);					        
					        Matrix transTempmaxx = new Matrix(1, 3);
					        Matrix transTempmaxy = new Matrix(1, 3);
					         for (int k = 0; k < 2; k++) {
							         transTempmaxx.set(0, k, tempx.getElem(0, k));
							         transTempmaxy.set(0, k, tempx.getElem(2, k+2));
					         }	
					         //notice:the 3(com) is the 7(com) of the 7*7 matrix
					         transTempmaxx.set(0, 2, tempx.getElem(0, 6));
					        // transTempmaxx.set(0, 2, tempx.getElem(2, 6));
					         transTempmaxy.set(0, 2, tempx.getElem(2, 6));
					         matbpmx.add(transTempmaxx);
					         matbpmy.add(transTempmaxy);
				    }
                   // System.out.println("step: "+step);
				    listmatbpmx.put((double) (step-1), matbpmx);
				    listmatbpmy.put((double) (step-1), matbpmy);
				    scenario.removeModelInput( quadmagnet,ElectromagnetPropertyAccessor.PROPERTY_FIELD );		      
				}

			}				
			probe.reset();				
			scenario.resyncFromCache();
		} catch (SynchronizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}									
	}


	public void setStep(int value) {
		_step=value;
		
	}   
}
