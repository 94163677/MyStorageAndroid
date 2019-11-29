package air.kanna.mystorage.sync.process;

public interface ProcessListener {

	public void setMax(int max);
	public void next(String message);
	public void setPosition(int current, String message);
	public void finish(String message);
	
}
