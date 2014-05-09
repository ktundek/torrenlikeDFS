package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import Common.ChunkInfo;
import Common.ChunkManager;
import Common.ChunkState;
import Common.FileData;
import Common.FileDataListClient;
import Common.Group;
import Exceptions.UnableToReadFileException;
import Exceptions.UnexpectedMessageException;
import Messages.ChunkReq;
import Messages.PeerAliveReq;
import Messages.RegisterChunkReq;
import Messages.RegisterChunkResp;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.UnRegisterPeerReq;
import Common.Constants;

public class Peer implements Constants{
	private PeerData peerData;
	//private PeerItem peerItem;
	private Socket socket = null;
	private ObjectOutputStream out= null;
	private ObjectInputStream in = null; 
	private NotifyTracker notify = null;
	private PeerHandler handler = null;
	private PeerServer peerServer = null;
	private ChunkManager chunkm = null;
	private Socket seederSocket = null;
	private static String peerDir = "C:/PeerClient/";
	private static String peerChunkDir = "C:/PeerClientChunk/";

	public Peer(int port) {		
		try {
			this.peerData = new PeerData(port, InetAddress.getLocalHost());
			chunkm = new ChunkManager(peerDir, peerChunkDir, peerData, null);
			peerServer = new PeerServer(port, this, chunkm);			
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
				handler = new PeerHandler(socket, this, notify, in, out, chunkm);
				chunkm.setPeerHandler(handler);
				//chunkm = new ChunkManager(peerDir, peerData, handler);
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
		/*ObjectOutputStream outs = null;
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
		}*/
	}
	
	public void disconnectFromServer() throws IOException{
		//UnRegisterPeerReq unregreq = new UnRegisterPeerReq(peerItem);
		//out.writeObject(unregreq);
	}
	
	public void registerFile(String folderName){				
		FileDataListClient fdl = new FileDataListClient();
		fdl = getFileList(folderName);
		Group group = new Group(peerData, fdl);
		
		RegisterGroupReq rgr = new RegisterGroupReq(group);
		handler.sendMessage(rgr);		
	}
	
	public void registerChunks(String folderName){
		System.out.println("PEER: Register Chunks");
		//Map<String, ChunkInfo> chunks = null;
		Hashtable<String, ChunkInfo> chunks = null;
		File folder = new File(folderName);
		File[] chunkList = folder.listFiles();
		String [] desc = null;
		
		
		if (chunkList.length!=0){
			//chunks = new HashMap<String, ChunkInfo>();
			chunks = new Hashtable<String, ChunkInfo>();
			for (int i=0; i<chunkList.length; i++){
				if (getFileExtention(chunkList[i].getName()).equals("dsc")){
					
					desc = getDescData(chunkList[i]); // filename, size, crc, nrofchunks
					String fName = desc[0];
					String fSize = desc[1];
					String fCrc = desc[2];
					int fChunkNr = Integer.parseInt(desc[3]);
					//System.out.println("DESC PARAMETERS:"+fName+", "+fSize+", "+fCrc+", "+fChunkNr);
					
					if (!chunks.containsKey(fCrc)){						
						ChunkInfo ci = new ChunkInfo(fChunkNr);
						for (int j=0; j<chunkList.length; j++)
							for (int k=0; k<fChunkNr; k++)
								if (chunkList[j].getName().equals(fName+fSize+"_"+k+".chnk")){
									// we mark DOWNLOADING those chunks witch the client has and can be uploaded to server
									ci.setState(k, ChunkState.DOWNLOADING); 
									chunks.put(fCrc, ci);
								}
					}
					else{
						ChunkInfo ci = chunks.get(fCrc);
						for (int j=0; j<chunkList.length; j++)
							for (int k=0; k<fChunkNr; k++)
								if (chunkList[j].getName().equals(fName+fSize+"_"+k+".chnk"))
									ci.setState(k, ChunkState.DOWNLOADING);
						
					}						
				}
			}
			
		}
		RegisterChunkReq rcr = new RegisterChunkReq(chunks, peerData);
		//RegisterChunkResp resp = chunkm.processRegisterChunkRequest(rcr);
		handler.sendMessage(rcr);		
	}
	
	public String getFileExtention(String fileName){
		String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		return ext;
	}
	
	public String[] getDescData(File file){		
		String[] res = new String[4]; 
		
		try {
			    BufferedReader reader = new BufferedReader(new FileReader(file)); 
			    String line = null;
			    int ind = 0;
			    while ((line = reader.readLine()) != null) {
			        res[ind] = line;
			        //System.out.println("DESC data: "+ind+" "+res[ind]);
			        ind++;
			    }			    
			} catch (IOException x) {
			    System.err.println(x);
			}
		
		return res;
	}
	
	public FileDataListClient getFileList(String folderName){
		FileDataListClient fdl = new FileDataListClient();
		File folder = new File(folderName);
		File[] fileList = folder.listFiles();
		FileData fd = null;
		
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isFile()){
				fd =new FileData(fileList[i]); 
				fdl.addItem(fd);
			}
		}
		//fdl.toStringFileDataList();
		return fdl;
	}
	
	public void sendFileRequest(){
		File file = new File("C:/TreckerServer/life.pdf");
		FileData fd = new FileData(file);
		ChunkReq req = new ChunkReq(peerData);
		req.setFd(fd);
		req.setChunkNr(1);
		handler.sendMessage(req);	
	}
	
	public static void main(String args[]) throws UnexpectedMessageException, InterruptedException, ClassNotFoundException, IOException{
		//Peer peer = new Peer(TRACKER_PORT);
		Peer peer = new Peer(8118);
		//System.out.println("BEFORE_CALL");
		peer.connectToServer(TRACKER_HOST, TRACKER_PORT);
		peer.registerFile(peerDir);
		peer.registerChunks(peerChunkDir);
		//peer.sendFileRequest();
		//peer.connectoToSeeder(TRACKER_HOST, 8119);
		//System.out.println("AFTER_CALL");			
	}
}
