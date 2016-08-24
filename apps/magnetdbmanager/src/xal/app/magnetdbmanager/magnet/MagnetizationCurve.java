package xal.app.magnetdbmanager.magnet;


import java.util.ArrayList;
import java.util.List;

import xal.tools.data.DataAdaptor;


public class MagnetizationCurve implements Cloneable{
	private String ID = "";
	private double EffectiveLength = 0;
	private double Std_Current_Min = 0;
	private double Std_Current_Max = 0;
	private int GroupID = -1;
	private String Description = "";
	private ArrayList<MagnetizationCurveData> MCurveData = new ArrayList<MagnetizationCurveData>();
	
	private MCFitResult _ItoBFitResult = null;
	private MCFitResult _BtoIFitResult = null;
	
	public MagnetizationCurve(String id){
		ID = id;
	}
	
	public MagnetizationCurve clone(){
		MagnetizationCurve copy = null;
		try{
			copy = (MagnetizationCurve) super.clone();
		} catch(CloneNotSupportedException e) {
			   e.printStackTrace();
	    }
		
		copy.ID = ID;
		copy.EffectiveLength = EffectiveLength;
		copy.Std_Current_Min = Std_Current_Min;
		copy.GroupID = GroupID;
		copy.Description = Description;
		copy.MCurveData = new ArrayList<MagnetizationCurveData>();
		for(MagnetizationCurveData mecd : MCurveData){
			copy.MCurveData.add(mecd.clone());
		}
		
		if(_ItoBFitResult != null) copy._ItoBFitResult = _ItoBFitResult.clone();
		else _ItoBFitResult = null;
		
		if(_BtoIFitResult != null) copy._BtoIFitResult = _BtoIFitResult.clone();
		else _BtoIFitResult = null;
		
		return copy;
	}
	
	public String getID() {
		return ID;
	}
	
	public void setID(String iD) {
		ID = iD;
	}
	
	public double getEffectiveLength() {
		return EffectiveLength;
	}
	
	public void setEffectiveLength(double effectiveLength) {
		if(EffectiveLength != effectiveLength) clearFitResult();
		else return;
		
		EffectiveLength = effectiveLength;
	}
	
	public double getStd_Current_Min(){
		return Std_Current_Min;
	}
	
	public void setStd_Current_Min(double current){
		Std_Current_Min = current;
	}
	
	public double getStd_Current_Max(){
		return Std_Current_Max;
	}
	
	public void setStd_Current_Max(double current){
		Std_Current_Max = current;
	}
	
	public int getGroupID() {
		return GroupID;
	}
	
	public void setGroupID(Integer groupID) {
		GroupID = groupID;
	}
	
	public String getDescription() {
		return Description;
	}
	
	public void setDescription(String description) {
		Description = description;
	}
	
	public ArrayList<MagnetizationCurveData> getMagnetizationCurveDatas() {
		return MCurveData;
	}
	
	public boolean setMagnetizationCurve(ArrayList<MagnetizationCurveData> mcdata) {
		if(mcdata == null) return false;
		
		MCurveData = mcdata;
		clearFitResult();
		return true;
	}

	public MagnetizationCurveData getMagnetizationCurveData(int i){
		if(i < MCurveData.size()){
			return MCurveData.get(i);
		}
		return null;
	}
	
	public MCFitResult getItoBFitResult(){
		return _ItoBFitResult;
	}
	
	public void setItoBFitResult(MCFitResult itobl){
		_ItoBFitResult = itobl;
	}
	
	public MCFitResult getBtoIFitResult(){
		return _BtoIFitResult;
	}
	
	public void setBtoIFitResult(MCFitResult bltoi){
		_BtoIFitResult = bltoi;
	}
	
	public String MagnetizationCurveDataToString(){
		String text = "";
		
		for(int i=0; i<MCurveData.size(); i++){
			text = text + MCurveData.get(i).getI() + ", " + MCurveData.get(i).getBL() + "\n";
		}
		
		return text;
	}
	
	public boolean addMagnetizationCurveData(MagnetizationCurveData curveData){
		if(curveData == null) return false;
		MCurveData.add(curveData);
		clearFitResult();
		
		return true;
	}
	
	public void clearFitResult(){
		if(_ItoBFitResult != null) _ItoBFitResult.clear();
		if(_BtoIFitResult != null) _BtoIFitResult.clear();
	}
	
	public void clearMangetizationCurve(){
		MCurveData.clear();
		clearFitResult();
	}
	
	public void clear(){
		ID = "";
		EffectiveLength = 0;
		Std_Current_Min = 0;
		Std_Current_Max = Double.NaN;
		GroupID = -1;
		Description = "";
		clearMangetizationCurve();
		clearFitResult();
	}
	
	public double[] getI(){
		int n = MCurveData.size();
		if(n == 0) return null;
		
		double[] I = new double[n];
		for(int index=0; index<n; index++) I[index] = MCurveData.get(index).getI();
		
		return I;
	}
	
