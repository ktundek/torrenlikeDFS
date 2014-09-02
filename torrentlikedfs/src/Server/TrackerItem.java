package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.table.DefaultTableModel;


import Client.PeerData;
import Client.PeerItem;
import Common.ChunkManager;
import Common.FileData;
import Logger.Logging;
import Messages.ChunkListReq;
import Messages.ChunkListResp;
import Messages.ChunkMessage;
import Messages.ChunkReq;
import Messages.ChunkResp;
import Messages.GetFilesReq;
import Messages.GetFilesResp;
import Messages.PeerAliveReq;
import Messages.RegisterChunkReq;
import Messages.RegisterChunkResp;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerChunk;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.ServerFilesUpdate;
import Messages.ServerResp;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.TrackerAliveResp;
import Messages.UnRegisterPeerReq;

public class TrackerItem extends Thread{
	private Socket socket;
	private TrackerServerCore serverCore;
	private TrackerServer trackerserver;
	private ClientObserver observer;
	private ChunkManager chunkm;
	private PeerData peerData;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	private boolean isRunning = true;
	private int nr; 

	public TrackerItem(Socket socket, TrackerServerCore serverCore, TrackerServer trackerserver,
			ClientObserver observer, ChunkManager chunkm, int nr) {
		super();
		this.socket = socket;
		try {
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			Logging.write(this.getClass().getName(), "TrackerItem", "Error creating ObjectInputStream/ObjectOutputStream! "+e.getMessage());			
		}		
		this.serverCore = serverCore;
		this.trackerserver = trackerserver;
		this.observer = observer;
		this.chunkm = chunkm;
		this.nr = nr;
		this.start();		
	}
	
	public synchronized void sendMessage(Object obj){
		try {			
			out.writeObject(obj);
			out.flush();
		} catch (IOException e) {
			Logging.write(this.getClass().getName(),"senMessage" , "Error in sending message! "+e.getMessage());			
		}		
	}
	
	public void run(){
		Object req = null;
		Object resp = null;		
		
		try {			
			out.flush();						
			
			while(isRunning){
				req = in.readObject();				
				resp = getResponse(req);								
				sendMessage(resp);
			}
			
		} catch (IOException e) {						
			//Logging.write(this.getClass().getName(), "run", e.getMessage());
			dieAllThreads();
		} catch (ClassNotFoundException e) {			
			Logging.write(this.getClass().getName(), "run", e.getMessage());
		}
		finally
		{
			try {
			    if(out != null){out.close();}			    
			    if(in != null){in.close();}			    
			    if(socket != null){socket.close();}
			} catch (IOException e) {				
				Logging.write(this.getClass().getName(), "run", e.getMessage());
			}
		}
	}	
	
	public synchronized Object getResponse(Object request){
		ChunkMessage chresponse = null;		
		ServerResp response = null;
		
		if (request instanceof PeerAliveReq){  // PEER ALIVE
			observer.setTime(System.currentTimeMillis());
			response = new TrackerAliveResp(ServerRespMessageItems.ACK);
		}
		else if((request instanceof RegisterPeerReq) && (request!=null)){  // REGISTER PEER
			RegisterPeerReq rpr = (RegisterPeerReq)request;
			peerData = rpr.getPeerData();			
			response = new RegisterPeerResp(serverCore.registerPeer(rpr, socket.getPort()));			
			if ((response instanceof RegisterPeerResp) &&
					(response.getMsg().getMsg()=="OK")){				
				observer.start();
			}
		}		
		else if(request instanceof RegisterGroupReq){	// REGISTER GROUP
			RegisterGroupReq rgr = (RegisterGroupReq) request;
			chunkm.registerPeerFiles(rgr);
			response = new RegisterGroupResp(serverCore.registerGroup(rgr));
		}
		else if(request instanceof ChunkReq){  // CHUNK REQUEST			
			ChunkReq req = (ChunkReq) request;			
			chresponse =  chunkm.onChunkReq(req);			
		}
		else if(request instanceof ChunkListReq){ // CHUNK LIST REQUEST - get a list from the server
			ChunkListReq chlreq = (ChunkListReq) request;
			ChunkListResp resp = chunkm.onChunkListRequest(chlreq);
			chresponse = resp;
		}
		else if(request instanceof ChunkResp){	// CHUNK RESP				
			ChunkResp chresp = (ChunkResp) request;
			chunkm.onChunkRespTracker(chresp);			
		}
		else if (request instanceof RegisterChunkReq){ // REGISTER CHUNK
			RegisterChunkReq req = (RegisterChunkReq) request;
			RegisterChunkResp resp = chunkm.processRegisterChunkRequest(req);
			chresponse = resp;
		}
		else if(request instanceof RegisterPeerChunk){ // REGISTER CHUNKS OBTAINED BY PEER
			RegisterPeerChunk req = (RegisterPeerChunk)request;
			chunkm.registerObtainedChunk(req);
		}
		else if(request instanceof GetFilesReq){
			DefaultTableModel dtm = chunkm.getFilesServer();
			GetFilesResp resp = new GetFilesResp(((GetFilesReq) request).getPeerInfo());
			resp.setDtm(dtm);
			chresponse = resp;
		}
		else{
			//System.out.println("Unknown message type!");
		}
		
		if (response==null) return chresponse; 
		else return response;
	}
	
	public void notifyPeer(ServerFilesUpdate sfu){
		sendMessage(sfu);
	}
	
	public void dieThread(){
		this.dieThread();
	}
	
	public void dieAllThreads(){				
		PeerItem peerItem = new PeerItem(this.peerData, this.socket.getPort());
		serverCore.unregisterPeer(peerItem);
		chunkm.deletePeer(this.peerData);			
		Logging.write(this.getClass().getName(), "dieAllThreads", "TRCK "+nr+": The client "+ this.peerData.getInetAddress().toString() +" has signed out");
		observer.setIsRunning(false);		
		trackerserver.deleteTrackerItem(this);
		isRunning = false;	
	}
}
