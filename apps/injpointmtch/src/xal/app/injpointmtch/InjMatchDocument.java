/**
 * 
 */
package xal.app.injpointmtch;

import java.net.URL;

import xal.extension.application.*;
import xal.extension.application.smf.AcceleratorDocument;
import xal.extension.bricks.WindowReference;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.xml.XmlDataAdaptor;

/**
 * @author luxiaohan
 *
 */
public class InjMatchDocument extends AcceleratorDocument implements DataListener{
	
	/** main window reference */
	final private WindowReference WINDOW_REFERENCE;
	
	/**the injection match model*/
	final private InjMatchModel MODEL;
	
	
    /** Empty Constructor */
    public InjMatchDocument() {
        this( null );
    }
	
	
	   /** 
     * Primary constructor 
     * @param url The URL of the file to load into the new document.
     */
    public InjMatchDocument( final java.net.URL url )  {
    	setSource(url);
    	WINDOW_REFERENCE = getDefaultWindowReference( "MainWindow", this );
    	
    	MODEL = new InjMatchModel();
    	
		if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
        }
		
		setHasChanges( false );
    	
    }
    
    /** Make and configure the main window. */
    public void makeMainWindow() {
        mainWindow = (XalWindow)WINDOW_REFERENCE.getWindow();
		setHasChanges( false );
    }
    
    /**get the model*/
    
    public InjMatchModel getModel() {
    	return MODEL;
    }

	@Override
	public String dataLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(DataAdaptor adaptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(DataAdaptor adaptor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveDocumentAs(URL url) {
		writeDataTo( this, url );
		
	}

}
