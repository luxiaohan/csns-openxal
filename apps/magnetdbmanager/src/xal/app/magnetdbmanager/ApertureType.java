package xal.app.magnetdbmanager;

import java.util.ArrayList;
import java.util.Arrays;

public enum ApertureType {
	CIRCLE("CIRCLE", 1),
	ELLIPSE("ELLIPSE", 2),
	RECTANGLE("RECTANGLE", 2),
	LHCSCREEN("LHCSCREEN", 3),
	MARGUERITE("MARGUERITE", 3),
	RECTELLIPSE("RECTELLIPSE", 4),
	RACETRACK("RACETRACK", 3),
	OUTLINE("OUTLINE", 6);
	
	private String _name;
	private int _parametersNo;
	private double[] _apertureDatas;
	
	static private ArrayList<String> _apertureTypeList = new ArrayList<String>(Arrays.asList(
			CIRCLE.getName(),
		    ELLIPSE.getName(),
		    RECTANGLE.getName(),
		    LHCSCREEN.getName(),
		    MARGUERITE.getName(),
		    RECTELLIPSE.getName(),
		    RACETRACK.getName(),
		    OUTLINE.getName()
			));
	
	static private ArrayList<String> _descriptionList = new ArrayList<String>(Arrays.asList(
			"Radius is needed.",
			"Horizontal half axis and vertical half axis are needed.",
			"Half width and half height are needed.",
			"Half width, half height (of rect.) and radius (of circ.) are neeeded.",
			"Half width, half height (of rect.) and radius (of circ.) are neeeded.",
			"Half widht, half height (of rectangle), horizontal half axis, vertical half axis (of ellipse).",
			"Horizontal, vertical shift, radius shift.",
			"A list of x and y coordinates (at least 3 pairs) outlining the shape is needed."
			));
	
	static public boolean isDefinedApertureType(String aperturetype){
		if(_apertureTypeList.contains(aperturetype)) return true;
		return false;
	}
	
	static public String getApertureDescription(String aperturetype){
		if(!isDefinedApertureType(aperturetype)) return null;
		
		return _descriptionList.get(_apertureTypeList.indexOf(aperturetype));
	}
	
	static public ArrayList<String> getApertureTypes(){
		return _apertureTypeList;
	}
	
	private ApertureType(String name, int parno){
		_name = name;
		_parametersNo = parno;
		_apertureDatas = new double[_parametersNo];
	}
	
	public String getName(){
		return _name;
	}
	
	public int getApertureDataSize(){
		return _parametersNo;
	}
	
	public boolean setApertureData(double ...datas){
		if(_name.equalsIgnoreCase(OUTLINE.getName())){
			if(datas.length < 6) return false;
			_apertureDatas = new double[datas.length];
		}
		
		if(_parametersNo != datas.length) return false;
		
		for(int i=0;i<datas.length;i++) _apertureDatas[i] = datas[i];
		
		return true;
	}
}
