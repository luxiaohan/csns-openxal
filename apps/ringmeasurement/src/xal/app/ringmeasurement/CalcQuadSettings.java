package xal.app.ringmeasurement;

import java.util.*;
import java.text.NumberFormat;
import java.awt.event.*;

import xal.model.ModelException;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.*;
import xal.sim.scenario.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.extension.solver.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.hint.*;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.sync.SynchronizationException;
import xal.tools.beam.calc.CalculationsOnRings;
import xal.tools.math.r3.R3;

public class CalcQuadSettings implements Runnable {

	//ArrayList<ParameterProxy> variable_list = new ArrayList<ParameterProxy>();
	ArrayList<Variable> variable_list = new ArrayList<Variable>();

	Ring theRing;

	Scenario scenario;

	SimpleEvaluator se;

	List<AcceleratorNode> quads;

	ArrayList<MagnetMainSupply> qPSs = new ArrayList<MagnetMainSupply>();

	ArrayList<String> bpms;

	HashMap<String, String> quad2PSMap = new HashMap<String, String>();

	//ParameterProxy[] varQuadPSs;
	Variable[] varQuadPSs;

	TunePanel tp;

	double[] initVals;

	TransferMapProbe myProbe;

	//TransferMapTrajectory traj;
	Trajectory<TransferMapState> traj;

	double stepSize = 0.1;

	float elapseTime = 120.f;

	double maxBestScore = 0.001;

	private int fractionComplete = 0;
	private Problem problem;
	List<InitialDelta> varQuadPSsHints;

	public CalcQuadSettings(Ring ring, ArrayList<String> bpms, TunePanel tp) {
		theRing = ring;
		this.bpms = bpms;
		this.tp = tp;
		this.initVals = tp.qSetVals;

		quads = theRing.getAllNodesOfType("Q");
		Iterator<AcceleratorNode> it = quads.iterator();
		while (it.hasNext()) {
			Quadrupole quad = ((Quadrupole) it.next());
			MagnetMainSupply mps = quad.getMainSupply();

			quad2PSMap.put(quad.getId(), mps.getId());

			if (!qPSs.contains(mps)) {
				qPSs.add(mps);
			}
		}

		//varQuadPSs = new ParameterProxy[qPSs.size()];
		varQuadPSs = new Variable[qPSs.size()];
        varQuadPSsHints = new ArrayList<InitialDelta>(qPSs.size());
        problem = new Problem();

		for (int i = 0; i < qPSs.size(); i++) {
			// set parameter list
			//varQuadPSs[i] = new ParameterProxy(qPSs.get(i).getId(),
				//	initVals[i], stepSize, 0, 50.);
			varQuadPSs[i] = new Variable(qPSs.get(i).getId(),
                    initVals[i], 0, 50.);
			variable_list.add(varQuadPSs[i]);
            varQuadPSsHints.add(new InitialDelta(stepSize));
            varQuadPSsHints.get(i).addInitialDelta(varQuadPSs[i], stepSize);
            problem.addHint(varQuadPSsHints.get(i));
		}
        
        problem.setVariables( variable_list );
		/*
		 * // use the "good BPM" list from TunePanel List<AcceleratorNode>
		 * bpmls = theRing.getAllNodesOfType("BPM"); bpmls =
		 * AcceleratorSeq.filterNodesByStatus(bpmls, true); Iterator<AcceleratorNode>
		 * bpmIt = bpmls.iterator(); while (bpmIt.hasNext()) { bpms.add((BPM)
		 * bpmIt.next()); }
		 */
	}

