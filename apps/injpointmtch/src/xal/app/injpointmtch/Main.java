/**
 * 
 */
package xal.app.injpointmtch;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;
import xal.extension.application.smf.AcceleratorApplication;

/**
 * Application adaptor for the Injection Point Match application.
 * @author luxiaohan
 *
 */
public class Main extends ApplicationAdaptor {

	   /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return writableDocumentTypes();
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"injm"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new InjMatchDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new InjMatchDocument( url );
    }
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "Injection Point Match";
    }
    
    
    /** Capture the application launched event and print it */
    public void applicationFinishedLaunching() {
        System.out.println( "Application finished launching..." );
		Logger.getLogger( "global" ).log( Level.INFO, "Application finished launching." );
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println( "Launching Injection Point Match application..." );
			Logger.getLogger( "global" ).log( Level.INFO, "Launching the application..." );
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
			Logger.getLogger( "global" ).log( Level.SEVERE, "Error launching the application." , exception );
            exception.printStackTrace();
            JOptionPane.showMessageDialog( null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
        }
    }

}
