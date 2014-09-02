package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Vector;
import java.io.*;

import Common.ChunkInfo;
import Common.ChunkManager;
import Common.ChunkState;
import Common.Constants;
import Common.FileData;
import Common.FileDataListClient;
import Logger.Logging;
import Messages.RegisterChunkReq;
import Messages.ServerFilesUpdate;

public class TrackerServer implements Constants{	
	private Vector<TrackerItem> trackers = null;

	public TrackerServer() {
		trackers = new Vector<TrackerItem>();
	}	
	
	public FileDataListClient getFileList(String directory){
		FileDataListClient fdl = new FileDataListClient();
		File folder = new File(directory);
		File[] fileList = folder.listFiles();
		FileData fd = null;
		
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isFile()){
				fd =new FileData(fileList[i]); 
				fdl.addItem(fd);
			}
		}		
		return fdl;
	}
	
	public RegisterChunkReq getChunkList(String directory){
		RegisterChunkReq rcr = null;
				
		Hashtable<String, ChunkInfo> chunks = null;
		Hashtable<String, FileData> files = null;
		File folder = new File(directory);
		File[] chunkList = folder.listFiles();
		String [] desc = null;
				
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
		if (chunks!=null){
			rcr = new RegisterChunkReq(chunks, null, files);			
		}
		return rcr;
		
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
			} catch (IOException e) {
			    System.err.println(e);
			}
		
		return res;
	}
	
	public synchronized void notifyTrackerItems(ServerFilesUpdate sfu){
		for (int i=0; i<trackers.size(); i++){			
			Logging.write(this.getClass().getName(), "notifyTrackerItems" , "Notify all peers about the new file.");
			TrackerItem ti = trackers.get(i);
			ti.notifyPeer(sfu);
		}
	}
		
	public synchronized void deleteTrackerItem(TrackerItem ti){
		trackers.remove(ti);
	}
	
	public static void main(String args[]){
		FileDataListClient fdl = null;
		String dirr = "C:/TreckerServer/";		
		String dirw = "C:/TreckerServerChunk/";		

		TrackerServer ts = new TrackerServer();
		ServerSocket serverSocket = null;
		TrackerServerCore tsc = new TrackerServerCore();
		
		ChunkManager chunkm = new ChunkManager(dirr, dirw, ts, tsc);
		
		fdl = ts.getFileList(dirr);
		tsc.addFileList(fdl);
		
		chunkm.registerTrackerFiles(fdl);
		if (ts.getChunkList(dirw)!=null)
			chunkm.registerTrackerChunk(ts.getChunkList(dirw));
		//chunkm.writeoutfileChunks();		
				
		Socket socket = null;			
		int serverItemNr = 0;
		
		try {
			serverSocket = new ServerSocket(TRACKER_PORT);
		} catch (IOException e) {						
			Logging.write("TrackerServer", "main", "Could not listen on the port " + TRACKER_PORT+ "   "+e.getMessage());
			System.exit(-1);
			//e.printStackTrace();
		}
				
		Logging.write("TrackerServer", "main", "Tracker is waiting for clients...");
		System.out.println("Tracker is waiting for clients...");
		while (true){									
			try {
				socket = serverSocket.accept();				
				serverItemNr++;				
				Logging.write("TrackerServer", "main", "New client registered on port:"+ socket.getPort());
				ClientObserver observer = new ClientObserver(tsc, System.currentTimeMillis(), socket.getPort(), serverItemNr);
				TrackerItem ti = new TrackerItem(socket, tsc, ts, observer, chunkm, serverItemNr);
				ts.trackers.add(ti);
			} 
			catch (IOException e) {				
				Logging.write("TrackerServer", "main", "Connection is not accepted    "+e.getMessage());				
				break;
			}
		}
	}
}
