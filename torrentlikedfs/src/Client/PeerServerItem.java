package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Common.ChunkManager;
import Logger.Logging;
import Messages.ChunkReq;
import Messages.ChunkResp;
import Messages.ServerRespMessages;

public class PeerServerItem extends Thread{
	private Socket socket = null;
	private boolean isRunning = true;
	private ChunkManager chunkm = null;

	public PeerServerItem(Socket socket, ChunkManager chunkm) {
		super();
		this.socket = socket;
		this.chunkm = chunkm;
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
			
			while(true){
				req = in.readObject();				
				resp = getResponse(req);				
				out.writeObject(resp);				
			}
			
		} catch (IOException e) {			
			Logging.write(this.getClass().getName(), "run", "The peer logged out. "+e.getMessage());			
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
	
	public synchronized Object getResponse(Object req){
		Object resp = null;
		
		if (req instanceof ChunkReq){						
			ChunkReq chunkReq = (ChunkReq) req;
			resp = chunkm.onChunkReq(chunkReq);			
		}
		
		return resp;
	}
}
