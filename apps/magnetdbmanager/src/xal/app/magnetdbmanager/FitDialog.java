package xal.app.magnetdbmanager;

import xal.app.magnetdbmanager.magnet.MagnetizationCurve;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class FitDialog extends Stage{
	private MagnetizationCurve _curve;
	private String _sort;
	private MagnetizationCurve _tempcurve;
	private boolean _editable;
	
	public FitDialog(MagnetizationCurve curve, String sort, boolean editable, Window owner) {
		_curve = curve;
		_sort = sort;
		_editable = editable;
		
		setTitle("Magnetization Curve Fit");
		initStyle(StageStyle.UTILITY);
		initModality(Modality.APPLICATION_MODAL);
		initOwner(owner);
		setResizable(true);
		
		_tempcurve = _curve.clone();
		final MagnetizationCurveFitPane pane = new MagnetizationCurveFitPane(_tempcurve, _sort, _editable);
		
		final Scene scene = new Scene(pane);
		setScene(scene);
		
		this.setOnCloseRequest(new EventHandler<WindowEvent>(){
			@Override
			public void handle(WindowEvent event) {
				if(pane.needSave()){
					_curve.setItoBFitResult(_tempcurve.getItoBFitResult());
					_curve.setBtoIFitResult(_tempcurve.getBtoIFitResult());
				}
				
				((Stage)event.getSource()).close();
			}
		});
		
	}
	
	public void showDialog() {
        sizeToScene();
        centerOnScreen();
        showAndWait();
    }
}
