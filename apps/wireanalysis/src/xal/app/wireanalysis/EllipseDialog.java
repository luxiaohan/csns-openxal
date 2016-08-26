package xal.app.wireanalysis;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;

import xal.app.wireanalysis.phasespaceanalysis.*;
import xal.extension.widgets.plot.*;


public class EllipseDialog extends JDialog {
	protected GenWindow mywindow;
	protected FunctionGraphsJPanel GP_begin,GP_slit;
	protected JCheckBox jcbx,jcby;
	protected JButton jbplot;
	private PhasePlaneEllipse phasePlaneEllipse = new PhasePlaneEllipse();
	double alphax0,betax0,emittancex0;
	double alphay0,betay0,emittancey0;	
	double alphax,betax,emittancex;
	double alphay,betay,emittancey;
	
	protected JLabel jlalphax0 = new JLabel(" Alphax_b: ");
	protected JLabel jlbetax0 = new JLabel(" Betax_b: ");	
	protected JLabel jlalphay0 = new JLabel(" Alphay_b: ");
	protected JLabel jlbetay0 = new JLabel(" Betay_b: ");
	
	protected JLabel jlalphax = new JLabel(" Alphax_s: ");
	protected JLabel jlbetax = new JLabel(" Betax_s: ");	
	protected JLabel jlalphay = new JLabel(" Alphay_s: ");
	protected JLabel jlbetay = new JLabel(" Betay_s: ");
	
	NumberFormat nf;


	
	public EllipseDialog(GenWindow window){		
		super( window, "Phase Plane Ellipse", true );			
		nf= NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(5);
		nf.setMinimumFractionDigits(5);
		mywindow=window;
		setSize(800, 500);							
		makeContentView();
		setResizable( false );	
	}

