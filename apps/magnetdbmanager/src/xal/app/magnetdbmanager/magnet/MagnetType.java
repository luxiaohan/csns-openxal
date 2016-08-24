package xal.app.magnetdbmanager.magnet;

import java.util.ArrayList;
import java.util.Arrays;

public enum MagnetType {
	CORRECTOR("CORRECTOR"),
	DIPOLE("DIPOLE"),
	RBEND("RBEND"),
	SBEND("SBEND"),
	QUADRUPOLE("QUADRUPOLE"),
	SEXTUPOLE("SEXTUPOLE"),
	OCTUPOLE("OCTUPOLE");
	
	static private ArrayList<String> _magnetTypeList = new ArrayList<String>(Arrays.asList(
			CORRECTOR.getTypeName(),
			DIPOLE.getTypeName(),
			RBEND.getTypeName(),
			SBEND.getTypeName(),
			QUADRUPOLE.getTypeName(),
			SEXTUPOLE.getTypeName(),
			OCTUPOLE.getTypeName()
			));
	
	private String _type;
	
	private MagnetType(String type){
		_type = type;
	}
	
	public String getTypeName(){
		return _type;
	}
	
	static public boolean isDefinedMagnetType(String magnettype){
		if(_magnetTypeList.contains(magnettype)) return true;
		return false;
	}
	
	static public ArrayList<String> getMagnetTypes(){
		return _magnetTypeList;
	}
}
