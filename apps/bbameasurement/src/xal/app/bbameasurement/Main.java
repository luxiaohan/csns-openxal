/*
 * Main.java
 *
 * Created on March 19, 2003, 1:28 PM
 */

package xal.app.bbameasurement;

import java.util.logging.*;

import xal.extension.application.*;
import xal.extension.application.smf.AcceleratorApplication;


/**
 * Main is a demo concrete subclass of ApplicationAdaptor.  This demo application
 * is a simple plain text editor that demonstrates how to build a simple 
 * application using the application framework.
 *
 * @author  t6p
 */
public class Main extends ApplicationAdaptor {
    // --------- Variables specific to this application ------------------------
        

    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"bba"};
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"bba"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new BBADocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new BBADocument(url);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "BBA Measurement";
    }
    
    
        
    
    // --------- Application events --------------------------------------------
    
    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println("Application has finished launching!");
    }
	
	
	/**
	 * Constructor
	 */
	public Main() {
	}
    
    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
            AcceleratorApplication.launch( new Main() );
            
        }
        
        catch(Exception exception) {
			Logger.global.log( Level.SEVERE, "Failed application launch.", exception );
			System.err.println( exception.getMessage() );
			exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}