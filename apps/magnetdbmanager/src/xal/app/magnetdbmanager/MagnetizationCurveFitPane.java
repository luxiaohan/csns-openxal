package xal.app.magnetdbmanager;

//import gov.sns.application.Application;

import java.text.DecimalFormat;

import xal.app.magnetdbmanager.magnet.*;
import xal.extension.application.Application;
import xal.extension.fit.LinearLeastSquareMethod;
import xal.extension.fit.OrthogonalLeastSquareMethod;
/*import csns.physics.database.magnet.MCFitResult;
import csns.physics.database.magnet.MagnetizationCurve;
import csns.tools.fit.LinearLeastSquareMethod;
import csns.tools.fit.OrthogonalLeastSquareMethod;*/
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * 励磁曲线拟合窗口
 * 
 * @author Qiu Jing, liuwb
 *
 */
public class MagnetizationCurveFitPane extends Pane {
	private MagnetizationCurve _curve;
	private String _sort;
	private boolean _editable = false;
	private boolean _needSave = false;
	
	private ObservableList<MagnetDataFitData> _tableDatas = FXCollections.observableArrayList();
	private ObservableList<MagnetDataFitData> _originDatas = FXCollections.observableArrayList();
	private ObservableList<MagnetDataFitData> _fitDatas = FXCollections.observableArrayList();
	
	private ObservableList<Integer> _orderlist = FXCollections.observableArrayList();
	
	private double[] _IValue;
	private double[] _BLValue;
	private double[] _BValue;
	
	private MCFitResult _originItoBCoe = null;
	private MCFitResult _originBtoICoe = null;
	
	private MCFitResult _ItoBCoe = null;
	private MCFitResult _BtoICoe = null;
	
	private ObservableList<MCCoeTableData> _coeDatas = FXCollections.observableArrayList();
	private ObservableList<MCCoeTableData> _originCoeDatas = FXCollections.observableArrayList();
	private ObservableList<MCCoeTableData> _fitCoeDatas = FXCollections.observableArrayList();

	//order 是为拟合阶数
    private int _order = 0;
    
    RadioButton _methodbutton1;
    RadioButton _methodbutton2;
    
    // 表格调用的是“MagnetDataFitData”类型类
    private TableView<MagnetDataFitData> _datatable = new TableView<MagnetDataFitData>();
    
    private TableView<MCCoeTableData> _coetable = new TableView<MCCoeTableData>();
    
