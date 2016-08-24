package xal.app.magnetdbmanager;

import java.util.HashMap;


import xal.app.magnetdbmanager.magnet.*;
/*import csns.physics.database.magnet.Magnet;
import csns.physics.database.magnet.MultipoleField;
import csns.physics.database.magnet.MultipoleFieldData;*/
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;

public class MultipoleFieldInfoPane extends Pane {
    private Label _currentlabel;
    private TextField _currenttf;
    
    private Label _grouplabel;
    private ComboBox<Integer> _groupcb;
    private HashMap<Integer, MultipoleField> _mfmap;
    
    private Label _currenterrorlabel;
    private TextField _currenterrortf;
    
    private Label _dxlabel;
    private TextField _dxtf;
    private Label _dylabel;
    private TextField _dytf;
    
    private Label _temperatureinlabel;
    private TextField _temperatureintf;
    private Label _temperatureoutlabel;
    private TextField _temperatureouttf;
    
    private Label _descriptionlabel;
    private TextField _descriptiontf;
    
    private TableView<MultipoleTableData> _multipoleFieldTableView = null;
    
    private boolean _editable = false;
    private Magnet _magnet = null;
    
    private ObservableList<MultipoleTableData> _mfList = FXCollections.observableArrayList();
    
    private Group _egroup;
    
    private Label _orderlabel;
    private ComboBox<Integer> _ordercb;
    
    private Label _bnlabel;
    private TextField _bntf;
    private Label _anlabel;
    private TextField _antf;
    
    private Button _setbn;

	public MultipoleFieldInfoPane(){
		VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(15, 5, 5, 5));
    	
        HBox hbox1 = new HBox();
        hbox1.setAlignment(Pos.CENTER_LEFT);
        hbox1.setSpacing(10);
        
