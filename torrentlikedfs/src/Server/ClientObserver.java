package Server;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import Client.PeerData;

public class ClientObserver extends Thread implements Constants{
	private TrackerServerCore serverCore=null;
	//private TrackerItem ti = null;
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
				e.printStackTrace();
				//this.interrupt();
			}
			//System.out.println("GET TIME:"+ time);
			if (this.time + TRACKER_ALIVE_TIME <= System.currentTimeMillis()){			
				System.out.println("Client is dead!");
				this.interrupt();
			}			
			else System.out.println("OBS"+nr+": Client on port: "+socketPort+" is alive! Time is:" +System.currentTimeMillis());
		}
	}

}
