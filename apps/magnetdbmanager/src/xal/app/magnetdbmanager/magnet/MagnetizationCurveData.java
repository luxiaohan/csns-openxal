package xal.app.magnetdbmanager.magnet;


public class MagnetizationCurveData implements Comparable<MagnetizationCurveData>, Cloneable {
	private int order;
	private double I;
	private double BL;
	
	public MagnetizationCurveData(int order, double I, double BL){
		this.order = order;
		this.I = I;
		this.BL = BL;
	}
	
	/**
	 * Clone a new class from a class given.
	 *  
	 * @param mecd
	 */
	public MagnetizationCurveData(MagnetizationCurveData mecd){
		this.order = mecd.getOrder();
		this.I = mecd.getI();
		this.BL = mecd.getBL();
	}
	
	public MagnetizationCurveData clone(){
		MagnetizationCurveData copy = null;
		try{
			copy = (MagnetizationCurveData) super.clone();
		} catch(CloneNotSupportedException e) {
			   e.printStackTrace();
	    }
		
		copy.order = order;
		copy.I = I;
		copy.BL = BL;
		
		return copy;
	}
	
	public int getOrder() {
		return order;
	}
	
	public double getI() {
		return I;
	}
	
	public double getBL() {
		return BL;
	}

	@Override
	public int compareTo(MagnetizationCurveData o) {
		if(o == null) throw new ClassCastException("Can't compare.");
		
		return this.getOrder() - o.getOrder();
	}

	public String toString(){
		String value = "";
		value = value + this.getOrder() + ": " + this.getI() + ", " + this.getBL();
		return value;
	}
	
	public boolean equals(final MagnetizationCurveData mecd){
		if(this.getOrder() == mecd.getOrder() && this.getI() == mecd.getI() && this.getBL() == mecd.getBL()) return true;
		return false;
	}
}
