package xal.app.bbameasurement;

import java.net.URL;

import xal.extension.application.smf.AcceleratorDocument;

public class BBADocument extends AcceleratorDocument{

	/** Create a new empty document */
	public BBADocument() {
		this(null);
	}

	public BBADocument(java.net.URL url) {
		setSource(url);
		if (url == null)
			return;
	}
	
	/**
	 * Make a main window by instantiating the my custom window. Set the text
	 * pane to use the textDocument variable as its document.
	 */
	public void makeMainWindow() {
		mainWindow = new BBAWindow(this);
	}

	public void acceleratorChanged() {
		if(mainWindow != null) ((BBAWindow) mainWindow).setAccelerator(this.accelerator);
    }

	public void selectedSequenceChanged() {
    	((BBAWindow) mainWindow).setSequence(this.selectedSequence);
    }

	@Override
	public void saveDocumentAs(URL url) {
		// TODO Auto-generated method stub
		
	}
	
}