	public void run() {

		// prepare for online model run
		//myProbe = ProbeFactory.getTransferMapProbe(theRing,
			//	new TransferMapTracker());
		 try {
			myProbe = ProbeFactory.getTransferMapProbe(theRing, AlgorithmFactory.createTransferMapTracker(theRing));
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			scenario = Scenario.newScenarioFor(theRing);
			scenario.setProbe(myProbe);
			// set the model synch mode to "DESIGN", then override with live
			// quad snapshot.
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		} catch (ModelException e) {
			System.out.println(e);
		}

		// variable_list.clear();

		// set up solver
		//Solver solver = new Solver();
		Solver solver = new Solver(SolveStopperFactory.minMaxTimeSatisfactionStopper(0, elapseTime, 0.001 ));
		//final SimplexSearchAlgorithm algorithm = new SimplexSearchAlgorithm();
		//solver.setSearchAlgorithm(algorithm);
		//solver.setVariables(variable_list);
		problem.setVariables(variable_list);

		se = new SimpleEvaluator(this);
		//solver.setScorer(se);
		//solver.setStopper(SolveStopperFactory.targetStopperWithMaxTime(
		//		maxBestScore, elapseTime));

		tp.progBar.setMaximum(Math.round(elapseTime));
		// for the progress bar:
		final int delay = 2000; // milliseconds
		fractionComplete = 0;
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fractionComplete += delay / 1000;
				tp.progBar.setValue(fractionComplete);
			}
		};

		// progress bar timer
		javax.swing.Timer timer;
		timer = new javax.swing.Timer(delay, taskPerformer);
		timer.start();
		// solve the problem
		//solver.solve();
		solver.solve(problem);
		timer.stop();

		tp.progBar.setValue(tp.progBar.getMaximum());

		//Scoreboard scoreboard = solver.getScoreboard();
		ScoreBoard scoreboard = solver.getScoreBoard();
		System.out.println(scoreboard.toString());

		// rerun with the solution and look at results:
		//HashMap solutionMap = (HashMap) scoreboard.getBestSolutionMap();
		HashMap<Variable, Number> solutionMap = (HashMap<Variable, Number>) scoreboard.getBestSolution().getTrialPoint().getValueMap();
		//solver.setProxyFromMap(solutionMap);

		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(4);

		for (int i = 0; i < qPSs.size(); i++) {
			/*System.out.println(varQuadPSs[i].getName() + " = "
					+ varQuadPSs[i].getValue());
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^6");
			tp.quadTableModel.setValueAt(numberFormat.format(varQuadPSs[i]
					.getValue()), i, 3);*/
			System.out.println(varQuadPSs[i].getName() + " = " + varQuadPSs[i].getInitialValue());
			tp.quadTableModel.setValueAt(numberFormat.format(varQuadPSs[i].getInitialValue()), i, 3);

			// calculate new set points
			/*double errRatio = tp.designMap.get(qPSs.get(i)).doubleValue()
					/ varQuadPSs[i].getValue();*/
			double errRatio = tp.designMap.get(qPSs.get(i)).doubleValue() / varQuadPSs[i].getInitialValue();
			System.out.println("errRatio="+errRatio);	
			double newSetPt = Math.abs(errRatio * initVals[i]);
			System.out.println("newSetPt="+newSetPt);	
			tp.quadTableModel.setValueAt(numberFormat.format(newSetPt), i, 4);
		}
		tp.quadTableModel.fireTableDataChanged(); 
		tp.setQuadBtn.setEnabled(true);
	}

	public void updateModel() {
		// set updated quad value
		for (int i = 0; i < quads.size(); i++) {
			double quadVal = 0.;
			AcceleratorNode quad = quads.get(i);
			String quadPSId = quad2PSMap.get(quads.get(i).getId());

			for (int j = 0; j < qPSs.size(); j++) {
				if (varQuadPSs[j].getName().equals(quadPSId)) {
					//quadVal = ((Quadrupole) quad).getPolarity()
					//		* varQuadPSs[j].getValue();
					quadVal = ((Quadrupole) quad).getPolarity() * varQuadPSs[j].getInitialValue();
				}
			}

			scenario.setModelInput(quad,
					ElectromagnetPropertyAccessor.PROPERTY_FIELD, quadVal);
		}

		try {
			// myProbe.reset();
			scenario.resetProbe();
			scenario.resync();
		} catch (SynchronizationException e) {
			System.out.println(e);
		}

		try {
			// scenario.setStartElementId("Ring_Inj:Foil");
			scenario.run();
		} catch (ModelException e) {
			System.out.println(e);
		}

	}

	protected double calcError() {
		double error = 10000.;

		// get BPM measured phases
		double[] bpmXPhs = tp.xPhaseDiff;
		double[] bpmYPhs = tp.yPhaseDiff;

		// get the online model calculated BPM phase
		myProbe = (TransferMapProbe) scenario.getProbe();
		//traj = (TransferMapTrajectory) myProbe.getTrajectory();
		traj = myProbe.getTrajectory();

		// get the 1st BPM betatron phase as the reference
		TransferMapState state0 = traj.stateForElement(bpms
				.get(0));
		//double xPhase0 = state0.getBetatronPhase().getx();
		//double yPhase0 = state0.getBetatronPhase().gety();
		//double xTunes0 = traj.getTunes()[0];
		//double yTunes0 = traj.getTunes()[1];
		
        CalculationsOnRings     cmpRingParams = new CalculationsOnRings(traj);
		
		// CKA - This
		R3    vecPhase0 = cmpRingParams.ringBetatronPhaseAdvance();
		
		double xPhase0 = vecPhase0.getx();
		double yPhase0 = vecPhase0.gety();

		double sum = 0.;
		for (int i = 1; i < bpms.size(); i++) {
			TransferMapState state = traj
					.stateForElement(bpms.get(i));
			if (!tp.badBPMs.contains(new Integer(i))) {
			    // CKA - This
			    R3   vecPhase = cmpRingParams.computeBetatronPhase(state);
			    
			    double xPhase = vecPhase.getx() - xPhase0;
			    double yPhase = vecPhase.gety() - yPhase0;
			    
				//double xPhase = state.getBetatronPhase().getx() - xPhase0;
			//	double yPhase = state.getBetatronPhase().gety() - yPhase0;
				if (xPhase < 0.)
					xPhase = xPhase + 2. * Math.PI;
				if (yPhase < 0.)
					yPhase = yPhase + 2. * Math.PI;

				sum = sum + (bpmXPhs[i] - xPhase) * (bpmXPhs[i] - xPhase)
						+ (bpmYPhs[i] - yPhase) * (bpmYPhs[i] - yPhase);
			}
		}
			
		//System.out.println("xtunes=" + tp.xAvgTune+"     xmodel="+xTunes0);
		//System.out.println("ytunes=" + tp.yAvgTune+"     ymodel="+yTunes0);
		
		//sum=sum+(xTunes0-tp.xAvgTune)*(xTunes0-tp.xAvgTune)
		//           +(yTunes0-tp.yAvgTune)*(yTunes0-tp.yAvgTune);

		error = sum;

		System.out.println("error = " + error);

		return error;
	}

}
