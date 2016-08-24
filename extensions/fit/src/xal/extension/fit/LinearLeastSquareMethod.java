package xal.extension.fit;

import Jama.Matrix;

public class LinearLeastSquareMethod {
    
    /**
     * 函数功能：最小二乘法曲线拟合（least-squares fit method),
     * 
     * 函数形式：y = Sum( coe_i * x^i, {i, 0, n} )， x和y为原始数据，coe_i为各阶系数。
     * 如果hasconstant为false，则拟合无常数项。
     * 
     * @param x
     * @param y
     * @param order
     * @param hasconstant 拟合是否包含常数项
     * @return
     */
    public static double[] Fit(double x[], double y[], int order, boolean hasconstant){
        if(hasconstant) return Fit(x, y, order);
        return FitWithoutConstant(x, y, order);
    }
    
    /**
     * 
     * 函数功能：最小二乘法曲线拟合（least-squares fit method),
     * 
     * 函数形式：y = Sum( coe_i * x^i, {i, 0, n} )， x和y为原始数据，coe_i为各阶系数。
     * 
     * @author qiujing
     * @version 1.0
     * 
     */
    public static double[] Fit(double x[], double y[], int order){
        if(x == null || y == null) return null;
        if(x.length != y.length) return null;
        
        int dim = x.length;
        if(order >= dim) return null;
        
        double[] coe = new double[order + 1];
        
        Matrix X = new Matrix(dim, order+1);
        Matrix Y = new Matrix(dim,1);
        for(int i=0; i<dim; i++){
            for(int j=0; j<=order; j++) X.set(i, j, Math.pow(x[i], j));
            Y.set(i, 0, y[i]);
        }

        Matrix C = new Matrix(order+1, 1);
        
        C = (X.transpose().times(X)).inverse().times(X.transpose()).times(Y);
        
        for(int i=0; i<order+1; i++) coe[i] = C.get(i, 0);
        
        return coe;
    }
    
    /**
     * 
     * 函数功能：不包含常数项的最小二乘法曲线拟合（least-squares fit method),
     * 
     * 函数形式：y = Sum( coe_i * x^i, {i, 1, n} )， x和y为原始数据，coe_i为各阶系数。
     * 
     * @author qiujing
     * @version 1.0
     * 
     */
    public static double[] FitWithoutConstant(double x[], double y[], int order){
        if(x == null || y == null) return null;
        if(x.length != y.length) return null;
        
        int dim = x.length;
        if(order > dim) return null;
        
        double[] coe = new double[order];
        
        Matrix X = new Matrix(dim, order);
        Matrix Y = new Matrix(dim,1);
        for(int i=0; i<dim; i++){
            for(int j=1; j<=order; j++) X.set(i, j-1, Math.pow(x[i], j));
            Y.set(i, 0, y[i]);
        }

        Matrix C = new Matrix(order, 1);
        
        C = (X.transpose().times(X)).inverse().times(X.transpose()).times(Y);
        
        for(int i=0; i<order; i++) coe[i] = C.get(i, 0);
        
        return coe;
    }
    
    /**
     * 由X值获得Y值
     * 
     * @param x
     * @param coe
     * @param hasconstant
     * @return
     */
    public static double getY(double x, double[] coe, boolean hasconstant){
        if(coe == null) return Double.NaN;
        
        double y = 0;

        int pow = 0;
        if(!hasconstant) pow = 1;
        
        for(int i=0; i<coe.length; i++){
            y = y + coe[i]*Math.pow(x, i+pow);
        }
        
        return y;
    }
    
    /**
     * 计算残差
     * 
     * @param x
     * @param xfit
     * @return
     */
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

    
    public static void main(String[] args){
        double[] x=new double[2];
        double[] y=new double[2];
        
        x[0]=1;
        y[0]=1;
        x[1]=2;
        y[1]=2;
        
        double[] coe = LinearLeastSquareMethod.Fit(x, y, 1, false);
        
        //System.out.println(coe[0] + "," + coe[1]);
        System.out.println(LinearLeastSquareMethod.getY(1115, coe, false));
    }
}
