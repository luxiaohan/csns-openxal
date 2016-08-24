/*
 * MyWindow.java
 *
 * Created on March 14, 2003, 10:25 AM
 */

package xal.app.magnetdbmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xal.app.magnetdbmanager.magnet.*;
import xal.extension.application.*;
import xal.plugin.mysql.MySQLDatabaseAdaptor;
import xal.tools.database.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

/**
 *
 * @author  Liu Weibin
 */
public class MagnetDBManagerWindow extends XalWindow{
//public class MyWindow extends XalWindow implements MagnetSelectedListener{
    private TableView<MagnetTableData> _magnetTableView = null;
    
    /** 默认的窗口宽度 */
    private int _width = 1320;
    /** 默认的窗口高度 */
    private int _height = 850;
    
    /** 默认的Magnet表格宽度 */
    private int _tablewidth = 500;
    /** 默认的Magnet表格高度 */
    private int _tableheight = _height - 150;
    
    /** 显示Magnet信息窗口的宽度 */
    //private int _infowidth = _width - _tablewidth;
    
    private CheckBox _allcbox;
    private CheckBox _dipolecbox;
    private CheckBox _quadcbox;
    private CheckBox _corrcbox;
    private CheckBox _octcbox;
    
    MagnetInfoPane _miPane;
    MagnetizationCurveInfoPane _mciPane;
    MultipoleFieldInfoPane _mfiPane;
    
    //private final MessageCenter _messageCenter;
    //private final MagnetSelectedListener _eventProxy;
    
    
    private HashMap<String, Magnet> _magnetsbyid = new HashMap<String, Magnet>();
    private ObservableList<MagnetTableData> _magnetList = FXCollections.observableArrayList();
    
    private HashMap<String, Magnet> _magnetsEditing = new HashMap<String, Magnet>();
    private Magnet _magnetEditing;
    
    final private String STATUS_DB = "DB";
    final private String STATUS_LOCAL = "LO";
    
    
    Connection _connection = null;
	Statement _statement = null;
    
    /** Creates a new instance of MainWindow */
    public MagnetDBManagerWindow(XalDocument aDocument) {
        super(aDocument);
        this.setMinimumSize(new Dimension(_width, _height));
        
        //_messageCenter = new MessageCenter("CSNSMagnetDBManager");
        //_eventProxy = (MagnetSelectedListener) _messageCenter.registerSource(this, MagnetSelectedListener.class);
        
        //_messageCenter.registerTarget(this, MagnetSelectedListener.class);

        makeContent();
    }
    
    
    /**
     * Register actions specific to this window instance. 
     * This code demonstrates how to define custom actions for menus and the toolbar for
	 * a particular window instance.
     * This method is optional.  You may similarly define actions in the document class
     * if those actions are document specific and also for the entire application if 
	 * the actions are application wide.
     * @param commander The commander with which to register the custom commands.
     * @Override
     */
    public void customizeCommands( final Commander commander ) {
    	/*
        // define a toggle "edit" action
		final ToggleButtonModel editModel = new ToggleButtonModel();
		editModel.setSelected(true);
		editModel.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				textView.setEditable( editModel.isSelected() );
				Logger.getLogger("global").log( Level.INFO, "Toggle whether text is editable." );
                System.out.println("toggled editable...");				
            }
		});
        commander.registerModel("toggle-editable", editModel);
        */
	}

    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
    	//getContentPane().add(new JButton("test"));
    	
    	//JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    	// layout的选择会影响窗口部件的缩放
    	JPanel panel = new JPanel(new BorderLayout());
    	
        final JFXPanel fxPanel = new JFXPanel();
        
        // 如果直接把fxPanel加进去，则布局上会乱。
        getContentPane().add(panel, "Center");
    	panel.add(fxPanel);
    	
