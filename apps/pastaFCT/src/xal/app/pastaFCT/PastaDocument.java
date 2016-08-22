/*
 * PastaDocument.java
 *
 * Created on June 14, 2004
 * Revised by Na Wang for the application with FCT on March, 2013
 */

package xal.app.pastaFCT;

/*import gov.sns.ca.ChannelFactory;
import gov.sns.tools.scan.SecondEdition.MeasuredValue;
import gov.sns.tools.scan.SecondEdition.ScanVariable;
import gov.sns.xal.smf.AcceleratorNode;
import gov.sns.xal.smf.AcceleratorSeq;
import gov.sns.xal.smf.AcceleratorSeqCombo;
import gov.sns.xal.smf.application.AcceleratorDocument;
import gov.sns.xal.smf.impl.CurrentMonitor;
import gov.sns.xal.smf.impl.FCT;
import gov.sns.xal.smf.impl.RfCavity;
import gov.sns.xal.smf.impl.qualify.KindQualifier;*/

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import xal.ca.ChannelFactory;
import xal.extension.application.smf.AcceleratorDocument;
import xal.extension.scan.MeasuredValue;
import xal.extension.scan.ScanVariable;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.*;
import xal.smf.impl.qualify.KindQualifier;

/**
 * This class contains the primary internal working objects of the 
 * pasta application. E.g. which parts of the accelerator are being used.
 *
 * @author  jdg
 */
public class PastaDocument extends AcceleratorDocument {

    /** the helper class to save and open documents to/from xml files */
    private SaveOpen saveOpen;

    /** the first BPM to use in gathering beam info */
    private FCT BPM1;
    /** the second BPM to use in gathering beam info */
    private FCT BPM2;	
    /** The RF cavity to analyze */
    private RfCavity theCavity;
    /**
     * return the cavity design amplitude (MV/m)
     * Note - since we iterate the design values during the matching proceedure,
     * this should be grabed before matching starts
     **/
    private double theDesignAmp;
    /** the cavity design phase (deg)*/
    private double theDesignPhase;
    
    /** The BCM to validate with */
    protected CurrentMonitor theBCM;

    /** the collection of possible BPMs */
    private Collection<AcceleratorNode> theBPMs = new ArrayList<AcceleratorNode>();
     /** the collection of possible cavities */
    private Collection<AcceleratorNode> theCavities = new ArrayList<AcceleratorNode>();
    /** list of BCMs to use as validator */
    private Collection<AcceleratorNode> theBCMs;
  
    /** the parametric scan variable (cavity amplitude) */ 
    private ScanVariable scanVariableParameter = null;
    /** the scan variable (cavity phase) */ 
    private ScanVariable scanVariable = null;
    /** container for the measured variables (BPM phases + amplitudes) */ 
    private Vector measuredValuesV;
    /** the measured quantities for the Scan */
    private MeasuredValue BPM1PhaseMV, BPM1AmpMV, BPM2PhaseMV, BPM2AmpMV;
    
    /** make a copy of the selctedSequence so other classes in this package can use it */
    protected AcceleratorSeq theSequence;
    
    /** container of scan information) */
    protected ScanStuff scanStuff;
 
   /** container of analysis information) */
    protected AnalysisStuff analysisStuff;
    
    /**
     * an amount to shift the DTL phase by for analysis, to avoid +-180 deg 
     * wrapping which complicates the analysis
     **/
    protected double DTLPhaseOffset = 0.;
    
    /** an amount to shift the BPM phase difference by for analysis + measurement, 
    * to avoid +-180 deg  wrapping which complicates the analysis */
    protected double BPMPhaseDiffOffset = 0.;    
    
    /** this array holds Booleans indicating whether or not to use the scan 
    * number corresponding to the array index (starts at 0) 
    * in the matching analysis */
    protected ArrayList<Boolean> useScanInMatch = new ArrayList<Boolean>();
    
    /** this is an arbitrary shift to add to the model calculated BPM phase difference. It should be 0 - use cauutiously (deg) */
    protected double fudgePhaseOffset = 0.;
    
    /** a flag to use the fudgePhaseOffset fudge factor as a matching variable */
    protected boolean varyFudgePhaseOffset = false;
        