	private void makeContentView() {
		
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		
		final Box chooseView = new Box( BoxLayout.X_AXIS );
		final Box plotView = new Box( BoxLayout.X_AXIS );
		jcbx = new JCheckBox("X");
		jcby = new JCheckBox("Y");
		jbplot=new JButton("PLOT");
		jbplot.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				plotEllipse();
			}

		 });
		chooseView.add(jcbx);
		chooseView.add(jcby);
		chooseView.add(Box.createHorizontalStrut(20) );
		chooseView.add(jbplot);
		
		GP_begin = new FunctionGraphsJPanel();
		GP_begin.setPreferredSize(new Dimension(300, 300));
		GP_begin.setOffScreenImageDrawing(true);
		GP_begin.setGraphBackGroundColor(Color.BLUE);
		GP_begin.setGridLinesVisibleX(false);
		GP_begin.setGridLinesVisibleY(false);
		GP_begin.setName("Phase Plane Ellipse at Entrance");
		GP_begin.setAxisNames("x (y), [mm]", "xp (yp), [mrad]");
		
		GP_slit = new FunctionGraphsJPanel();
		GP_slit.setPreferredSize(new Dimension(300, 300));
		GP_slit.setOffScreenImageDrawing(true);
		GP_slit.setGraphBackGroundColor(Color.BLUE);
		GP_slit.setGridLinesVisibleX(false);
		GP_slit.setGridLinesVisibleY(false);
		GP_slit.setName("Phase Plane Ellipse at Slit");
		GP_slit.setAxisNames("x (y), [mm]", "xp (yp), [mrad]");
				
		plotView.add(getleftview());		
		plotView.add(Box.createHorizontalStrut(10) );
		plotView.add(getrightview());
		
     	mainView.add(chooseView);
		mainView.add(Box.createVerticalStrut(20));
		mainView.add(plotView);
		
		getContentPane().add( mainView );		
		
	}
	
	private Component getleftview() {
		final Box leftplotView = new Box( BoxLayout.Y_AXIS );
		final Box twissxView = new Box( BoxLayout.X_AXIS );
		final Box twissyView = new Box( BoxLayout.X_AXIS );	
		twissxView.add(jlalphax0);
		twissxView.add(Box.createHorizontalStrut(50) );
		twissxView.add(jlbetax0);
		
		twissyView.add(jlalphay0);
		twissyView.add(Box.createHorizontalStrut(50) );
		twissyView.add(jlbetay0);
		
		leftplotView.add(GP_begin);
		leftplotView.add(twissxView);
		leftplotView.add(twissyView);

		return leftplotView;
	}
	
	private Component getrightview() {
		final Box rightplotView = new Box( BoxLayout.Y_AXIS );
		final Box twissxView = new Box( BoxLayout.X_AXIS );
		final Box twissyView = new Box( BoxLayout.X_AXIS );	
		twissxView.add(jlalphax);
		twissxView.add(Box.createHorizontalStrut(50) );
		twissxView.add(jlbetax);
		
		twissyView.add(jlalphay);
		twissyView.add(Box.createHorizontalStrut(50) );
		twissyView.add(jlbetay);
		
		rightplotView.add(GP_slit);
		rightplotView.add(twissxView);
		rightplotView.add(twissyView);

		return rightplotView;
	}

	
	
	public void setTwisParamitersX0(double emittancelocx0,double alphalocx0,double betalocx0){
		emittancex0=emittancelocx0;
		alphax0=alphalocx0;
		betax0=betalocx0;
		jlalphax0.setText(" Alphax_b: "+nf.format(alphalocx0));
		jlbetax0.setText(" Betax_b: "+nf.format(betalocx0));	

	}
	
	public void setTwisParamitersY0(double emittancelocy0,double alphalocy0,double betalocy0){
		emittancey0=emittancelocy0;
		alphay0=alphalocy0;
		betay0=betalocy0;
		jlalphay0.setText(" Alphay_b: "+nf.format(alphalocy0));
		jlbetay0.setText(" Betay_b: "+nf.format(betalocy0));
	}
	
	public void setTwisParamitersX(double emittancelocx,double alphalocx,double betalocx){
		emittancex=emittancelocx;
		alphax=alphalocx;
		betax=betalocx;
		jlalphax.setText(" Alphax_s: "+nf.format(alphalocx));
		jlbetax.setText(" Betax_s: "+nf.format(betalocx));
	}
	
	public void setTwisParamitersY(double emittancelocy,double alphalocy,double betalocy){
		emittancey=emittancelocy;
		alphay=alphalocy;
		betay=betalocy;
		jlalphay.setText(" Alphay_s: "+nf.format(alphalocy));
		jlbetay.setText(" Betay_s: "+nf.format(betalocy));
	}
	
	/**
	 *  Plots the ellipse on the phasespace graph.
	 */
	private void plotEllipse() {	
		GP_begin.removeAllCurveData();
		GP_slit.removeAllCurveData();
		if(jcbx.isSelected()){
			PhasePlaneEllipse phasePlaneEllipsex0=new PhasePlaneEllipse();
			phasePlaneEllipsex0.getCurveData().setColor(Color.white);
			phasePlaneEllipsex0.getCurveData().setLineWidth(2);
			phasePlaneEllipsex0.getCurveData().clear();		
			phasePlaneEllipsex0.setEmtAlphaBeta(emittancex0,alphax0,betax0);
			phasePlaneEllipsex0.calcCurvePoints();
			GP_begin.addCurveData(phasePlaneEllipsex0.getCurveData());
			
			PhasePlaneEllipse phasePlaneEllipsex=new PhasePlaneEllipse();
			phasePlaneEllipsex.getCurveData().setColor(Color.white);
			phasePlaneEllipsex.getCurveData().setLineWidth(2);
			phasePlaneEllipsex.getCurveData().clear();		
			phasePlaneEllipsex.setEmtAlphaBeta(emittancex,alphax,betax);
			phasePlaneEllipsex.calcCurvePoints();
			GP_slit.addCurveData(phasePlaneEllipsex.getCurveData());
		}
		if(jcby.isSelected()){
			PhasePlaneEllipse phasePlaneEllipsey0=new PhasePlaneEllipse();
			phasePlaneEllipsey0.getCurveData().setColor(Color.magenta);
			phasePlaneEllipsey0.getCurveData().setLineWidth(2);
			phasePlaneEllipsey0.getCurveData().clear();		
			phasePlaneEllipsey0.setEmtAlphaBeta(emittancey0,alphay0,betay0);
			phasePlaneEllipsey0.calcCurvePoints();
			GP_begin.addCurveData(phasePlaneEllipsey0.getCurveData());

			
			PhasePlaneEllipse phasePlaneEllipsey=new PhasePlaneEllipse();
			phasePlaneEllipsey.getCurveData().setColor(Color.magenta);
			phasePlaneEllipsey.getCurveData().setLineWidth(2);
			phasePlaneEllipsey.getCurveData().clear();		
			phasePlaneEllipsey.setEmtAlphaBeta(emittancey,alphay,betay);
			phasePlaneEllipsey.calcCurvePoints();
			GP_slit.addCurveData(phasePlaneEllipsey.getCurveData());
		}		
	}
}