    	Platform.runLater(new Runnable() {
			@Override
			public void run() {
				fxPanel.setScene(makeJavaFXScene());
			}
    	});
    }
    
    private Scene makeJavaFXScene(){
    	// 把界面分成左右两个区域，左侧存放磁铁表格，右侧磁铁具体信息
    	SplitPane hsp = createSplitPane();

    	Scene scene = new Scene(hsp, Color.ALICEBLUE);
    	
    	return scene;
    }
    
    /**
     * 把界面分成左右两个区域，左侧存放磁铁表格，右侧磁铁具体信息
     * 
     * @return SplitPane
     */
    private SplitPane createSplitPane(){
    	SplitPane hsp = new SplitPane();
    	hsp.setOrientation(Orientation.HORIZONTAL);
    	
    	/** 创建左侧区域 */
    	Pane tablepane = createTablePane();
    	
    	/** 创建中间区域 */
    	Pane infopane = createInfoPane();
    	
    	/** 创建右侧区域 */
//    	Pane buttonpane = createButtonPane();
    	
//    	hsp.getItems().addAll(tablepane, infopane, buttonpane);
//    	hsp.setDividerPositions(0.4, 0.4);
    	hsp.getItems().addAll(tablepane, infopane);
    	hsp.setDividerPositions(0.4);
        
        return hsp;
    }
    
    /**
     * 创建存放Magnet表格的区域。
     * @return Pane
     */
    private Pane createTablePane(){
    	Pane pane = new Pane();
    	pane.setMaxWidth(_tablewidth + 300);
    	
    	pane.setMinWidth(_tablewidth + 10);
    	pane.setMinHeight(_tableheight + 50);

    	VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(4);

        pane.getChildren().add(vbox);
        
        
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setPrefSize(_tablewidth, 60);
        hbox.setAlignment(Pos.CENTER);
        
        final Label csnsmagnetlabel = new Label("CSNS Magnets");
        csnsmagnetlabel.setFont(new Font("Arial", 20));
        csnsmagnetlabel.setMinWidth(300);
        
        final Button fetchmagnetsbutton = new Button("Fetch");
        
        final RadioButton fromlocalbutton = new RadioButton("from Local");
        final RadioButton fromdbbutton = new RadioButton("from Database");
        ToggleGroup fetchfromgroup = new ToggleGroup();
        fromlocalbutton.setToggleGroup(fetchfromgroup);
        fromdbbutton.setToggleGroup(fetchfromgroup);
        fromlocalbutton.setSelected(true);
        
        VBox fetchfromvbox = new VBox();
        fetchfromvbox.setSpacing(2);
        fetchfromvbox.setAlignment(Pos.CENTER_LEFT);
        fetchfromvbox.getChildren().addAll(fromlocalbutton, fromdbbutton);
        
        hbox.getChildren().addAll(csnsmagnetlabel, fetchmagnetsbutton, fetchfromvbox);
        
        fetchmagnetsbutton.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent arg0) {
				String suffix;
				if(fromlocalbutton.isSelected()){
					if(!_magnetsbyid.isEmpty()){
//						if(FXDialog.showConfirmDialog("Magnet Database Manager", "Fetching will clean all the work in the workspace. Are you sure to continue to fetch magnets from the local files?", null) == FXDialog.Response.NO) return;
						if(Application.displayConfirmDialog("Magnet Database Manager", "Fetching will clean all the work in the workspace. Are you sure to continue to fetch magnets from the local files?") == Application.NO_OPTION) return;
					}
					
					String bakpath = ((MagnetDBManagerDocument) document).getBackupPath();
	        		if(bakpath == null || bakpath.isEmpty()) bakpath = System.getProperty("user.home");
	        		
	        		FileChooser fileChooser = new FileChooser();
	        		fileChooser.setTitle("Open Local Manget File");
	        		fileChooser.setInitialDirectory(new File(bakpath));
	        		fileChooser.getExtensionFilters().addAll(
	        				new FileChooser.ExtensionFilter("XML Files", "*.xml"),
	        				new FileChooser.ExtensionFilter("All Files", "*.*")
	        				);
	        		
	        		List<File> files = fileChooser.showOpenMultipleDialog(null);
	        		
	        		if(files == null) return;
	        		
	        		SwingUtilities.invokeLater(new Runnable() {
	        		    @Override
	        		    public void run() {
	        		    	document.setHasChanges(false);
	        		    }
	        		});
	        		//document.setHasChanges(false);
					_magnetTableView.getSelectionModel().clearSelection();
					
					_magnetsbyid.clear();
					_magnetList.clear();
					_magnetsEditing.clear();
					_magnetEditing = null;
			        
					int index = 0;
					for(File file : files){
						try {
							XmlDataAdaptor xda;
							xda = XmlDataAdaptor.adaptorForFile(file, false);
							
							Magnet magnet = Magnet.ParseXMLToMagnet(xda);
							if(magnet != null){
								boolean flag = false;
								if(_allcbox.isSelected()) flag = true;
								else if(_dipolecbox.isSelected() &&
										(magnet.getSort().equalsIgnoreCase("DIPOLE")
												|| magnet.getSort().equalsIgnoreCase("RBEND")
												|| magnet.getSort().equalsIgnoreCase("SBEND"))) flag = true;
								else if(_quadcbox.isSelected() && magnet.getSort().equalsIgnoreCase("QUADRUPOLE")) flag = true;
								else if(_corrcbox.isSelected() && magnet.getSort().equalsIgnoreCase("CORRECTOR")) flag = true;
								else if(_octcbox.isSelected() && magnet.getSort().equalsIgnoreCase("OCTUPOLE")) flag = true;
								
								if(flag){
									SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmm");
									index = Integer.parseInt(df.format(magnet.getDate()));
									
									_magnetsbyid.put(magnet.getID(), magnet);
									MagnetTableData magdata = new MagnetTableData(index, magnet);
									magdata.setStatus(STATUS_LOCAL);
							    	_magnetList.add(magdata);
								}
							}
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (ResourceNotFoundException e) {
							e.printStackTrace();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
					
					suffix = " Local:";
				}
				else{
					if(_connection == null){
//						FXDialog.showWarningDialog("Manget Database Manager", "Connect CSNS Magnet Database first.", null);
						Application.displayWarning("Manget Database Manager", "Connect CSNS Magnet Database first.");
						return;
					}
					
					if(!_magnetsbyid.isEmpty()){
//						if(FXDialog.showConfirmDialog("Magnet Database Manager", "Fetching will clean all the work in the workspace. Are you sure to continue to fetch magnets from the database?", null) == FXDialog.Response.NO) return;
						if(Application.displayConfirmDialog("Magnet Database Manager", "Fetching will clean all the work in the workspace. Are you sure to continue to fetch magnets from the database?") == Application.NO_OPTION) return;
					}
	        		
					// fetch magnets from database ......
					
					SwingUtilities.invokeLater(new Runnable() {
	        		    @Override
	        		    public void run() {
	        		    	document.setHasChanges(false);
	        		    }
	        		});
					_magnetTableView.getSelectionModel().clearSelection();
					
					_magnetsbyid.clear();
					_magnetList.clear();
					_magnetsEditing.clear();
					_magnetEditing = null;

					QueryMagnetsFromDB();

					suffix = " Database:";
				}
				csnsmagnetlabel.setText("CSNS Magnets from" + suffix);
				_magnetTableView.autosize();
			}
        });
        
        
        HBox filterhbox = new HBox();
        filterhbox.setSpacing(15);
        filterhbox.setPrefWidth(_tablewidth);
        filterhbox.setAlignment(Pos.TOP_CENTER);
        
        _allcbox = new CheckBox("All");
        _allcbox.setSelected(true);
        _dipolecbox = new CheckBox("Dipole");
        _quadcbox = new CheckBox("Quadrupole");
        _corrcbox = new CheckBox("Corrector");
        _octcbox = new CheckBox("Octupole");
        
        _dipolecbox.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> value, Boolean oldvalue, Boolean newvalue) {
				if(newvalue) _allcbox.setSelected(false);
			}
        });
        
        _quadcbox.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> value, Boolean oldvalue, Boolean newvalue) {
				if(newvalue) _allcbox.setSelected(false);
			}
        });
        
        _corrcbox.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> value, Boolean oldvalue, Boolean newvalue) {
				if(newvalue) _allcbox.setSelected(false);
			}
        });
        
        _octcbox.selectedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> value, Boolean oldvalue, Boolean newvalue) {
				if(newvalue) _allcbox.setSelected(false);
			}
        });
        
        filterhbox.getChildren().addAll(_allcbox, _dipolecbox, _quadcbox, _corrcbox, _octcbox);
        
        
        createCSNSMagnetTable();
        _magnetTableView.prefWidthProperty().bind(pane.widthProperty());
        _magnetTableView.prefHeightProperty().bind(pane.heightProperty().subtract(70));
        
        vbox.getChildren().addAll(hbox, filterhbox, _magnetTableView);

    	return pane;
    }
    
    /**
     * 创建现实Magnet的TableView。
     */
    private void createCSNSMagnetTable(){
    	_magnetTableView = new TableView<MagnetTableData>();
    	_magnetTableView.setEditable(false);
    	//_tableView.setPadding(new Insets(5, 5, 5, 5));
    	_magnetTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        _magnetTableView.setMinSize(_tablewidth - 80, _tableheight);
        
        _magnetTableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MagnetTableData>(){
			@Override
			public void changed(ObservableValue<? extends MagnetTableData> observable, MagnetTableData oldvalue, MagnetTableData newvalue) {
				if(newvalue != null) MagnetSelected(newvalue);
			}
        	
        });
        
        final ContextMenu menu = new ContextMenu();
        final MenuItem newitem = new MenuItem("New");
        final MenuItem edititem = new MenuItem("Edit");
        final MenuItem addecitem = new MenuItem("Add a magnetization curve");
        final MenuItem addmfitem = new MenuItem("Add a set of multipole field");
        final MenuItem removeitem = new MenuItem("Remove current magnetization curve");
        final MenuItem discarditem = new MenuItem("Discard changes");
        final MenuItem deleteitem = new MenuItem("Delete");
        final MenuItem saveitem = new MenuItem("Save to local file");
        final MenuItem uploaditem = new MenuItem("Upload this magnet");
        
        newitem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
            	int index = 1;
            	if(_magnetList != null) index = _magnetList.size() + 1;
            	String id = "ID_" + index;
            	String pid = "PID_" + index;
            	
            	while(_magnetsbyid.get(id) != null){
            		id = id.concat("*");
            		pid = pid.concat("*");
            	}
            	
            	MagnetTableData mtd = new MagnetTableData(index, new Magnet(id, pid, "DIPOLE"));
            	mtd.setStatus("**");

            	_magnetsbyid.put(id, mtd.getMagnet());
            	_magnetList.add(mtd);
            	_magnetsEditing.put(mtd.getID(), mtd.getMagnet().clone());
            	SwingUtilities.invokeLater(new Runnable() {
        		    @Override
        		    public void run() {
        		    	document.setHasChanges(true);
        		    }
        		});
            }
        });
        
        edititem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
            	MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
            	String status = tabledata.getStatus();
            	
            	if(!status.startsWith("*")){
            		tabledata.setStatus("*" + status);
            		_magnetsEditing.put(tabledata.getID(), tabledata.getMagnet().clone());
            		SwingUtilities.invokeLater(new Runnable() {
	        		    @Override
	        		    public void run() {
	        		    	document.setHasChanges(true);
	        		    }
	        		});
            	}
            	
            	_magnetEditing = _magnetsEditing.get(tabledata.getID());
            	
            	_miPane.setMagnet(_magnetEditing);
        		_miPane.show();
        		
        		_mciPane.setMagnet(_magnetEditing);
        		_mciPane.show();
        		
        		_mfiPane.setMagnet(_magnetEditing);
        		_mfiPane.show();
        		
                setEditable(true);
            }
        });
        
        addecitem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
            	MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
            	
            	if(_magnetEditing == null) return;
            	
            	int newgroupid = _magnetEditing.getMagnetizationCurveInfos().size() + 1;
            	for(int i = 1; i <= _magnetEditing.getMagnetizationCurveInfos().size(); i++){
            		if(_magnetEditing.getMagnetizationCurveInfos().get(i) == null){
            			newgroupid = i;
            			break;
            		}
            	}
            	
            	MagnetizationCurve newcurve = new MagnetizationCurve(tabledata.getID());
            	newcurve.setGroupID(newgroupid);
            	_magnetEditing.addMagnetizationCurve(newcurve);
            	
            	_mciPane.selectGroup(newgroupid);
            }
        });
        
        addmfitem.setOnAction(new EventHandler<ActionEvent>(){
        	public void handle(ActionEvent e){
//        		MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
            	
            	if(_magnetEditing == null) return;
            	
            	_mfiPane.addGroup();
        	}
        });
        
        removeitem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
            	MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
            	
            	_mciPane.removeCurrentECurve();
            	
            	MagnetSelected(tabledata);
            }
        });
        
        discarditem.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e) {
        		MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
        		String id = tabledata.getID();
        		
//        		if(FXDialog.showConfirmDialog("Magnet Database Manager", "Are you sure to discard all changes of this magnet (ID = " + id + ")?", null) == FXDialog.Response.NO) return;
        		if(Application.displayConfirmDialog("Magnet Database Manager", "Are you sure to discard all changes of this magnet (ID = " + id + ")?") == Application.NO_OPTION) return;
        		
            	String status = tabledata.getStatus();
            	tabledata.setStatus(status.substring(1));
            	
            	_magnetsEditing.remove(id);
            	if(_magnetsEditing.size() == 0){
            		SwingUtilities.invokeLater(new Runnable() {
	        		    @Override
	        		    public void run() {
	        		    	document.setHasChanges(false);
	        		    }
	        		});
            	}
            	_magnetEditing = null;
            	
            	MagnetSelected(tabledata);
        	}
        });
        
        deleteitem.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e) {
        		MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
        		String id = tabledata.getID();
        		String status = tabledata.getStatus();
        		
        		String content = "Are you sure to delete this magnet (ID = " + id + ") from CSNS Database?";
        		if(status.equals(STATUS_LOCAL) || status.equals("*" + STATUS_LOCAL)) content = "Are you sure to delete this magnet (ID = " + id + ") from the list?";
//        		if(FXDialog.showConfirmDialog("Magnet Database Manager", content, null) == FXDialog.Response.NO) return;
        		if(Application.displayConfirmDialog("Magnet Database Manager", content) == Application.NO_OPTION) return;
            	
            	_magnetsEditing.remove(id);
            	if(_magnetsEditing.size() == 0){
            		SwingUtilities.invokeLater(new Runnable() {
	        		    @Override
	        		    public void run() {
	        		    	document.setHasChanges(false);
	        		    }
	        		});
            	}
            	_magnetEditing = null;
            	
            	if(status.equals(STATUS_DB) || status.equals("*" + STATUS_DB)){
	            	String delete = "delete from magnet_information_table where id='" + id +"';";
	        		String deletecurve = "delete from magnetization_curve_table where id='" + id +"';";
	        		String deleteinfo = "delete from magnetization_curve_information_table where id='" + id +"';";
	        		
	        		try {
	        			String[] command = (delete + deletecurve + deleteinfo).split(";");
	        			for(int i=0; i<command.length; i++) _statement.addBatch(command[i]);
	        			
	        			_statement.executeBatch();
	        			_statement.clearBatch();
	        			Logger.getLogger("global").log( Level.INFO, "Maget (ID=" + id + ") is deleted from CSNS magnet Database." );
	        		} catch (SQLException exception) {
//	        			FXDialog.showErrorDialog("Manget Database Manager", "Deleting manget from CSNS Magnet Database failed!", null);
	        			Application.displayError("Manget Database Manager", "Deleting manget from CSNS Magnet Database failed!");
	        			exception.printStackTrace();
	        			return;
	        		}
        		}
        		
            	_magnetList.remove(tabledata);
            	_magnetsbyid.remove(id);
            	
            	MagnetSelected(_magnetTableView.getSelectionModel().getSelectedItem());
        	}
        });
        
        saveitem.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
            	FileChooser fileChooser = new FileChooser();
        		fileChooser.setTitle("Save Magnet to a Local File");
        		fileChooser.getExtensionFilters().addAll(
        				new FileChooser.ExtensionFilter("XML Files", "*.xml"),
        				new FileChooser.ExtensionFilter("All Files", "*.*")
        				);
        		
        		File file = fileChooser.showSaveDialog(null);
        		
        		if(file == null) return;
        		SaveMagnetToFile(_magnetTableView.getSelectionModel().getSelectedItem().getMagnet(), file);
            }
        });
        
        uploaditem.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent e) {
        		String bakpath = ((MagnetDBManagerDocument) document).getBackupPath();
        		if(bakpath == null || bakpath.isEmpty()){
//        			FXDialog.showWarningDialog("Manget Database Manager", "Default backup folder must be set before upload any magnet data!", null);
        			Application.displayWarning("Manget Database Manager", "Default backup folder must be set before upload any magnet data!");
        			return;
        		}
        		
        		MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
        		String id = tabledata.getID();
        		String pid = tabledata.getPID();
        		int index = tabledata.getIndex();
        		String status = tabledata.getStatus();
        		
        		String newid = _magnetEditing.getID();
        		String newpid = _magnetEditing.getPID();
        		
        		if(_magnetEditing.equals(tabledata.getMagnet()) && (!status.equals("**"))){
//        			FXDialog.showWarningDialog("Manget Database Manager", "There is not any changes. Uploading discarded.", null);
        			Application.displayWarning("Manget Database Manager", "There is not any changes. Uploading discarded.");
        			return;
        		}
        		
        		if(!newid.equalsIgnoreCase(id) && _magnetsbyid.get(newid) != null && !status.startsWith("*LO")){
//        			FXDialog.showWarningDialog("Manget Database Manager", "There is another magnet with the same ID (" + _magnetEditing.getID() + "). Change the ID and upload again.", null);
        			Application.displayWarning("Manget Database Manager", "There is another magnet with the same ID (" + _magnetEditing.getID() + "). Change the ID and upload again.");
        			return;
        		}
        		if(!newpid.equalsIgnoreCase(pid) && !status.startsWith("*LO")){
	    			for(MagnetTableData mtd : _magnetList){
	    				if(mtd.getPID().equalsIgnoreCase(newpid)){
//	    					FXDialog.showWarningDialog("Manget Database Manager", "There is another magnet with the same PID (" + _magnetEditing.getPID() + "). Change the PID and upload again.", null);
	    					Application.displayWarning("Manget Database Manager", "There is another magnet with the same PID (" + _magnetEditing.getPID() + "). Change the PID and upload again.");
	            			return;
	    				}
	    			}
    			}
        		
        		String query = "SELECT 'index' FROM magnet_information_table WHERE ID = '" + newid + "' or PID = '" + newpid + "';";
        		try {
        			_statement.execute(query);
        			ResultSet rs = _statement.getResultSet();
        			rs.last();
        			int row = rs.getRow();
					if(row > 1){
//						FXDialog.showWarningDialog("Manget Database Manager", "There is another magnet with the same ID (" + newid + ") or PID (" + newpid + ")! Change the ID or PID and upload again.", null);
						Application.displayWarning("Manget Database Manager", "There is another magnet with the same ID (" + newid + ") or PID (" + newpid + ")! Change the ID or PID and upload again.");
            			return;
					}
					else if(row == 1 && status.equals("**")){
//						FXDialog.showWarningDialog("Manget Database Manager", "There is another magnet with the same ID (" + newid + ") or PID (" + newpid + ")! Reload it from the database again and modify it. Or change the ID and PID.", null);
						Application.displayWarning("Manget Database Manager", "There is another magnet with the same ID (" + newid + ") or PID (" + newpid + ")! Reload it from the database again and modify it. Or change the ID and PID.");
            			return;
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
//					FXDialog.showErrorDialog("Manget Database Manager", "Query CSNS Magnet Database failed!", null);
					Application.displayError("Manget Database Manager", "Query CSNS Magnet Database failed!");
					return;
				}
        		
        		_magnetEditing.setDate(new Date());
        		if(!BackupMagnet(_magnetEditing)){
//        			FXDialog.showErrorDialog("Manget Database Manager", "Backup magnet (ID=" + id + ") failed! Before upload magnet to database, backup locally is necessary. Upload cancelled", null);
        			Application.displayError("Manget Database Manager", "Backup magnet (ID=" + id + ") failed! Before upload magnet to database, backup locally is necessary. Upload cancelled");
					return;
        		}
        		
        		MagnetTableData magnettabledata = tabledata;
        		Magnet magnet = _magnetEditing.clone();
        		System.out.println(_magnetEditing);
        		System.out.println("********");
        		if(uploadMagnet(_magnetEditing)){
        			Logger.getLogger("global").log( Level.INFO, "Maget (ID=" + id + ") is uploaded to CSNS magnet Database." );
        			
    				_magnetsbyid.remove(id);
    				_magnetList.remove(tabledata);
    				_magnetsbyid.put(newid, magnet);
    				
    				magnettabledata = new MagnetTableData(index, magnet);
    				magnettabledata.setStatus(STATUS_DB);
    				_magnetList.add(magnettabledata);
    				FXCollections.sort(_magnetList);
        			
        			_magnetsEditing.remove(id);
        			if(_magnetsEditing.size() == 0){
        				SwingUtilities.invokeLater(new Runnable() {
    	        		    @Override
    	        		    public void run() {
    	        		    	document.setHasChanges(false);
    	        		    }
    	        		});
        			}
        			_magnetEditing = null;
        			
        			_magnetTableView.getSelectionModel().select(magnettabledata);
        		}
        		else{
//        			FXDialog.showErrorDialog("Manget Database Manager", "Upload magnet (ID=" + id + ") to database failed! Check the network and try again.", null);
        			Application.displayError("Manget Database Manager", "Upload magnet (ID=" + id + ") to database failed! Check the network and try again.");
        		}
        	}
        });
        
        menu.getItems().addAll(newitem, edititem, addecitem, addmfitem, removeitem, discarditem, deleteitem, saveitem, uploaditem);

        _magnetTableView.setContextMenu(menu);
        
        menu.setOnShowing(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
            	MagnetTableData tabledata = _magnetTableView.getSelectionModel().getSelectedItem();
                if(_magnetsbyid.isEmpty() || tabledata == null){
                	newitem.setDisable(false);
                	edititem.setDisable(true);
                	addecitem.setDisable(true);
                	addmfitem.setDisable(true);
                	removeitem.setDisable(true);
                	discarditem.setDisable(true);
                	deleteitem.setDisable(true);
                	saveitem.setDisable(true);
                	uploaditem.setDisable(true);
                	return;
                }
               
            	String status = tabledata.getStatus();
            	boolean editing = false;
            	if(status.startsWith("*")) editing = true;
            	     	
            	edititem.setDisable(editing);
            	addecitem.setDisable(!editing);
            	addmfitem.setDisable(!editing);
            	if(editing && _magnetEditing.getMagnetizationCurveInfos().size() > 0) removeitem.setDisable(false);
            	else removeitem.setDisable(true);
            	discarditem.setDisable(!editing);
            	if(status.equals("**")) discarditem.setDisable(true);
            	deleteitem.setDisable(false);
            	saveitem.setDisable(false);
            	uploaditem.setDisable(!editing);

            	setEditable(editing);
            }
        });
        
