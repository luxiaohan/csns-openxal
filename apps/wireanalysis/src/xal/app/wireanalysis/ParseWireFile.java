/*
 * ParseWirefile.java
 *
 * Created on November 12, 2004 */

package xal.app.wireanalysis;


import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Read and parse a wirescan file, extract data and send out as HashMap.
 *
 * @author  cp3
 */
 
public class ParseWireFile{
    
    public ArrayList wiredata = new ArrayList();
    
    /** Creates new ParseWireFile */
    public ParseWireFile() {
	
    }
    
    public ArrayList parseFile(File newfile) throws IOException  {
	String s;
	String firstName;
	String header;
	String name = null;
	Integer PVLoggerID = new Integer(0);
	String[] tokens;
	int nvalues = 0;
	double num1, num2, num3;
	double xoffset = 1.0;
	double xdelta = 1.0;
	double yoffset = 1.0;
	double ydelta = 1.0;
	double zoffset = 1.0;
	double zdelta = 1.0;
	boolean readfit = false;
	boolean readraw = false;
	boolean zerodata = false;
	boolean baddata = false;
	boolean harpdata = false;
	boolean elsdata = false;
	boolean elsx = false;
	boolean elsy = false;
	ArrayList fitparams = new ArrayList();
	ArrayList xraw = new ArrayList();
	ArrayList yraw = new ArrayList();
	ArrayList zraw = new ArrayList();
	ArrayList sraw = new ArrayList();
	ArrayList sxraw = new ArrayList();
	ArrayList syraw = new ArrayList();
	ArrayList szraw = new ArrayList();
	
	//Open the file.
	//URL url = getClass().getResource(filename);
	
	URL url = newfile.toURI().toURL();
	
	InputStream is = url.openStream();
	InputStreamReader isr = new InputStreamReader(is);
	BufferedReader br = new BufferedReader(isr);
        
	
	while((s=br.readLine()) != null){
	    tokens = s.split("\\s+");
	    nvalues = tokens.length;
	    firstName = (String)tokens[0];
	    //if(((String)tokens[0]).length()==0){  //Skip blank lines
	    if((tokens[0]).length()==0 & firstName.length()>1){ //liyong modify
		readraw = false;
		readfit = false;
		continue;
	    }
	    if( (nvalues == 4) && (!firstName.startsWith("---")) ){
		//System.out.println("tokens[0]" + tokens[0]);
		if( (Double.parseDouble(tokens[1]) == 0.) && (Double.parseDouble(tokens[2]) == 0.) && (Double.parseDouble(tokens[3]) == 0.) ){
		    zerodata = true;
		}
		else{
		    zerodata = false;
		}
		if( tokens[1].equals("NaN") || tokens[2].equals("NaN") || tokens[3].equals("NaN")){
		    baddata = true;
		}
		else{
		    baddata = false;
		}
	    }
	    if(firstName.startsWith("start")){
		header = s;
	    }
	    if((firstName.indexOf("WS") > 0) || (firstName.indexOf("LW") > 0)){
		if(name != null){
		    dumpData(name, fitparams, sraw, sxraw, syraw, szraw, yraw, zraw, xraw);
		}
		name = tokens[0];
		readraw = false;
		readfit = false;
		zerodata = false;
		baddata = false;
		harpdata=false;
		fitparams.clear();
		xraw.clear();
		yraw.clear();
		zraw.clear();
		sraw.clear();
		sxraw.clear();
		syraw.clear();
		szraw.clear();
	    }
	    if(firstName.startsWith("Area")) ;
	    if(firstName.startsWith("Ampl")) ;
	    if(firstName.startsWith("Mean")) ;
	    if(firstName.startsWith("Sigma")){
		fitparams.add(new Double(Double.parseDouble(tokens[3]))); //zfit
		fitparams.add(new Double(Double.parseDouble(tokens[1]))); //yfit
		fitparams.add(new Double(Double.parseDouble(tokens[5]))); //xfit
	    }
	    if(firstName.startsWith("Offset")) ;
	    if(firstName.startsWith("Slope")) ;
	    if((firstName.equals("Position")) && (((String)tokens[2]).equals("Raw")) ){
		readraw = true;
		continue;
	    }
	    if((firstName.equals("Position")) && (((String)tokens[2]).equals("Fit")) ){
		readfit = true;
	    	continue;
	    }		
	    if((firstName.contains("Harp"))){
		xraw.clear();
		yraw.clear();
		zraw.clear();
		sraw.clear();
		sxraw.clear();
		syraw.clear();
		szraw.clear();
		fitparams.clear();
		harpdata = true;
		readraw = true;
		name = tokens[0];
		fitparams.add(new Double(0.0)); //xfit
		fitparams.add(new Double(0.0)); //yfit
		fitparams.add(new Double(0.0)); //zfit
		continue;
	    }
	    
	    if((firstName.contains("ELS"))){
			elsdata=true;
			name = tokens[0];
			fitparams.add(new Double(0.0)); //xfit
			fitparams.add(new Double(0.0)); //yfit
			fitparams.add(new Double(0.0)); //zfit
			continue;
	    }
		
		if(elsdata && firstName.contains("X_Position")){
			System.out.println("Found X for ELS");
			elsx = true;
			elsy = false;
			xraw.clear();
			yraw.clear();
			zraw.clear();
			sraw.clear();
			sxraw.clear();
			syraw.clear();
			szraw.clear();
			fitparams.clear();
			//elsdata = true;
			readraw = true;
			continue;
		}
		if(elsdata && firstName.contains("Y_Position")){
			System.out.println("Found Y for ELS");
			elsx = false;
			elsy = true;
			continue;
		}
		
	    if(firstName.startsWith("---")) continue ;
	    
	    if(harpdata==true){
		if(((String)tokens[0]).length()!=0){  //Skip blank lines
		    if(firstName.startsWith("PVLogger")){
			try{
			    PVLoggerID = new Integer(Integer.parseInt(tokens[2]));
			}
			catch(NumberFormatException e){
			}
		    }
		    else{
			sxraw.add(new Double(Double.parseDouble(tokens[0])));
			xraw.add(new Double(Double.parseDouble(tokens[1])));
			syraw.add(new Double(Double.parseDouble(tokens[2])));
			yraw.add(new Double(Double.parseDouble(tokens[3])));
			szraw.add(new Double(Double.parseDouble(tokens[4])));
			zraw.add(new Double(Double.parseDouble(tokens[5])));
			
		    }
		}
		continue;
	    }	
	    
	    if(elsdata==true){
		    if(elsx == true){
			    if(((String)tokens[0]).length()!=0){  //Skip blank lines
				    sxraw.add(new Double(Double.parseDouble(tokens[0])));
				    xraw.add(new Double(Double.parseDouble(tokens[1])));
			    }
		    }
		    if(elsy == true){
			    if(((String)tokens[0]).length()!=0){  //Skip blank lines
				    syraw.add(new Double(Double.parseDouble(tokens[0])));
				    yraw.add(new Double(Double.parseDouble(tokens[1])));
					szraw.add(new Double(0.0));
					zraw.add(new Double(0.0));
			    }
		    }
		continue;
	    }	
	    
	    
	   // if(readraw && (!zerodata) && (!baddata) ){
	    if(readraw && (!zerodata) && (!baddata) && tokens.length>3){//liyong add
		if(tokens.length == 7){
		    sraw.add(new Double(Double.parseDouble(tokens[0])/Math.sqrt(2.0)));
		    sxraw.add(new Double(Double.parseDouble(tokens[4])));
		    syraw.add(new Double(Double.parseDouble(tokens[5])));
		    szraw.add(new Double(Double.parseDouble(tokens[6])));
		    yraw.add(new Double(Double.parseDouble(tokens[1])));
		    zraw.add(new Double(Double.parseDouble(tokens[2])));
		    xraw.add(new Double(Double.parseDouble(tokens[3])));   
		}
		else{
		    sraw.add(new Double(Double.parseDouble(tokens[0])/Math.sqrt(2.0)));
		    sxraw.add(new Double(Double.parseDouble(tokens[0])/Math.sqrt(2.0)));
		    syraw.add(new Double(Double.parseDouble(tokens[0])/Math.sqrt(2.0)));
		    szraw.add(new Double(Double.parseDouble(tokens[0])));
		    yraw.add(new Double(Double.parseDouble(tokens[1])));
		    zraw.add(new Double(Double.parseDouble(tokens[2])));
		    xraw.add(new Double(Double.parseDouble(tokens[3])));
		}
	    }
	    if(firstName.startsWith("PVLogger")){
		try{
		    PVLoggerID = new Integer(Integer.parseInt(tokens[2]));
		}
		catch(NumberFormatException e){
		}
	    }
	    
	  
	}
	dumpData(name, fitparams, sraw, sxraw, syraw, szraw, yraw, zraw, xraw);
	//writeData();
	wiredata.add((Integer)PVLoggerID);
	return wiredata;
 }