	// 设置显示格式
    private DecimalFormat _dataformat1 = new DecimalFormat("#.00000000");
    private DecimalFormat _dataformat2 = new DecimalFormat("0.00E0");
    private DecimalFormat _dataformat3 = new DecimalFormat("#.00000");
    private DecimalFormat _dataformat4 = new DecimalFormat("0.000000E000");
    
    
	@SuppressWarnings("unchecked")
	public MagnetizationCurveFitPane(MagnetizationCurve curve, String sort, boolean savable){
		_curve = curve;
		_sort = sort;
		_editable = savable;
		
		if(_curve != null){
			_IValue = _curve.getI();
			_BLValue = _curve.getBL();
			_BValue = new double[_BLValue.length];
			for(int i=0; i<_BLValue.length; i++) _BValue[i] = _BLValue[i]/_curve.getEffectiveLength();
			
			_originItoBCoe = _curve.getItoBFitResult();
			_originBtoICoe = _curve.getBtoIFitResult();
			
			_ItoBCoe = _originItoBCoe;
			_BtoICoe = _originBtoICoe;
			
			_originDatas = calculateTableData(_curve.getItoBFitResult(), _curve.getBtoIFitResult());
			
			//拟合阶数设置
			if(_originDatas.size() > 1){
				int ordermax = 20;
				if(_originDatas.size() > 1 && _originDatas.size() <= 21) ordermax = _originDatas.size() - 1;
				
				for(int i=1; i<=ordermax; i++) _orderlist.add(i);
			}
			
			_originCoeDatas = this.calculateCoeTableData(_originItoBCoe, _originBtoICoe);
		}
		
		// 数据表的名称和字体
        Label mctabletitle = new Label("Magnetization Curve Table");
        mctabletitle.setFont(new Font("Arial", 20));
        
        // 拟合阶数选择标签
        Label orderlabel = new Label("Choosing Fit Order: ");
        orderlabel.setFont(new Font("Arial", 20));
        
        // 拟合按钮
        Button fitButton = new Button("Fit"); 
		
		// 显示拟合方程表达式
        Image image1 = new Image(getClass().getResourceAsStream("resources/Formula-1.png"));
		Image image2 = new Image(getClass().getResourceAsStream("resources/Formula-2.png"));
		_methodbutton1 = new RadioButton();
		_methodbutton1.setGraphic(new ImageView(image1));
		_methodbutton2 = new RadioButton();
		_methodbutton2.setGraphic(new ImageView(image2));
		
		ToggleGroup methodgroup = new ToggleGroup();
		_methodbutton1.setToggleGroup(methodgroup);
		_methodbutton2.setToggleGroup(methodgroup);
		_methodbutton1.setSelected(true);
		
		ChoiceBox<Integer> orderchoicebox = new ChoiceBox<Integer>(_orderlist);
		
		orderchoicebox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				_order = _orderlist.get(arg2.intValue());
			}
		});
		
		//这个表格用于显示测量数据，拟合数据，拟合误差等数据
		//注意：数据类型是MagnetDataFitData 型
		TableColumn<MagnetDataFitData,String> orderCol = new TableColumn<MagnetDataFitData,String>("Index");
		
		//表格共7列：index, I,BL, FitBL, BLpercent, FitI, Ipercent
		orderCol.setCellValueFactory(new PropertyValueFactory<MagnetDataFitData, String>("order"));
		orderCol.setMinWidth(50);
		
        TableColumn<MagnetDataFitData,String> currCol = new TableColumn<MagnetDataFitData,String>("I(A)");
        currCol.setCellValueFactory(new PropertyValueFactory<MagnetDataFitData, String>("I"));
        currCol.setMinWidth(100);
        
        String BLcname = "BL(T m)";
        String BLFitcname = "BLFit(T m)";
        String itoblcname = "I to B";
        String bltoicname = "B to I";
        String itoblbuttontext = "I to B";
        String bltoibuttontext = "B to I";
        if(_sort.equalsIgnoreCase("QUADRUPOLE")){
            BLcname = "GL(T/m m)";
            BLFitcname = "GL_fit(T/m m)";
            itoblcname = "I to G";
            bltoicname = "G to I";
            itoblbuttontext = "I to G";
            bltoibuttontext = "G to I";
        }
        else if(_sort.equalsIgnoreCase("SEXTUPOLE")){
            BLcname = "B''(T/m)";
            BLFitcname = "B''_fit(T/m)";
            itoblcname = "I to B''";
            bltoicname = "B'' to I";
            itoblbuttontext = "I to B''";
            bltoibuttontext = "B'' to I";
        }
        else if(_sort.equalsIgnoreCase("OCTUPOLE")){
            BLcname = "B'''(T/m^2)";
            BLFitcname = "B'''_fit(T/m^2)";
            itoblcname = "I to B'''";
            bltoicname = "B''' to I";
            itoblbuttontext = "I to B'''";
            bltoibuttontext = "B''' to I";
        }
            
        TableColumn<MagnetDataFitData,String> magCol = new TableColumn<MagnetDataFitData,String>(BLcname);
        magCol.setCellValueFactory(new PropertyValueFactory<MagnetDataFitData, String>("BL"));
        magCol.setMinWidth(100);
        
        TableColumn<MagnetDataFitData,String> fitmagCol = new TableColumn<MagnetDataFitData,String>(BLFitcname);
        fitmagCol.setCellValueFactory(new PropertyValueFactory<MagnetDataFitData, String>("FitBL"));
        fitmagCol.setMinWidth(100);
        
        TableColumn<MagnetDataFitData,String> magpercentCol = new TableColumn<MagnetDataFitData,String>("Error(%)");
        magpercentCol.setCellValueFactory(new PropertyValueFactory<MagnetDataFitData, String>("BLpercent"));
        magpercentCol.setMinWidth(100);
        TableColumn<MagnetDataFitData,String> fitcurrentCol = new TableColumn<MagnetDataFitData,String>("I_fit(A)");
        fitcurrentCol.setCellValueFactory(new PropertyValueFactory<MagnetDataFitData, String>("FitI"));
        fitcurrentCol.setMinWidth(100);
        
        TableColumn<MagnetDataFitData,String> ipercentCol = new TableColumn<MagnetDataFitData,String>("Error(%)");
        ipercentCol.setCellValueFactory(new PropertyValueFactory<MagnetDataFitData, String>("Ipercent"));
        ipercentCol.setMinWidth(100);
        
         // 测量得到的I和BL
        _tableDatas = _originDatas;
        _datatable.setItems(_tableDatas);
        _datatable.getColumns().addAll(orderCol,currCol,magCol,fitmagCol,magpercentCol,fitcurrentCol,ipercentCol);
        
        TableColumn<MCCoeTableData, String> fitresultrowname = new TableColumn<MCCoeTableData, String>("Name");
        TableColumn<MCCoeTableData, String> fitresultitobcontent = new TableColumn<MCCoeTableData, String>(itoblcname);
        TableColumn<MCCoeTableData, String> fitresultbtoicontent = new TableColumn<MCCoeTableData, String>(bltoicname);
        
        fitresultrowname.setCellValueFactory(new PropertyValueFactory<MCCoeTableData, String>("rowname"));
        fitresultitobcontent.setCellValueFactory(new PropertyValueFactory<MCCoeTableData, String>("itobcontent"));
        fitresultbtoicontent.setCellValueFactory(new PropertyValueFactory<MCCoeTableData, String>("btoicontent"));
        fitresultrowname.setMinWidth(60);
        fitresultitobcontent.setMinWidth(100);
        fitresultbtoicontent.setMinWidth(100);
        
        _coetable.getColumns().addAll(fitresultrowname, fitresultitobcontent, fitresultbtoicontent);
        _coeDatas = _originCoeDatas;
        _coetable.setItems(_coeDatas);
        
        // 拟合数据
        fitButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
			    if(Math.abs(_curve.getEffectiveLength()) < 0.001) return;
				if(_order < 1) return;

				//拟合系数，用测量的I去拟合BL
				double iave = 0;
				double[] itobcoe;
				if(_methodbutton1.isSelected()){
				    double[] coe = LinearLeastSquareMethod.Fit(_IValue, _BValue, _order, false);
				    itobcoe = new double[coe.length + 1];
				    itobcoe[0] = 0;
				    for(int i=0; i<coe.length; i++) itobcoe[i+1] = coe[i];
				}
				else{
				    iave = OrthogonalLeastSquareMethod.getAverage(_IValue);
				    itobcoe = OrthogonalLeastSquareMethod.PolyFit(_IValue, _BValue, _order + 1);
				}
				
				_ItoBCoe = new MCFitResult(iave, itobcoe);
				
				//拟合系数，用测量的BL去拟合I
				double bave = 0;
				double[] btoicoe;
				if(_methodbutton1.isSelected()){
				    double[] coe = LinearLeastSquareMethod.Fit(_BValue, _IValue, _order, false);
				    btoicoe = new double[coe.length + 1];
				    btoicoe[0] = 0;
				    for(int i=0; i<coe.length; i++) btoicoe[i+1] = coe[i];
				}
				else{
				    bave = OrthogonalLeastSquareMethod.getAverage(_BValue);
				    btoicoe = OrthogonalLeastSquareMethod.PolyFit(_BValue, _IValue, _order + 1);
				}
				
				_BtoICoe = new MCFitResult(bave, btoicoe);
				
				_fitDatas = calculateTableData(_ItoBCoe, _BtoICoe);
				_tableDatas = _fitDatas;
				_datatable.setItems(_tableDatas);
				
				_fitCoeDatas = calculateCoeTableData(_ItoBCoe, _BtoICoe);
				_coeDatas = _fitCoeDatas;
				_coetable.setItems(_coeDatas);
			}
		});
        
        VBox data_box = new VBox();
        data_box.setSpacing(20);
        data_box.setPadding(new Insets(10, 0, 0, 10));
        
        HBox button_box = new HBox();
        button_box.setSpacing(30);
        Button keepButton = new Button("Keep Fit Result");
        keepButton.setDisable(!_editable);
        Button resetButton = new Button("Reset");
        button_box.getChildren().addAll(keepButton, resetButton);
        
        keepButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				if(_ItoBCoe == null || _BtoICoe == null) return;
				
				_curve.setItoBFitResult(_ItoBCoe);
				_curve.setBtoIFitResult(_BtoICoe);
				
				_needSave = true;
				
