package xal.app.magnetdbmanager;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class MCCoeTableData {
	private SimpleStringProperty rowname;
	private SimpleDoubleProperty itobcontent;
	private SimpleDoubleProperty btoicontent;
	
	public MCCoeTableData(String columname, double itob, double btoi){
		rowname = new SimpleStringProperty(columname);
		itobcontent = new SimpleDoubleProperty(itob);
		btoicontent = new SimpleDoubleProperty(btoi);
	}
	
	public MCCoeTableData(String columname, String itobl, String bltoi){
		rowname = new SimpleStringProperty(columname);
		itobcontent = new SimpleDoubleProperty(Double.parseDouble(itobl));
		btoicontent = new SimpleDoubleProperty(Double.parseDouble(bltoi));
	}
	
	public SimpleStringProperty rownameProperty(){
		return rowname;
	}
	
	public SimpleDoubleProperty itobcontentProperty(){
		return itobcontent;
	}
	
	public SimpleDoubleProperty btoicontentProperty(){
		return btoicontent;
	}
}
