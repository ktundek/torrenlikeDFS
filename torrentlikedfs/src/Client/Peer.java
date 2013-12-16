package Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.TimeUnit;

import Exceptions.UnexpectedMessageException;
import Messages.PeerAliveReq;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.UnRegisterPeerReq;
import Server.Constants;

public class Peer implements Constants{
	private PeerData peerData;
	private Socket socket = null;
	private ObjectOutputStream out= null;
	private ObjectInputStream in = null; 
	private NotifyTracker notify = null;

	public Peer(int port) {		
		try {
			this.peerData = new PeerData(port, InetAddress.getLocalHost());
		} catch (UnknownHostException e) {			
			e.printStackTrace();
		}
	}
	
	public void connectToServer(String host, int port) throws UnknownHostException, IOException, ClassNotFoundException, UnexpectedMessageException{					
		socket = new Socket(host, port);			
		out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		in = new ObjectInputStream(socket.getInputStream());			

		RegisterPeerReq req = new RegisterPeerReq(peerData);
		out.writeObject(req);
		Object resp = in.readObject();
		System.out.println("Server's response: "+resp.toString());

		if (resp instanceof RegisterPeerResp){
			RegisterPeerResp rpresp = (RegisterPeerResp) resp;
			//System.out.println("Registered client!"+ ((ServerResp)rpresp.getServerRespMessages()).getMsg());			
			System.out.println("Registered client!");
			notify = new NotifyTracker(in, out, socket);
			notify.start();
		}
		else{throw new UnexpectedMessageException("RegisterPeerResp");}

	}
		
	
	public void disconnectFromServer() throws IOException{
		UnRegisterPeerReq unregreq = new UnRegisterPeerReq(peerData);
		out.writeObject(unregreq);
	}
	
	public static void main(String args[]) throws UnexpectedMessageException, InterruptedException{
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
