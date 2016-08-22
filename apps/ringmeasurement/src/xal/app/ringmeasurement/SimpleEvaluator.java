package xal.app.ringmeasurement;

import java.util.List;
import xal.extension.solver.*;

public class SimpleEvaluator implements Scorer {

    CalcQuadSettings cqs;
    public SimpleEvaluator(CalcQuadSettings m) {
        cqs = m;
    }
    
	/* (non-Javadoc)
	 * @see gov.sns.tools.optimizer.Scorer#score()
	 */
	/*public double score() {
		cqs.updateModel();
		double myScore = cqs.calcError();

		return myScore;
	}*/
	public double score(Trial trial, List<Variable> variables) {
		cqs.updateModel();
		double myScore = cqs.calcError();

		return myScore;
	}//liyong modify

	public boolean accept() {
		return true;
	}
}
