package xal.app.magnetdbmanager.magnet;

//import gov.sns.tools.data.DataAdaptor;
//import gov.sns.tools.xml.XmlDataAdaptor;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;


public class Magnet implements Cloneable{
	private String _pid;
	private String _id;
	private String _sort;
	
	private Date _date;
	
	private MagnetInformation _info;
	private HashMap<Integer, MagnetizationCurve> _magnetizationcurves = new HashMap<Integer, MagnetizationCurve>();
	private HashMap<Double, MultipoleField> _multipolefields = new HashMap<Double, MultipoleField>();

	public Magnet(String id, String pid, String sort){
		_id = id;
		_pid = pid;
		_sort = sort;
		
		_date = new Date();
		
		_info = new MagnetInformation(_id, _pid, _sort);
	}
	
	public Magnet clone(){
		Magnet copy = null;
		
		try{
			copy = (Magnet) super.clone();
		} catch(CloneNotSupportedException e) {
			   e.printStackTrace();
	    }
		
		copy._pid = _pid;
		copy._id = _id;
		copy._sort = _sort;
		copy._date = (Date) _date.clone();
		copy._info = (MagnetInformation) _info.clone();
		
		copy._magnetizationcurves  = new HashMap<Integer, MagnetizationCurve>();
		for(int i : _magnetizationcurves.keySet()) copy.addMagnetizationCurve(_magnetizationcurves.get(i).clone());
		
		copy._multipolefields  = new HashMap<Double, MultipoleField>();
		for(double I : _multipolefields.keySet()) copy.addMultipoleField(_multipolefields.get(I).clone());
		
		return copy;
	}
	
	public String getPID(){
		return _pid;
	}
	
	public void setPID(String pid){
		_pid = pid;
		if(_info != null) _info.setPID(_pid);
	}
	
	public String getID(){
		return _id;
	}
	
	public void setID(String id){
		_id = id;
		if(_info != null) _info.setID(_id);
		for(int i : _magnetizationcurves.keySet()){
			_magnetizationcurves.get(i).setID(_id);
		}
	}
	
	public String getSort(){
		return _sort;
	}
	
	public void setSort(String sort){
		_sort = sort;
		if(_info != null) _info.setSort_ID(sort);
	}
	
	public void setDate(Date date){
		if(date == null) return;
		_date = date;
	}
	
	public Date getDate(){
		return _date;
	}
	
	public MagnetInformation getInfo(){
		return _info;
	}
	
	public void setInfo(MagnetInformation info){
		if(!_pid.equalsIgnoreCase(info.getPID())) return;
		if(!_id.equalsIgnoreCase(info.getID())) return;
		if(!_sort.equalsIgnoreCase(info.getSort_ID())) return;
		
		_info = info;
	}
	
	public HashMap<Double, MultipoleField> getMultipoleFields(){
		return _multipolefields;
	}
	
	public MultipoleField getMultipoleField(double current){
		return _multipolefields.get(current);
	}
	
	public void addMultipoleField(MultipoleField mfi){
		if(!_id.equalsIgnoreCase(mfi.getID())) return;
		_multipolefields.put(mfi.getCurrent(), mfi);
	}
	
	public void clearMultipoleFields(){
		_multipolefields.clear();
	}
	
	public void removeMultipoleFieldInfo(double current){
		_multipolefields.remove(current);
	}
	
	public HashMap<Integer, MagnetizationCurve> getMagnetizationCurveInfos(){
		return _magnetizationcurves;
	}
	
	public MagnetizationCurve getMagnetizatonCurveInfo(int groupid){
		return _magnetizationcurves.get(groupid);
	}
	
	public void addMagnetizationCurve(MagnetizationCurve ecurve){
		if(!_id.equalsIgnoreCase(ecurve.getID())) return;
		_magnetizationcurves.put(ecurve.getGroupID(), ecurve);
	}
	
	public void clearMagnetizationCurves(){
		_magnetizationcurves.clear();
	}
	
	public void removeMagnetizationCurve(int groupid){
		_magnetizationcurves.remove(groupid);
	}
	
