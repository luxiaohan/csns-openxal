/*
 * Main.java
 *
 * Created on March 19, 2003, 1:28 PM
 */

package xal.app.magnetdbmanager;

import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.event.*;

import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.Commander;
import xal.extension.application.XalDocument;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.logging.*;

//import gov.sns.application.*;


/**
 * Main is a demo concrete subclass of ApplicationAdaptor.  This demo application
 * is a simple plain text editor that demonstrates how to build a simple 
 * application using the application framework.
 * @author  t6p
 */
public class Main extends ApplicationAdaptor {
    // --------- Variables specific to this application ------------------------
    
    // simple instance variable to hold state for the Demo application
	/*
    private boolean isRunning;
    private boolean isPaused;
    private Action startAction;
    private Action stopAction;
    private Action pauseAction;
    */
    

    // --------- Document management -------------------------------------------
    
    /**
     * Returns the text file suffixes of files this application can open.
     * @return Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[] {"mdbm", "xml"};
    }
    
    
    /**
     * Returns the text file suffixes of files this application can write.
     * @return Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[] {"mdbm", "xml"};
    }
    
    
    /**
     * Implement this method to return an instance of my custom document.
     * @return An instance of my custom document.
     */
    public XalDocument newEmptyDocument() {
        return new MagnetDBManagerDocument();
    }
    
    
    /**
     * Implement this method to return an instance of my custom document 
     * corresponding to the specified URL.
     * @param url The URL of the file to open.
     * @return An instance of my custom document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new MagnetDBManagerDocument(url);
    }
    
    
    // --------- Global application management ---------------------------------
    
    
    /**
     * Specifies the name of my application.
     * @return Name of my application.
     */
    public String applicationName() {
        return "CSNS Magnet Database Manager";
    }
    
    
    /**
     * Implement a preference panel for the document in this case.  One could 
     * implement application wide preferences or a combination of both application and 
     * document preferences.
     * This method is optional to define.  If this method is missing, the preferences menu
     * item will be disabled.  Here we demonstrate how to implement a simple preference panel
     * for a document.
     * @param document The document whose preferences are being changed.  Subclass may ignore.
     */
    public void editPreferences(XalDocument document) {
        //((MyDocument)document).editPreferences();
    }
    
    
    /**
     * Register actions for the Special menu items.  These actions simply 
     * print to standard output or standard error.
     * This code demonstrates how to define custom actions for menus and the toolbar
     * This method is optional.  You may similarly define actions in the document class
     * if those actions are document specific.  Here the actions are application wide.
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands(Commander commander) {
    	/*
        // define the "start run" demo action
        startAction = new AbstractAction( "start-run" ) {
            public void actionPerformed(ActionEvent event) {
                isRunning = true;
                isPaused = false;
                updateActions();        // update the menu item enable state for user feedback
                System.out.println("Starting the run...");
				Logger.getLogger("global").log( Level.INFO, "Starting the run." );
            }
        };
        commander.registerAction(startAction);
        
        // define the "pause run" demo action
        pauseAction = new AbstractAction( "pause-run" ) {
            public void actionPerformed(ActionEvent event) {
                isRunning = false;
                isPaused = true;
                updateActions();        // update the menu item enable state for user feedback
                System.out.println("Pausing the run...");
				Logger.getLogger("global").log( Level.INFO, "Pausing the run." );
            }
        };        
        commander.registerAction(pauseAction);
        
        
        // define the "stop run" demo action
        stopAction = new AbstractAction( "stop-run" ) {
            public void actionPerformed(ActionEvent event) {
                isRunning = false;
                isPaused = false;
                updateActions();        // update the menu item enable state for user feedback
                System.err.println( "Stopping the run..." );
				Logger.getLogger( "global" ).log( Level.INFO, "Stopping the run." );
            }
        };        
        commander.registerAction(stopAction);
        
        
        // define the "whatif mode" button model
		ToggleButtonModel whatifModel = new ToggleButtonModel();
		whatifModel.setSelected(true);
		whatifModel.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.out.println("changed to whatif mode...");
				Logger.getLogger("global").log( Level.INFO, "Changed to whatif mode." );
            }
		});
        commander.registerModel("whatif-mode", whatifModel);
        
        
        // define the "live mode" button model
		ToggleButtonModel liveModel = new ToggleButtonModel();
		liveModel.setSelected(false);
		liveModel.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.out.println("changed to live mode...");
				Logger.getLogger("global").log( Level.INFO, "Changed to live mode." );
            }
		});
        commander.registerModel("live-mode", liveModel);

        
        updateActions();
        */
    }
    
    
    /**
     * Update the action enable states.  This method demonstrates how to enable
     * or disable menu or toolbar items.  Often this is unnecessary, but it is
     * included to demonstrate how to do this if you wish to provide this kind
     * of feedback to the user.
     */
    /*
    protected void updateActions() {
        startAction.setEnabled(!isRunning);
        pauseAction.setEnabled(isRunning);
        stopAction.setEnabled(isRunning || isPaused);
    }
    */
        
    
    // --------- Application events --------------------------------------------
    
    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     */
    public void applicationFinishedLaunching() {
        System.out.println("Application has finished launching!");
		Logger.getLogger("global").log( Level.INFO, "Application has finished launching." );
    }
    
    @Override
    public void applicationWillQuit(){
    	System.out.println("Application terminate!");
    	Logger.getLogger("global").log( Level.INFO, "Application terminate." );
    }
    
    
    /**
     * Constructor
     */
    public Main() {
        //isRunning = false;
        //isPaused = false;
    }
    
    
    /** The main method of the application. */
    static public void main(String[] args) {
		String path = System.getProperty("user.home");
		path = path + "/.log";
		
		boolean loggerfolderexists = true;
		
		File filepath = new File(path);
		if(filepath.exists() && !filepath.isDirectory()){
			System.out.println("Cannot create logger because .log is not a directory.");
			loggerfolderexists = false;
		}
		if(!filepath.exists()){
			if(!filepath.mkdir()){
				System.out.println("Cannot create .log directory to store logger.");
				loggerfolderexists = false;
			}
		}
		
		if(loggerfolderexists){
	    	try {
	    		String date = (new Date()).toString();
	    		FileHandler fileTxt = new FileHandler(path + "/CSNSMagnetDBLogger " + date + ".txt");
	    		SimpleFormatter formatterTxt =new SimpleFormatter();
				fileTxt.setFormatter(formatterTxt);
				
				Logger logger = Logger.getLogger("global");
				logger.addHandler(fileTxt);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	
        try {
            System.out.println("Starting application...");
			Logger.getLogger("global").log( Level.INFO, "Starting application..." );
            Application.launch( new Main() );
        }
        catch(Exception exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error starting application.", exception );
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
			System.exit(-1);
        }
    }
}
