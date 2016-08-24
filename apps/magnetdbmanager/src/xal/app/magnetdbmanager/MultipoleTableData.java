package xal.app.magnetdbmanager;

import xal.app.magnetdbmanager.magnet.MultipoleFieldData;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MultipoleTableData implements Comparable<MultipoleTableData>{
	private SimpleIntegerProperty order;
	private SimpleDoubleProperty bn;
	private SimpleDoubleProperty an;
	
	private MultipoleFieldData _md;
	
	public MultipoleTableData(MultipoleFieldData md){
		_md = md;
		order = new SimpleIntegerProperty(_md.getOrder());
		bn = new SimpleDoubleProperty(_md.getBn());
		an = new SimpleDoubleProperty(_md.getAn());
	}
	
	public int getOrder() {
		return order.intValue();
	}
	
	public SimpleIntegerProperty orderProperty(){
		return order;
	}

	public double getBn() {
		return bn.doubleValue();
	}
	
	public SimpleDoubleProperty bnProperty() {
		return bn;
	}

	public double getAn() {
		return an.doubleValue();
	}
	
	public SimpleDoubleProperty anProperty() {
		return an;
	}
	
	public MultipoleFieldData getMultipoleData() {
		return _md;
	}

	@Override
	public int compareTo(MultipoleTableData md){
		return _md.compareTo(md.getMultipoleData());
	}
}
