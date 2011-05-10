package xal.model.probe.traj;

import xal.tools.RealNumericIndexer;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.IArchive;
import xal.model.probe.Probe;
import xal.model.xml.ParsingException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the history for a probe.  Saves <code>ProbeState</code> objects,
 * each of which reflects the state of the <code>Probe</code> at a particular
 * point in time.
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 * (cosmetic enhancements)
 * @version $id:
 * 
 */
public abstract class Trajectory implements IArchive {
	
    /*
     * Global Constants
     */

    // *********** I/O Support

    /** XML element tag for trajectory */
    public final static String TRAJ_LABEL = "trajectory";
    
    /** XML element tag for trajectory concrete type */
    private final static String TYPE_LABEL = "type";
    
    /** XML element tag for time stamp data */
    private static final String TIMESTAMP_LABEL = "timestamp";
    
    /** XML element tag for user comment data */
    private static final String DESCRIPTION_LABEL = "description";



    /*
     *  Local Attributes
     */
     
    /** any user comments regard the trajectory */
    private String description = "";
    
    /** the history of probe states along the trajectory */
    private RealNumericIndexer<ProbeState> _history;
    
    /** time stamp of trajectory */
    private Date timestamp = new Date();





    // Factory Methods ========================================================


    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return
     * an instance of the appropriate Trajectory species.
     * 
     * @param container <code>DataAdaptor</code> to read a Trajectory from
     * @return a Trajectory for the contents of the DataAdaptor
     * @throws ParsingException error encountered reading the DataAdaptor
     */
    public static Trajectory readFrom(DataAdaptor container)
        throws ParsingException {
        DataAdaptor daptTraj = container.childAdaptor(Trajectory.TRAJ_LABEL);
        if (daptTraj == null)
            throw new ParsingException("Trajectory#readFrom() - DataAdaptor contains no trajectory node");
        
        String type = container.stringValue(Trajectory.TYPE_LABEL);
        Trajectory trajectory;
        try {
            Class<?> trajectoryClass = Class.forName(type);
            trajectory = (Trajectory) trajectoryClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParsingException(e.getMessage());
        }
        trajectory.load(daptTraj);
        return trajectory;
    }





    // ************* abstract protocol specification

    /**
     * Creates a new <code>ProbeState</code> object with the proper type for the trajectory.
     * 
     * @return      new, empty <code>ProbeState</code> object
     */
    protected abstract ProbeState newProbeState();

    /**
     * Override this method in subclasses to add subclass-specific properties to
     * the output.  Subclass implementations should call super.addPropertiesTo
     * so that superclass implementations are executed.
     * 
     * @param container the <code>DataAdaptor</code> to add properties to
     */
    protected void addPropertiesTo(DataAdaptor container) {}

    /**
     * Allow subclasses to read subclass-specific properties from the <code>
     * DataAdaptor</code>.  Implementations should call super.readPropertiesFrom
     * to ensure that superclass implementations are executed.
     * 
     * @param container <code>DataAdaptor</code> to read properties from
     */
    protected void readPropertiesFrom(DataAdaptor container) throws ParsingException {}







    // ************ initialization
    
    /**
     * Create a new, empty <code>Trajectory</code> object.
     */
    public Trajectory() {
		_history = new RealNumericIndexer<ProbeState>();
    }
	

    /**
     * Set the user comment string
     * 
     *  @param  strDescr    user comment string
     */
    public void setDescription(String strDescr) {   description = strDescr; };

    /**
     * Set the time stamp of the trajectory.
     * 
     * @param lngTimeStamp  number of milliseconds since January 1, 1970 GMT  
     */
    public void setTimestamp(long lngTimeStamp) { timestamp = new Date(lngTimeStamp);  }





    

    // ************* Trajectory Operations

    /**
     * Captures the specified probe's current state to a <code>ProbeState</code> object
     * then saves it to the trajectory.  State goes at the tail of the trajectory list.
     * 
     *  @param  probe   target probe object
     */
    public void update(Probe probe) {
        ProbeState state = probe.createProbeState();
        saveState(state);
    }

    /**
     * Save the <code>ProbeState</code> object directly to the trajectory at the tail.
     * @param state     new addition to trajectory
     */
    public void saveState( final ProbeState state ) {
        _history.add( state.getPosition(), state );
    }
    
