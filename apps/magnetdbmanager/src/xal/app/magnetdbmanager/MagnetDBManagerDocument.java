/*
 * MyDocument.java
 *
 * Created on March 19, 2003, 1:32 PM
 */

package xal.app.magnetdbmanager;

//import gov.sns.application.Commander;
//import gov.sns.application.XalDocument;
//import gov.sns.tools.apputils.files.RecentFileTracker;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import xal.extension.application.Commander;
import xal.extension.application.XalDocument;
import xal.tools.apputils.files.RecentFileTracker;

/**
 * MyDocument is a custom XalDocument for my application.  Each document instance 
 * manages a single plain text document.  The document manages the data that is 
 * displayed in the window.
 *
 * @author  t6p
 */
public class MagnetDBManagerDocument extends XalDocument {
    
	private RecentFileTracker _bakpathFileTracker;
	private String _bakpath = "";
    
    /** Create a new empty document */
    public MagnetDBManagerDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public MagnetDBManagerDocument(java.net.URL url) {
        setSource(url);
        //makeTextDocument();
        
        _bakpathFileTracker = new RecentFileTracker(1, this.getClass(), "bakpath");
        
        File defaultbakpath = _bakpathFileTracker.getMostRecentFile();
		if(defaultbakpath != null && defaultbakpath.canWrite()) _bakpath = defaultbakpath.getAbsolutePath();
        
        if ( url == null )  return;

        //XmlDataAdaptor xml = XmlDataAdaptor.adaptorForUrl(url, false);
        //System.out.println(xml);
        
        /*
        try {
            final int charBufferSize = 1000;
            InputStream inputStream = url.openStream();
            BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream) );
            
            StringBuffer stringBuffer = new StringBuffer();
            char[] charBuffer = new char[charBufferSize];
            int numRead = 0;
            while ( (numRead = reader.read(charBuffer, 0, charBufferSize)) != -1 ) {
                stringBuffer.append(charBuffer, 0, numRead);
            }
            
            textDocument.insertString(0, stringBuffer.toString(), null);
            setHasChanges(false);
        }
        catch(java.io.IOException exception) {
            throw new RuntimeException( exception.getMessage() );
        }
        catch(BadLocationException exception) {
            throw new RuntimeException( exception.getMessage() );
        }
        */
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new MagnetDBManagerWindow(this);
        //myWindow().getTextView().setDocument(textDocument);        
    }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
    	/*
        try {
            int length = textDocument.getLength();
            String text = textDocument.getText( 0, length );
            
            File file = new File( url.toURI() );
            if ( !file.exists() ) {
                file.createNewFile();
            }
            
            FileWriter writer = new FileWriter( file );
            writer.write( text, 0, text.length() );
            writer.flush();
            setHasChanges( false );
        }
        catch(BadLocationException exception) {
			System.err.println(exception);
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning("Save Failed!", "Save Failed due to an internal exception!", exception);
        }
        catch(IOException exception) {
			System.err.println(exception);
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning("Save Failed!", "Save Failed due to an internal exception!", exception);
        }
		catch( java.net.URISyntaxException exception ) {
			System.err.println( exception );
			Logger.getLogger("global").log( Level.WARNING, "Save Failed", exception );
			displayWarning( "Save Failed!", "Save Failed due to an internal exception!", exception );
        }
        */
	}
    
    
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    private MagnetDBManagerWindow myWindow() {
        return (MagnetDBManagerWindow)mainWindow;
    }
    
    
    /**
     * Register custom actions for the document.
     * @param commander The commander with which to register the custom commands.
     * @override
     */
    public void customizeCommands( final Commander commander ) {
    	final Action setdbserverAction = new AbstractAction("connect-db-server"){
    		public void actionPerformed(final ActionEvent event){
    			myWindow().ConnectDB();
    		}
    	};
    	
    	final Action setbakfolderAction = new AbstractAction("set-bak-folder"){
    		public void actionPerformed(final ActionEvent event){
    			File currentpath = _bakpathFileTracker.getMostRecentFile();
    			String bakpath = ".";
    			if(currentpath != null) bakpath = currentpath.getAbsolutePath();
    			
    			JFrame frame = new JFrame();
				JFileChooser fileChooser = new JFileChooser(bakpath);
				fileChooser.setDialogTitle("Set the Magnet Backup Folder");
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int status = fileChooser.showOpenDialog(frame);
				if (status == JFileChooser.APPROVE_OPTION) {
					_bakpathFileTracker.cacheURL(fileChooser.getSelectedFile());
					File file = fileChooser.getSelectedFile();
					
					if(!file.canWrite()){
						displayWarning("Manget Database Manager", "Backup folder must be writable. Change a folder and try again.");
						return;
					}
					
					bakpath = file.getPath();
					if(!_bakpath.equals(bakpath)){
						_bakpath = bakpath;
						Logger.getLogger("global").log( Level.WARNING, "Change default backup folder to " + bakpath + "!" );
					}
				}
    		}
    	};
    	
    	commander.registerAction(setdbserverAction);
    	commander.registerAction(setbakfolderAction);
    }
    
    public String getBackupPath(){
    	return _bakpath;
    }
    
	
    /** 
     * Instantiate a new PlainDocument that servers as the document for the text pane.
     * Create a handler of text actions so we can determine if the document has 
     * changes that should be saved.
     */
    /*
    private void makeTextDocument() {
        textDocument = new PlainDocument();
        textDocument.addDocumentListener(new DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
                setHasChanges(true);
            }
            public void removeUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }
            public void insertUpdate(DocumentEvent evt) {
                setHasChanges(true);
            }
        });
    }
    */
    
    
    /**
     * Edit preferences for the document.  Here we simply change the background color of the 
     * text pane.
     */
    /*
    void editPreferences() {
        Object[] colors = {Color.white, Color.gray, Color.red, Color.green, Color.blue};
        Color selection = (Color)JOptionPane.showInputDialog(myWindow(), "Choose a background color:", "Background Color", JOptionPane.INFORMATION_MESSAGE,
        null, colors, myWindow().getTextView().getBackground());
        if ( selection != null ) {
            myWindow().getTextView().setBackground(selection);
        }
    }
    */
    
//    @Override
//    public void setHasChanges( final boolean changeStatus ) {
//        if ( changeStatus != hasChanges ) {
//            hasChanges = changeStatus;
//        }
//    }
}