    /** workaround to avoid jca context initialization exception */
    static{
	ChannelFactory.defaultFactory().init();
    }
   
   
    /** Create a new empty document */
    public PastaDocument() {
    	scanStuff = new ScanStuff(this);
    	analysisStuff = new AnalysisStuff(this);
    	saveOpen = new SaveOpen(this);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public PastaDocument(java.net.URL url) {
        this();
        if ( url == null )  {
    	    return;
    	}
        else {
            System.out.println("Opening document: " + url.toString());
            setSource(url);
        }
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new PastaWindow(this);

    	// now that we have a window, let's read in the input file + set it up
    	if(getSource() != null ) saveOpen.readSetupFrom(getSource());
    }    

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
    	saveOpen.saveTo(url);
    	setHasChanges(false);	    
    }
    
    
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    protected PastaWindow myWindow() {
        return (PastaWindow)mainWindow;
    }
    
    /**
     * Handle the accelerator changed event by displaying the elements of the 
     * accelerator in the main window.
     */
    public void acceleratorChanged() {
        System.out.println("accelerator path: " + acceleratorFilePath);
    }
    
    /**
     * Handle the selected sequence changed event by displaying the elements of the 
     * selected sequence in the main window.
     */
    public void selectedSequenceChanged() {
    	theSequence = selectedSequence;
	
        if ( selectedSequence == null ) return;
	
        theBPMs.clear();
    	theBPMs = selectedSequence.getAllNodesOfType("fct");
//    	theCavities  = selectedSequence.getAllNodesOfType("rfcavity");
    	/*
    	if ((selectedSequence.getClass()).equals(AcceleratorSeqCombo.class)) 
    	{
    		KindQualifier kq = new KindQualifier("rfcavity");
    		cavs2 = ((AcceleratorSeqCombo) selectedSequence).getConstituentsWithQualifier(kq);
    		Iterator itr = cavs2.iterator();
    		while (itr.hasNext())
    			theCavities.add(itr.next());
    	}
    	else {
    		if(selectedSequence.isKindOf("rfcavity"))
    			theCavities.add(selectedSequence);
    	}
    	*/
    	KindQualifier kq = new KindQualifier("rfcavity");
    	theCavities.clear();
    	theCavities.addAll(selectedSequence.getAllInclusiveNodesWithQualifier(kq));

    	
    	// get a list of BCMs in the linac:
    	AcceleratorSeqCombo seq2 = getAccelerator().getComboSequence("MEBT-DTL");
    	theBCMs = seq2.getAllNodesOfType("BCM");
    		
    	myWindow().updateSelectionLists();
    	
    	analysisStuff.modelReady = false;
    	
    	/** set the analysis model for the selected sequence */
    }
    
    public Collection<AcceleratorNode> getFCTs(){
        return theBPMs;
    }
    
    public Collection<AcceleratorNode> getBCMs(){
        return theBCMs;
    }
    
    /** the first FCT to use in gathering beam info */
    public FCT getFCT1(){
        return BPM1;
    }
    
    public void setFCT1(FCT fct){
        BPM1 = fct;
    }
    
    /** the second BPM to use in gathering beam info */
    public FCT getFCT2(){
        return BPM2;
    }
    
    public void setFCT2(FCT fct){
        BPM2 = fct;
    }
    
    public RfCavity getCavity(){
        return theCavity;
    }
    
    public void setCavity(RfCavity cavity){
        theCavity = cavity;
        theDesignAmp = theCavity.getDfltCavAmp();
        theDesignPhase = theCavity.getDfltCavPhase();
        System.out.println(theDesignAmp);
        System.out.println(theDesignPhase);
        System.out.println("===========================");
    }
    
    /**
     * return the cavity design amplitude (MV/m)
     * Note - since we iterate the design values during the matching proceedure,
     * this should be grabed before matching starts
     **/
    public double getCavityDesignAmp(){
        if(theCavity == null) return Double.NaN;
        return theDesignAmp;
    }
    
    /** the cavity design phase (deg)*/
    public double getCavityDesignPhase(){
        if(theCavity == null) return Double.NaN;
        return theDesignPhase;
    }
    
    public Collection<AcceleratorNode> getCavities(){
        return theCavities;
    }
}
