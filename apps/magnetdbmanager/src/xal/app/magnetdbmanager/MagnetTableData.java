package xal.app.magnetdbmanager;

//import csns.physics.database.magnet.Magnet;
import xal.app.magnetdbmanager.magnet.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class MagnetTableData implements Comparable<MagnetTableData>{
	private SimpleIntegerProperty index;
	private final SimpleStringProperty pid;
	private final SimpleStringProperty id;
	private final SimpleStringProperty sort;
	private SimpleStringProperty status = new SimpleStringProperty("");
	
	private Magnet _magnet;
	
	public MagnetTableData(int index, String pid, String id, String sort){
		this.index = new SimpleIntegerProperty(index);
		this.pid = new SimpleStringProperty(pid);
		this.id = new SimpleStringProperty(id);
		this.sort = new SimpleStringProperty(sort);
	}
	
	public MagnetTableData(int index, Magnet magnet){
		this.index = new SimpleIntegerProperty(index);
		_magnet = magnet;
		this.pid = new SimpleStringProperty(_magnet.getPID());
		this.id = new SimpleStringProperty(_magnet.getID());
		this.sort = new SimpleStringProperty(_magnet.getSort());
	}
	
	public int getIndex(){
		return index.intValue();
	}
	
	public SimpleIntegerProperty indexProperty(){
		return index;
	}
	
	public String getPID(){
		return pid.getValue();
	}
	
	public SimpleStringProperty pidProperty(){
		return pid;
	}
	
	public String getID(){
		return id.getValue();
	}
	
	public SimpleStringProperty idProperty(){
		return id;
	}
	
	public String getSort(){
		return sort.getValue();
	}
	
	public SimpleStringProperty sortProperty(){
		return sort;
	}
	
	public String getStatus(){
		return status.getValue();
	}
	
	public SimpleStringProperty statusProperty(){
		return status;
	}
	
	public void setStatus(String status){
		this.status.setValue(status);
	}
	
	public Magnet getMagnet(){
		return _magnet;
	}
	
	public void setMagnet(Magnet magnet){
		if(magnet == null) return;
		
		id.setValue(magnet.getID());
		pid.setValue(magnet.getPID());
		sort.setValue(magnet.getSort());
		_magnet = magnet;

		//if(pid.getValue().equalsIgnoreCase(magnet.getPID()) && id.getValue().equalsIgnoreCase(magnet.getID()) && sort.getValue().equalsIgnoreCase(magnet.getSort())) _magnet = magnet;
	}
	
	@Override
	public int compareTo(MagnetTableData mtd){
		return index.getValue() - mtd.getIndex();
	}

}
