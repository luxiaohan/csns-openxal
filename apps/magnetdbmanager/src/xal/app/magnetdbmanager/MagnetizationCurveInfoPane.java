package xal.app.magnetdbmanager;

import java.util.ArrayList;
import java.util.HashMap;

import xal.app.magnetdbmanager.magnet.*;
import xal.extension.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

public class MagnetizationCurveInfoPane extends Pane {
	
	private Label _groupidlabel;
    private ComboBox<Integer> _groupidcb;
    private Label _curvedescriptionlabel;
    private TextField _curvedescriptiontf;
    private Button _fitbutton;
    
    private Label _effectivelengthlabel;
    private TextField _effectivelengthtf;
    
    private Label _minstdcurrentlabel;
    private TextField _minstdcurrenttf;
    private Label _maxstdcurrentlabel;
    private TextField _maxstdcurrenttf;
    
    private Label _datalabel;
    private TextArea _datata;
    private ScatterChart<Number, Number> _curvesc;
    
    private NumberAxis _xaxis;
    private NumberAxis _yaxis;
    
    private Magnet _magnet = null;
    //private HashMap<String, Magnet> _magnetsbyid = null;
    //private TableView<MagnetTableData> _magnetTableView = null;
    
    boolean _editable = false;

	public MagnetizationCurveInfoPane(){
		VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.setPadding(new Insets(15, 5, 5, 5));
        vbox.setSpacing(10);
        //vbox.prefHeightProperty().bind(pane.heightProperty());
        
        HBox hbox1 = new HBox();
        hbox1.setAlignment(Pos.CENTER_LEFT);
        hbox1.setSpacing(10);
        _groupidlabel = new Label("MAGNETIZATION CURVE ID:");
        _groupidcb = new ComboBox<Integer>();
        _groupidcb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>(){
			@Override
			public void changed(ObservableValue<? extends Integer> selected, Integer oldvalue, Integer newvalue) {
				if(_magnet == null) return;
				
				if(selected.getValue() == null) return;
				
				MagnetizationCurve curveinfo = _magnet.getMagnetizatonCurveInfo(selected.getValue());
				if(curveinfo == null){
					_curvedescriptiontf.setText("");
					_effectivelengthtf.setText("");
					_minstdcurrenttf.setText("");
					_maxstdcurrenttf.setText("");
					_datata.setText("");
				}
				else{
					_curvedescriptiontf.setText(curveinfo.getDescription());
					_effectivelengthtf.setText(String.valueOf(curveinfo.getEffectiveLength()));
					_minstdcurrenttf.setText(String.valueOf(curveinfo.getStd_Current_Min()));
					_maxstdcurrenttf.setText(String.valueOf(curveinfo.getStd_Current_Max()));
					
					_datata.setText(curveinfo.MagnetizationCurveDataToString());
				}
			}
        });
        
