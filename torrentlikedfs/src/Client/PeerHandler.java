package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Common.FileDataListClient;
import Messages.PeerAliveReq;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.ServerListRespMessages;
import Messages.ServerResp;
import Messages.ServerRespMessageItems;
import Messages.TrackerAliveResp;

public class PeerHandler extends Thread{
	private Socket socket;
	private Peer peer;
	private NotifyTracker nt;
	private boolean isRunning = true;
	private Object obj = null;	
	private Object resp = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	
	public PeerHandler(Socket socket, Peer peer, NotifyTracker nt, ObjectInputStream in, ObjectOutputStream out) {
		super();
		this.socket = socket;
		this.peer = peer;
		this.nt = nt;	
		this.in = in;
		this.out = out;
		this.start();
	}
	
	public void sendMessage(Object obj){
		try {			
			out.writeObject(obj);
		} catch (IOException e) {
			System.out.println("PEERHANDLER: error in sending message!");
			e.printStackTrace();
		}
		this.obj= obj; 
	}
	
	public void run(){		
		Object resp = null;		
		
		try {			
			while(isRunning){
				resp = in.readObject();				
				processResponse(resp);					
			}
			
		} catch (IOException e) {			
			e.printStackTrace();			
			//dieThreads();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}
		finally
		{
			try {
			    if(out != null){out.close();}			    
			    if(in != null){in.close();}			    
			    if(socket != null){socket.close();}
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void processResponse(Object response){				
		
		if (response!=null){  
			if(response instanceof TrackerAliveResp){
				//nt.setServerResp(true);
				System.out.println("PEERHANDLER: TrackerAliveResp");
			}
			else if(response instanceof RegisterGroupResp){
				System.out.println("PEERHANDLER: RegisterGroupResp");
				RegisterGroupResp rgr = (RegisterGroupResp)response; 
				ServerListRespMessages msg = (ServerListRespMessages)rgr.getServerRespMessages();
				FileDataListClient fileList = (FileDataListClient)msg.getObj();
				for (int i=0; i<fileList.getSize(); i++){
					System.out.println("PEERHANDLER File list: "+fileList.getItem(i).getName());
				}
			}
			else
				System.out.println("PEERHANDLER: Other type of message");
		}
		else 
			System.out.println("PEERHANDLER: the response is empty");
	}
}