 private void dumpData(String label, ArrayList fitparams, ArrayList sraw, ArrayList sxraw, ArrayList syraw, ArrayList szraw, ArrayList yraw, ArrayList zraw, ArrayList xraw){
    
	 HashMap data = new HashMap();

     data.put("name", label);
     data.put("fitparams", new ArrayList(fitparams));
     data.put("sdata", new ArrayList(sraw));
     data.put("sxdata", new ArrayList(sxraw));
     data.put("sydata", new ArrayList(syraw));
     data.put("szdata", new ArrayList(szraw));
     data.put("xdata", new ArrayList(xraw));
     data.put("ydata", new ArrayList(yraw));
     data.put("zdata", new ArrayList(zraw));
	      
     wiredata.add((HashMap)data);
     
   }
     
 private void writeData(){
     //This is just a routine to write out the current data set.
     Iterator itr = wiredata.iterator();
     	while(itr.hasNext()){
	    HashMap map = (HashMap)itr.next();
	    ArrayList fitlist = (ArrayList)map.get("fitparams");
	    ArrayList slist = (ArrayList)map.get("sdata");
	    ArrayList ylist = (ArrayList)map.get("ydata");
	    ArrayList zlist = (ArrayList)map.get("zdata");
	    ArrayList xlist = (ArrayList)map.get("xdata");
	    int ssize = slist.size();
	    int xsize = xlist.size();
	    int ysize = ylist.size();
	    int zsize = zlist.size();
	    System.out.println("This is " + map.get("name"));
	    System.out.println("With fit params " + fitlist.get(0) + " " + fitlist.get (1) + " " +fitlist.get(2));

	   if((ssize == xsize ) && (xsize == ysize) &&  (ysize == zsize)){ 
		for(int i = 0; i<ssize; i++){
		    System.out.println(slist.get(i) + "  " + ylist.get(i) + "  " + zlist.get(i) + "  " + xlist.get(i));
		}
	    }
	    else{
		System.out.println("Oops, a problem with array sizing.");
		System.out.println(ssize + " " + xsize + " " + ysize + " " + zsize);
	    }
	    
	}
 }
}
 


