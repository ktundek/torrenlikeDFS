package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Common.ChunkManager;
import Logger.Logging;

public class PeerServer extends Thread {
	private int port;
	private ServerSocket pssocket; // peer server socket
	private Peer peer;	
	private int nrOfPeers = 0;
	private ChunkManager chunkm = null;
		
	public PeerServer(int port, Peer peer, ChunkManager chunkm) {
		super();
		this.port = port;
		this.peer = peer;
		this.chunkm = chunkm;
		this.start();	
	}
	
	public void run(){
		try {
			pssocket = new ServerSocket(port);					
		} catch (IOException e) {			
			Logging.write(this.getClass().getName(), "run", "Can not listen on port "+port +": "+e.getMessage());			
		}
		
		Socket socket = null;
		//System.out.println("Waiting for other Peers...");
		while (true){
			try {
				socket = pssocket.accept();
				nrOfPeers++;				
				Logging.write(this.getClass().getName(), "run", "Peer on port: "+port+ "It is the "+nrOfPeers+".th peer." );
				PeerServerItem psi = new PeerServerItem(socket, chunkm);
			} catch (IOException e) {				
				Logging.write(this.getClass().getName(), "run", "Connection is not accepted. "+e.getMessage());				
			}			
		}
	}
	
}
