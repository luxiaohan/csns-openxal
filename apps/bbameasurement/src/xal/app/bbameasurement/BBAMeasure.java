package xal.app.bbameasurement;


public class BBAMeasure implements Runnable {
	FindBPMCenter _fb;
	
	public BBAMeasure(FindBPMCenter fb){
		_fb = fb;
	}

	@Override
	public void run(){
        synchronized(this){
        	try {
				_fb.findbpmcenter();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

}