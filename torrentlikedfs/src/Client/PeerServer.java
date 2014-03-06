package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer extends Thread {
	private int port;
	private ServerSocket pssocket; // peer server socket
	private Peer peer;	
	
	public PeerServer(int port, Peer peer) {
		super();
		this.port = port;
		this.peer = peer;
		this.start();	
	}
	
	public void run(){
		try {
			pssocket = new ServerSocket(port);		
			System.out.println("PEERSERVER: Listening on port: "+port);
		} catch (IOException e) {
			System.out.println("PEERSERVER: Can not listen on port: "+port);
			e.printStackTrace();
		}
		
		Socket socket = null;
		System.out.println("Waiting for other Peers...");
		while (true){
			try {
				socket = pssocket.accept();
			} catch (IOException e) {
				System.out.println("PEERSERVER: Connection is not accepted.");
				e.printStackTrace();
			}
		}
	}
	
}
