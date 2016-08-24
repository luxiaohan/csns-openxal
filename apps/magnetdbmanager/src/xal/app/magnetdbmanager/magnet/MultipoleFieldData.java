package xal.app.magnetdbmanager.magnet;

public class MultipoleFieldData implements Comparable<MultipoleFieldData>, Cloneable {

	private int order;
	
	/** 磁通，*10E-08 V.S */
	private double phi;
	
	/** degree */
	private double angle;
	
	private double bn;
	private double an;
	
	/** BnLeff = coe * sqrt(bn*bn + an*an) * Leff
	 *  unit: T m
	 **/
	private double BnLeff;

	
	public MultipoleFieldData(int order, double angle, double phi, double bn, double an, double BnLeff){
		this.order = order;
		this.angle = angle;
		this.phi = phi;
		this.bn = bn;
		this.an = an;
		this.BnLeff = BnLeff;
	}
	
	public MultipoleFieldData(int order, double bn, double an){
		this.order = order;
		this.angle = Math.atan(an/bn);
		this.phi = 0;
		this.bn = bn;
		this.an = an;
		this.BnLeff = 0;
	}
	
	public MultipoleFieldData(MultipoleFieldData o){
		this.order = o.order;
		this.angle = o.angle;
		this.phi = o.phi;
		this.bn = o.bn;
		this.an = o.an;
		this.BnLeff = o.BnLeff;
	}
	
	public MultipoleFieldData clone(){
		MultipoleFieldData copy = null;
		try{
			copy = (MultipoleFieldData) super.clone();
		} catch(CloneNotSupportedException e) {
			   e.printStackTrace();
	    }
		
		copy.order = order;
		copy.angle = angle;
		copy.phi = phi;
		copy.bn = bn;
		copy.an = an;
		copy.BnLeff = BnLeff;
		
		return copy;
	}
	
	@Override
	public int compareTo(MultipoleFieldData o) {
		if(o == null) throw new ClassCastException("Can't compare.");
		
		if(order == o.order){
			int flag = compare(angle, o.angle);
			if(flag == 0){
				flag = compare(phi, o.phi);
				if(flag == 0){
					flag = compare(bn, o.bn);
					if(flag == 0){
						flag = compare(an, o.an);
						if(flag == 0) return compare(BnLeff, o.BnLeff);
					}
				}
			}
			return flag;
		}

		return order - o.order;
	}
	
	private int compare(double a, double b){
		if(a < b) return -1;
		else if(a > b) return 1;
		else return 0;
	}
	
	public int getOrder() {
		return order;
	}

	public double getPhi() {
		return phi;
	}

	public double getAngle() {
		return angle;
	}

	public double getBn() {
		return bn;
	}

	public double getAn() {
		return an;
	}
	
	public double getBnLeff() {
		return BnLeff;
	}

	public String toString(){
		String value = "";
		value = value + this.getOrder() + ": " + this.getAngle() + ", " + this.getPhi() + ", " + this.getBn() + ", " + this.getAn() + ", " + this.getBnLeff();
		return value;
	}
	
	public boolean equals(final MultipoleFieldData o){
		if(this.getOrder() == o.getOrder() && this.getAngle() == o.getAngle() && this.getPhi() == o.getPhi() && this.getBn() == o.getBn() && this.getAn() == o.getAn() && this.BnLeff == o.getBnLeff()) return true;
		return false;
	}
}