        _grouplabel = new Label("GROUP:");
        _groupcb = new ComboBox<Integer>();
        _groupcb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>(){
			@Override
			public void changed(ObservableValue<? extends Integer> selected, Integer oldvalue, Integer newvalue) {
				if(_magnet == null) return;
				if(selected.getValue() == null) return;
				
				selectGroup(selected.getValue());
				
				_mfList.clear();
				for(MultipoleFieldData md : _mfmap.get(selected.getValue()).getMultipoleFieldDatas().values()){
					_mfList.add(new MultipoleTableData(md));
				}
			}
        });
        
        int width = 70;
    	_currentlabel = new Label("CURRENT (A):");
    	_currenttf = new TextField("");
    	_currenttf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null  && oldPropertyValue && _magnet.getMultipoleFields().size() > 0){
		        	double current = Tools.parseDouble(_currenttf.getText(), "Manget Database Manager", "Current");
		        	if(current == Double.NaN) return;
		        	
		        	MultipoleField mf = _mfmap.get(_groupcb.getValue());
		        	double oldcurrent = mf.getCurrent();
		        	mf.setCurrent(current);
		        	
		        	_magnet.removeMultipoleFieldInfo(oldcurrent);
		        	_magnet.addMultipoleField(mf);
		        }
		    }
		});
    	
    	_currenterrorlabel = new Label("CURRENT ERROR (A):");
    	_currenterrortf = new TextField(); _currenterrortf.setMinWidth(width);
    	_currenterrortf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMultipoleFields().size() > 0){
		        	double value = Tools.parseDouble(_currenterrortf.getText(), "Manget Database Manager", "Current Error");
		        	if(value == Double.NaN) return;
		        	
		        	MultipoleField mf = _mfmap.get(_groupcb.getValue());
		        	mf.setCurrent_error(value);
		        }
		    }
		});
    	
    	hbox1.getChildren().addAll(_grouplabel, _groupcb, _currentlabel, _currenttf, _currenterrorlabel, _currenterrortf);
    	
    	HBox hbox2 = new HBox();
        hbox2.setAlignment(Pos.CENTER_LEFT);
        hbox2.setSpacing(10);
        
        _dxlabel = new Label("DX (mm):");
        _dxtf = new TextField(); _dxtf.setMinWidth(width);
        _dxtf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMultipoleFields().size() > 0){
		        	double value = Tools.parseDouble(_dxtf.getText(), "Manget Database Manager", "Dx");
		        	if(value == Double.NaN) return;
		        	
		        	MultipoleField mf = _mfmap.get(_groupcb.getValue());
		        	mf.setDx(value);
		        }
		    }
		});
        
        _dylabel = new Label("DY (mm):");
        _dytf = new TextField(); _dytf.setMinWidth(width);
        _dytf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMultipoleFields().size() > 0){
		        	double value = Tools.parseDouble(_dytf.getText(), "Manget Database Manager", "Dy");
		        	if(value == Double.NaN) return;
		        	
		        	MultipoleField mf = _mfmap.get(_groupcb.getValue());
		        	mf.setDy(value);
		        }
		    }
		});
        
        hbox2.getChildren().addAll(_dxlabel, _dxtf, _dylabel, _dytf);
    	
        HBox hbox3 = new HBox();
        hbox3.setAlignment(Pos.CENTER_LEFT);
        hbox3.setSpacing(10);
        
        _temperatureinlabel = new Label("TEMPERATURE_IN (C):");
        _temperatureintf = new TextField(); _temperatureintf.setMinWidth(width);
        _temperatureintf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMultipoleFields().size() > 0){
		        	double value = Tools.parseDouble(_temperatureintf.getText(), "Manget Database Manager", "Water Temperature In");
		        	if(value == Double.NaN) return;
		        	
		        	MultipoleField mf = _mfmap.get(_groupcb.getValue());
		        	mf.setWater_temperature_in(value);
		        }
		    }
		});
        
        _temperatureoutlabel = new Label("TEMPERATURE_OUT (C):");
        _temperatureouttf = new TextField(); _temperatureouttf.setMinWidth(width);
        _temperatureouttf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMultipoleFields().size() > 0){
		        	double value = Tools.parseDouble(_temperatureouttf.getText(), "Manget Database Manager", "Water Temperature out");
		        	if(value == Double.NaN) return;
		        	
		        	MultipoleField mf = _mfmap.get(_groupcb.getValue());
		        	mf.setWater_temperature_out(value);
		        }
		    }
		});
        
        hbox3.getChildren().addAll(_temperatureinlabel, _temperatureintf, _temperatureoutlabel, _temperatureouttf);
        
        HBox hbox4 = new HBox();
        hbox4.setAlignment(Pos.CENTER_LEFT);
        hbox4.setSpacing(10);
        _descriptionlabel = new Label("DESCRIPTION:");
        _descriptiontf = new TextField(); _descriptiontf.setMinWidth(200);
        _descriptiontf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMultipoleFields().size() > 0){
		        	MultipoleField mf = _mfmap.get(_groupcb.getValue());
		        	mf.setDescription(_descriptiontf.getText());
		        }
		        
		    }
		});
        hbox4.getChildren().addAll(_descriptionlabel, _descriptiontf);
        
        _multipoleFieldTableView = new TableView<MultipoleTableData>();
        _multipoleFieldTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        _multipoleFieldTableView.setEditable(false);
        //_multipoleFieldTableView.prefWidthProperty().bind(this.heightProperty().subtract(100));
        _multipoleFieldTableView.setPrefHeight(320);
        _multipoleFieldTableView.setPrefWidth(500);
        
        TableColumn<MultipoleTableData, Integer> orderColumn = new TableColumn<MultipoleTableData, Integer>("Order");
        TableColumn<MultipoleTableData, Double> bnColumn = new TableColumn<MultipoleTableData, Double>("bn");
        TableColumn<MultipoleTableData, Double> anColumn = new TableColumn<MultipoleTableData, Double>("an");
        
        orderColumn.setCellValueFactory(new PropertyValueFactory<MultipoleTableData, Integer>("Order"));
        bnColumn.setCellValueFactory(new PropertyValueFactory<MultipoleTableData, Double>("bn"));
        anColumn.setCellValueFactory(new PropertyValueFactory<MultipoleTableData, Double>("an"));
        
        _multipoleFieldTableView.getColumns().add(orderColumn);
        _multipoleFieldTableView.getColumns().add(bnColumn);
        _multipoleFieldTableView.getColumns().add(anColumn);
        
        _multipoleFieldTableView.setItems(_mfList);
        
        //TODO
        
        HBox hbox5 = new HBox();
        hbox5.setAlignment(Pos.CENTER_LEFT);
        hbox5.setSpacing(10);
        hbox5.getChildren().add(_multipoleFieldTableView);
        
        
        _orderlabel = new Label("ORDER:"); _orderlabel.setVisible(false);
        _ordercb = new ComboBox<Integer>(); _ordercb.setVisible(false);
        
        ObservableList<Integer> grouporders = FXCollections.observableArrayList();
        for(int i = 1; i <= 20; i++){
        	grouporders.add(i);
        }
        _ordercb.setItems(grouporders);
        
        _bnlabel = new Label("bn:"); _bnlabel.setVisible(false);
        _bntf = new TextField(""); _bntf.setVisible(false);
        
        _anlabel = new Label("an:"); _anlabel.setVisible(false);
        _antf = new TextField(""); _antf.setVisible(false);
        
        _setbn = new Button("SET"); _setbn.setVisible(false);
        
        _setbn.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent e) {
				if(_magnet == null || _groupcb.getValue() == null || _ordercb.getValue() == null) return;
				
				int order = _ordercb.getValue();
				
				double bn = Tools.parseDouble(_bntf.getText(), "Manget Database Manager", "bn");
				if(bn == Double.NaN) return;
				
				double an = Tools.parseDouble(_antf.getText(), "Manget Database Manager", "an");
				if(an == Double.NaN) return;
			
				_mfmap.get(_groupcb.getValue()).addMultipoleFieldData(new MultipoleFieldData(order, bn, an));
				
				_mfList.clear();
				
				for(MultipoleFieldData md : _mfmap.get(_groupcb.getValue()).getMultipoleFieldDatas().values()){
					_mfList.add(new MultipoleTableData(md));
				}
			}
        	
        });
        
        ContextMenu menu = new ContextMenu();
        
        final MenuItem removeitem = new MenuItem("Remove");
        removeitem.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				// TODO Auto-generated method stub
				if(_magnet == null) return;
				
				MultipoleTableData tabledata = _multipoleFieldTableView.getSelectionModel().getSelectedItem();
				if(tabledata == null) return;
				
				int order = tabledata.getOrder();
				
				_mfmap.get(_groupcb.getValue()).removeMultipoleFieldData(order);
				
				_mfList.clear();
				for(MultipoleFieldData md : _mfmap.get(_groupcb.getValue()).getMultipoleFieldDatas().values()){
					_mfList.add(new MultipoleTableData(md));
				}
			}
        	
        });
        
        menu.setOnShowing(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
            	MultipoleTableData tabledata = _multipoleFieldTableView.getSelectionModel().getSelectedItem();
                if(tabledata == null || !_currenttf.isEditable() ){
                	removeitem.setDisable(true);
                	return;
                }
                
                removeitem.setDisable(false);
            }
        });
        
        menu.getItems().add(removeitem);
        _multipoleFieldTableView.setContextMenu(menu);
        
        _egroup = new Group();
        _egroup.getChildren().addAll(_orderlabel, _ordercb, _bnlabel, _bntf, _anlabel, _antf, _setbn);
        
        //TODO
        
        HBox hbox6 = new HBox();
        hbox6.setAlignment(Pos.CENTER_LEFT);
        hbox6.setSpacing(10);
        hbox6.getChildren().addAll(_orderlabel, _ordercb, _bnlabel, _bntf, _anlabel, _antf, _setbn);
        
        
    	vbox.getChildren().addAll(hbox1, hbox2, hbox3, hbox4, hbox5, hbox6);
    	
    	this.getChildren().add(vbox);
	}
	
	public void setMagnet(Magnet magnetediting){
		_magnet = magnetediting;
		setGroup();
	}
	
	private void setGroup(){
		if(_magnet == null) return;
		
		ObservableList<Integer> groupids = FXCollections.observableArrayList();
		if(_mfmap != null) _mfmap.clear();
		else _mfmap = new HashMap<Integer, MultipoleField>();
		
		int i = 1;
		if(_magnet.getMultipoleFields().size() != 0){
			for(MultipoleField mf : _magnet.getMultipoleFields().values()){
				_mfmap.put(i, mf);
				groupids.add(i);
				i++;
			}
		}
		
		FXCollections.sort(groupids);
		_groupcb.setItems(groupids);
	}
	
	public void show(){
		selectGroup(null);
	}
	
	public void selectGroup(Integer index){
		if(_magnet == null) return;
		
		ObservableList<Integer> groupids = _groupcb.getItems();
		
    	if(groupids.isEmpty()){
    		_groupcb.getSelectionModel().select(null);
    		_currenttf.clear();
    		_currenterrortf.clear();
    	    _dxtf.clear();
    	    _dytf.clear();
    	    _temperatureintf.clear();
    	    _temperatureouttf.clear();
    	    _descriptiontf.clear();
    	    
    	    _mfList.clear();
    	}
    	else{
    		MultipoleField mf = null;
    		if(index == null) index = 1;
    		else if(index == groupids.size() + 1){
    			for(int i : _mfmap.keySet()){
    				if(_mfmap.get(i).getCurrent() == 0){
    					mf = _mfmap.get(i);
    					index = i;
    					break;
    				}
    			}
    		}
    		
    		mf = _mfmap.get(index);
    		
    		if(mf == null) return;
    		
    		_groupcb.getSelectionModel().select(index);
    		_currenttf.setText(Double.toString(mf.getCurrent()));
			_currenterrortf.setText(Double.toString(mf.getCurrent_error()));
		    _dxtf.setText(Double.toString(mf.getDx()));
		    _dytf.setText(Double.toString(mf.getDy()));
		    _temperatureintf.setText(Double.toString(mf.getWater_temperature_in()));
		    _temperatureouttf.setText(Double.toString(mf.getWater_temperature_out()));
		    _descriptiontf.setText(mf.getDescription());
		    
		    _mfList.clear();
			for(MultipoleFieldData md : mf.getMultipoleFieldDatas().values()){
				_mfList.add(new MultipoleTableData(md));
			}
		    //TODO
    	}
	}
	
	public void addGroup(){
		if(_magnet.getMultipoleField(0) != null) return;
		
		_magnet.addMultipoleField(new MultipoleField(_magnet.getID(), 0));
		setGroup();
		selectGroup(_magnet.getMultipoleFields().size() + 1);
	}
	
	public void setEditable(boolean editable){
		if(_magnet == null) return;
		
		_editable = editable;
		
		_currenttf.setEditable(_editable);
		_currenterrortf.setEditable(_editable);
		_dxtf.setEditable(_editable);
		_dytf.setEditable(_editable);
		_temperatureintf.setEditable(_editable);
		_temperatureouttf.setEditable(_editable);
		_descriptiontf.setEditable(_editable);
		
		_orderlabel.setVisible(editable);
		_ordercb.setVisible(editable);
		_bnlabel.setVisible(editable);
		_bntf.setVisible(editable);
		_bntf.setEditable(editable);
		_anlabel.setVisible(editable);
		_antf.setVisible(editable);
		_antf.setEditable(editable);
		_setbn.setVisible(editable);
	}

//	private double parseDouble(String value, String name){
//		double result;
//		try{
//			result = Double.parseDouble(value);
//		} catch(NumberFormatException e){
//			result = Double.NaN;
//			FXDialog.showErrorDialog("Manget Database Manager", name + " must be a number.", null);
//		}
//		return result;
//	}
}
