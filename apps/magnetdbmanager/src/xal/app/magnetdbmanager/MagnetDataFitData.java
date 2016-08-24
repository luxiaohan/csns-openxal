package xal.app.magnetdbmanager;

/**
 * 
 * @author annqiujing
 * 包含励磁曲线拟合中所有需要的数据
 * order:拟合阶数
 * I: 测量电流
 * B：测量磁场
 * FitB:拟合磁场
 * FitI:拟合电流
 * Bpercent：磁场拟合误差
 * Ipercent：电流拟合误差
 *
 */
public class MagnetDataFitData {
	private final int order;
	private final double I;
	private final double BL;
	private final String FitBL;
	private final String FitI;
	private final String BLpercent;
	private final String Ipercent;
	
	
	
	public MagnetDataFitData(int order,double I, double BL, String fitbl,  String blpercent, String fiti, String ipercent){
		this.order = order;
		this.I = I;
		this.BL = BL;
		this.FitBL = fitbl;
		this.FitI = fiti;
		this.BLpercent = blpercent;
		this.Ipercent = ipercent;
	}
	
	public int getOrder(){
		return order;
	}
	
	public double getI(){
		return I;
	}
	
	public double getBL(){
		return BL;
	}
	
	public String getFitBL(){
		return FitBL;
	}
	
	public String getFitI(){
		return FitI;
	}
	
	public String getBLpercent(){
		return BLpercent;
	}
	
	public String getIpercent(){
		return Ipercent;
	}
}