	/**
	 *  Compare Magnet except date.
	 * @param magnet
	 * @return
	 */
	public boolean equals(final Magnet magnet){
		if(!(_id.equalsIgnoreCase(magnet.getID()) &&
				_pid.equalsIgnoreCase(magnet.getPID()) &&
				_sort.equalsIgnoreCase(magnet.getSort()) &&
				_date.toString().equalsIgnoreCase(magnet.getDate().toString()) &&
				_info.equals(magnet.getInfo())))
			return false;

		if(!_info.equals(magnet.getInfo())) return false;
		
		if(_magnetizationcurves.size() == magnet.getMagnetizationCurveInfos().size()){
			for(int i : _magnetizationcurves.keySet()){
				if(!magnet.getMagnetizationCurveInfos().containsKey(i)) return false;
				if(!_magnetizationcurves.get(i).equals(magnet.getMagnetizationCurveInfos().get(i))) return false;
			}
		}
		else return false;
		
		if(_multipolefields.size() == magnet.getMultipoleFields().size()){
			for(Double I : _multipolefields.keySet()){
				if(!magnet.getMultipoleFields().containsKey(I)) return false;
				if(!_multipolefields.get(I).equals(magnet.getMultipoleField(I))) return false;
			}
		}
		else return false;
		
		return true;
	}
	
	public String toString(){
		String output = "";
		
		output = output + _info.toString();
		
		for(int i : _magnetizationcurves.keySet()){
			output = output + _magnetizationcurves.get(i).toString();
		}
		
		for(double current : _multipolefields.keySet()){
			output = output + _multipolefields.get(current).toString();
		}
		
		return output;
	}
	
	public void toXmlDataAdaptor(XmlDataAdaptor xda){
		DataAdaptor mda = xda.createChild("magnet");
		
		mda.setValue("id", _info.getID());
		mda.setValue("pid", _info.getPID());
		mda.setValue("sort_id", _info.getSort_ID());
		
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		mda.setValue("date", df.format(_date));
		
		mda.setValue("aperture_type", _info.getAperture_Type());
		mda.setValue("aperture", _info.getAperture());
		
		mda.setValue("type_id", _info.getType_ID());
		mda.setValue("length", _info.getLength());
		
		if(_sort.equalsIgnoreCase("dipole")){
			mda.setValue("angle", _info.getAngle());
			mda.setValue("e1", _info.getE1());
			mda.setValue("e2", _info.getE2());
		}
		else if(_sort.equalsIgnoreCase("corrector")) mda.setValue("angle", _info.getAngle());
		
		mda.setValue("description", _info.getDescription());
		
		for(int groupid : _magnetizationcurves.keySet()){
			DataAdaptor cda = mda.createChild("magnetization_curve");
			
			MagnetizationCurve curve = _magnetizationcurves.get(groupid);
			curve.writeToDataAdaptor(cda);
//			cda.setValue("group_id", curve.getGroupID());
//			cda.setValue("effective_length", curve.getEffectiveLength());
//			cda.setValue("std_current_min", curve.getStd_Current_Min());
//			cda.setValue("std_current_max", curve.getStd_Current_Max());
//			cda.setValue("description", curve.getDescription());
//			
//			for(int index = 0; index < curve.getMagnetizationCurve().size(); index++){
//				DataAdaptor dda = cda.createChild("value");
//				
//				MagnetizationCurveData data = curve.getMagnetizationCurveData(index);
//				dda.setValue("index", data.getOrder());
//				dda.setValue("i", data.getI());
//				dda.setValue("bl", data.getBL());
//			}
		}
		
		for(Double I : _multipolefields.keySet()){
			DataAdaptor mfsda = mda.createChild("multipole_field");
			
			MultipoleField mf = _multipolefields.get(I);
			mfsda.setValue("current", mf.getCurrent());
			mfsda.setValue("current_error", mf.getCurrent_error());
			mfsda.setValue("dx", mf.getDx());
			mfsda.setValue("dy", mf.getDy());
			mfsda.setValue("water_temperature_in", mf.getWater_temperature_in());
			mfsda.setValue("water_temperature_out", mf.getWater_temperature_out());
			mfsda.setValue("description", mf.getDescription());
			
			for(int order : mf.getMultipoleFieldDatas().keySet()){
				DataAdaptor mfda = mfsda.createChild("value");
				
				MultipoleFieldData mfdata = mf.getMultipoleFieldData(order);
				mfda.setValue("order", mfdata.getOrder());
				mfda.setValue("bn", mfdata.getBn());
				mfda.setValue("an", mfdata.getAn());
				mfda.setValue("angle", mfdata.getAngle());
			}
		}
	}
	