//        EventHandler<MouseEvent> event = new EventHandler<MouseEvent>() {
//            @Override
//            public void handle(MouseEvent me) {
//                if (me.getButton() == MouseButton.SECONDARY) {
//                	//if(_tableView.getSelectionModel().getSelectedItem() == null) _tableView.getContextMenu().hide();
//                	//else _tableView.getContextMenu().show(_tableView, me.getScreenX(), me.getScreenY());
//                	//else menu.show(_tableView, me.getScreenX(), me.getScreenY());
//                	menu.hide();
//                	System.out.println("hide");
//                }
//            }
//        };
//        _tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event);
        
        TableColumn<MagnetTableData, String> indexColumn = new TableColumn<MagnetTableData, String>("Index");
        TableColumn<MagnetTableData, String> pidColumn = new TableColumn<MagnetTableData, String>("PID");
        TableColumn<MagnetTableData, String> idColumn = new TableColumn<MagnetTableData, String>("ID");
        TableColumn<MagnetTableData, String> sortColumn = new TableColumn<MagnetTableData, String>("Sort");
        TableColumn<MagnetTableData, String> statusColumn = new TableColumn<MagnetTableData, String>("Status");
        
        indexColumn.prefWidthProperty().bind(_magnetTableView.widthProperty().multiply(0.05));
        pidColumn.prefWidthProperty().bind(_magnetTableView.widthProperty().multiply(0.3));
        idColumn.prefWidthProperty().bind(_magnetTableView.widthProperty().multiply(0.3));
        sortColumn.prefWidthProperty().bind(_magnetTableView.widthProperty().multiply(0.3));
        statusColumn.prefWidthProperty().bind(_magnetTableView.widthProperty().multiply(0.05));
        
        
        indexColumn.setCellValueFactory(new PropertyValueFactory<MagnetTableData, String>("index"));
        pidColumn.setCellValueFactory(new PropertyValueFactory<MagnetTableData, String>("pid"));
        idColumn.setCellValueFactory(new PropertyValueFactory<MagnetTableData, String>("id"));
        sortColumn.setCellValueFactory(new PropertyValueFactory<MagnetTableData, String>("sort"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<MagnetTableData, String>("status"));
        
        _magnetTableView.getColumns().add(indexColumn);
        _magnetTableView.getColumns().add(pidColumn);
        _magnetTableView.getColumns().add(idColumn);
        _magnetTableView.getColumns().add(sortColumn);
        _magnetTableView.getColumns().add(statusColumn);
        
        _magnetTableView.setItems(_magnetList);
    	
    }
    
    /**
     * 创建存放磁铁信息的区域，分为上下两个部分，分别存放磁铁基本信息和励磁曲线信息。
     * @return Pane
     */
    private Pane createInfoPane(){
    	Pane pane = new Pane();
    	pane.setMinWidth(650);
    	
    	VBox vbox = new VBox();
        vbox.setAlignment(Pos.TOP_LEFT);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        vbox.setSpacing(20);
        vbox.prefWidthProperty().bind(pane.widthProperty());
        vbox.prefHeightProperty().bind(pane.heightProperty());
        
        // show fist part of the magnet information
        _miPane = new MagnetInfoPane();
        _miPane.setMagnet(_magnetEditing);
    	
    	// show second part of the magnet information
    	TabPane tabpane = new TabPane();
    	tabpane.prefHeightProperty().bind(pane.heightProperty().subtract(_miPane.heightProperty()));
    	
    	Tab ecurvetab = new Tab("Magnetization Curve");
    	Tab multifieldtab = new Tab("Multipole Field");
    	
    	tabpane.getTabs().addAll(ecurvetab, multifieldtab);
    	
    	_mciPane = new MagnetizationCurveInfoPane();
    	_mciPane.setMagnet(_magnetEditing);
    	
    	ecurvetab.setClosable(false);
    	ecurvetab.setContent(_mciPane);
    	
    	_mfiPane = new MultipoleFieldInfoPane();
    	_mfiPane.setMagnet(_magnetEditing);
    	
    	multifieldtab.setClosable(false);
    	multifieldtab.setContent(_mfiPane);
        
    	setEditable(false);
    	
        vbox.getChildren().addAll(_miPane, tabpane);
        
        pane.getChildren().add(vbox);
    	
    	return pane;
    }
    
    private void setEditable(boolean editable){
    	_miPane.setEditable(editable);
	    
	    _mciPane.setEditable(editable);
	    _mfiPane.setEditable(editable);
    }

	private void MagnetSelected(MagnetTableData magnetdata) {
	    if(magnetdata == null){
	        _miPane.clear();
	        _mciPane.clear();
	        return;
	    }
		String id = magnetdata.getID();
		boolean isediting;
		Magnet magnet;
		if(_magnetsEditing.containsKey(id)){
			magnet = _magnetsEditing.get(id);
			_magnetEditing = magnet;
			isediting = true;
		}
		else{
			magnet = magnetdata.getMagnet();
			isediting = false;
		}
		
		_miPane.setMagnet(magnet);
		_miPane.show();
		
		_mciPane.setMagnet(magnet);
		_mciPane.show();
		
		_mfiPane.setMagnet(magnet);
		_mfiPane.show();

		setEditable(isediting);
	}
	
	public Connection ConnectDB(){
		final ConnectionDictionary dictionary = ConnectionDictionary.getPreferredInstance( "reports" );
		final ConnectionDialog dialog = ConnectionDialog.getInstance( this, dictionary );
		//Connection connection = dialog.showConnectionDialog( new MariaDBDatabaseAdaptor());
		Connection connection = dialog.showConnectionDialog( new MySQLDatabaseAdaptor());//LIYONG MODIFY
		
		if(connection == null) return null;
		
		try {
			Statement statement = connection.createStatement();
			
			String databasename = "`physics_magnet_database`";
			String selectDB = "USE " + databasename;
			statement.execute(selectDB);
			
			_connection = connection;
			_statement = statement;
			
			Logger.getLogger("global").log( Level.INFO, "Changed to physics_magnet_database." );
		} catch (SQLException e) {
			displayWarning("Manget Database Manager", "Changing to physics_magnet_database failed. Connect the database again.");
			e.printStackTrace();
			return null;
		}
		
		displayWarning("Manget Database Manager", "Connection to CSNS magnet database successfully.");
		
		return _connection;
	}
	
	//联接数据库
