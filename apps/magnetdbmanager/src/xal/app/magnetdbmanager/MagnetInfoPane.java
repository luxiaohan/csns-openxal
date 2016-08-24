package xal.app.magnetdbmanager;

//import csns.physics.database.magnet.Magnet;
//import csns.physics.database.magnet.MagnetInformation;
//import csns.physics.database.magnet.MagnetType;
import xal.app.magnetdbmanager.magnet.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MagnetInfoPane extends Pane {

	/** 字体大小 */
    private int _fontSize = 16;
    
    private Label _idlabel;
    private TextField _idtf;
    private Label _pidlabel;
    private TextField _pidtf;
    
    private Label _sortlabel;
    private ComboBox<String> _sortidcb;
    final ObservableList<String> _sortoptions = FXCollections.observableArrayList(MagnetType.getMagnetTypes());
    private Label _typelabel;
    private TextField _typetf;
    
    private Label _lengthlabel;
    private TextField _lengthtf;
    private Label _anglelabel;
    private TextField _angletf;
    private Label _e1label;
    private TextField _e1tf;
    private Label _e2label;
    private TextField _e2tf;
    
    private Label _apertypelabel;
    private ComboBox<String> _apertypecb;
    private final ObservableList<String> _aperoptions = FXCollections.observableArrayList(ApertureType.getApertureTypes());
    private Label _aperlabel;
    private TextField _apertf;
    
    private Label _disclabel;
    private TextField _disctf;
    
    private boolean _editable = false;
    private Magnet _magnet= null;
	
	public MagnetInfoPane(){
		setPadding(new Insets(5, 5, 15, 5));
    	setMinHeight(200);
		
		VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setSpacing(10);
        
        Label maginfolabel = new Label("Magnet Information:");
        maginfolabel.setFont(new Font(_fontSize));
        
        HBox hbox1 = new HBox();
        hbox1.setAlignment(Pos.CENTER_LEFT);
        hbox1.setSpacing(10);
        
        _idlabel = new Label("ID:");
        _idtf = new TextField();
        _idtf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue) _magnet.setID(_idtf.getText());
		    }
		});
        
        _pidlabel = new Label("PID:");
        _pidtf = new TextField();
        _pidtf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue) _magnet.setPID(_pidtf.getText());
		    }
		});
        
        hbox1.getChildren().addAll(_idlabel, _idtf, _pidlabel, _pidtf);
        
        HBox hbox2 = new HBox();
        hbox2.setAlignment(Pos.CENTER_LEFT);
        hbox2.setSpacing(10);
        _sortlabel = new Label("SORT:");
        _sortidcb = new ComboBox<String>();
        //_sortidcb.setItems(_sortoptions);
        _sortidcb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> selected, String oldvalue, String newvalue) {
				if (_magnet == null || newvalue == null) return;
	        	_magnet.setSort(newvalue);
	        	
	        	boolean isdipole = false;
	        	if(newvalue.equalsIgnoreCase("DIPOLE") || newvalue.equalsIgnoreCase("RBEND") || newvalue.equalsIgnoreCase("SBEND")) isdipole = true;
	        	if(isdipole || newvalue.equalsIgnoreCase("CORRECTOR")){
		    		_anglelabel.setVisible(true);
		    		_angletf.setVisible(true); _angletf.setText(Double.toString(_magnet.getInfo().getAngle()));
		    		if(isdipole){
		    			_e1label.setVisible(true);
			    		_e1tf.setVisible(true);
			    		_e2label.setVisible(true);
			    		_e2tf.setVisible(true);
			    		_e1tf.setText(Double.toString(_magnet.getInfo().getE1()));
			    		_e1tf.setText(Double.toString(_magnet.getInfo().getE2()));
		    		}
	        	}
	        	else{
	        		_anglelabel.setVisible(false);
		    		_angletf.setVisible(false);
		    		_e1label.setVisible(false);
		    		_e1tf.setVisible(false);
		    		_e2label.setVisible(false);
		    		_e2tf.setVisible(false);
		    		
		    		_angletf.setText("0");
		    		_e1tf.setText("0");
		    		_e2tf.setText("0");
	        	}
			}
        });
        
        _typelabel = new Label("TYPE:");
        _typetf = new TextField();
        _typetf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue) _magnet.getInfo().setType_ID(_typetf.getText());
		    }
		});
        hbox2.getChildren().addAll(_sortlabel, _sortidcb, _typelabel, _typetf);
        
        HBox hbox3 = new HBox();
        hbox3.setAlignment(Pos.CENTER_LEFT);
        hbox3.setSpacing(10);
        int width = 70;
        _lengthlabel = new Label("LENGTH (m):");
        _lengthtf = new TextField(); _lengthtf.setPrefWidth(width);
        _lengthtf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue){
		        	double value = Tools.parseDouble(_lengthtf.getText(), "Manget Database Manager", "Magnet length");
		        	if(value == Double.NaN) return;
		        	_magnet.getInfo().setLength(value);
		        }
		    }
		});
        
        _anglelabel = new Label("ANGLE (mrad):");
        _angletf = new TextField(); _angletf.setPrefWidth(width);
        _angletf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue){
		        	double value = Tools.parseDouble(_angletf.getText(), "Manget Database Manager", "Angle");
		        	if(value == Double.NaN) return;
		        	_magnet.getInfo().setAngle(value);
		        }
		    }
		});
        
        _e1label = new Label("E1 (mrad):");
        _e1tf = new TextField(); _e1tf.setPrefWidth(width);
        _e1tf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue){
		        	double value = Tools.parseDouble(_e1tf.getText(), "Manget Database Manager", "Dipole edge angle");
		        	if(value == Double.NaN) return;
		        	_magnet.getInfo().setE1(value);
		        }
		    }
		});
        _e2label = new Label("E2 (mrad):");
        _e2tf = new TextField(); _e2tf.setPrefWidth(width);
        _e2tf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue){
		        	double value = Tools.parseDouble(_e2tf.getText(), "Manget Database Manager", "Dipole edge angle");
		        	if(value == Double.NaN) return;
		        	_magnet.getInfo().setE2(value);
		        }
		    }
		});
        
        hbox3.getChildren().addAll(_lengthlabel, _lengthtf, _anglelabel, _angletf, _e1label, _e1tf, _e2label, _e2tf);
        
        HBox hbox4 = new HBox();
        hbox4.setAlignment(Pos.CENTER_LEFT);
        hbox4.setSpacing(10);
        _apertypelabel = new Label("APERTURE TYPE:");
        _apertypecb = new ComboBox<String>();
        _apertypecb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> selected, String oldvalue, String newvalue) {
				if (newvalue == null) return;
				
				if (_magnet != null) _magnet.getInfo().setAperture_Type(newvalue);
				
				String tooltiptext = createApertypeTooltip(newvalue);
				_apertypecb.getTooltip().setText(tooltiptext);
			}
        });
        
        _aperlabel = new Label("APERTURE (mm):");
        _apertf = new TextField(); _apertf.setMinWidth(200);
        _apertf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue){
		        	_magnet.getInfo().setAperture(_apertf.getText());
		        }
		    }
		});
        
        hbox4.getChildren().addAll(_apertypelabel, _apertypecb, _aperlabel, _apertf);
        
        HBox hbox5 = new HBox();
        hbox5.setAlignment(Pos.CENTER_LEFT);
        hbox5.setSpacing(10);
        _disclabel = new Label("DISCRIPTIOIN:");
        _disctf = new TextField(); _disctf.setMinWidth(300);
        _disctf.focusedProperty().addListener(new ChangeListener<Boolean>(){
		    @Override
		    public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue){
		        if (_magnet != null && oldPropertyValue) _magnet.getInfo().setDescription(_disctf.getText());
		    }
		});
        
        hbox5.getChildren().addAll(_disclabel, _disctf);
        
        vbox.getChildren().addAll(maginfolabel, hbox1, hbox2, hbox3, hbox4, hbox5);
        
        this.getChildren().addAll(vbox);
	}
	
	public void setMagnet(Magnet magnet){
		_magnet = magnet;
		//TODO
	}
	
	public void setEditable(boolean editable){
		_editable = editable;
		
		_idtf.setEditable(_editable);
    	_pidtf.setEditable(_editable);
    	_typetf.setEditable(_editable);
    	_lengthtf.setEditable(_editable);

		_angletf.setEditable(_editable);
		_e1tf.setEditable(_editable);
		_e2tf.setEditable(_editable);

		_apertf.setEditable(_editable);
	    _disctf.setEditable(_editable);
	    
	    if(_editable){
	    	String sort = _sortidcb.getValue();
	    	_sortidcb.setItems(_sortoptions);
	    	_sortidcb.setValue(sort);
	    	
	    	String type = _apertypecb.getValue();
	    	_apertypecb.setItems(_aperoptions);
	    	_apertypecb.setValue(type);
	    }
	}
	
	public void show(){
		if(_magnet == null){
		    clear();
		    return;
		}
		
		_idtf.setText(_magnet.getID());
		_pidtf.setText(_magnet.getPID());
		if(!_editable){
			final ObservableList<String> options = FXCollections.observableArrayList();
			options.add(_magnet.getSort());
			_sortidcb.setItems(options);
		}
		else _sortidcb.setItems(_sortoptions);
		_sortidcb.setValue(_magnet.getSort());
		
		MagnetInformation info = _magnet.getInfo();
		_typetf.setText(info.getType_ID());
		
		_lengthtf.setText(String.valueOf(info.getLength()));
		_angletf.setText(String.valueOf(info.getAngle()));
		_e1tf.setText(String.valueOf(info.getE1()));
		_e2tf.setText(String.valueOf(info.getE2()));
		
		boolean flag = false;
		if(_magnet.getSort().equalsIgnoreCase("DIPOLE") || _magnet.getSort().equalsIgnoreCase("RBEND") || _magnet.getSort().equalsIgnoreCase("SBEND")) flag = true;
		_anglelabel.setVisible(flag);
		_angletf.setVisible(flag);
		_e1label.setVisible(flag);
		_e1tf.setVisible(flag);
		_e2label.setVisible(flag);
		_e2tf.setVisible(flag);
		
		_apertypecb.setTooltip(new Tooltip());
		if(!_editable){
			final ObservableList<String> options = FXCollections.observableArrayList();
			options.add(info.getAperture_Type());
			_apertypecb.setItems(options);
		}
		else _apertypecb.setItems(_aperoptions);
		_apertypecb.setValue(info.getAperture_Type());
		
		if(_editable){
			Tooltip apertooltip = new Tooltip();
			_apertypecb.setTooltip(apertooltip);
			
			String tooltiptext = createApertypeTooltip(_apertypecb.getValue());
			apertooltip.setText(tooltiptext);
		}
		
		_apertf.setText(info.getAperture());
		
		_disctf.setText(info.getDescription());
	}
	
	public void clear(){
        _idtf.clear();
        _pidtf.clear();
        _sortidcb.setItems(_sortoptions);
        _sortidcb.setValue(null);
        _typetf.clear();
        _lengthtf.clear();
        _angletf.clear();
        _e1tf.clear();
        _e2tf.clear();
        _apertypecb.setItems(_aperoptions);
        _apertypecb.setValue(null);
        _apertf.clear();
        _disctf.clear();
	}
	
	private String createApertypeTooltip(String apertype){
		String tooltiptext = "";
		if(apertype.equalsIgnoreCase("CIRCLE"))  tooltiptext = "Radius is needed.";
		else if(apertype.equalsIgnoreCase("ELLIPSE")) tooltiptext = "Horizontal half axis and vertical half axis are needed.";
		else if(apertype.equalsIgnoreCase("RECTANGLE")) tooltiptext = "Half width and half height are needed.";
		else if(apertype.equalsIgnoreCase("LHCSCREEN")) tooltiptext = "Half width, half height (of rect.) and radius (of circ.) are neeeded.";
		else if(apertype.equalsIgnoreCase("MARGUERITE")) tooltiptext = "Half width, half height (of rect.) and radius (of circ.) are neeeded.";
		else if(apertype.equalsIgnoreCase("RECTELLIPSE")) tooltiptext = "Half widht, half height (of rectangle), horizontal half axis, vertical half axis (of ellipse).";
		else if(apertype.equalsIgnoreCase("RACETRACK")) tooltiptext = "Horizontal, vertical shift, radius shift.";
		else if(apertype.equalsIgnoreCase("OUTLINE")) tooltiptext = "A list of x and y coordinates (at least 3 pairs) outlining the shape is needed.";
		
		return tooltiptext;
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
