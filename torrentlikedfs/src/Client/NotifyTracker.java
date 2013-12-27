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
	
	public NotifyTracker(ObjectInputStream in, ObjectOutputStream out, Socket socket) {
		super();
		this.in = in;
		this.out = out;
		this.socket = socket;
	}
	
	public void run(){
		PeerAliveReq alive = new PeerAliveReq();		
		while (isRunning){
			try {
				
				out.writeObject(alive);
				out.flush();
				Object resp = in.readObject();
				processServerResp((ServerResp)resp);
				//System.out.println("Server's response to ALIVE: "+resp.toString());
				TimeUnit.SECONDS.sleep(10);
				
			} catch (IOException e) {
				System.out.println("SOCKET EXCEPTION!");
				isRunning = false;
				e.printStackTrace();			
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {				
				e.printStackTrace();
			} catch (UnexpectedMessageException e) {
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
