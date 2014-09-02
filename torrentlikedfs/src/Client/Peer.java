package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import Common.ChunkInfo;
import Common.ChunkManager;
import Common.ChunkState;
import Common.FileData;
import Common.FileDataListClient;
import Common.Group;
import Common.Test;
import Exceptions.ExceptionMessage;
import Exceptions.UnexpectedMessageException;
import Logger.Logging;
import Messages.ChunkListReq;
import Messages.ChunkListResp;
import Messages.ChunkReq;
import Messages.GetFilesReq;
import Messages.GetFilesResp;
import Messages.RegisterChunkReq;
import Messages.RegisterGroupReq;
import Messages.RegisterPeerReq;
import Messages.RegisterPeerResp;
import Messages.ServerFilesUpdate;
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
	private PeerGUI peergui = null;
	private static String peerDir = "";
	private static String peerChunkDir = "";

	public Peer(PeerGUI peergui) {		
		try {
			int port = 8119;
			this.peergui = peergui;
			System.out.println(System.getProperty("os.name"));
			setDirectories();
						
			this.peerData = new PeerData(port, InetAddress.getLocalHost());
			chunkm = new ChunkManager(peerDir, peerChunkDir, peerData, null);
			peerServer = new PeerServer(port, this, chunkm);
			
			connectToServer(TRACKER_HOST, TRACKER_PORT);
			registerFile(peerDir);
			registerChunks(peerChunkDir);
			completeFileDownloading(peerChunkDir);
			
		} catch (UnknownHostException e) {			
			e.printStackTrace();
		}
	}
	
	public void setGUI(PeerGUI gui){
		this.peergui = gui;
	}
	
	public void setDirectories(){
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("windows")){
			peerDir = "C:/PeerClient/";
			peerChunkDir = "C:/PeerClientChunk/";
		}
		if (os.contains("linux")){			
			peerDir = System.getProperty("user.home")+File.separator+"PeerClient"+File.separator;
			peerChunkDir = System.getProperty("user.home")+File.separator+"PeerClientChunk"+File.separator;
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

			if (resp instanceof RegisterPeerResp){  // the client was succesfully registered
				RegisterPeerResp rpresp = (RegisterPeerResp) resp;						
				//System.out.println("Registered client!");
				Logging.write(this.getClass().getName(), "connecToServer", "Registered client!: "+peerData.getInetAddress());
				notify = new NotifyTracker(in, out, socket);
				notify.start();
				//peerServer = new PeerServer(((RegisterPeerResp) resp).getPort(), this);
				handler = new PeerHandler(socket, this, notify, in, out, chunkm);
				chunkm.setPeerHandler(handler);
				registerPeerFiles(peerDir);
				registerPeersChunks(peerChunkDir);		
				//chunkm.writeoutfileChunks();
				handler.sendMessage(new GetFilesReq(this.peerData));				
			}
			else{throw new UnexpectedMessageException("RegisterPeerResp");}
			
		} catch (IOException e) {							
			ExceptionMessage.messageBox("Cannot connect to the server! Check the IP!");
			notify.interrupt();			
		} catch (ClassNotFoundException e) {			
			Logging.write(this.getClass().getName(), "connectToServer", e.getMessage());
		} catch (UnexpectedMessageException e) {			
			Logging.write(this.getClass().getName(), "connectToServer", e.getMessage());
		}							
	}			
	
	// szamba veszi, h milyen fajlokkal rendelkezik
	public synchronized void registerPeerFiles(String folderName){
		FileDataListClient fdl = getFileList(folderName);
		chunkm.initializePeerFileList(fdl);
	}
	
	// szamba veszi, hogy milyen fajldarabokkal rendelkezik
	public void registerPeersChunks(String folderName){
		Hashtable<String, ChunkInfo> chunks = null;		
		File folder = new File(folderName);
		File[] chunkList = folder.listFiles();
		String [] desc = null;
		
		if (chunkList!=null)
		if (chunkList.length!=0){			
			chunks = new Hashtable<String, ChunkInfo>();			
			for (int i=0; i<chunkList.length; i++){
				if (getFileExtention(chunkList[i].getName()).equals("dsc")){
					
					desc = getDescData(chunkList[i]); // filename, size, crc, nrofchunks
					String fName = desc[0];
					String fSize = desc[1];
					String fCrc = desc[2];
					int fChunkNr = Integer.parseInt(desc[3]);
					
					FileData fd = new FileData();
					fd.setName(fName);
					fd.setSize(Long.valueOf(fSize));
					fd.setCrc(fCrc);				
					
					if (!chunks.containsKey(fCrc)){						
						ChunkInfo ci = new ChunkInfo(fChunkNr);
						for (int j=0; j<chunkList.length; j++)
							for (int k=0; k<fChunkNr; k++){
								String chname = fName+fSize+fCrc.substring(0, 5)+"_"+k+".chnk";
								if (chunkList[j].getName().equals(chname)){									
									ci.setState(k, ChunkState.COMPLETED); 
									chunks.put(fCrc, ci);
								}
							}
					}
					else{
						ChunkInfo ci = chunks.get(fCrc);
						for (int j=0; j<chunkList.length; j++)
							for (int k=0; k<fChunkNr; k++){
								String chname = fName+fSize+fCrc.substring(0, 5)+"_"+k+".chnk";
								if (chunkList[j].getName().equals(chname))
									ci.setState(k, ChunkState.COMPLETED);
							}
						
					}						
				}
			}
			
		}
		if (chunks!=null)
			chunkm.initializePeerChunkList(chunks);
	}
	
	// beregisztralja a szerverre a meglevo fajlokat
	public synchronized void registerFile(String folderName){				
		FileDataListClient fdl = new FileDataListClient();
		fdl = getFileList(folderName);
		Group group = new Group(peerData, fdl);
		
		RegisterGroupReq rgr = new RegisterGroupReq(group);
		handler.sendMessage(rgr);		
	}
	
	// beregisztralja a szerverre a meglevo chunkokat
	public synchronized void registerChunks(String folderName){
		System.out.println("PEER: Register Chunks");		
		Hashtable<String, ChunkInfo> chunks = null;
		Hashtable<String, FileData> files = null;
		File folder = new File(folderName);
		File[] chunkList = folder.listFiles();
		String [] desc = null;
		
		if (chunkList!=null)
		if (chunkList.length!=0){			
			chunks = new Hashtable<String, ChunkInfo>();
			files = new Hashtable<String, FileData>();
			for (int i=0; i<chunkList.length; i++){
				if (getFileExtention(chunkList[i].getName()).equals("dsc")){
					
					desc = getDescData(chunkList[i]); // filename, size, crc, nrofchunks
					String fName = desc[0];
					String fSize = desc[1];
					String fCrc = desc[2];
					int fChunkNr = Integer.parseInt(desc[3]);
					
					FileData fd = new FileData();
					fd.setName(fName);
					fd.setSize(Long.valueOf(fSize));
					fd.setCrc(fCrc);
					files.put(fCrc, fd);				
					
					if (!chunks.containsKey(fCrc)){						
						ChunkInfo ci = new ChunkInfo(fChunkNr);
						for (int j=0; j<chunkList.length; j++)
							for (int k=0; k<fChunkNr; k++){
								String chname = fName+fSize+fCrc.substring(0, 5)+"_"+k+".chnk";
								if (chunkList[j].getName().equals(chname)){
									// we mark DOWNLOADING those chunks witch the client has and can be uploaded to server
									ci.setState(k, ChunkState.DOWNLOADING); 
									chunks.put(fCrc, ci);
								}
							}
					}
					else{
						ChunkInfo ci = chunks.get(fCrc);
						for (int j=0; j<chunkList.length; j++)
							for (int k=0; k<fChunkNr; k++){
								String chname = fName+fSize+fCrc.substring(0, 5)+"_"+k+".chnk";
								if (chunkList[j].getName().equals(chname))
									ci.setState(k, ChunkState.DOWNLOADING);
							}
						
					}						
				}
			}
			
		}
		if (chunks!=null){
			RegisterChunkReq rcr = new RegisterChunkReq(chunks, peerData, files);					
			handler.sendMessage(rcr);
		}
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
			        ind++;
			    }			    
			} catch (IOException x) {
			    System.err.println(x);
			}
		
		return res;
	}
	
	public synchronized FileDataListClient getFileList(String folderName){
		FileDataListClient fdl = new FileDataListClient();
		File folder = new File(folderName);
		File[] fileList = folder.listFiles();
		FileData fd = null;
		
		if (fileList!=null)
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isFile()){
				fd =new FileData(fileList[i]); 
				fdl.addItem(fd);
			}
		}		
		return fdl;
	}	
	
	// when the peer wants to download a file, it asks the server for a peer list
	public synchronized void sendChunkListRequest(FileData fd){		
		ChunkListReq req = new ChunkListReq(fd, peerData);
		handler.sendMessage(req);
	}
	
	// the peer downloads the selected file
	public synchronized void downloadAFile(ChunkListResp chunkList){
		//chunkm.writeoutfileChunks();
		//Test test = new Test();		
		//PeerClient pclient = new PeerClient(chunkm, handler, test.createChunkList(peerData), this.peerData);
		PeerClient pclient = new PeerClient(chunkm, handler, chunkList, this.peerData);
	}
	
	// create peer table
	public synchronized DefaultTableModel buildTable(){		
		return chunkm.getFiles();
	}
	
	// create server table
	public synchronized void buildServerTable(GetFilesResp gfs){
		peergui.buildServerTable( gfs.getDtm());
	}
	
	// update row or insert a new row
	public synchronized void updatePeerTable(Vector<Object> rowData){		
		peergui.peerTableRows(rowData);
	}
	
	// add a new row to server table
	public synchronized void updateServerTable(ServerFilesUpdate sfu){
		Vector<Object> rowData = sfu.getRow();
		peergui.serverTableRows(rowData);
	}

	// copies the selected file into PeerClient folder
	public void copyFile(File source, String fileName){
		File dest = new File(peerDir+fileName);		
		InputStream input = null;
		OutputStream output = null;

		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
			FileData fd = new FileData(dest);
			chunkm.addNewFile(fd);
			chunkm.sendNewFileChunks(fd);
		} catch (FileNotFoundException e) {		
			Logging.write(this.getClass().getName(), "copyFile", e.getMessage());
		} catch (IOException e) {
			Logging.write(this.getClass().getName(), "copyFile", e.getMessage());			
		}			
		finally {
			try {
				input.close();
				output.close();
			} catch (IOException e) {
				Logging.write(this.getClass().getName(), "copyFile", e.getMessage());				
			}			
		}
	}

	// the peer has to download automatically the uncompleted files
	public void completeFileDownloading(String folderName){		
		File folder = new File(folderName);
		File[] fileList = folder.listFiles();
		String [] desc = null;
		
		if (fileList!=null)
		if (fileList.length!=0){			
			for (int i=0; i<fileList.length; i++){
				if (getFileExtention(fileList[i].getName()).equals("dsc")){
					
					desc = getDescData(fileList[i]); // filename, size, crc, nrofchunks
					String fName = desc[0];
					String fSize = desc[1];
					String fCrc = desc[2];
					int fChunkNr = Integer.parseInt(desc[3]);
					
					FileData fd = new FileData();
					fd.setName(fName);
					fd.setSize(Long.valueOf(fSize));
					fd.setCrc(fCrc);
					
					sendChunkListRequest(fd);
						
				}
			}
		}
	}
		
}
