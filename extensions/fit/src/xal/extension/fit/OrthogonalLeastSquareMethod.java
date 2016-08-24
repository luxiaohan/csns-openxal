package xal.extension.fit;

import java.util.ArrayList;


/**
 * 
 * 函数功能：正交最小二乘法曲线拟合（orthogonal least-squares fit method)，
 * 
 * 函数形式：y = Sum( coe_i * (x - xavg)^i, {i, 0, n} )， x和y为原始数据，coe_i为各阶系数。
 * 
 * @author qiujing
 * @version 1.0
 * 
 */
public class OrthogonalLeastSquareMethod {
	
	/**
	 * @param x 变量值
	 * @param y 原始数据
	 * @param m 拟合多项式的项数，即拟合多项式的最高阶数为 m-1. 要求 m<=x.length 且m<=21。若 m>x.length 或 m>21 ，按
	 *        m = min{x.length, 21}处理
	 * @return 拟合得到的各阶系数
	 */
	public static double[] PolyFit(double x[], double y[], int m) {
		if(x.length != y.length) return null;
		
		int i, j, k;
		//datalength 是拟合数据的个数,datalength_Double为datalength的实数形式
		int datalength = x.length;
		double datalength_Double = 1.0*datalength;
		
		//中间变量
		double x_average, p, c, g, q = 0.0, d1, d2;
		
		//中间数组变量
		int dim = 21;
		double[] s = new double[dim];
		double[] t = new double[dim];
		double[] b = new double[dim];
		double[] dt = new double[3];
		
		//coe 实型一维数组，长度为 m 。返回 m-1次拟合多项式的 m 个系数 coe[0]~coe[m-1]
		double[] coe = new double[m];
		//为coe赋初值
		for (i = 0; i <m; i++) coe[i] = 0.0;
		
		//如果m大于拟合数据长度，则 m 值改为拟合数据长度，同时还要保证m值小于等于21
		if (m > datalength) m = datalength;

		if (m > dim) m = dim;
		
		
		x_average = 0.0;
		for (i = 0; i <datalength; i++) {
			x_average = x_average + x[i] / datalength_Double;//求x[]数组的平均值
		}
		
		b[0] = 1.0;
		d1 = 1.0 * datalength;
		p = 0.0;
		c = 0.0;
		for (i = 0; i <datalength; i++) {
			p = p + (x[i] - x_average); //计算x[]离差和
			c = c + y[i]; //计算y[]总和
		}
		c = c / d1;  //计算y[]平均值
		p = p / d1; //计算x[]离差
		
		coe[0] = c * b[0]; //等于 y[] 平均值
		if (m > 1) {
			t[1] = 1.0;
			t[0] = -p;
			d2 = 0.0;
			c = 0.0;
			g = 0.0;
			for (i = 0; i <datalength; i++) {
				q = x[i] - x_average - p;
				d2 = d2 + q * q;
				c = c + y[i] * q;
				g = g + (x[i] - x_average) * q * q;
			}
			c = c / d2;
			p = g / d2;
			q = d2 / d1;
			d1 = d2;
			coe[1] = c * t[1];
			coe[0] = c * t[0] + coe[0];
		}
		
		for (j = 2; j <m; j++) {
			s[j] = t[j - 1];
			s[j - 1] = -p * t[j - 1] + t[j - 2];
			if (j >= 3){
				for (k = j - 2; k >= 1; k--) {
					s[k] = -p * t[k] + t[k - 1] - q * b[k];
				}
			}
			s[0] = -p * t[0] - q * b[0];
			
			d2 = 0.0;
			c = 0.0;
			g = 0.0;
			for (i = 0; i <datalength; i++) {
				q = s[j];
				for (k = j - 1; k >= 0; k--) {
					q = q * (x[i] - x_average) + s[k];
				}
				d2 = d2 + q * q;
				c = c + y[i] * q;
				g = g + (x[i] - x_average) * q * q;
			}
			
			c = c / d2;
			p = g / d2;
			q = d2 / d1;
			d1 = d2;
			coe[j] = c * s[j];
			t[j] = s[j];
			for (k = j - 1; k >= 0; k--) {
				coe[k] = c * s[k] + coe[k];
				b[k] = t[k];
				t[k] = s[k];
			}
		}
		
		dt[0] = 0.0;
		dt[1] = 0.0;
		dt[2] = 0.0;
		for (i = 0; i <datalength; i++) {
			q = coe[m - 1];
			for (k = m - 2; k >= 0; k--) {
				q = coe[k] + q * (x[i] - x_average);
			}
			p = q - y[i];
			if (Math.abs(p) > dt[2]) {
				dt[2] = Math.abs(p);
			}
			dt[0] = dt[0] + p * p;
			dt[1] = dt[1] + Math.abs(p);
		}
		
		return coe;
	}
	