    /**
     * Remove the last state from the trajectory and return it.
     * 
     * @return  the most recent <code>ProbeState</code> in the history
     */
    public ProbeState   popLastState()  {
        return _history.remove( _history.size() - 1 );
    }



    // ************* Trajectory Data

	
    /**
     * Return the user comment associated with this trajectory.
     * 
     * @return user comment (meta-data)
     */
    public String getDescription() {return description; }


    /**
     * Return the time stamp of the trajectory object
     * 
     * @return  trajectory time stamp
     */
    public Date getTimestamp() { return timestamp; }

    
    
    /**
     * Return an Iterator over the iterator's states.
     */
    public Iterator<ProbeState> stateIterator() {
        return _history.iterator();
    }

    /**
     * Return the number of states in the trajectory.
     * 
     * @return the number of states
     */
    public int numStates() {
        return _history.size();
    }


    /**
     * Returns the probe's initial state or null if there is none.
     * 
     * @return the probe's initial state or null
     */
    public ProbeState initialState() {
        return stateWithIndex(0);
    }
	
   /**
     * Returns the probe's final state or null if there is none.
     * 
     * @return the probe's final state or null
     */
    public ProbeState finalState() {
        return stateWithIndex(numStates()-1);
    }
    
    
	/**
	 * Get the list of states.
	 *
	 * @return a new list of this trajectory's states
	 */
	protected List<ProbeState> getStates() {
		return _history.toList();
	}
	

	
    /**
     * Returns the probe state at the specified position.  Returns null if there
     * is no state for the specified position.
     */
    public ProbeState stateAtPosition(double pos) {
        for(ProbeState state : _history) {
            if (state.getPosition() == pos)
                return state;
        }
        return null;
    }
	
	
	/**
	 * Get the state that is closest to the specified position
	 * @param position the position for which to find a state
	 * @return the state nearest the specified position
	 */
	public ProbeState stateNearestPosition( final double position ) {
		final int index = _history.getClosestIndex( position );
		return _history.size() > 0 ? _history.get( index ) : null;
	}
	

    /**
     * Returns the states that fall within the specified position range, inclusive.
     * @param low lower bound on position range
     * @param high upper bound on position range
     * @return an array of <code>ProbeState</code> objects whose position falls
     * within the specified range
     */
    public ProbeState[] statesInPositionRange( final double low, final double high ) {
		final int[] range = _history.getIndicesWithinLocationRange( low, high );
		if ( range != null ) {
			final List<ProbeState> result = new ArrayList<ProbeState>( range[1] - range[0] + 1 );
			for ( int index = range[0] ; index <= range[1] ; index++ ) {
				result.add( _history.get( index ) );
			}
			final ProbeState[] resultArray = new ProbeState[result.size()];
			return result.toArray( resultArray );
		}
		else {
			return new ProbeState[0];
		}
    }
	
	
	/**
	 * Get the probe state for the specified element ID.
	 * @param elemID the name of the element for which to get the probe state
	 * @return The first probe state for the specified element.
	 */
	public ProbeState stateForElement( final String elemID ) {
		return statesForElement( elemID )[0];
	}

	
    /**
     * Returns the states associated with the specified element.
     * @param strElemId    the name of the element to search for
     * @return             an array of <code>ProbeState</code> objects for that element
     */
    public ProbeState[] statesForElement(String strElemId) {
        List<ProbeState> result = new ArrayList<ProbeState>();
        Iterator<ProbeState> it = stateIterator();
        while (it.hasNext()) {
            ProbeState state = it.next();
            if ((state.getElementId().equals(strElemId))
            	||(state.getElementId().equals(strElemId+"y"))) {
                result.add(state);
            }
        }
        ProbeState[] resultArray = new ProbeState[result.size()];
        return result.toArray(resultArray);
    }

    /**
     * Returns an array of the state indices corresponding to the specified element.
     * @param element name of element to search for
     * @return an array of integer indices corresponding to that element
     */
    public int[] indicesForElement(String element) {
        List<Integer> indices = new ArrayList<Integer>();
        int c1 = 0;
        Iterator<ProbeState> it = stateIterator();
        while (it.hasNext()) {
            ProbeState state = it.next();
            if ((state.getElementId().equals(element)
            		|| state.getElementId().equals(element+"y"))) {
                indices.add(c1);
            }
            c1++;
        }
        int[] resultArray = new int[indices.size()];
        int c2 = 0;
        for (Iterator<Integer> indIt = indices.iterator(); indIt.hasNext(); c2++) {
            resultArray[c2] = indIt.next();
        }
        return resultArray;
    }
    
