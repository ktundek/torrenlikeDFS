package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.ServerResp;
import Messages.ServerRespMessages;

public class TrackerItem extends Thread{
	private Socket socket;
	private TrackerServerCore serverCore;

	public TrackerItem(Socket socket, TrackerServerCore serverCore) {
		super();
		this.socket = socket;
		this.serverCore = serverCore;
		this.start();
	}
	
	public void run(){
		Object req = null;
		Object resp = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());
			System.out.println("TRACKER ITEM");
			
			while(true){
				req = in.readObject();					
				resp = getRequest(req);					
				out.writeObject(resp);
			}
			
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		finally
		{
			try {
			    if(out != null)
			    {
					out.close();
			    }
			    
			    if(in != null)
			    {
			    	in.close();
			    }
			    
			    if(socket != null)
			    {
			    	socket.close();
			    }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized Object getRequest(Object request){		
		Object respObj=null;
		ServerResp response = null;
		
		if((request instanceof RegisterPeerReq) && (request!=null)){
			RegisterPeerReq rpr = (RegisterPeerReq)request;
			response = new RegisterPeerResp(serverCore.registerPeer(rpr));
		}
		else{
			System.out.println("Unknown message type!");
		}
		
		return response;
	}	
}
