package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import Exceptions.UnexpectedMessageException;
import Messages.PeerAliveReq;
import Messages.ServerResp;
import Messages.TrackerAliveResp;

public class NotifyTracker extends Thread {
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private Socket socket = null;
	private boolean isRunning = true;
	private boolean serverResp = true;
	
	public NotifyTracker(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
		super();
		this.in = in;
		this.out = out;
		this.socket = socket;
	}
	
	public void setServerResp(boolean resp){
		this.serverResp = resp;
	}
	
	public void run(){
		PeerAliveReq alive = new PeerAliveReq();		
		while (isRunning){
			try {
				
				//if (serverResp){
				out.writeObject(alive);
				out.flush();				
				TimeUnit.SECONDS.sleep(10);
				//serverResp=false;
				//}
				//else
				//	System.out.println("Server is down. Try to reconnect!");
				
			} catch (IOException e) {
				System.out.println("SOCKET EXCEPTION in NOTIFY TRACKER!");
				isRunning = false;
				e.printStackTrace();			
			} catch (InterruptedException e) {				
				e.printStackTrace();			
			}							
		}
	}
	
	public void processServerResp(ServerResp response) throws UnexpectedMessageException{
		if (!(response instanceof TrackerAliveResp)){
			throw new UnexpectedMessageException("TrackerAliveResp");
		}		
	}
}
