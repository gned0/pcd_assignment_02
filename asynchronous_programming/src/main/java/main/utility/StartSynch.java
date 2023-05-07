package main.utility;

public class StartSynch {

	private boolean started;
//	private Task fullJob;
	public StartSynch(){
		started = false;
	}
	
//	public synchronized Task waitStart() {
//		while (!started) {
//			try {
//				wait();
//			} catch (InterruptedException ex) {
//				ex.printStackTrace();
//			}
//		}
//		started = false;
//		return fullJob;
//	}

	public synchronized void notifyStarted(final String d, final int i, final int ml, final int nTopFiles) {
		started = true;
//		fullJob = new MasterWorkerBeginTask(d, i, ml, nTopFiles);
		notifyAll();
	}
}
