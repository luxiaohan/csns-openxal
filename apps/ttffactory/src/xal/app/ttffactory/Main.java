/*
 * Main.java - Main file for the ttfFactory application, sets up the GUI and its functions
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.sun.glass.events.KeyEvent;

import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;


/**
 * This is the main class for the transit time factor parsing application  .
 *
 * @author  James Ghawaly Jr.
 * @version   0.1  15 June 2015
 */
public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new main.
	 */
	public Main() {

        initUI();
    }
	
	/**
	 * This method initializes the Graphical user Interface (GUI)
	 */
    private void initUI() {

    	final Parser parser = new Parser();
    	
    	// Initiate GUI Buttons
    	
        final JButton fileSelectorButton =          new JButton("Browse");  

        final JButton runButton =                   new JButton("Run");
        
        final JButton analyzeButton =               new JButton("Analyze");
        
        final JButton acceleratorTTFButton =        new JButton("Accelerator");
        
        final JButton sequenceTTFButton =           new JButton("Sequence");
        
        final JButton gapTTFButton =                new JButton("Gap");
        
        final JButton acceleratorCompareButton =    new JButton("Accelerator");
        
        final JButton sequenceCompareButton =       new JButton("Sequence");
        
        final JButton gapCompareButton =            new JButton("Gap");
        
        final JButton acceleratorGenButton =        new JButton("Accelerator");
        
        final JButton sequenceGenButton =           new JButton("Sequence");
        
        final JButton gapGenButton =                new JButton("Gap");
        
        final JLabel calcLabel =                    new JLabel("Calculate TTF:      ");
        
        final JLabel compLabel =                    new JLabel("Compare TTF:       ");
        
        final JLabel genLabel =                     new JLabel("Generate TTF File: ");
        
        // When hovering cursor over the buttons, display the selected button's purpose
        fileSelectorButton.setToolTipText("Select File from Directory Browser");  
        runButton.setToolTipText("Run the Parser and Create New File");
        analyzeButton.setToolTipText("Retrieve the specified data from the specified gap");
        acceleratorTTFButton.setToolTipText("Generate TTFs for all gaps in a chosen accelerator");
        sequenceTTFButton.setToolTipText("Generate TTFs for all gaps in a chosen sequence");
        gapTTFButton.setToolTipText("Generate TTF for a single chosen gap.");
        acceleratorCompareButton.setToolTipText("Compare integral-calculated TTFs for all gaps in a chosen accelerator to Andrei's TTFs.");
        sequenceCompareButton.setToolTipText("Compare integral-calculated TTFs for all gaps in a chosen sequence to Andrei's TTFs.");
        gapCompareButton.setToolTipText("Compare integral-calculated TTF for a single chosen gap to Andrei's TTFs.");
        acceleratorGenButton.setToolTipText("Generate TTF files for all gaps in a chosen accelerator");
        sequenceGenButton.setToolTipText("Generate TTF filesfor all gaps in a chosen sequence");
        gapGenButton.setToolTipText("Generate TTF files for a single chosen gap.");
        runButton.setEnabled(false);
        analyzeButton.setEnabled(false);
        
        //create a text field with a default file name
        final JTextField fileLabel = new JTextField("name of file to save to.xdxf");
        
        //JTextField valueLabel = new JTextField("Value Tag");
        String[] tagOptions = {"ttf", "stf", "ttfp", "stfp"};
        final JComboBox<String> valueLabel = new JComboBox<String>(tagOptions);
        
        //create a text area with instructions for how to use the program on the T(k) Tab
        final JTextArea infoBox = new JTextArea("- To parse a file, select the 'Browse' button and choose your file, then select 'Run.' An option to save the new file will be given.\n "
        		                              + "\n- To retrieve a particular value from a specific gap; after running the parser, type the tag of the value that you want to retrieve into the 'Value Tag' text area, choose the gap from the drop down menu and select 'Analyze.'\n "
        		                              + "\n- Possible choices are: ttf, stf, ttfp, stfp.\n \n- See the README for more information.");
        infoBox.setEditable(false);
        infoBox.setWrapStyleWord(true);
        infoBox.setLineWrap(true);

        //create a text area with instructions for how to use the program on the T(Beta) tab
        final JTextArea infoBox2 = new JTextArea("- To calculate transit time factors at points or to generate polynomials, use the buttons on the 'Calculate TTF' row.\n" +
        										 "- To compare calculated transit time factors to Andrei's, use the buttons on the 'Compare TTF' row.\n"+
        										 "- To generate an xdxf file of integral-calculated TTF polynomials, use the buttons on the 'Generate TTF File' row.\n");
        infoBox2.setEditable(false);
        infoBox2.setWrapStyleWord(true);
        infoBox2.setLineWrap(true);
        
        //create a label for the results of a value point analysis
        final JLabel resultLabel = new JLabel("Result: ");
        
        final JTextField resultText = new JTextField("...");
        resultText.setEditable(false);
        
        final JLabel gapLabel = new JLabel("Choose RF Gap: ");
        
        //create a drop down menu that eventually contains all of the gaps in the accelerator
        final JComboBox<String> gapChooser = new JComboBox<String>();
        
        //INITIATE ALL FILE CHOOSERS
        // This is the file chooser menu, it brings up a directory explorer
        final JFileChooser fileSelector = new JFileChooser();
        
        final JFileChooser saveSelector = new JFileChooser();
        saveSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        final JFileChooser gapSelector = new JFileChooser();
        
        final JFileChooser acceleratorSelector = new JFileChooser();
        acceleratorSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        final JFileChooser sequenceSelector = new JFileChooser();
        sequenceSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        gapChooser.setToolTipText("Choose a Gap From the Drop-down Menu to Analyze");
        valueLabel.setToolTipText("Type the Tag of the Value You Want to Get From the Selected Gap; options: ttf, stf, ttfp, stfp");
        
      //----------------------------------------------------------------------------------------------- START ACTIONS
        
        
        // We add an action listener to the file selector button, which signals the method actionPerformed when clicked
        fileSelectorButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	
            	// choice is an integer that corresponds to the action selected by the user
                int choice = fileSelector.showOpenDialog(Main.this);
                
                // if the choice is valid
                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = fileSelector.getSelectedFile();
                	// print the name of the file we are opening
                	System.out.println("Opening File: " + file.getName());
                	runButton.setEnabled(true);
                } else {
                	// If the user closes the file chooser before selecting a file, print the following line
                	System.out.println("File Selection Aborted by User\n");
                }
                getContentPane().revalidate();
            }
            
        });

        // We add an action listener to the run button, which signals the method actionPerformed when clicked 
        runButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	//Parser parser = new Parser();
        		try {
					parser.parse(fileSelector.getSelectedFile());
	        		System.out.println("File Parsed");
	        		ArrayList<String> gapList = parser.getGapList();
	        		// This adds the gaps to the gap chooser
	        		for (String str : gapList) {
	        			gapChooser.addItem(str);
	        		}
	        		
	        		int optionResult = JOptionPane.showConfirmDialog(getContentPane(), "Would you like to save the parsed data to a new file?");
	        		// allow the analyze button to be pressed
                    analyzeButton.setEnabled(true);
                    getContentPane().revalidate();
                    
	        		// If the user chooses to save the parsed data to a file, run this block of code
	        		if (optionResult == JOptionPane.YES_OPTION) {
	                    int choice = saveSelector.showOpenDialog(Main.this);
	                    // if the choice is valid
	                    if (choice == JFileChooser.APPROVE_OPTION) {
	                    	// grab the selected file using java's io File class
	                    	File file = saveSelector.getSelectedFile();
	                    	// print the name of the file we are opening
	                    	System.out.println("Saving to File: " + file.getName());
	                    	String filename = fileLabel.getText();
	                    	parser.pack(new File(file,filename));
	                    } else {
	                    	// If the user closes the file chooser before selecting a file, print the following line
	                    	System.out.println("File Selection Aborted by User");
	                    }
	                    
	        		}
	        		
	        		// The following code block creates an error dialog box if an error of the types listed below is encountered
				} catch (ParseException | ResourceNotFoundException
						| MalformedURLException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}

            }
        });
        
        // This action listener is called when the "Analyze" button is clicked
        analyzeButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	String valueChoice = (String) valueLabel.getSelectedItem();
            	String gapChoice = (String) gapChooser.getSelectedItem();
            	String data = parser.getValue(gapChoice, valueChoice);
            	resultText.setText(data);
            	JTextField infoText = new JTextField(data);
            	infoText.setEditable(false);
            	JOptionPane.showMessageDialog(getContentPane(), infoText,valueChoice + " for " + gapChoice,JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        //TODO: Handle SCL and END gaps
        gapTTFButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event) {
        		int choice = gapSelector.showOpenDialog(Main.this);

                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = gapSelector.getSelectedFile();
                	try {
						gapTTF(file);
					} catch (IOException e) {
						e.printStackTrace();
					}

                } else {
                	System.out.println("File Selection Aborted by User");
                }
        		
        	}
        });
        
        //TODO: Handle SCL and END gaps
        sequenceTTFButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event) {
        		
        		int choice = sequenceSelector.showOpenDialog(Main.this);
        		
        		if(choice == JFileChooser.APPROVE_OPTION) {
        			File file = sequenceSelector.getSelectedFile();
        			
	        		try {
						directoryTTF(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
        		
        		} else {
        			System.out.println("File Selection Aborted by User");
        		}
        	}
        });
        
        //TODO: Handle SCL and END gaps
        acceleratorTTFButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event) {

        		int choice = acceleratorSelector.showOpenDialog(Main.this);
            	
                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = acceleratorSelector.getSelectedFile();
                	
                	try {
						directoryTTF(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
                } else {
                	System.out.println("File Selection Aborted by User");
                }
        	}
        });
        //----------------------------------------------------------------------------------------------- END ACTIONS
        JTabbedPane tabPane = new JTabbedPane();

    	JPanel panes = new JPanel();
    	JPanel pane2 = new JPanel();
    	
        // This line calls the createLayout method, which formats how items are displayed on the GUI
        GroupLayout gl = createLayout(true, panes, fileSelectorButton, runButton, fileLabel, gapLabel, gapChooser, valueLabel, analyzeButton, resultLabel, resultText, infoBox);
        //                                                  0                     1            2                   3                      4                   5               6                     7               8         9         10       11       12
        GroupLayout gl2 = createLayout(false,pane2,acceleratorTTFButton,sequenceTTFButton,gapTTFButton,acceleratorCompareButton,sequenceCompareButton,gapCompareButton,acceleratorGenButton,sequenceGenButton,gapGenButton,calcLabel,compLabel,genLabel,infoBox2);
        
        panes.setLayout(gl);
        pane2.setLayout(gl2);
        
        tabPane.addTab("T(k)",panes);
        tabPane.addTab("T(Beta)",pane2);
        
        paint(tabPane);
        
    }
    
    /**
     * This method defines how the GUI component will be formatted. In this case we use a GroupLayout setup, as it is robust
     *
     * @param arg the list of components in the GUI
     */
    private GroupLayout createLayout(Boolean tabChoice, Container cont, JComponent... arg) {
    	GroupLayout gl = null;
    	if(tabChoice) {
    		gl = new GroupLayout(cont);
	        
	        gl.setAutoCreateGaps(true);
	        gl.setAutoCreateContainerGaps(true);
	
	        gl.setHorizontalGroup(
	        		gl.createParallelGroup()
	        		.addComponent(arg[9],GroupLayout.PREFERRED_SIZE, 740,GroupLayout.PREFERRED_SIZE)
	                .addGroup(gl.createSequentialGroup()
	                		.addComponent(arg[0]) // browse button
	                		.addComponent(arg[2]) // file chosen label
	                		.addComponent(arg[1]) // run button
	                		)
	                .addGroup(gl.createSequentialGroup()
	                		.addComponent(arg[6]) // analyze button
	                		.addComponent(arg[4]) // gapChooser
	                		.addComponent(arg[5]) // 
	                )
	
	        );
	        gl.setVerticalGroup(
	        		gl.createSequentialGroup()
	        		.addComponent(arg[9],GroupLayout.PREFERRED_SIZE, 150,GroupLayout.PREFERRED_SIZE)
	                .addGroup(gl.createParallelGroup()
	                		.addComponent(arg[0]) // browse button
	                		.addComponent(arg[2]) // file chosen label
	                		.addComponent(arg[1]) // run button
	                		)
	                .addGroup(gl.createParallelGroup()
	                		.addComponent(arg[6]) // analyze button
	                		.addComponent(arg[4]) // gapChooser
	                		.addComponent(arg[5]) // 
	                		)
	        );
    	} else {
    		gl = new GroupLayout(cont);
    		
    		gl.setAutoCreateGaps(true);
	        gl.setAutoCreateContainerGaps(true);
	        
	        gl.setHorizontalGroup(
	        		gl.createParallelGroup()
	        		.addComponent(arg[12],GroupLayout.PREFERRED_SIZE, 750,GroupLayout.PREFERRED_SIZE)
	        		.addGroup(gl.createSequentialGroup()
	        				.addComponent(arg[9])
	        				.addComponent(arg[0])
	        				.addComponent(arg[1])
	        				.addComponent(arg[2])
	        				)
	        		.addGroup(gl.createSequentialGroup()
	        				.addComponent(arg[10])
	        				.addComponent(arg[3])
	        				.addComponent(arg[4])
	        				.addComponent(arg[5])
	        				)
	        		.addGroup(gl.createSequentialGroup()
	        				.addComponent(arg[11])
	        				.addComponent(arg[6])
	        				.addComponent(arg[7])
	        				.addComponent(arg[8])
	        				)
	        		);
	        gl.setVerticalGroup(
	        		gl.createSequentialGroup()
	        		.addComponent(arg[12],GroupLayout.PREFERRED_SIZE, 70,GroupLayout.PREFERRED_SIZE)
	        		.addGroup(gl.createParallelGroup()
	        				.addComponent(arg[9])
	        				.addComponent(arg[0])
	        				.addComponent(arg[1])
	        				.addComponent(arg[2])
	        				)
	        		.addGroup(gl.createParallelGroup()
	        				.addComponent(arg[10])
	        				.addComponent(arg[3])
	        				.addComponent(arg[4])
	        				.addComponent(arg[5])
	        				)
	        		.addGroup(gl.createParallelGroup()
	        				.addComponent(arg[11])
	        				.addComponent(arg[6])
	        				.addComponent(arg[7])
	        				.addComponent(arg[8])
	        				)
	        		);
    	}
    	
        return gl;
    }
    
    
    /**
     * This method actually paints the GUI onto the screen
     *
     * @param comp the component containing all of the GUI features to be added to the frame
     */
    public void paint(JComponent comp) {
    	JFrame frame = new JFrame();
        frame.add(comp, BorderLayout.CENTER);
        frame.setTitle("TTF Parser");
        frame.setSize(780, 300);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);                              // This line centers the GUI on the screen
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);                  // Exits application upon clicking the X button on the GUI
        frame.setVisible(true);
    }
    
    public void gapTTF(File file) throws IOException {
	    String filePath = file.getAbsolutePath();
	    
		Parser betaParser = new Parser();
		betaParser.readBetaConfigFile();
		DataTree betaTree = betaParser.getDataTree();
		
		ElectricFileReader eFR = new ElectricFileReader(filePath);
		List<Double> ZData = eFR.getDblZpoints();
		List<Double> EFdata = eFR.getDblEField();
		//for SCL only
		int i = 0;
		for(Double dbl:EFdata) {
			EFdata.set(i, -1.0*dbl);
			i++;
		}
			//END SCl ONly	
		TTFTools ttfT = new TTFTools();
		Tools tools = new Tools();
		
		String parsedName = tools.transformName(file.getName());
		System.out.println(parsedName);
		double betaMin = Double.parseDouble(betaTree.getValue(parsedName, "beta_min"));
		double betaMax = Double.parseDouble(betaTree.getValue(parsedName, "beta_max"));
		double frequency = Double.parseDouble(betaTree.getValue(parsedName, "frequency"));
		
		double[] betaList = ttfT.linspace(betaMin, betaMax , 100); //x
		double[] ttfList = ttfT.getTTFForBetaRange(ZData, EFdata, true, frequency, betaList); 
		
		//System.out.println("ZData: " + ZData.toString());
		//System.out.println("EFData: " + EFdata.toString() + "\n");
		//System.out.println("Betas: " + Arrays.toString(betaList));
		//System.out.println("TTFs: " + Arrays.toString(ttfList)+ "\n");

		PolynomialFit polyFit = new PolynomialFit(betaList,ttfList);
		double[] consts = polyFit.getPolyConstants();
		System.out.println(polyFit.toString(consts));


    }
    
    public void directoryTTF(File file) throws IOException {
    	String filePathString = file.getAbsolutePath();
    	
    	Path begin = Paths.get(filePathString);
    	
    	// we want to walk through all subdirectories of the chosen directory and calculate the TTFs of the RF Gap files
		Parser betaParser = new Parser();
		betaParser.readBetaConfigFile();
		DataTree betaTree = betaParser.getDataTree();
		
		Tools tools = new Tools();

		Files.walkFileTree(begin, new SimpleFileVisitor<Path>() { 
		    @Override
		    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
		    	File file = filePath.toFile();
		    	//Skip all of the .DS_Store files
		    	if(!file.isHidden()){
		    		String fileName = file.getName();
		    		//This converts the filename to the name required for OpenXAL
		    		String parsedName = tools.transformName(fileName);
		    		
		    		System.out.println("Analyzing: " + filePath.getFileName());
		    		
		    		if(!tools.isEndGap(fileName)) {
		    			//Instantiate a new electric file reader for the file path and grab the Z and EF data
			    		ElectricFileReader eFR = new ElectricFileReader(file.getAbsolutePath());
		    			List<Double> ZData = eFR.getDblZpoints();
						List<Double> EFdata = eFR.getDblEField();
						
						TTFTools ttfT = new TTFTools();
						
						double betaMin = Double.parseDouble(betaTree.getValue(parsedName, "beta_min"));
						double betaMax = Double.parseDouble(betaTree.getValue(parsedName, "beta_max"));
						double frequency = Double.parseDouble(betaTree.getValue(parsedName, "frequency"));
						
						//Calculate the transit time factor at the beta range
						double[] betaList = ttfT.linspace(betaMin, betaMax, 100); 
						double[] ttfList = ttfT.getTTFForBetaRange(ZData, EFdata, true, frequency, betaList); 
						
						System.out.println("TTF Calculated...");
		    		}
		    		else if(tools.isEndGap(fileName)) {
		    			System.out.println("Skipped END Gap...");
		    		}
		    		else if(fileName.contains("SCL")){
		    			System.out.println("Skipped SCL for now...");
		    		}
					
		    	}
		    	return FileVisitResult.CONTINUE;
		    }
		    	
		});
    }
    
    /**
     * The main method. Sets up the GUI
     *
     * @param args There are no arguments
     */
    public static void main(String[] args) {
    	
    	try {
    		System.out.println("Launching Application ttfParser...");
    		// This line prevents the application from having UI update concurrency issues
	        EventQueue.invokeLater(new Runnable() {               
	        
	            @Override
	            public void run() {
	                @SuppressWarnings("unused")
					Main ex = new Main(); // instantiate the main class
	            }
	        });
	        System.out.println("Application Launched");
    	}
    	// prints an exception and ends the program if there are errors while starting up the application
    	catch (Exception exception){
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			System.exit( -1 );
    	}

    }
}