	static public Magnet ParseXMLToMagnet(XmlDataAdaptor xda){
		Magnet magnet = null;
		
		DataAdaptor mda = xda.childAdaptor("magnet");
		String id = mda.stringValue("id");
		String pid = mda.stringValue("pid");
		String sort_id = mda.stringValue("sort_id");
		String datestring = mda.stringValue("date");
		
		if(id == null || id.isEmpty() || pid == null || pid.isEmpty() || sort_id == null) return null;
		
		if(!MagnetType.isDefinedMagnetType(sort_id)) return null;
		
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
		df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		Date date;
		try {
			date = df.parse(datestring);
		} catch (ParseException e) {
			e.printStackTrace();
			date = new Date();
		}
		
		String aperture_type = mda.stringValue("aperture_type");
		String aperture = mda.stringValue("aperture");
		
		String type = mda.stringValue("type_id");
		double length = mda.doubleValue("length");
		
		double angle = 0;
		if(type.equalsIgnoreCase(MagnetType.DIPOLE.getTypeName()) || type.equalsIgnoreCase(MagnetType.CORRECTOR.getTypeName())) angle = mda.doubleValue("angle");
		
		double e1 = 0;
		double e2 = 0;
		if(type.equalsIgnoreCase(MagnetType.DIPOLE.getTypeName())){
			e1 = mda.doubleValue("e1");
			e2 = mda.doubleValue("e2");
		}
		
		String description = mda.stringValue("description");
		if(description == null) description = "";
		
		magnet = new Magnet(id, pid, sort_id);
		magnet.setDate(date);
		
		MagnetInformation info = magnet.getInfo();
		info.setType_ID(type);
		info.setLength(length);
		info.setAngle(angle);
		info.setE1(e1);
		info.setE2(e2);
		info.setAperture_Type(aperture_type);
		info.setAperture(aperture);
		info.setDescription(description);
		
		List<DataAdaptor> curves = mda.childAdaptors("magnetization_curve");
		for(DataAdaptor curveda : curves){
			MagnetizationCurve curve = MagnetizationCurve.parseDataAdaptor(curveda);
			curve.setID(id);
			
//			MagnetizationCurve curve = new MagnetizationCurve(id);
//			int group_id = curveda.intValue("group_id");
//			curve.setGroupID(group_id);
//			curve.setEffectiveLength(curveda.doubleValue("effective_length"));
//			curve.setStd_Current_Min(curveda.doubleValue("std_current_min"));
//			curve.setStd_Current_Max(curveda.doubleValue("std_current_max"));
//			description = curveda.stringValue("description");
//			if(description == null) description = "";
//			curve.setDescription(description);
//			
//			List<DataAdaptor> datas = curveda.childAdaptors("value");
//			for(DataAdaptor da : datas){
//				MagnetizationCurveData data = new MagnetizationCurveData(da.intValue("index"), da.doubleValue("i"), da.doubleValue("bl"));
//				curve.addMagnetizationCurveData(data);
//			}
			
			magnet.addMagnetizationCurve(curve);
		}
		
		List<DataAdaptor> mfs = mda.childAdaptors("multipole_field");
		for(DataAdaptor mf : mfs){
			double current = mf.doubleValue("current");
			MultipoleField multipolefield = new MultipoleField(id, current);
			multipolefield.setCurrent_error(mf.doubleValue("current_error"));
			multipolefield.setDx(mf.doubleValue("dx"));
			multipolefield.setDx(mf.doubleValue("dy"));
			multipolefield.setWater_temperature_in(mf.doubleValue("water_temperature_in"));
			multipolefield.setWater_temperature_out(mf.doubleValue("water_temperature_out"));
			multipolefield.setDescription(mf.stringValue("description"));
			
			List<DataAdaptor> datas = mf.childAdaptors("value");
			for(DataAdaptor da : datas){
				MultipoleFieldData md = new MultipoleFieldData(da.intValue("order"), da.doubleValue("bn"), da.doubleValue("an"));
				multipolefield.addMultipoleFieldData(md);
			}
			
			magnet.addMultipoleField(multipolefield);
		}
		
		return magnet;
	}
}