	/**
	 * @param x 变量值
	 * @param y 原始数据
	 * @param m 拟合多项式的相数，即拟合多项式的最高阶数为 m-1. 要求 m<=x.length 且m<=21。若 m>x.length 或 m>21，按
	 *        m = min{x.length, 21}处理
	 * @return 拟合得到的各阶系数
	 */
	public static ArrayList<Double> PolyFit(ArrayList<Double> x, ArrayList<Double> y, int m) {
		if(x.size() != y.size()) return null;
		
		int n = x.size();
		double[] xx = new double[n];
		double[] yy = new double[n];
		
		for(int i=0; i<n; i++){
			xx[i] = x.get(i);
			yy[i] = y.get(i);
		}
		
		double[] coe = OrthogonalLeastSquareMethod.PolyFit(xx, yy, m);
		
		ArrayList<Double> coeArray = new ArrayList<Double>();
		for(int i=0; i<coe.length; i++) coeArray.add(coe[i]);
		
		return coeArray;
	}
	
	/**
	 * 求数组的平均值
	 *
	 * @author qiujing
	 * 
	 * @param x 
	 * @return 平均值
	 */
	public static double getAverage(double[] x) {
		double ave = 0;
		double sum = 0;
		if (x != null) {
			for (int i = 0; i < x.length; i++) sum += x[i];
			ave = sum / x.length;
		}
		return ave;
	}
	
	public static double getAverage(ArrayList<Double> x) {
		double ave = 0;
		double sum = 0;
		if (x != null) {
			for (int i = 0; i < x.size(); i++) sum += x.get(i);
			ave = sum / x.size();
		}
		return ave;
	}
	
	/**
	 * <p>
	 * 由X值获得Y值
	 * </p>
	 * 
	 * @param x 求x对应的函数值
	 * @param xarray 
	 *            
	 * @param coe 存储多项式系数的数组
	 *@author qiujing
	 *           
	 * @return 对应X值的Y值
	 */
	public static double getY(double x, double ave, double[] coe) {
		double y = 0;

		for (int i = 1; i < coe.length; i++) {
			y += coe[i] * Math.pow((x - ave), i);
		}
		
		return y + coe[0];
	}
	
	public static double Calerror(double[] x, double[] xfit){
	    if(x == null || xfit == null || x.length != xfit.length) return Double.NaN;
	    
		double error = 0.0;
		for(int i=0;i<x.length;i++){
			error+=Math.pow((x[i]-xfit[i]), 2);	
		}
		error= error/x.length;
		error = Math.sqrt(error);
		
		return error;	
	}
	
	public static double Calerror(ArrayList<Double> x,ArrayList<Double> xfit){
		double error = 0.0;
		for(int i=0; i<x.size(); i++){
			error+=Math.pow((x.get(i)-xfit.get(i)), 2);	
		}
		error= error/x.size();
		error = Math.sqrt(error);
		
		return error;
	}
	
	
	/**
	 * An example.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		double[] x = { 9.99, 19.97, 30.03, 40.01, 49.98, 59.96, 70.02, 80.0,
				89.97, 99.95, 109.93, 119.99, 129.97, 139.94, 149.92, 159.71,
				169.96, 179.93, 189.9, 199.98, 209.94, 219.83, 229.89, 239.88,
				249.95, 259.91, 267.87 };
		double[] y = { -65.6, -129.8, -194.8, -259.3, -323.8, -388.4, -453.6,
				-518.4, -583.1, -647.8, -712.6, -778.0, -842.8, -907.4, -972.2,
				-1035.8, -1102.2, -1166.9, -1231.3, -1296.8, -1361.3, -1425.2,
				-1490.2, -1554.4, -1619.6, -1683.7, -1734.9 };
		
//		int n = x.length;
//		ArrayList<Double> xx = new ArrayList<Double>();
//		ArrayList<Double> yy = new ArrayList<Double>();
//		
//		for(int i=0; i<n; i++){
//			xx.add(x[i]);
//			yy.add(y[i]);
//		}
//		
//		ArrayList<Double> coeArray = PolyFit(xx, yy, 15);
//		
//		double[] aa = new double[coeArray.size()];
//		for(int i=0; i<coeArray.size(); i++) aa[i] = coeArray.get(i);

		// 计算得到系数
		double[] coe = PolyFit(x, y, 15);
		
		double ave = getAverage(x);
		
		// 打印拟合得到的结果和与原始值的偏差
		for (int i = 0; i < x.length; i++) {
			System.out.println("拟合--> " + getY(x[i], ave, coe) + ", "
				+ (getY(x[i], ave, coe) - y[i]) / y[i]);
		}

	}

}

