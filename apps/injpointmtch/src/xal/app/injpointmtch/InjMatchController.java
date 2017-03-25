/**
 * 
 */
package xal.app.injpointmtch;

import xal.extension.bricks.WindowReference;

/**
 * @author luxiaohan
 * controller for binding the Injection Match model to the user interface
 */
public class InjMatchController {
	
	/**The Injection match model*/
	final private InjMatchModel MODEL;
	
	/**Constructor*/
	public InjMatchController ( final InjMatchDocument document, final WindowReference windowReference ) {
		MODEL = document.getModel();
		initMainWindow( windowReference );
		
	}
	
	
	/**initialize main window*/
	private void initMainWindow( final WindowReference windowReference ) {
		makeNewParametersView(windowReference);
		makeDiagnosticsView(windowReference);
		makeMatrixPlotView(windowReference);
		makeCalculationView(windowReference);
		makeCalculationHistoryView(windowReference);
		
	}
	
	/**setup new parameters view*/
	private void makeNewParametersView( final WindowReference windowReference ) {
		
	}
	
	/**setup diagnostics view*/
	private void makeDiagnosticsView( final WindowReference windowReference ) {
		
	}
	
	/**setup matrix plot view*/
	private void makeMatrixPlotView( final WindowReference windowReference ) {
		
	}
	
	/**setup calculation view*/
	private void makeCalculationView( final WindowReference windowReference ) {
		
	}
	
	/**setup calculation history view*/
	private void makeCalculationHistoryView( final WindowReference windowReference ) {
		
	}

}
