package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;

import Exceptions.UnexpectedMessageException;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.ServerResp;
import Server.Constants;

public class Peer implements Constants{
	private PeerData peerData;

	public Peer(int port) {		
		try {
			this.peerData = new PeerData(port, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {			
			e.printStackTrace();
		}
	}
	
	public void connectToServer(String host, int port) throws UnknownHostException, IOException, ClassNotFoundException, UnexpectedMessageException{					
			Socket socket = new Socket(host, port);			
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());			
			
			RegisterPeerReq req = new RegisterPeerReq(peerData);
			out.writeObject(req);
			Object resp = in.readObject();
			System.out.println("Server's response: "+resp.toString());
			
			if (resp instanceof RegisterPeerResp){
				RegisterPeerResp rpresp = (RegisterPeerResp) resp;
				//System.out.println("Registered client!"+ ((ServerResp)rpresp.getServerRespMessages()).getMsg());			
				System.out.println("Registered client!");
			}
			else{throw new UnexpectedMessageException("RegisterPeerResp");}
			
	}
	
	public static void main(String args[]) throws UnexpectedMessageException{
		Peer peer = new Peer(TRACKER_PORT);
		try {
			System.out.println("BEFORE_CALL");
			peer.connectToServer(TRACKER_HOST, TRACKER_PORT);
			System.out.println("AFTER_CALL");
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Peer on port "+ peer.peerData.getPort()+" can not connect to server");
			e.printStackTrace();
		}
	}
}
