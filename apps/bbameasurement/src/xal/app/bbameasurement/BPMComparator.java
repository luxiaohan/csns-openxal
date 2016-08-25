package xal.app.bbameasurement;

import java.util.*;

import xal.smf.AcceleratorSeq;
import xal.smf.impl.BPM;

/**
 * BPMComparator
 *
 * @author  tap
 * @since Oct 04, 2004
 */
public class BPMComparator implements Comparator<BPM> {
	/** accelerator sequence used to determine the BPM positions */
	final protected AcceleratorSeq _sequence;
	
	
	/**
	 * Constructor
	 */
	public BPMComparator( final AcceleratorSeq sequence ) {
		_sequence = sequence;
	}
	
	
	/**
	 * Compare two BPM based upon the position of each BPM
	 * within the selected sequence.
	 *
	 * @param  bpmAgent1  the first BPM agent in the comparison
	 * @param  bpmAgent2  the second BPM agent in the comparison
	 * @return       0 for equal positions, -1 if the first position is greater than the second and 1 otherwise.
	 */
	public int compare( final BPM bpm1, final BPM bpm2 ) {
		double position1 = bpm1.getPosition( );
		double position2 = bpm2.getPosition( );

		return ( position1 == position2 ) ? 0 : ( ( position1 > position2 ) ? 1 : -1 );
	}


	/**
	 * Test whether the specified comparator is equal to this instance.
	 *
	 * @param  comparator  the comparator to compare for equality with this instance
	 * @return             true if the comparator is equal to this instance and false if not
	 */
	public boolean equals( final Object comparator ) {
		return comparator == this;
	}
}