//				FXDialog.showWarningDialog("Mangetizatin Curve Fit", "Fit results are kept temporarily.", null);
				Application.displayWarning("Mangetizatin Curve Fit", "Fit results are kept temporarily.");
			}
        });
        
        resetButton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				_datatable.setItems(_originDatas);
				_coetable.setItems(_originCoeDatas);
				
				_ItoBCoe = _originItoBCoe;
				_BtoICoe = _originBtoICoe;
				
//				FXDialog.showWarningDialog("Mangetizatin Curve Fit", "Fit results are restored to the origin value.", null);
				Application.displayWarning("Mangetizatin Curve Fit", "Fit results are restored to the origin value.");
			}
        });
        
        HBox cal_box = new HBox();
        cal_box.setSpacing(20);
        cal_box.setAlignment(Pos.CENTER);
        Label inputlabel = new Label("Input:");
        final TextField inputtf = new TextField();
        Button calbutton = new Button(" --> ");
        final TextField resulttf = new TextField();
        
        final RadioButton itoblbutton = new RadioButton(itoblbuttontext);
        final RadioButton bltoibutton = new RadioButton(bltoibuttontext);
        ToggleGroup transgroup = new ToggleGroup();
        itoblbutton.setToggleGroup(transgroup);
        bltoibutton.setToggleGroup(transgroup);
        itoblbutton.setSelected(true);
        
        HBox methodbox = new HBox();
        methodbox.getChildren().addAll(_methodbutton1, _methodbutton2);
        methodbox.setSpacing(20);
        
        cal_box.getChildren().addAll(inputlabel, inputtf, calbutton, resulttf, itoblbutton, bltoibutton);
        
        calbutton.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				double inputvalue = Double.parseDouble(inputtf.getText());
				double resultvalue;
				
				if(itoblbutton.isSelected()){
					if(_ItoBCoe == null || _ItoBCoe.getCoe() == null) return;
					resultvalue = _ItoBCoe.calculate(inputvalue);
				}
				else{
					if(_BtoICoe == null || _BtoICoe.getCoe() == null) return;
					resultvalue = _BtoICoe.calculate(inputvalue);
				}
				
				resulttf.setText(Double.toString(resultvalue));
			}
        });
        
        data_box.getChildren().addAll(mctabletitle, _datatable, button_box, cal_box);
        
        
        HBox fit_box = new HBox();
        fit_box.setSpacing(10);
        fit_box.setPadding(new Insets(10, 0, 0, 10));
        fit_box.getChildren().addAll(orderlabel, orderchoicebox, fitButton);
        
        VBox fitresult_box = new VBox();
        fitresult_box.setSpacing(10);
        fitresult_box.setAlignment(Pos.TOP_CENTER);
        fitresult_box.setPadding(new Insets(10, 0, 0, 10));
        fitresult_box.getChildren().addAll(fit_box, methodbox, _coetable);
        
        HBox total_box = new HBox();
        total_box.setPadding(new Insets(10, 10, 10, 10));
        total_box.getChildren().addAll(data_box, fitresult_box);
        
        this.getChildren().addAll(total_box);
	}
	
	public ObservableList<MagnetDataFitData> calculateTableData(MCFitResult itobfitresult, MCFitResult btoifitresult){
		ObservableList<MagnetDataFitData> datas = FXCollections.observableArrayList();
		int datasize = _curve.getMagnetizationCurveDatas().size();
		
		if(itobfitresult == null || btoifitresult == null || itobfitresult.getCoe() == null || btoifitresult.getCoe() == null){
			for (int i = 0; i < datasize; i++) {
				MagnetDataFitData tempdata = new MagnetDataFitData(i+1, _IValue[i], _BLValue[i], "", "", "", "");
				datas.addAll(tempdata);
			}
			return datas;
		}
		
		double[] BLFit = new double[datasize];
		double[] IFit = new double[datasize];
		
		String[] sbfit = new String[datasize];
		String[] sifit = new String[datasize];
		
		String[] sbpercent = new String[datasize];
		String[] sipercent = new String[datasize];
	
		for(int i=0; i<datasize; i++){
			BLFit[i] = itobfitresult.calculate(_IValue[i])*_curve.getEffectiveLength();
			sbfit[i] = _dataformat1.format(BLFit[i]);
			sbpercent[i] = _dataformat2.format((BLFit[i] - _BLValue[i])/_BLValue[i]*100);
		}

		for(int i=0;i<datasize;i++){
			IFit[i] = btoifitresult.calculate(_BValue[i]);
			sifit[i] = _dataformat3.format(IFit[i]);
			sipercent[i] = _dataformat2.format((IFit[i] - _IValue[i])/_IValue[i]*100);
		}
		
		for (int i = 0; i < datasize; i++) {
			MagnetDataFitData tempdata = new MagnetDataFitData(i+1, _IValue[i], _BLValue[i], sbfit[i], sbpercent[i], sifit[i], sipercent[i]);
			datas.addAll(tempdata);
		}
		
		return datas;
	}
	
	public ObservableList<MCCoeTableData> calculateCoeTableData(MCFitResult itobfitresult, MCFitResult btoifitresult){
		if(itobfitresult == null || btoifitresult == null) return null;
		
		ObservableList<MCCoeTableData> datas = FXCollections.observableArrayList();
		
		if(itobfitresult.getCoe() == null || btoifitresult.getCoe() == null) return datas;
		
		MCCoeTableData rdata = new MCCoeTableData("Average", itobfitresult.getAverage(), btoifitresult.getAverage());
		datas.add(rdata);
		for(int i=0; i<itobfitresult.size(); i++){
			MCCoeTableData tempdata = new MCCoeTableData("Coe" + i, _dataformat4.format(itobfitresult.getCoe(i)), _dataformat4.format(btoifitresult.getCoe(i)));
			datas.add(tempdata);
		}
		
		double itobResidual = 0;
		double btoiResidual = 0;
		for(int i=0; i<_curve.getMagnetizationCurveDatas().size(); i++){
			itobResidual = itobResidual + (itobfitresult.calculate(_IValue[i]) - _BValue[i]) * (itobfitresult.calculate(_IValue[i]) - _BValue[i]);
			btoiResidual = btoiResidual + (btoifitresult.calculate(_BValue[i]) - _IValue[i]) * (btoifitresult.calculate(_BValue[i]) - _IValue[i]);
		}
		MCCoeTableData residualdata = new MCCoeTableData("Residual", itobResidual, btoiResidual);
		datas.add(residualdata);
		
		return datas;
	}
	
	public boolean needSave(){
		return _needSave;
	}

}