	public double[] getBL(){
		int n = MCurveData.size();
		if(n == 0) return null;
		
		double[] BL = new double[n];
		for(int index=0; index<n; index++) BL[index] = MCurveData.get(index).getBL();
		
		return BL;
	}

	public String toString(){
		String text = "";
		
		text = "ID: " + this.getID() + "\n";
		text = text + "Effective_Length (m): " + this.getEffectiveLength() + "\n";
		text = text + "Std_Current_Min (A): " + this.getStd_Current_Min() + "\n";
		text = text + "Std_Current_Max (A): " + this.getStd_Current_Max() + "\n";
		text = text + "Group_ID: " + this.getGroupID() + "\n";
		text = text + "Description: " + this.getDescription() + "\n";
		if(MCurveData.size() == 0) text = text + "Magnetization curve (I (A) ~ BL (T): null\n";
		else text = text + "Magnetization curve (I (A) ~ BL (T)): \n";
		for(int i=0; i<MCurveData.size(); i++){
			text = text + "    " + MCurveData.get(i).getOrder() + ": " + MCurveData.get(i).getI() + ", " + MCurveData.get(i).getBL() + "\n";
		}
		
		if(_ItoBFitResult != null){
			text = text + "I to B Fit Result:\n";
			text = text + _ItoBFitResult.toString();
		}
		
		if(_BtoIFitResult != null){
			text = text + "B to I Fit Result:\n";
			text = text + _BtoIFitResult.toString();
		}
		
		return text;
	}
	
	public boolean equals(final MagnetizationCurve meci){
		/*
		if(this.getID().equalsIgnoreCase(meci.getID()) &&
				this.getEffectiveLength() == meci.getEffectiveLength() &&
				this.getStd_Current_Min() == meci.getStd_Current_Min() &&
				this.getDescription().equalsIgnoreCase(meci.getDescription()) &&
				this.getExcitationCurve().equals(meci.getExcitationCurve())){
			if(this.getGroupID() == null && meci.getGroupID() == null) return true;
			else if(this.getGroupID() != null || meci.getGroupID() != null) return false;
			else if(this.getGroupID().intValue() == meci.getGroupID().intValue()) return true;
		}
		*/
		if(this.toString().equalsIgnoreCase(meci.toString())) return true;
		
		return false;
	}
	
	public void writeToDataAdaptor(DataAdaptor cda){
		cda.setValue("group_id", getGroupID());
		cda.setValue("effective_length", getEffectiveLength());
		cda.setValue("std_current_min", getStd_Current_Min());
		cda.setValue("std_current_max", getStd_Current_Max());
		cda.setValue("description", getDescription());
		
		for(int index = 0; index < getMagnetizationCurveDatas().size(); index++){
			DataAdaptor dda = cda.createChild("value");
			
			MagnetizationCurveData data = getMagnetizationCurveData(index);
			dda.setValue("index", data.getOrder());
			dda.setValue("i", data.getI());
			dda.setValue("bl", data.getBL());
		}
		
		if(_ItoBFitResult != null && _ItoBFitResult.getOrder() != 0){
			DataAdaptor itobda = cda.createChild("ItoB");
			_ItoBFitResult.writeToDataAdaptor(itobda);
		}
		
		if(_BtoIFitResult != null && _BtoIFitResult.getOrder() != 0){
			DataAdaptor btoida = cda.createChild("BtoI");
			_BtoIFitResult.writeToDataAdaptor(btoida);
		}
	}
	
	static public MagnetizationCurve parseDataAdaptor(DataAdaptor curveda){
		if(curveda == null) return null;
		
		MagnetizationCurve curve = new MagnetizationCurve("temp_id");
		int group_id = curveda.intValue("group_id");
		curve.setGroupID(group_id);
		curve.setEffectiveLength(curveda.doubleValue("effective_length"));
		curve.setStd_Current_Min(curveda.doubleValue("std_current_min"));
		curve.setStd_Current_Max(curveda.doubleValue("std_current_max"));
		String description = curveda.stringValue("description");
		if(description == null) description = "";
		curve.setDescription(description);
		
		List<DataAdaptor> datas = curveda.childAdaptors("value");
		for(DataAdaptor da : datas){
			MagnetizationCurveData data = new MagnetizationCurveData(da.intValue("index"), da.doubleValue("i"), da.doubleValue("bl"));
			curve.addMagnetizationCurveData(data);
		}
		
		DataAdaptor itobda = curveda.childAdaptor("ItoB");
		curve.setItoBFitResult(MCFitResult.parseDataAdaptor(itobda));
		
		DataAdaptor btoida = curveda.childAdaptor("BtoI");
		curve.setBtoIFitResult(MCFitResult.parseDataAdaptor(btoida));
		
		return curve;
	}
}
