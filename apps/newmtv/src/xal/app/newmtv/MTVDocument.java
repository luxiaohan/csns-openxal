package xal.app.newmtv;

import java.net.URL;

import xal.extension.application.smf.AcceleratorDocument;

public class MTVDocument  extends AcceleratorDocument {

	public MTVDocument(URL url) {
		setSource(url);
		if (url == null)
			return;
	}

	public MTVDocument() {
		this(null);
	}

	@Override
	public void makeMainWindow() {
		mainWindow = new MTVWindow(this);

	}
	
	public void acceleratorChanged() {
		if(mainWindow != null) ((MTVWindow) mainWindow).setAccelerator(this.accelerator);	
		
    }

	public void selectedSequenceChanged() {
    	((MTVWindow) mainWindow).setSequence(this.selectedSequence);
    }

	@Override
	public void saveDocumentAs(URL url) {
		// TODO Auto-generated method stub

	}

}
