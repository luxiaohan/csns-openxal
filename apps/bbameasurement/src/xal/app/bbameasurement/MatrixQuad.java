package xal.app.bbameasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import xal.ca.*;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.*;
import xal.sim.sync.SynchronizationException;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.tools.beam.PhaseMatrix;
import Jama.Matrix;

public class MatrixQuad {
	AcceleratorSeq _sequence;
	int _step;
	TreeMap<Double, List<Matrix>> listmatbpmx;
	List<AcceleratorNode> bpms;
	List<AcceleratorNode> hcorr;
	List<AcceleratorNode> quads;
	EnvelopeProbe probe;
	Scenario scenario = null;
	Trajectory traj;
	TreeMap<Double, TreeMap<Double, List<Double>>> listmapquadbpmx,listmapquadbpmy;

	public MatrixQuad(AcceleratorSeq sequence) {
		_sequence = sequence;
		initial();
		listmapquadbpmx = new TreeMap<Double, TreeMap<Double, List<Double>>>();
		listmapquadbpmy = new TreeMap<Double, TreeMap<Double, List<Double>>>();
	}
	

	public TreeMap<Double, TreeMap<Double, List<Double>>> getListmapquadbpmx() {
		return listmapquadbpmx;
	}

	public TreeMap<Double, TreeMap<Double, List<Double>>> getListmapquadbpmy() {
		return listmapquadbpmy;
	}

	public void initial() {
		bpms = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(
				BPM.s_strType).and(QualifierFactory.getStatusQualifier(true)));
		quads = _sequence.getAllNodesWithQualifier(new AndTypeQualifier().and(
				Quadrupole.s_strType).and(
				QualifierFactory.getStatusQualifier(true)));
		probe = ProbeFactory.getEnvelopeProbe(_sequence, new EnvelopeTracker());
		try {
			scenario = Scenario.newScenarioFor(_sequence);
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// compute the transformatix of the quad.
	public Matrix getQuadmatrix(Trajectory traj, Quadrupole quad) {
		int[] indices = traj.indicesForElement(quad.getId());
		int startindex = indices[0] - 1;
		int stopindex = indices[indices.length - 1];

		EnvelopeProbeState stateStart = (EnvelopeProbeState) traj.stateWithIndex(startindex);
		EnvelopeProbeState stateEnd = (EnvelopeProbeState) traj.stateWithIndex(stopindex);
	//	final PhaseMatrix matStartInv = stateStart.getResponseMatrix().inverse();
		final PhaseMatrix matStart = stateStart.getResponseMatrix();
		final PhaseMatrix matStartInv = matStart.inverse();
		//matStartInv.print(NumberFormat.getInstance(),6);
		final PhaseMatrix matQuadMatrix = stateEnd.getResponseMatrix().times(matStartInv);

		Matrix QuadMatrix = new Matrix(6, 6);
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				QuadMatrix.set(i, j, matQuadMatrix.getElem(i, j));
			}
		}
		return QuadMatrix;
	}

	/*
	 * compute the transfer matrix from quad to bpm
	 */

	public Matrix getQtoBMatrix(Trajectory traj, Quadrupole quad,
			BPM bpm) {
		int[] indices = traj.indicesForElement(quad.getId());
		int stopindex = indices[indices.length - 1];
		final EnvelopeProbeState stateQuad = (EnvelopeProbeState) traj
				.stateWithIndex(stopindex);
		final EnvelopeProbeState stateBPM = (EnvelopeProbeState) traj
				.stateForElement(bpm.getId());

		double quadPosition = stateQuad.getPosition();
		double bpmPosition = stateBPM.getPosition();

		Matrix QuadToBPMMatrix = new Matrix(6, 6);
		//final PhaseMatrix matQuadInv = stateQuad.getResponseMatrix().inverse();
		final PhaseMatrix matQuad = stateQuad.getResponseMatrix();
		//System.out.println("matQuad: "+matQuad);
		final PhaseMatrix matQuadInv = matQuad.inverse();
		final PhaseMatrix matQtoBMatrix = stateBPM.getResponseMatrix().times(
				matQuadInv);
		if (quadPosition < bpmPosition) {
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 6; j++) {
					QuadToBPMMatrix.set(i, j, matQtoBMatrix.getElem(i, j));
				}
			}
		} else {
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 6; j++) {
					QuadToBPMMatrix.set(i, j, 0);
				}
			}
		}
		return QuadToBPMMatrix;
	}

	public void getlistMatrixQuad() {
		try {
			int NUM_STEPS = _step;
			int numquads = quads.size();
			int step = 0;
			final String FIELD_PROPERTY = ElectromagnetPropertyAccessor.PROPERTY_FIELD;
			//probe.reset();			
			scenario.setProbe(probe);
			//scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
			scenario.resync();
			for (int i = 0; i < numquads; i++) {				
				Electromagnet quadmagnet = (Electromagnet) quads.get(i);
				//final double initialField = quadmagnet.getDfltField();
				final double initialField = quadmagnet.getField();
				double TRIAL_FIELD_EXCURSION = initialField * 0.1;
				double FIELD_STEP = 2 * TRIAL_FIELD_EXCURSION / (NUM_STEPS - 1);
				double startField = initialField - TRIAL_FIELD_EXCURSION;
				for (int stepIndex = 0; stepIndex < NUM_STEPS; stepIndex++) {
					step++;
					final double trialField = startField + stepIndex * FIELD_STEP;
					//System.out.println("step index: "+stepIndex+"   trialField: " + trialField);
					scenario.setModelInput(quadmagnet,
							ElectromagnetPropertyAccessor.PROPERTY_FIELD,
							trialField);	
					probe.reset();
					scenario.resyncFromCache();
					scenario.run();
					traj = scenario.getProbe().getTrajectory();
					Matrix tempfinalmax;
					Matrix tempqtobmax = new Matrix(6, 6);
					Matrix tempqmax = new Matrix(6, 6);
					TreeMap<Double, List<Double>> mapquadbpmx = new TreeMap<Double, List<Double>>();
					TreeMap<Double, List<Double>> mapquadbpmy = new TreeMap<Double, List<Double>>();
					for (int j = 0; j < bpms.size(); j++) {
						BPM bpm = (BPM) bpms.get(j);
						List<Double> matquadbpmx = new ArrayList<Double>(
								bpms.size());
						List<Double> matquadbpmy = new ArrayList<Double>(
								bpms.size());
						for (int k = 0; k < quads.size(); k++) {
							Quadrupole quad = (Quadrupole) quads.get(k);
							tempqmax = this.getQuadmatrix( traj, quad);
							tempqtobmax = this.getQtoBMatrix(traj, quad, bpm);
							tempfinalmax = tempqtobmax.times(tempqtobmax.minus(tempqmax));
							matquadbpmx.add(tempfinalmax.get(0, 0));
							matquadbpmy.add(tempfinalmax.get(2, 2));
						}
						mapquadbpmx.put((double) j, matquadbpmx);
						mapquadbpmy.put((double) j, matquadbpmy);
					}
					
					listmapquadbpmx.put((double) (step - 1), mapquadbpmx);
					listmapquadbpmy.put((double) (step - 1), mapquadbpmy);
				}
				scenario.removeModelInput( quads.get(i), FIELD_PROPERTY );
			}
			probe.reset();
			scenario.resyncFromCache();
		} catch (SynchronizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (ConnectionException e) {
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