        _curvedescriptionlabel = new Label("DESCRIPTION:");
        _curvedescriptiontf = new TextField(); _curvedescriptiontf.setMinWidth(200);
        _curvedescriptiontf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMagnetizationCurveInfos().size() > 0)
		        	_magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()).setDescription(_curvedescriptiontf.getText());
		    }
		});
        
        _fitbutton = new Button("Fit & Fit Result");
        _fitbutton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				if(_magnet == null) return;
				if(_groupidcb.getValue() == null) return;
				
				MagnetizationCurve curve = _magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue());
				if(curve == null) return;
				
				FitDialog fd = new FitDialog(curve, _magnet.getSort(), _editable, null);
				fd.showDialog();
			}
        	
        });

        hbox1.getChildren().addAll(_groupidlabel, _groupidcb, _curvedescriptionlabel, _curvedescriptiontf, _fitbutton);
        
        HBox hbox2 = new HBox();
        hbox2.setAlignment(Pos.CENTER_LEFT);
        hbox2.setSpacing(10);
        _effectivelengthlabel = new Label("EFFECTIVE LENGTH (m):");
        _effectivelengthtf = new TextField();
        _effectivelengthtf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMagnetizationCurveInfos().size() > 0){
		        	double value = Tools.parseDouble(_effectivelengthtf.getText(), "Manget Database Manager", "Magnet effective length");
		        	if(value == Double.NaN) return;
		        	_magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()).setEffectiveLength(value);
		        }
		    }
		});
        
        hbox2.getChildren().addAll(_effectivelengthlabel, _effectivelengthtf);
        
        HBox hbox3 = new HBox();
        hbox3.setAlignment(Pos.CENTER_LEFT);
        hbox3.setSpacing(10);
        _minstdcurrentlabel = new Label("MIN STD CURRENT (A):");
        _minstdcurrenttf = new TextField();
        _minstdcurrenttf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMagnetizationCurveInfos().size() > 0){
		        	double value = Tools.parseDouble(_minstdcurrenttf.getText(), "Manget Database Manager", "The minimum magnet standardization current");
		        	if(value == Double.NaN) return;
		        	_magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()).setStd_Current_Min(value);
		        }
		    }
		});
        
        _maxstdcurrentlabel = new Label("MAX STD CURRENT (A):");
        _maxstdcurrenttf = new TextField();
        _maxstdcurrenttf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue && _magnet.getMagnetizationCurveInfos().size() > 0){
		        	double value = Tools.parseDouble(_maxstdcurrenttf.getText(), "Manget Database Manager", "The minimum magnet standardization current");
		        	if(value == Double.NaN) return;
		        	_magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()).setStd_Current_Max(value);
		        }
		    }
		});
        
        hbox3.getChildren().addAll(_minstdcurrentlabel, _minstdcurrenttf, _maxstdcurrentlabel, _maxstdcurrenttf);
        
        HBox hbox4 = new HBox();
        hbox4.setAlignment(Pos.TOP_LEFT);
        hbox4.setSpacing(10);
        
        VBox datavbox = new VBox();
        datavbox.setAlignment(Pos.TOP_LEFT);
        datavbox.setSpacing(10);
        _datalabel = new Label("Magnetic Field Data:");
        _datata = new TextArea();
        _datata.setMinSize(200, 360);
        _datata.setMaxWidth(200);
        //_datata.prefHeightProperty().bind(hbox4.heightProperty().subtract(_datalabel.heightProperty()));
        
        datavbox.getChildren().addAll(_datalabel, _datata);
        
        _xaxis = new NumberAxis();
        _xaxis.setAutoRanging(true);
        _xaxis.setLabel("Current (A)");
        _yaxis = new NumberAxis();
        _yaxis.setAutoRanging(true);
        _yaxis.setLabel("Magnetic Field");
        _curvesc = new ScatterChart<Number, Number>(_xaxis, _yaxis);
        _curvesc.setTitle("Magnetization Curve");
        _curvesc.setLegendVisible(false);
        
        _datata.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue){
		        	String magneticField = _datata.getText();

					if(magneticField != null && (!magneticField.isEmpty())){
						String[] data = magneticField.split("[\n]");
						String[] temp;
						ArrayList<Double> list = new ArrayList<Double>();
						for(int i=0; i<data.length; i++){
							temp = data[i].trim().split("[,; \t]");
							if(temp == null || temp.length == 0) continue;
							list.clear();
							for(int j=0; j<temp.length; j++){
								if(temp[j] == null || temp[j].isEmpty()) continue;
								try{
									list.add(Double.parseDouble(temp[j]));
								} catch(NumberFormatException e){
//									FXDialog.showErrorDialog("Manget Database Manager", "Magnetization data must be number.", null);
									Application.displayError("Manget Database Manager", "Magnetization data must be number.");
									return;
								}
							}
							
							if(list.size() == 0) continue;
							if(list.size() != 2){
								FXDialog.showErrorDialog("Manget Database Manager", "Only 2 numbers are allowed in one line.", null);
								return;
							}
						}
					}
		        }
		    }
		});
        
        _datata.textProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> value, String oldvalue, String newvalue) {
				String magneticField = newvalue;
				
				XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
				XYChart.Series<Number, Number> upseries = new XYChart.Series<Number, Number>();
				XYChart.Series<Number, Number> downseries = new XYChart.Series<Number, Number>();
				
				ArrayList<MagnetizationCurveData> ecurvedata = new ArrayList<MagnetizationCurveData>();
				int index = 1;
				if(magneticField != null && (!magneticField.isEmpty())){
					String[] data = magneticField.split("[\n]");
					String[] temp;
					ArrayList<Double> list = new ArrayList<Double>();
					boolean up = true;
					double Imax = 0;
					for(int i=0; i<data.length; i++){
						temp = data[i].trim().split("[,; \t]");
						if(temp == null || temp.length == 0) continue;
						list.clear();
						for(int j=0; j<temp.length; j++){
							if(temp[j] == null || temp[j].isEmpty()) continue;
							try{
								list.add(Double.parseDouble(temp[j]));
							} catch(NumberFormatException e){
								return;
							}
						}
						
						if(list.size() == 0) continue;
						if(list.size() != 2) return;
						
						ecurvedata.add(new MagnetizationCurveData(index, list.get(0), list.get(1)));
						index++;
						
						XYChart.Data<Number, Number> updowndata = new XYChart.Data<Number, Number>(list.get(0).doubleValue(), list.get(1).doubleValue());
						XYChart.Data<Number, Number> xydata = new XYChart.Data<Number, Number>(list.get(0).doubleValue(), list.get(1).doubleValue());
						if(up && Math.abs(list.get(0)) > Imax){
							Imax = Math.abs(list.get(0));
							upseries.getData().add(updowndata);
						}
						else{
							up = false;
							downseries.getData().add(updowndata);
						}
						
						series.getData().add(xydata);
					}
				}
				
				if(_magnet != null){
    				String ylabel = "Magnetic Field ";
    		        if(_magnet.getSort().equalsIgnoreCase("QUADRUPOLE")) ylabel += "(T/m m)";
    		        else if(_magnet.getSort().equalsIgnoreCase("SEXTUPOLE")) ylabel += "(T/m^2 m)";
    		        else if(_magnet.getSort().equalsIgnoreCase("OCTUPOLE")) ylabel += "(T/m^3 m)";
    		        else ylabel += "(T m)";
    		        _yaxis.setLabel(ylabel);
				}
		        
				ObservableList<XYChart.Series<Number, Number>> olist = FXCollections.observableArrayList();
				if(series.getData().size() == 0) _curvesc.getData().clear();
				else{
					olist.add(series);
					_curvesc.setData((olist));
				}
				
				if (_magnet != null && _magnet.getMagnetizationCurveInfos().size() > 0 && _groupidcb.getValue() != null){
					if(_magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()) != null){
						MagnetizationCurve tempcurve = _magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()).clone();
						tempcurve.setMagnetizationCurve(ecurvedata);
						if(!tempcurve.MagnetizationCurveDataToString().equals(_magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()).MagnetizationCurveDataToString()))
							_magnet.getMagnetizationCurveInfos().get(_groupidcb.getValue()).setMagnetizationCurve(ecurvedata);
					}
				}
			}
        });
        
        hbox4.getChildren().addAll(datavbox, _curvesc);
    	
        vbox.getChildren().addAll(hbox1, hbox2, hbox3, hbox4);
        
        this.getChildren().add(vbox);
	}
	
	public void setMagnet(Magnet magnet){
		_magnet = magnet;
	}
	
	public void removeCurrentECurve(){
		if(_magnet == null) return;
		
		int index = _groupidcb.getValue();
    	_magnet.getMagnetizationCurveInfos().remove(index);
	}
	
	public void setEditable(boolean editable){
		_editable = editable;
		
		_curvedescriptiontf.setEditable(_editable);
	    _effectivelengthtf.setEditable(_editable);
	    _minstdcurrenttf.setEditable(_editable);
	    _maxstdcurrenttf.setEditable(_editable);
	    _datata.setEditable(_editable);
	}
	
	public void selectGroup(int groupid){
		if(_magnet == null){
		    clear();
		    return;
		}
		
		final ObservableList<Integer> groupidlist = FXCollections.observableArrayList();;
    	for(int i : _magnet.getMagnetizationCurveInfos().keySet()) groupidlist.add(i);
    	FXCollections.sort(groupidlist);
    	_groupidcb.setItems(groupidlist);
    	
    	if(groupidlist.isEmpty()){
    		clear();
			return;
    	}
    	
    	if(groupid == -1) _groupidcb.setValue(groupidlist.get(0));
    	else _groupidcb.setValue(groupid);
	}
	
	public void show(){
		if(_magnet == null) return;
		
		selectGroup(-1);
	}
	
	public void clear(){
	    _groupidcb.setItems(null);
        _curvedescriptiontf.setText("");
        _effectivelengthtf.setText("");
        _minstdcurrenttf.setText("");
        _maxstdcurrenttf.setText("");
        _datata.setText("");
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
