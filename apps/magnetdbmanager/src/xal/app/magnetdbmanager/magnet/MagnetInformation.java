package xal.app.magnetdbmanager.magnet;

import java.util.ArrayList;

public class MagnetInformation implements Cloneable{
	private String ID = "";
	private String PID = "";
	private String Sort_ID = "QUADRUPOLE";
	private String Type_ID = "";
	
	private double Length = 0;
	
	private String Aperture_Type = "CIRCLE";
	private String Aperture = "0";
	private ArrayList<Double> ApertureDataList;
	
	private double Angle = 0;
	private double E1 = 0;
	private double E2 = 0;
	
	private String Description = "";
	
	public MagnetInformation(String id, String pid, String sort){
		ID = id;
		PID = pid;
		Sort_ID = sort;
		
		ApertureDataList = new ArrayList<Double>();
		ApertureDataList.add(new Double(Double.NaN));
	}
	
	public MagnetInformation(MagnetInformation mi){
		ID = mi.getID();
		PID = mi.getPID();
		Sort_ID = mi.getSort_ID();
		Type_ID = mi.getType_ID();
		Length = mi.getLength();
		Aperture_Type = mi.getAperture_Type();
		Aperture = mi.getAperture();
		ApertureDataList = new ArrayList<Double>();
		for(double aper : mi.getApertureDataList()){
			ApertureDataList.add(aper);
		}
		
		Angle = mi.getAngle();
		E1 = mi.getE1();
		E2 = mi.getE2();
	}
	
	public MagnetInformation clone(){
		MagnetInformation copy = null;
		try{
			copy = (MagnetInformation) super.clone();
		} catch(CloneNotSupportedException e) {
			   e.printStackTrace();
	    }
		
		copy.ID = ID;
		copy.PID = PID;
		copy.Sort_ID = Sort_ID;
		copy.Type_ID = Type_ID;
		copy.Length = Length;
		copy.Aperture_Type = Aperture_Type;
		copy.Aperture = Aperture;
		copy.ApertureDataList = new ArrayList<Double>();
		for(double aper : ApertureDataList){
			copy.ApertureDataList.add(aper);
		}
		copy.Angle = Angle;
		copy.E1 = E1;
		copy.E2 = E2;
		
		return copy;
	}

	public String getID() {
		return ID;
	}
	
	public void setID(String id) {
		ID = id;
	}
	
	public String getPID() {
		return PID;
	}
	
	public void setPID(String pid) {
		PID = pid;
	}
	
	public void setSort_ID(String sort_id){
		Sort_ID = sort_id;
	}
	
	public String getSort_ID(){
		return Sort_ID;
	}
	
	public String getType_ID() {
		return Type_ID;
	}

	public void setType_ID(String type_id) {
		Type_ID = type_id;
	}
	
	public double getLength() {
		return Length;
	}

	public void setLength(double length) {
		Length = length;
	}

	public String getAperture_Type() {
		return Aperture_Type;
	}

	public void setAperture_Type(String aperture_type) {
		Aperture_Type = aperture_type;
	}
	
	public String getAperture(){
		return Aperture;
	}
	
	public ArrayList<Double> getApertureDataList(){
		return ApertureDataList;
	}
	
	public boolean setAperture(String aperture){
		ApertureDataList = MagnetInformation.StringToDoubleList(aperture);
		
		if(ApertureDataList == null) return false;

		Aperture = MagnetInformation.DoubleListToString(ApertureDataList);
		return true;
	}

	public double getAngle() {
		return Angle;
	}

	public void setAngle(double angle) {
		Angle = angle;
	}

	public double getE1() {
		return E1;
	}

	public void setE1(double e1) {
		E1 = e1;
	}

	public double getE2() {
		return E2;
	}

	public void setE2(double e2) {
		E2 = e2;
	}
	
	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}
	
	public boolean equals(final MagnetInformation mi){
		if(ID.equalsIgnoreCase(mi.getID()) &&
				PID.equalsIgnoreCase(mi.getPID()) &&
				Sort_ID.equalsIgnoreCase(mi.getSort_ID()) &&
				Type_ID.equalsIgnoreCase(mi.getType_ID()) &&
				Length == mi.getLength() &&
				Aperture_Type.equalsIgnoreCase(mi.getAperture_Type()) &&
				Aperture.equalsIgnoreCase(mi.getAperture()) &&
				Angle == mi.getAngle() &&
				E1 == mi.getE1() &&
				E2 == mi.getE2() &&
				Description.equalsIgnoreCase(mi.getDescription()))
			return true;
		return false;
	}
	
	public static String DoubleListToString(ArrayList<Double> list){
		String result = "";
		if(list == null) return result;

		for(int i=0; i<list.size(); i++){
			result = result + list.get(i);
			if(i < list.size() - 1) result = result + ", ";
		}
		return result;
	}
	
	public static ArrayList<Double> StringToDoubleList(String data){
		ArrayList<Double> list = new ArrayList<Double>();
		
		if(data == null || data.isEmpty()) return list;
		
		String[] str = data.trim().split("[,; \t]");
		
		if(str.length == 0) return list;
		
		
		for(int i=0; i<str.length; i++){
			if(str[i].isEmpty()) continue;
			list.add(Double.parseDouble(str[i]));
		}

		if(list.isEmpty() || list.size() == 0) return null;
		return list;
	}
	
	public String toString(){
		String output = "";
		output = output + "ID: " + this.getID() + "\n";
		output = output + "PID: " + this.getPID() + "\n";
		output = output + "Sort_ID: " + this.getSort_ID() + "\n";
		output = output + "Type_ID: " + this.getType_ID() + "\n";
		output = output + "Length (m): " + this.getLength() + "\n";
		output = output + "Aperture_Type: " + this.getAperture_Type() + "\n";
		output = output + "Aperture (mm): " + this.getAperture() + "\n";
		
		if(this.getSort_ID().equalsIgnoreCase("DIPOLE")
				|| this.getSort_ID().equalsIgnoreCase("RBEND")
				|| this.getSort_ID().equalsIgnoreCase("SBEND")){
			output = output + "Angle (mrad): " + this.getAngle() + "\n";
			output = output + "E1 (mrad): " + this.getE1() + "\n";
			output = output + "E2 (mrad): " + this.getE2() + "\n";
			
		}
		output = output + "Description: " + this.getDescription() + "\n";
		
		return output;
	}

}