//	protected boolean ConnectCSNSPhysicsMagnetDB(){
//		try {
//			Class.forName("org.mariadb.jdbc.Driver");
//			
//			//_connection = DriverManager.getConnection("jdbc:mysql://192.168.36.20/?user=root&password=csnsapgroup");
//			_connection = DriverManager.getConnection("jdbc:mysql://192.168.36.20/","root", "csnsapgroup");
//			
//			if(_connection != null) _statement = _connection.createStatement();
//			else{
//				FXDialog.showWarningDialog("Manget Database Manager", "Failed to connect to CSNS Magnet Database. Check you network.", null);
//				return false;
//			}
//		} catch (SQLException e) {
//			FXDialog.showWarningDialog("Manget Database Manager", "Failed to connect to CSNS Magnet Database. Check you network.", null);
//			e.printStackTrace();
//			return false;
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		String databasename = "`physics_magnet_database`";
//		String selectDB = "USE " + databasename;
//		try {
//			_statement.execute(selectDB);
//		} catch (SQLException e) {
//			FXDialog.showWarningDialog("Manget Database Manager", "Failed to change to physics_magnet_database.", null);
//			e.printStackTrace();
//			return false;
//		}
//		
//		return true;
//	}
	
	protected void QueryMagnetsFromDB(){
		try {
			String query = "select * from magnet_information_table";

			if(!_allcbox.isSelected()){
				String[] filters = new String[4];
				if(_dipolecbox.isSelected()) filters[0] = "SORT_ID='DIPOLE'";
				if(_quadcbox.isSelected()) filters[1] = "SORT_ID='QUADRUPOLE'";
				if(_corrcbox.isSelected()) filters[2] = "SORT_ID='CORRECTOR'";
				if(_octcbox.isSelected()) filters[3] = "SORT_ID='OCTUPOLE'";
				
				String filter = " where ";
				int filterno = 0;
				for(int i=0; i<filters.length; i++){
					if(filters[i] != null){
						if(filterno > 0) filter = filter + " or " + filters[i];
						else filter = filter + filters[i];
						filterno++;
					}
				}
				if(filterno > 0) query = query + filter;
			}
			
			ResultSet rs = _statement.executeQuery(query);
			
			QueryResultToTable(rs);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
	
	protected void QueryResultToTable(ResultSet rs){
		try {
			int i = 0;
			rs.beforeFirst();
			while(rs.next()){
				i++;
				
				String id = rs.getString("ID");
				String pid = rs.getString("PID");
				String sort = rs.getString("SORT_ID");
				
				Magnet magnet = new Magnet(id, pid, sort);
				MagnetInformation magnetInfo = new MagnetInformation(id, pid, sort);
				
				magnet.setInfo(magnetInfo);
				
				magnetInfo.setType_ID(rs.getString("TYPE_ID"));
				
				magnetInfo.setLength(rs.getDouble("LENGTH"));
				
				magnetInfo.setAperture_Type(rs.getString("APERTURE_TYPE"));
				magnetInfo.setAperture(rs.getString("APERTURE"));
				
				magnetInfo.setAngle(rs.getDouble("ANGLE"));
				magnetInfo.setE1(rs.getDouble("E1"));
				magnetInfo.setE2(rs.getDouble("E2"));
				
				magnetInfo.setDescription(rs.getString("DESCRIPTION"));
				
				
				String curveinfoquery = "select * from magnetization_curve_information_table where ID = '" + id + "';";
				ResultSet curveinfors = _statement.executeQuery(curveinfoquery);
				String curvequery = "select * from magnetization_curve_table where id = '" + id+ "';";
				ResultSet curvers = _statement.executeQuery(curvequery);
				
				String itobcoequery = "select * from i_to_b_coe_table where ID = '" + id + "';";
				ResultSet itoblcoers = _statement.executeQuery(itobcoequery);
				String btoicoequery = "select * from b_to_i_coe_table where ID = '" + id + "';";
				ResultSet btoicoers = _statement.executeQuery(btoicoequery);
				
				curveinfors.beforeFirst();
				while(curveinfors.next()){
					int groupid = curveinfors.getInt("group_id");
					
					MagnetizationCurve curveinfo = new MagnetizationCurve(id);
					curveinfo.setEffectiveLength(curveinfors.getDouble("effective_length"));
					curveinfo.setStd_Current_Min(curveinfors.getDouble("std_current_min"));
					curveinfo.setStd_Current_Max(curveinfors.getDouble("std_current_max"));
					
					curveinfo.setGroupID(new Integer(groupid));
					
					String text = curveinfors.getString("description");
					if(text == null) text = "";
					curveinfo.setDescription(text);
					
					magnet.addMagnetizationCurve(curveinfo);
					
					String idgroupid = curveinfors.getString("id_group_id");
					curvers.beforeFirst();
					while(curvers.next()){
						if(idgroupid.equalsIgnoreCase(curvers.getString("id_group_id"))){
							MagnetizationCurveData mcd = new MagnetizationCurveData(curvers.getInt("order"), curvers.getDouble("current"), curvers.getDouble("BL"));
							curveinfo.addMagnetizationCurveData(mcd);
						}
					}
					
					double itobave = 0;
					Map<Integer, Double> itobcoemap = new HashMap<Integer, Double>();
					itobcoemap.clear();
					itoblcoers.beforeFirst();
					while(itoblcoers.next()){
						if(idgroupid.equalsIgnoreCase(itoblcoers.getString("id_group_id"))){
							itobave = itoblcoers.getDouble("average");
							itobcoemap.put(itoblcoers.getInt("order"), itoblcoers.getDouble("coe"));
						}
					}
					MCFitResult itob = new MCFitResult(itobave, itobcoemap);
					curveinfo.setItoBFitResult(itob);
					
					double btoiave = 0;
					Map<Integer, Double> btoicoemap = new HashMap<Integer, Double>();
					btoicoemap.clear();
					btoicoers.beforeFirst();
					while(btoicoers.next()){
						if(idgroupid.equalsIgnoreCase(btoicoers.getString("id_group_id"))){
							btoiave = btoicoers.getDouble("average");
							btoicoemap.put(btoicoers.getInt("order"), btoicoers.getDouble("coe"));
						}
					}
					MCFitResult btoi = new MCFitResult(btoiave, btoicoemap);
					curveinfo.setBtoIFitResult(btoi);
				}
				
				
				String mfinfoquery = "select * from multipole_field_information_table where ID = '" + id + "';";
				ResultSet mfinfors = _statement.executeQuery(mfinfoquery);
				String mfquery = "select * from multipole_field_table where id = '" + id+ "';";
				ResultSet mfrs = _statement.executeQuery(mfquery);
				
				mfinfors.beforeFirst();
				while(mfinfors.next()){
					double current = mfinfors.getDouble("current");
					MultipoleField mf = new MultipoleField(id, current);
					
					magnet.addMultipoleField(mf);
					
					mf.setCurrent_error(mfinfors.getDouble("current_error"));
					mf.setDx(mfinfors.getDouble("dx"));
					mf.setDy(mfinfors.getDouble("dy"));
					mf.setWater_temperature_in(mfinfors.getDouble("water_temperature_in"));
					mf.setWater_temperature_out(mfinfors.getDouble("water_temperature_out"));
					mf.setWater_pressure_in(mfinfors.getDouble("water_pressure_in"));
					mf.setWater_pressure_out(mfinfors.getDouble("water_pressure_out"));
					mf.setWater_flow(mfinfors.getDouble("water_flow"));
					mf.setOperator(mfinfors.getString("operator"));
					mf.setDate(mfinfors.getDate("date"));
					mf.setDescription(mfinfors.getString("description"));
					
					String id_current = mfinfors.getString("id_current");
					mfrs.beforeFirst();
					while(mfrs.next()){
						if(id_current.equalsIgnoreCase(mfrs.getString("id_current"))){
							MultipoleFieldData mfd = new MultipoleFieldData(mfrs.getInt("order"), mfrs.getDouble("angle"), mfrs.getDouble("phi"), mfrs.getDouble("bn"), mfrs.getDouble("an"), mfrs.getDouble("BnLeff"));
							mf.addMultipoleFieldData(mfd);
						}
					}
				}

				_magnetsbyid.put(id, magnet);
				MagnetTableData magdata = new MagnetTableData(i, magnet);
				magdata.setStatus(this.STATUS_DB);
		    	_magnetList.add(magdata);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean uploadMagnet(Magnet magnet){
		if(magnet == null) return false;
		
		if(_connection == null){
//			FXDialog.showWarningDialog("Manget Database Manager", "Connect CSNS Magnet Database first.", null);
			Application.displayWarning("Manget Database Manager", "Connect CSNS Magnet Database first.");
			return false;
		}

		String sqlcommand = createMagnetInfoSQLCommand(magnet) + createMCInfoSQLCommand(magnet) + createMFInfoSQLCommand(magnet);

		if(sqlcommand.isEmpty()) return false;

		try {
			String[] command = sqlcommand.split(";");
			for(int i=0; i<command.length; i++) _statement.addBatch(command[i]);
			
			_statement.executeBatch();
			_statement.clearBatch();
		} catch (SQLException e) {
//			FXDialog.showErrorDialog("Manget Database Manager", "Uploading manget information to CSNS Magnet Database failed!", null);
			Application.displayError("Manget Database Manager", "Uploading manget information to CSNS Magnet Database failed!");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Create SQL command.
	 * First delete old magnet information from DB. Second insert new information to DB.
	 * @param magnet
	 * @return
	 */
	private String createMagnetInfoSQLCommand(Magnet magnet){
		MagnetInformation info = magnet.getInfo();
		
		MagnetTableData mtd = _magnetTableView.getSelectionModel().getSelectedItem();
		String id = mtd.getID();
		String pid = mtd.getPID();
		
		String delete = "delete from magnet_information_table where id='" + id +"' and pid='" + pid + "';";
		
		String insert = "";
		insert = "INSERT INTO magnet_information_table(ID, PID, SORT_ID, TYPE_ID, LENGTH, APERTURE_TYPE, APERTURE, ANGLE, E1, E2, DESCRIPTION) VALUES(";
		insert = insert + "'" + info.getID() + "'";
		insert = insert + ", '" + info.getPID() + "'";
		insert = insert + ", '" + info.getSort_ID() + "'";
		insert = insert + ", '" + info.getType_ID() + "'";
		insert = insert + ", " + info.getLength();
		insert = insert + ", '" + info.getAperture_Type() + "'";
		insert = insert + ", '" + info.getAperture() + "'";
		insert = insert + ", " + info.getAngle();
		insert = insert + ", " + info.getE1();
		insert = insert + ", " + info.getE2();
		insert = insert + ", '" + info.getDescription() + "'";
		insert = insert + ");";
		
		return delete + insert;
	}
	
	/**
	 * Create SQL command.
	 * First delete old magnetization curve informations and curves from DB.
	 * Second insert new curve informations and curves to DB.
	 * 
	 * @param magnet
	 * @return
	 */
	private String createMCInfoSQLCommand(Magnet magnet){
		MagnetTableData mtd = _magnetTableView.getSelectionModel().getSelectedItem();
		String id = mtd.getID();
		
		String deletecurve = "delete from magnetization_curve_table where id='" + id +"';";
		String deleteinfo = "delete from magnetization_curve_information_table where id='" + id +"';";
		String deleteitob = "delete from i_to_b_coe_table where id='" + id +"';";
		String deletebtoi = "delete from b_to_i_coe_table where id='" + id +"';";

		String insertmcinfo = "";
		String insertmcdata = "";
		for(int i : magnet.getMagnetizationCurveInfos().keySet()){
			MagnetizationCurve mcinfo = magnet.getMagnetizationCurveInfos().get(i);

			insertmcinfo = insertmcinfo + "INSERT INTO magnetization_curve_information_table(id, group_id, effective_length, std_current_min, std_current_max, description, id_group_id) VALUES(";
			insertmcinfo = insertmcinfo + "'" + mcinfo.getID() + "'";
			insertmcinfo = insertmcinfo + ", '" + mcinfo.getGroupID() + "'";
			insertmcinfo = insertmcinfo + ", '" + mcinfo.getEffectiveLength() + "'";
			insertmcinfo = insertmcinfo + ", '" + mcinfo.getStd_Current_Min() + "'";
			insertmcinfo = insertmcinfo + ", '" + mcinfo.getStd_Current_Max() + "'";			
			insertmcinfo = insertmcinfo + ", '" + mcinfo.getDescription() + "'";
			insertmcinfo = insertmcinfo + ", + CONCAT(id,'_',group_id)";
			insertmcinfo = insertmcinfo + ");";
			
			if(mcinfo.getMagnetizationCurveDatas().size() > 0){
				//insertmcdata = insertmcdata + "insert into magnetization_curve_table(id, group_id, order, current, BL) values";
				insertmcdata = insertmcdata + "insert into magnetization_curve_table values";
				for(int index=0; index<mcinfo.getMagnetizationCurveDatas().size(); index++){
					insertmcdata = insertmcdata + "('" + mcinfo.getID() + "'";
					insertmcdata = insertmcdata + ", '" + mcinfo.getID() + "_" + mcinfo.getGroupID() + "'";
					insertmcdata = insertmcdata + ", '" + mcinfo.getMagnetizationCurveData(index).getOrder() + "'";
					insertmcdata = insertmcdata + ", '" + mcinfo.getMagnetizationCurveData(index).getI() + "'";
					insertmcdata = insertmcdata + ", '" + mcinfo.getMagnetizationCurveData(index).getBL() + "'";
					insertmcdata = insertmcdata + ")";
					if(index != mcinfo.getMagnetizationCurveDatas().size() - 1) insertmcdata = insertmcdata + ", ";
				}
				insertmcdata = insertmcdata + ";";
			}
			
			MCFitResult itob = mcinfo.getItoBFitResult();
			if(itob != null && itob.getOrder() > 0){
				insertmcdata = insertmcdata + "insert into i_to_b_coe_table values";
				for(int index=0; index<=itob.getOrder(); index++){
					insertmcdata = insertmcdata + "('" + mcinfo.getID() + "'";
					insertmcdata = insertmcdata + ", '" + mcinfo.getID() + "_" + mcinfo.getGroupID() + "'";
					insertmcdata = insertmcdata + ", '" + itob.getAverage() + "'";
					insertmcdata = insertmcdata + ", '" + index + "'";
					insertmcdata = insertmcdata + ", '" + itob.getCoe(index) + "'";
					insertmcdata = insertmcdata + ")";
					if(index != itob.getOrder()) insertmcdata = insertmcdata + ", ";
				}
				insertmcdata = insertmcdata + ";";
			}
			
			MCFitResult btoi = mcinfo.getBtoIFitResult();
			if(btoi != null && btoi.getOrder() > 0){
				insertmcdata = insertmcdata + "insert into b_to_i_coe_table values";
				for(int index=0; index<=btoi.getOrder(); index++){
					insertmcdata = insertmcdata + "('" + mcinfo.getID() + "'";
					insertmcdata = insertmcdata + ", '" + mcinfo.getID() + "_" + mcinfo.getGroupID() + "'";
					insertmcdata = insertmcdata + ", '" + btoi.getAverage() + "'";
					insertmcdata = insertmcdata + ", '" + index + "'";
					insertmcdata = insertmcdata + ", '" + btoi.getCoe(index) + "'";
					insertmcdata = insertmcdata + ")";
					if(index != btoi.getOrder()) insertmcdata = insertmcdata + ", ";
				}
				insertmcdata = insertmcdata + ";";
			}
		}

		return deletecurve + deleteinfo + deleteitob + deletebtoi + insertmcinfo + insertmcdata;
	}
	
	/**
	 * Create SQL command.
	 * First delete old multipole field information and data from DB.
	 * Second insert new multipole field information and data to DB.
	 * 
	 * @param magnet
	 * @return
	 */
	private String createMFInfoSQLCommand(Magnet magnet){
		MagnetTableData mtd = _magnetTableView.getSelectionModel().getSelectedItem();
		String id = mtd.getID();
		
		String deletecurve = "delete from multipole_field_table where id='" + id +"';";
		String deleteinfo = "delete from multipole_field_information_table where id='" + id +"';";

		String insertmfinfo = "";
		String insertmfdata = "";
		for(double I : magnet.getMultipoleFields().keySet()){
			MultipoleField mf = magnet.getMultipoleFields().get(I);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			insertmfinfo = insertmfinfo + "INSERT INTO multipole_field_information_table(id, current, current_error, dx, dy, water_pressure_in, water_pressure_out, water_temperature_in, water_temperature_out, water_flow, operator, date, description, id_current) VALUES(";
			insertmfinfo = insertmfinfo + "'" + mf.getID() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getCurrent() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getCurrent_error() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getDx() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getDy() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getWater_pressure_in() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getWater_pressure_out() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getWater_temperature_in() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getWater_temperature_out() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getWater_flow() + "'";
			insertmfinfo = insertmfinfo + ", '" + mf.getOperator() + "'";
			
			insertmfinfo = insertmfinfo + ", '" + sdf.format(mf.getDate()) + "'";
			
			insertmfinfo = insertmfinfo + ", '" + mf.getDescription() + "'";
			insertmfinfo = insertmfinfo + ", " + "CONCAT(id,'_',Round(current*1000,0))";
			insertmfinfo = insertmfinfo + ");";
			
			if(mf.getMultipoleFieldDatas().size() > 0){
				int i=0;
				//insertmfdata = insertmfdata + "insert into multipole_field_table(id, id_current, order, bn, an, angle, phi, BnLeff) values";
				insertmfdata = insertmfdata + "insert into multipole_field_table values";
				for(int order : mf.getMultipoleFieldDatas().keySet()){
					MultipoleFieldData mfd = mf.getMultipoleFieldDatas().get(order);
					
					insertmfdata = insertmfdata + "('" + mf.getID() + "'";
					insertmfdata = insertmfdata + ", '" + mf.getID() + "_" + Math.round(mf.getCurrent()*1000) + "'";
					insertmfdata = insertmfdata + ", '" + mfd.getOrder() + "'";
					insertmfdata = insertmfdata + ", '" + mfd.getBn() + "'";
					insertmfdata = insertmfdata + ", '" + mfd.getAn() + "'";
					insertmfdata = insertmfdata + ", '" + mfd.getAngle() + "'";
					insertmfdata = insertmfdata + ", '" + mfd.getPhi() + "'";
					insertmfdata = insertmfdata + ", '" + mfd.getBnLeff() + "'";
					insertmfdata = insertmfdata + ")";
					
					i++;
					if(i < mf.getMultipoleFieldDatas().size()) insertmfdata = insertmfdata + ", ";
				}
				insertmfdata = insertmfdata + ";";
			}
		}

		return deletecurve + deleteinfo + insertmfinfo + insertmfdata;
	}
	
	private boolean BackupMagnet(Magnet magnet){
		String bakpath = ((MagnetDBManagerDocument) document).getBackupPath();
		String delimiter = System.getProperty("file.separator");
		
		if(!bakpath.endsWith(delimiter)) bakpath = bakpath + delimiter;
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String filename = bakpath + magnet.getID() + "_" + df.format(new Date()) + ".xml";
		
		return SaveMagnetToFile(magnet, new File(filename));
	}
	
	private boolean SaveMagnetToFile(Magnet magnet, File file){
		XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
		magnet.toXmlDataAdaptor(xda);
		
		try {
			xda.writeTo(file);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
}