    /**
     * Returns the state corresponding to the specified index, or null if there is none.
     * @param i index of state to return
     * @return state corresponding to specified index
     */
    public ProbeState stateWithIndex(int i) {
        try {
            return _history.get(i); 
        } catch (IndexOutOfBoundsException e) {
            return null;    
        }
    }
		



    // *********** debugging


    /**
     * Store a textual representation of the trajectory to a string
     * @return     trajectory contents in string form 
     */
    @Override
    public String toString() {
    StringBuffer buf = new StringBuffer();
        buf.append("Trajectory: " + getClass().getName() + "\n");
        buf.append("Time: " + getTimestamp() + "\n");
        buf.append("Description: " + getDescription() + "\n");
        buf.append("States: " + _history.size() + "\n");
        Iterator<ProbeState> it = stateIterator();
        while (it.hasNext()) {
            buf.append(it.next().toString() + "\n");
        }
        return buf.toString();
    }






    // Persistence Methods ====================================================


    /**
     * Adds a representation of this Trajectory and its state history to the supplied <code>DataAdaptor</code>.
     * @param container <code>DataAdaptor</code> in which to add <code>Trajectory</code> data
     */
    public void save(DataAdaptor container) {
        DataAdaptor trajNode = container.createChild(TRAJ_LABEL);
        trajNode.setValue(TYPE_LABEL, getClass().getName());
        trajNode.setValue(TIMESTAMP_LABEL, new Double(getTimestamp().getTime()));
        if (getDescription().length() > 0)
            trajNode.setValue(DESCRIPTION_LABEL, getDescription());
        addPropertiesTo(trajNode);
        addStatesTo(trajNode);
    }

    /**
     * Load the current <code>Trajectory</code> object with the state history
     * information in the <code>DataAdaptor</code> object.
     * 
     *  @param  container   <code>DataAdaptor</code> from which state history is extracted
     * 
     *  @exception  DataFormatException     malformated data in <code>DataAdaptor</code>
     */
    public void load(DataAdaptor container) throws DataFormatException {
//        DataAdaptor daptTraj = container.childAdaptor(Trajectory.TRAJ_LABEL);
//        if (daptTraj == null)
//            throw new DataFormatException("Trajectory#load() - DataAdaptor contains no trajectory node");
        DataAdaptor daptTraj = container;

        long time =
            new Double(daptTraj.doubleValue(TIMESTAMP_LABEL)).longValue();
        setTimestamp(time);
        setDescription(daptTraj.stringValue(DESCRIPTION_LABEL));
        try {
            readPropertiesFrom(daptTraj);
            readStatesFrom(daptTraj);
        } catch (ParsingException e) {
            e.printStackTrace();
            throw new DataFormatException(
                "Exception loading from adaptor: " + e.getMessage());
        }
    }





    // Support Methods ========================================================

    /**
     * Iterates over child nodes, asking the concrete Trajectory subclass to
     * create a <code>ProbeState</code> of the appropriate species, initialized
     * from the contents of the supplied <code>DataAdaptor</code>
     * 
     * @param container <code>DataAdaptor</code> containing the child state nodes
     */
    private void readStatesFrom(DataAdaptor container)
        throws ParsingException {
        Iterator<? extends DataAdaptor> childNodes = container.childAdaptors().iterator();
        while (childNodes.hasNext()) {
            DataAdaptor childNode = childNodes.next();
            if (!childNode.name().equals(ProbeState.STATE_LABEL)) {
                throw new ParsingException(
                    "Expected state element, got: " + childNode.name());
            }
            ProbeState state = newProbeState();
            state.load(childNode);
            saveState(state);
        }
    }

    /**
     * Save the current trajectory information in the proper trajectory format to the target <code>DataAdaptor</code> object.
     * @param container     <code>DataAdaptor</code> to receive trajectory history
     */
    private void addStatesTo(DataAdaptor container) {
        Iterator<ProbeState> it = stateIterator();
        while (it.hasNext()) {
            ProbeState ps = it.next();
            ps.save(container);
        }
    }

}