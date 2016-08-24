package xal.app.magnetdbmanager.magnet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MultipoleField implements Cloneable{
	private String id;

	/** A */
	private double current;
	/** standard deviation, A */
	private double current_error;
	
	// alignment error
	private double dx;
	private double dy;
	
	// water pressure, Bar
	private double water_pressure_in;
	private double water_pressure_out;

	// water temperature, C
	private double water_temperature_in;
	private double water_temperature_out;
	
	// water flow, L/s
	private double water_flow;
	
	private String operator;
	
	private Date date = new Date();
	
	private String description = "";
	
	HashMap<Integer, MultipoleFieldData> fielddatas = new HashMap<Integer, MultipoleFieldData>();
	
	public MultipoleField(String id, double current){
		this.id = id;
		this.current = current;
	}
	
	public MultipoleField clone(){
		MultipoleField copy = null;
		try{
			copy = (MultipoleField) super.clone();
		} catch(CloneNotSupportedException e) {
			   e.printStackTrace();
	    }
		
		copy.id = id;
		copy.current = current;
		copy.current_error = current_error;
		copy.dx = dx;
		copy.dy = dy;
		copy.water_pressure_in = water_pressure_in;
		copy.water_pressure_out = water_pressure_out;
		copy.water_temperature_in = water_temperature_in;
		copy.water_temperature_out = water_temperature_out;
		copy.water_flow = water_flow;
		copy.operator = operator;
		if(date == null) copy.date = null;
		else copy.date = (Date) date.clone();
		
		copy.fielddatas = new HashMap<Integer, MultipoleFieldData>();
		for(MultipoleFieldData md : fielddatas.values()){
			copy.addMultipoleFieldData(md.clone());
		}
		
		return copy;
	}
	
	public String getID() {
		return id;
	}

	public double getCurrent() {
		return current;
	}
	
	public void setCurrent(double current){
		this.current = current;
	}

	public double getCurrent_error() {
		return current_error;
	}

	public void setCurrent_error(double current_error) {
		this.current_error = current_error;
	}

	public double getDx() {
		return dx;
	}

	public void setDx(double dx) {
		this.dx = dx;
	}

	public double getDy() {
		return dy;
	}

	public void setDy(double dy) {
		this.dy = dy;
	}

	public double getWater_pressure_in() {
		return water_pressure_in;
	}

	public void setWater_pressure_in(double water_pressure_in) {
		this.water_pressure_in = water_pressure_in;
	}

	public double getWater_pressure_out() {
		return water_pressure_out;
	}

	public void setWater_pressure_out(double water_pressure_out) {
		this.water_pressure_out = water_pressure_out;
	}

	public double getWater_temperature_in() {
		return water_temperature_in;
	}

	public void setWater_temperature_in(double water_temperature_in) {
		this.water_temperature_in = water_temperature_in;
	}

	public double getWater_temperature_out() {
		return water_temperature_out;
	}

	public void setWater_temperature_out(double water_temperature_out) {
		this.water_temperature_out = water_temperature_out;
	}

	public double getWater_flow() {
		return water_flow;
	}

	public void setWater_flow(double water_flow) {
		this.water_flow = water_flow;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public HashMap<Integer, MultipoleFieldData> getMultipoleFieldDatas() {
		return fielddatas;
	}

	public void setMultipoleFieldDatas(HashMap<Integer, MultipoleFieldData> datas) {
		this.fielddatas = datas;
	}
	
	public void addMultipoleFieldData(MultipoleFieldData md){
		fielddatas.put(md.getOrder(), md);
	}
	
	public void removeMultipoleFieldData(int order){
		fielddatas.remove(order);
	}
	
	public MultipoleFieldData getMultipoleFieldData(int order){
		return fielddatas.get(order);
	}
	
	public String toString(){
		String text = "";
		
		text = "ID: " + this.getID() + "\n";
		text = text + "Current (A): " + this.getCurrent() + "\n";
		text = text + "Current_Error (A): " + this.getCurrent_error() + "\n";
		text = text + "Dx (mm): " + this.getDx() + "\n";
		text = text + "Dy (mm): " + this.getDy() + "\n";
		text = text + "Water_Temperature_In (C): " + this.getWater_temperature_in() + "\n";
		text = text + "Water_Temperature_Out (C): " + this.getWater_temperature_out() + "\n";
		text = text + "Description: " + this.getDescription() + "\n";
		
		if(fielddatas.size() == 0) text = text + "Multipole Fields (Order, Bn, An, Angle): null\n";
		else text = text + "Multipole Fields (Order, Bn, An, Angle): \n";
		
		for(int order : fielddatas.keySet()){
			text = text + "    " + this.getMultipoleFieldData(order).getOrder() + ": " + this.getMultipoleFieldData(order).getBn() + ", " + this.getMultipoleFieldData(order).getAn() + ", " + this.getMultipoleFieldData(order).getAngle() + "\n";
		}
		
		return text;
	}
	
	public boolean equals(final MultipoleField other){
		if(this.toString().equalsIgnoreCase(other.toString())) return true;
		return false;
	}

}
