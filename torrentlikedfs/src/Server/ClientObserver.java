package Server;
import java.net.Socket;
import java.util.concurrent.TimeUnit;


import Client.PeerData;
import Common.Constants;
import Logger.Logging;

public class ClientObserver extends Thread implements Constants{
	private TrackerServerCore serverCore=null;
	private long time = 0;
	private PeerData peerData = null;
	private int socketPort = 0;
	private boolean isRunning = true;
	private int nr;
	
	public ClientObserver(TrackerServerCore serverCore,	long time, int sckPort, int nr) {
		super();
		this.serverCore = serverCore;		
		this.time = time;
		this.socketPort = sckPort;
		this.nr = nr;
	}
	
	public void setTime(long time){
		this.time = time;		
	}
	
	public void setIsRunning(boolean isRunning){
		this.isRunning = isRunning;
	}
	
	public void run(){
		while (isRunning){
			try {				
				this.sleep(5000);
			} catch (InterruptedException e) {
				Logging.write(this.getClass().getName(), "run", e.getMessage());				
			}			
			if (this.time + TRACKER_ALIVE_TIME <= System.currentTimeMillis()){
				Logging.write(this.getClass().getName(), "run", "The client "+ peerData.getInetAddress() + " is dead!");				
				this.interrupt();
			}			
			else System.out.println("ClientObserver: OBS"+nr+": Client on port: "+socketPort+" is alive! Time is:" +time);//System.currentTimeMillis());
			//TODO
		}
	}	
}
