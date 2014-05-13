package Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import Client.PeerData;
import Client.PeerItem;
import Common.ChunkManager;
import Messages.ChunkListReq;
import Messages.ChunkListResp;
import Messages.ChunkMessage;
import Messages.ChunkReq;
import Messages.ChunkResp;
import Messages.PeerAliveReq;
import Messages.RegisterChunkReq;
import Messages.RegisterChunkResp;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.ServerResp;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.TrackerAliveResp;
import Messages.UnRegisterPeerReq;

public class TrackerItem extends Thread{
	private Socket socket;
	private TrackerServerCore serverCore;
	private ClientObserver observer;
	private ChunkManager chunkm;
	private PeerData peerData;
	private boolean isRunning = true;
	private int nr; 

	public TrackerItem(Socket socket, TrackerServerCore serverCore, ClientObserver observer, ChunkManager chunkm, int nr) {
		super();
		this.socket = socket;
		this.serverCore = serverCore;
		this.observer = observer;
		this.chunkm = chunkm;
		this.nr = nr;
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
			
			while(isRunning){
				req = in.readObject();				
				resp = getResponse(req);				
				out.writeObject(resp);				
			}
			
		} catch (IOException e) {			
			e.printStackTrace();			
			dieThreads();
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
	
	public void processAlive(){
		System.out.println("Peer is Alive: "+ System.currentTimeMillis());
	}
	
	public synchronized Object getResponse(Object request){
		ChunkMessage chresponse = null;
		//ServerResp response = (ServerResp) resp;
		ServerResp response = null;
		
		if (request instanceof PeerAliveReq){  // PEER ALIVE
			observer.setTime(System.currentTimeMillis());
			response = new TrackerAliveResp(ServerRespMessageItems.ACK);
		}
		else if((request instanceof RegisterPeerReq) && (request!=null)){  // REGISTER PEER
			RegisterPeerReq rpr = (RegisterPeerReq)request;
			peerData = rpr.getPeerData();
			//PeerItem peerItem = new PeerItem(peerData, socket.getPort());
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
			System.out.println("TRACKERITEM: CHUNK REQUEST MESSAGE ");
			ChunkReq req = (ChunkReq) request;
			//chresponse = new ChunkResp(req.getPeerInfo());
			chresponse =  chunkm.onChunkReq(req);
			if (chresponse instanceof ChunkResp){
				//ChunkResp ch = (ChunkResp) chresponse;
				System.out.println("TRACKERITEM: RESP:");			
			}
		}
		else if(request instanceof ChunkListReq){ // CHUNK LIST REQUEST - get a list from the server
			ChunkListReq chlreq = (ChunkListReq) request;
			ChunkListResp resp = chunkm.onChunkListRequest(chlreq);
			chresponse = resp;
		}
		else if(request instanceof ChunkResp){	// CHUNK RESP				
			ChunkResp chresp = (ChunkResp) request;
			chunkm.onChunkRespTracker(chresp);
			//chunkm.writeChunk(resp.getFd(), resp.getChunkNr(), resp.getData());
			//chunkm.mergeChunks(resp.getFd());
		}
		else if (request instanceof RegisterChunkReq){ // REGISTER CHUNK
			RegisterChunkReq req = (RegisterChunkReq) request;
			RegisterChunkResp resp = chunkm.processRegisterChunkRequest(req);
			chresponse = resp;
		}
		else{
			System.out.println("Unknown message type!");
		}
		
		if (response==null) return chresponse; 
		else return response;
	}	
	
	public void dieThreads(){		
		//System.out.println("DIE Nr of Peers: "+serverCore.getNrRegisteredPeer());
		PeerItem peerItem = new PeerItem(this.peerData, this.socket.getPort());
		serverCore.unregisterPeer(peerItem);
		System.out.println("TRCK "+nr+": The client has signed out");
		observer.setIsRunning(false);
		isRunning = false;
	}
}
