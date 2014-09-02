package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

import Common.ChunkManager;
import Common.FileDataListClient;
import Logger.Logging;
import Messages.ChunkListResp;
import Messages.ChunkReq;
import Messages.ChunkResp;
import Messages.GetFilesResp;
import Messages.PeerAliveReq;
import Messages.RegisterChunkResp;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.ServerFilesUpdate;
import Messages.ServerListRespMessages;
import Messages.ServerResp;
import Messages.ServerRespMessageItems;
import Messages.TrackerAliveResp;

public class PeerHandler extends Thread{
	private Socket socket;
	private Peer peer;
	private NotifyTracker nt;
	private ChunkManager chunkm;
	private boolean isRunning = true;	
	private Object resp = null;
	private ObjectOutputStream out = null;
	private ObjectInputStream in = null;
	
	public PeerHandler(Socket socket, Peer peer, NotifyTracker nt, 
			ObjectInputStream in, ObjectOutputStream out, ChunkManager chunkm) 
	{
		super();
		this.socket = socket;
		this.peer = peer;
		this.nt = nt;	
		this.in = in;
		this.out = out;
		this.chunkm = chunkm;
		this.start();
	}
	
	public synchronized void sendMessage(Object obj){
		try {			
			out.writeObject(obj);
			out.flush();
		} catch (IOException e) {
			Logging.write(this.getClass().getName(), "sendMessage", "Error in sending message! "+e.getMessage());			
		}
	}
	
	public void run(){		
		Object resp = null;		
		
		try {			
			while(isRunning){
				resp = in.readObject();				
				processResponse(resp);					
			}
			
		} catch (IOException e) {
			Logging.write(this.getClass().getName(),"run", e.getMessage());			
		} catch (ClassNotFoundException e) {			
			Logging.write(this.getClass().getName(),"run", e.getMessage());
		}
		finally
		{
			try {
			    if(out != null){out.close();}			    
			    if(in != null){in.close();}			    
			    if(socket != null){socket.close();}
			} catch (IOException e) {				
				Logging.write(this.getClass().getName(),"run", e.getMessage());
			}
		}
	}
	
	public synchronized void processResponse(Object response){				
		
		if (response!=null){  
			if(response instanceof TrackerAliveResp){
				//nt.setServerResp(true);
				//System.out.println("PEERHANDLER: TrackerAliveResp");
			}
			else if(response instanceof RegisterGroupResp){				
				RegisterGroupResp rgr = (RegisterGroupResp)response; 
				ServerListRespMessages msg = (ServerListRespMessages)rgr.getServerRespMessages();
				FileDataListClient fileList = (FileDataListClient)msg.getObj();
				for (int i=0; i<fileList.getSize(); i++){
					System.out.println("PEERHANDLER RegisterGroupResp: File list: "+fileList.getItem(i).getName());
				}				
				chunkm.processFileListChunkReq(fileList);
			}
			else if (response instanceof ChunkReq){
				ChunkReq req = (ChunkReq) response;
				chunkm.onChunkReq(req);
			}
			else if (response instanceof ChunkResp){				
				ChunkResp resp = (ChunkResp) response; 
				chunkm.onChunkRespPeer(resp);
			}
			else if(response instanceof RegisterChunkResp){  // REGISTER CHUNK RESPONSE				
				RegisterChunkResp rcr = (RegisterChunkResp) response;
				chunkm.processChunkListReq(rcr);
			}
			else if(response instanceof ChunkListResp){
				ChunkListResp chunkList = (ChunkListResp)response;
				peer.downloadAFile(chunkList);
			}
			else if(response instanceof GetFilesResp){				
				peer.buildServerTable((GetFilesResp)response);
			}
			else if (response instanceof ServerFilesUpdate){				
				peer.updateServerTable((ServerFilesUpdate)response);
			}
			else
				System.out.println("PEERHANDLER: Other type of message");
		}
		//else 
			//System.out.println("PEERHANDLER: the response is empty");
	}
	
	// update peer's file table
	public synchronized void updatePeerTable(Vector<Object> row){
		peer.updatePeerTable(row);
	}
	
	// update server's file table
	/*public synchronized void updateServerTable(Vector<Object> row){
		peer.updateServerTable(row);
	}*/
}
