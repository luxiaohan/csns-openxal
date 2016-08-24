package xal.app.magnetdbmanager.magnet;

//import gov.sns.tools.data.DataAdaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import xal.extension.fit.OrthogonalLeastSquareMethod;
import xal.tools.data.DataAdaptor;

//import csns.tools.fit.OrthogonalLeastSquareMethod;

/**
 * Magnetization curve fit result. I_to_B or B_to_I.
 * 
 * B(I)=Sum[coeib_i*(I-I_average)^i), {i,0,n}], or
 * I(B)=Sum[coebi_i*(B-B_average)^i), {i,0,n}].
 * 
 * @author liuwb
 *
 */
public class MCFitResult implements Cloneable{
	private double _average = 0;
	private double[] _coe = null;
	
	public MCFitResult(double average, ArrayList<Double> coe){
		if(coe == null) return;
		
		_average = average;
		_coe = new double[coe.size()];
		for(int i=0; i<coe.size(); i++) _coe[i] = coe.get(i);
	}
	
	public MCFitResult(double average, double[] coe){
		if(coe == null) return;
		
		_average = average;
		_coe = new double[coe.length];
		for(int i=0; i<coe.length; i++) _coe[i] = coe[i];
	}
	
	public MCFitResult(double average, Map<Integer, Double> coemap){
		if(!setCoe(coemap)) return;
		
		_average = average;
	}
	
	public void setAverage(double average){
		_average = average;
	}
	
	public void setCoe(double[] coe){
		if(coe == null) return;
		
		_coe = new double[coe.length];
		for(int i=0; i<coe.length; i++) _coe[i] = coe[i];
	}
	
	public void setCoe(ArrayList<Double> coe){
		if(coe == null) return;
		
		_coe = new double[coe.size()];
		for(int i=0; i<coe.size(); i++) _coe[i] = coe.get(i);
	}
	
	public boolean setCoe(int order, double coe){
		if(order<0 || order>_coe.length-1) return false;
		
		_coe[order] = coe;
		
		return true;
	}
	
	public boolean setCoe(Map<Integer, Double> coemap){
		if(coemap == null || coemap.size() == 0) return false;
		
		Set<Integer> orders = coemap.keySet();
		
		int maxorder = 0;
		for(Integer order : orders){
			if(order < 0) return false;
			if(order > maxorder) maxorder = order;
		}
		if(maxorder == 0){
			_coe = null;
			return false;
		}
		else _coe = new double[maxorder + 1];
		
		for(Integer order : orders){
			_coe[order] = coemap.get(order);
		}
		
		return true;
	}
	
	public int size(){
		if(_coe == null) return 0;
		return _coe.length;
	}
	
	public MCFitResult clone(){
		MCFitResult copy = null;
		try{
			copy = (MCFitResult) super.clone();
		} catch(CloneNotSupportedException e) {
			   e.printStackTrace();
	    }
		
		copy._average = _average;
		
		if(_coe != null){
			copy._coe = new double[_coe.length];
			for(int i=0; i<_coe.length; i++) copy._coe[i] = _coe[i];
		}
		
		return copy;
	}
	
	public int getOrder(){
		if(_coe == null) return 0;
		
		return _coe.length - 1;
	}
	
	public double getAverage(){
		return _average;
	}
	
	public double getCoe(int order){
		if(_coe == null || order >= _coe.length) return Double.NaN;
		return _coe[order];
	}
	
	public double[] getCoe(){
		return _coe;
	}
	
	public void clear(){
		_average = 0;
		_coe = null;
	}
	
	public double calculate(double x){
		if(_coe == null) return Double.NaN;
		
		return OrthogonalLeastSquareMethod.getY(x, _average, _coe);
	}
	
	public boolean equals(final MCFitResult mcfitresult){
		if(_average != mcfitresult.getAverage()) return false;
		
		if(_coe == null && mcfitresult.getCoe() == null) return true;
		else if(_coe == null || mcfitresult.getCoe() == null) return false;
		else if(_coe.length != mcfitresult.getCoe().length) return false;
		
		for(int i=0; i<_coe.length; i++){
			if(_coe[i] != mcfitresult.getCoe(i)) return false;
		}
		
		return true;
	}
	
	public String toString(){
		String text = "";
		
		text = text + "Average = " + _average + "\n";
		
		if(_coe != null){
			for(int i=0; i<_coe.length; i++) text = text + "Coe["+ i + "] = " + _coe[i] + "\n";
		}

		return text;
	}
	
	public void writeToDataAdaptor(DataAdaptor da){
		if(getOrder() == 0) return;
		da.setValue("average", _average);
		for(int order=0; order<=getOrder(); order++){
			DataAdaptor coeda = da.createChild("value");
			coeda.setValue("order", order);
			coeda.setValue("coe", getCoe(order));
		}
	}
	
	static public MCFitResult parseDataAdaptor(DataAdaptor da){
		if(da == null) return null;
		
		double average = da.doubleValue("average");
		
		List<DataAdaptor> datas = da.childAdaptors("value");
		int max = 0;
		for(DataAdaptor data : datas){
			int order = data.intValue("order");
			if(order < 0) return null;
			if(order > max) max = order;
		}
		if(max == 0) return null;
		
		double[] coe = new double[max+1];
		for(DataAdaptor data : datas){
			int order = data.intValue("order");
			double value = data.doubleValue("coe");
			coe[order] = value;
		}
		
		MCFitResult mcfresult = new MCFitResult(average, coe);
		return mcfresult;
	}
	
}
