package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Common.ChunkManager;

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
			//System.out.println("PEERSERVER: Listening on port: "+port);
		} catch (IOException e) {
			System.out.println("PEERSERVER: Can not listen on port: "+port);
			e.printStackTrace();
		}
		
		Socket socket = null;
		System.out.println("Waiting for other Peers...");
		while (true){
			try {
				socket = pssocket.accept();
				nrOfPeers++;
				System.out.println("Peer on port "+port+": My "+nrOfPeers+". client! ");
				PeerServerItem psi = new PeerServerItem(socket, chunkm);
			} catch (IOException e) {
				System.out.println("PEERSERVER: Connection is not accepted.");
				e.printStackTrace();
			}			
		}
	}
	
}
