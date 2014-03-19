package Client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.concurrent.TimeUnit;

import Common.FileData;
import Common.FileDataListClient;
import Common.Group;
import Exceptions.UnableToReadFileException;
import Exceptions.UnexpectedMessageException;
import Messages.ChunkReq;
import Messages.PeerAliveReq;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.UnRegisterPeerReq;
import Server.Constants;

public class Peer implements Constants{
	private PeerData peerData;
	//private PeerItem peerItem;
	private Socket socket = null;
	private ObjectOutputStream out= null;
	private ObjectInputStream in = null; 
	private NotifyTracker notify = null;
	private PeerHandler handler = null;
	private PeerServer peerServer = null;
	private Socket seederSocket = null;

	public Peer(int port) {		
		try {
			this.peerData = new PeerData(port, InetAddress.getLocalHost());	
			peerServer = new PeerServer(port, this);
		} catch (UnknownHostException e) {			
			e.printStackTrace();
		}
	}
	
	public void connectToServer(String host, int port) {					
		try {
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
				System.out.println("Registered client!");				
				notify = new NotifyTracker(in, out, socket);
				notify.start();
				//peerServer = new PeerServer(((RegisterPeerResp) resp).getPort(), this);
				handler = new PeerHandler(socket, this, notify, in, out);				
			}
			else{throw new UnexpectedMessageException("RegisterPeerResp");}
			
		} catch (IOException e) {				
			System.out.println("Input EXCEPTION!!! - Check the ip address!");
			notify.interrupt();
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		} catch (UnexpectedMessageException e) {			
			e.printStackTrace();
		}							
	}		
	
	public void connectoToSeeder(String host, int port){
		ObjectOutputStream outs = null;
		ObjectInputStream ins = null;
		try {
			seederSocket = new Socket(host, port);
			outs = new ObjectOutputStream(seederSocket.getOutputStream());
			outs.flush();
			ins = new ObjectInputStream(seederSocket.getInputStream());
			
			ChunkReq req = new ChunkReq();
			outs.writeObject(req);		
			Object resp = ins.readObject();
			System.out.println(resp);			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void disconnectFromServer() throws IOException{
		//UnRegisterPeerReq unregreq = new UnRegisterPeerReq(peerItem);
		//out.writeObject(unregreq);
	}
	
	public void registerFile(String fileName){		
		FileData fd = new FileData(new File(fileName));
		FileDataListClient fdl = new FileDataListClient();
		fdl.addItem(fd);
		Group group = new Group(peerData, fdl);
		
		RegisterGroupReq rgr = new RegisterGroupReq(group);
		handler.sendMessage(rgr);		
	}
	
	public static void main(String args[]) throws UnexpectedMessageException, InterruptedException, ClassNotFoundException, IOException{
		//Peer peer = new Peer(TRACKER_PORT);
		Peer peer = new Peer(8118);
		//System.out.println("BEFORE_CALL");
		peer.connectToServer(TRACKER_HOST, TRACKER_PORT);
		peer.registerFile("alma.jpg");
		//peer.connectoToSeeder(TRACKER_HOST, 8119);
		//System.out.println("AFTER_CALL");			
	}
}
