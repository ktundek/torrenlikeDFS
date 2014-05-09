package Common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import Client.PeerData;
import Client.PeerHandler;
import Messages.ChunkReq;
import Messages.ChunkResp;
import Messages.RegisterChunkReq;
import Messages.RegisterChunkResp;
import Messages.RegisterGroupReq;
import Server.TrackerServer;
import Server.TrackerServerCore;

public class ChunkManager {	
	private PeerData peer = null;
	private TrackerServer trackers = null;
	private TrackerServerCore tsc = null;
	private PeerHandler phandler = null;
	private String dirr = ""; //read from this directory
	private String dirw = ""; //write into this directory
	
	//for peers
	private Map <FileData, ChunkInfo>  availableFiles = null;
	private Map <ChunkData, PeerList> chunkOwners = null;
	
	// for tracker's file's state
	//private Map<FileData, ChunkInfo> fileChunks=null;	// file chunk map
	private Map<String, ChunkInfo> fileChunks=null;	// file chunk map
		
	public ChunkManager(String dirr, String dirw, Object obj, Object serverObj){
		//fileChunks = Collections.synchronizedMap(new HashMap<FileData, ChunkInfo>());
		fileChunks = Collections.synchronizedMap(new HashMap<String, ChunkInfo>());
		if (obj!=null && obj instanceof PeerData) this.peer = (PeerData) obj; 
		if (serverObj!=null && serverObj instanceof TrackerServerCore) this.tsc = (TrackerServerCore) serverObj;
		//this.phandler = phandler;
		this.dirr = dirr;
		this.dirw = dirw;
		
		//addInitialFiles(dirr);
		checkAndCreateDir(dirr);
		
		//addInitialFiles(dirw);
		checkAndCreateDir(dirw);		
	}
	
	private void checkAndCreateDir(String dir) {
		File file = new File(dir);
		if (!file.exists())
		{			
			file.mkdirs(); //create directory
		}
		else if (!file.isDirectory())
		{
			log("Warning: the directory [" + dir + "] already exists.");
		}
	}
	
	private void log(String msg)
	{
		String caller = "unknownMethod";
		
		caller = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println("ChunkManager." + caller + "(): " + msg);
	}
	
	public void setPeerHandler(PeerHandler phandler){
		this.phandler = phandler;
	}
	
	private void addInitialFiles(String dirName){
		File folder = new File(dirName);
		File[] fileList = folder.listFiles();
		
		FileData fd = null;
		ChunkInfo ci = null;
		
		for (int i=0; i<fileList.length; i++){
			fd = new FileData(fileList[i]);
			ci = new ChunkInfo(fd.getChunkNumber());
			for (int j=0; j<ci.nrChunks(); j++)
				ci.setState(j, ChunkState.COMPLETED);
			fileChunks.put(fd.getCrc(), ci);
		}		
	}
	
	public void printlnFileChunks(){
		//Iterator it = fileChunks.
		
	}
	
	private synchronized byte[] readChunk(String fileName, long fileSize, int chunkIndex) {
		FileInputStream fis = null;
		String size = String.valueOf(fileSize);
		byte[] buffer = null;
		
		try
		{
			File f = new File(dirw + fileName + size + "_" + chunkIndex + ".chnk");
			fis = new FileInputStream(f);
			
			buffer = new byte[Constants.CHUNK_SIZE];
			int read_size = fis.read(buffer);
			if (read_size < buffer.length)
			{
				byte[] new_buffer = new byte[read_size];
				for (int i=0;i<read_size;i++)
				{
					new_buffer[i] = buffer[i];
				}
				buffer = new_buffer;
			}
		}
		catch (FileNotFoundException e) 
		{
			log(e.getMessage());
		}
		catch (IOException e)
		{
			log(e.getMessage());
		}
		finally
		{
			if (fis != null) try {fis.close();} catch (IOException e) {}
		}
		
		return buffer;
	}
	
	private synchronized void writeChunk(FileData file, int chunkIndex, byte[] data) {
		FileOutputStream fos = null;		
		String fileName = file.getName();
		String filesize = String.valueOf(file.getSize());
		
		try
		{			
			File outf = new File(dirw + fileName + filesize + "_" + chunkIndex + ".chnk");						
			fos = new FileOutputStream(outf);					
			fos.write(data);					
		}
		catch(FileNotFoundException e)
		{
			log(e.getMessage());
		}
		catch (IOException e)
		{
			log(e.getMessage());
		}
		finally
		{
			if (fos != null) try {fos.close();} catch (IOException e) {}			
		}
	}
	
	private synchronized byte[] getChunkData(FileData file, int chunkIndex) {		
		FileInputStream fin = null;
		String fileName = file.getName();
		long filesize =  file.getSize();
		chunkIndex++;
		byte[] data = null;
		byte[] newChunk = null;
		
		try
		{
			File inf = new File(dirr + fileName);					
			fin = new FileInputStream(inf);			
			
			data = new byte[chunkIndex*Constants.CHUNK_SIZE];
			long readStart = (chunkIndex-1)*Constants.CHUNK_SIZE;			
			int byteNr = fin.read(data);
			newChunk = new byte[(int)(byteNr-readStart)];
			
			for (int i=0; i<(byteNr-readStart);i++) newChunk[i] = data[(int) readStart+i];
						
			log("Write size:" + newChunk.length);											
		}
		catch(FileNotFoundException e)
		{
			log(e.getMessage());
		}
		catch (IOException e)
		{
			log(e.getMessage());
		}
		finally
		{			
			if (fin != null) try {fin.close();} catch (IOException e) {}
		}
		return newChunk;
	}
	
	private synchronized byte[] getChunk(FileData file, int chunkIndex){
		FileInputStream fin = null;
		String fileName = file.getName();
		long filesize =  file.getSize();
		byte[] data = null;		
		
		try {
			
			fin = new FileInputStream(dirw+fileName+filesize+"_"+chunkIndex+".chnk");
			int count = fin.available();
			data = new byte[count];
			fin.read(data);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if (fin != null) try {fin.close();} catch (IOException e) {}
		}
		return data;
	}
	
	public synchronized void mergeChunks(FileData filedata) {
		//if (!isAllChunksCompleted(filedata)) return;
		//ChunkInfo ci = chunkInfoMap.get(filedata);

		FileOutputStream fos = null;
		FileInputStream fis = null;
		byte[] buffer = new byte[Constants.CHUNK_SIZE];
		try
		{
			fos = new FileOutputStream(dirr + filedata.getName());
			for (int i=0;i<filedata.getChunkNumber() ;i++)
			{
				// Read each chunk
				fis = new FileInputStream(dirw + filedata.getName()+ "_" + i + ".chnk");
				int readSize = fis.read(buffer);
				if (readSize<=0) log("Warning: read size is 0 (File:" + filedata.getName() + ",Chunk:" + i + ")");
				fis.close();
				
				// Write the chunk
				fos.write(buffer, 0, readSize);
			}
		}
		catch (FileNotFoundException e)
		{
			log(e.getMessage());
		}
		catch (IOException e)
		{
			log(e.getMessage());
		}
		finally
		{
			if (fis != null) try {fis.close();} catch (IOException e) {}
			if (fos != null) try {fos.close();} catch (IOException e) {}
		}
		
		//log(getSummaryString(fileInfo));
	}
	
	//public ChunkReq getChunkReq(ChunkResp resp){
	public ChunkReq getChunkReq(){
		ChunkReq chreq = new ChunkReq(peer);						
		/*chreq.setFd(fd);
		chreq.setChunkNr(chunkNr);*/
		return chreq;
	}
	
	//public ChunkResp getChunkResp(ChunkReq req){
	public ChunkResp getChunkResp(){
		ChunkResp resp = new ChunkResp(peer);
		
		return resp;
	}
	
	public synchronized void processFileListChunkReq(FileDataListClient fileList){
		ChunkResp resp =null;
		byte[] chunk;
		byte[] data = new byte[Constants.CHUNK_SIZE];
		FileData fd = null;
		int chunknr = 0;
		
		for (int i=0; i<fileList.getSize();i++){
			fd = fileList.getItem(i);
			chunknr = fd.getChunkNumber();
			for (int j=0; j<chunknr;j++){
				chunk = getChunkData(fd, j);								
				
				resp = new ChunkResp(peer);
				resp.setChunkNr(j);
				resp.setData(chunk);
				resp.setFd(fd);
				phandler.sendMessage(resp);
			}
				
		}
		System.out.println("CHUNKMANAGER: processFileListChunkReq");		
	}
	
	public synchronized void processChunkListReq(RegisterChunkResp rcr){
		System.out.println("CHUNKMANAGER: processChunkListReq");
		byte[] chunk = null;
		ChunkResp resp = null;
		Map <String, ChunkInfo> chunks = rcr.getChunks();		
		Iterator<Entry<String, ChunkInfo>> it = chunks.entrySet().iterator();
		
	    while (it.hasNext()) {
	    	Map.Entry pairs = (Map.Entry)it.next();
	    	String key = (String) pairs.getKey();
	    		    	
	    	String[] desc = getDescData(key+".dsc");
	    	FileData fd = new FileData();
	    	fd.setName(desc[0]);
	    	fd.setSize(Long.valueOf(desc[1]));
	    	fd.setCrc(desc[2]);	    	
	    		    	
	        ChunkInfo value = chunks.get(key);
	        for (int k=0; k<value.nrChunks(); k++)
	        	System.out.print(value.getState(k)+", ");
	        
	        
	        int chnr = value.nrChunks();
	        for (int i=0; i<chnr; i++){
	        	if (value.getState(i).equals(ChunkState.DOWNLOADING)){
	        		chunk = getChunk(fd, i);
	        		
	        		resp = new ChunkResp(peer);
	        		resp.setChunkNr(i);
	        		resp.setData(chunk);
	        		resp.setFd(fd);
	        		phandler.sendMessage(resp);
	        	}
	        }
	    }
	}
	
	
	public synchronized RegisterChunkResp processRegisterChunkRequest(RegisterChunkReq rcr){
		Map <String, ChunkInfo> res = Collections.synchronizedMap(new HashMap<String, ChunkInfo>());
		Map <String, ChunkInfo> req = rcr.getChunks();
		
		Iterator<Entry<String, ChunkInfo>> it = req.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String key = (String) pairs.getKey();
	        ChunkInfo value = req.get(key);
	        if (!fileChunks.containsKey(key)){
	        	res.put(key, value);
	        	System.out.println("a kliensnel meglevo chunkok: VALUE");
	        	for (int k=0; k<value.nrChunks(); k++)
		        	System.out.print(value.getState(k)+", ");
	        }
	        else{	        	
	        	ChunkInfo ci = fileChunks.get(key);
	        	System.out.println("a TRACKERNEL meglevo chunkok: CI");
	        	for (int k=0; k<value.nrChunks(); k++)
		        	System.out.print(value.getState(k)+", ");
	        	
	        	ChunkInfo newci = new ChunkInfo(ci.nrChunks());
	        	for (int i=0;i<ci.nrChunks();i++)
	        		if(value.getState(i).equals(ChunkState.DOWNLOADING) && 
	        				ci.getState(i).equals(ChunkState.EMPTY)){
	        			newci.setState(i, ChunkState.DOWNLOADING); // the peer has to upload these chunks to the server	        			
	        		}
	        	res.put(key, newci);
	        	System.out.println("a kliensnek feltoltesre visszakuldott chunkok: NEWCI");
	        	for (int k=0; k<value.nrChunks(); k++)
		        	System.out.print(value.getState(k)+", ");
	        	// be kell irnunk a mappekbe, h milyen chunkkok mely klienseknel talalhato meg
	        }	        
	    }
	    RegisterChunkResp resp = new RegisterChunkResp(res, rcr.getPeerInfo());
	    return resp;
	}
	
	public void registerPeerFiles(RegisterGroupReq rgr){
		PeerData pd = rgr.getGroup().getPeerData();
		FileDataListClient fdl = rgr.getGroup().getFileList();
		
		//if ()
		
		
	}
	
	public void writeoutfileChunk(String key, FileData fd){
		int a = fileChunks.size();
		System.out.println(fd.getName()+"FILECHUNKS size:"+a);
		ChunkInfo ci = fileChunks.get(key);
		for (int i=0;i<ci.nrChunks();i++){
			System.out.print(ci.getState(i)+", ");
		}		
	}
	
	// when the TrackerChunkManager gets a new chunk
	public synchronized void onChunkRespTracker(ChunkResp resp){
		System.out.println("ON CHUNK RESPONSE TRACKER!");
		FileData fd = resp.getFd();		
		int chunkNr = resp.getChunkNr();
		byte[] data = resp.getData();
		//chunkNr--; //because chunk numbers are from 1 not from 0
		
		//if (!fileChunks.containsKey(fd)){
		if (!fileChunks.containsKey(fd.getCrc())){
			System.out.println("First CHUNK");
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());		
			ci.setState(chunkNr, ChunkState.COMPLETED);
			//fileChunks.put(fd, ci);
			fileChunks.put(fd.getCrc(), ci);
			writeChunk(fd, chunkNr, data);
			//createFileDescriptor(fd);
			//writeoutfileChunk(fd.getCrc(), fd);
		}
		else{
			System.out.println("Even more chunks!");
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci.getState(chunkNr)==ChunkState.EMPTY){
				ci.setState(chunkNr, ChunkState.COMPLETED);
				writeChunk(fd, chunkNr, data);
			}
			writeoutfileChunk(fd.getCrc(), fd);
		}		
		if (isAllChunksCompleted(fd)){
			//System.out.println("Tracker has all file chunks!");
			mergeChunks(fd);
			tsc.addFile(fd);
			// we should delete the chunks
		}		
	}
	
	// when the peer gets a ChunkResp
	public synchronized void onChunkRespPeer(ChunkResp resp){
		FileData fd = resp.getFd();		
		int chunkNr = resp.getChunkNr();
		byte[] data = resp.getData();
		chunkNr--; //because chunk numbers are from 1 not from 0
		
		//if (!fileChunks.containsKey(fd)){
		if (!fileChunks.containsKey(fd.getCrc())){
			System.out.println("First CHUNK PEER");
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());		
			ci.setState(chunkNr, ChunkState.COMPLETED);
			//fileChunks.put(fd, ci);
			fileChunks.put(fd.getCrc(), ci);
			writeChunk(fd, chunkNr, data);
			createFileDescriptor(fd);
			writeoutfileChunk(fd.getCrc(), fd);
		}
		else{
			System.out.println("Even more chunks!");
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci.getState(chunkNr)==ChunkState.EMPTY){
				ci.setState(chunkNr, ChunkState.COMPLETED);
				writeChunk(fd, chunkNr, data);
			}
			writeoutfileChunk(fd.getCrc(), fd);
		}		
		if (isAllChunksCompleted(fd)){
			//System.out.println("Tracker has all file chunks!");
			mergeChunks(fd);
			tsc.addFile(fd);
			// we should delete the chunks
		}		
	}
	
	public synchronized void onChunkListRequest(){
		// megnezni, h a chunkkok mely klienseknel van es osszeallitani egy listat
		// ha nincs egy kliensnel sem, akkor a szervertol kell kerni
	}
	
	// abban az esetben, ha a kert file nincs meg egy peernel sem, a tracker fogja elkuldeni
	public synchronized ChunkResp onChunkReq(ChunkReq req){
		ChunkResp resp = null;
		FileData fd = req.getFd();
		int reqChunkNr = req.getChunkNr();
		System.out.println("CHUNKMANAGER: onChunkReq: "+fd.getName()+", chunk: "+ reqChunkNr);
		
		if (fileChunks.containsKey(fd.getCrc())){
			System.out.println("van file");
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci!=null){
				System.out.println("van chunkinfo");
				if (ci.getState(reqChunkNr) == ChunkState.COMPLETED){
					System.out.println("megvan a chunkinfo");
					resp = new ChunkResp(peer);
					resp.setFd(fd);
					resp.setChunkNr(reqChunkNr);
					resp.setData(getChunkData(fd, reqChunkNr));
				}					
			}
		}						
		return resp;	// the response is null if the server has not the file or the chunk is EMPTY
	}
	
	private synchronized boolean isAllChunksCompleted(FileData fd)
	{
		ChunkInfo ci = fileChunks.get(fd.getCrc());
		if (ci == null) return false;
		
		for (int i=0;i<ci.nrChunks();i++)
		{
			if (ci.getState(i) != ChunkState.COMPLETED) return false;
		}		
		return true; 
	}
	
	public void createFileDescriptor(FileData fd){
		try { 
			//File file = new File(dirw+fd.getName()+fd.getSize()+".dsc");
			File file = new File(dirw+fd.getCrc()+".dsc");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(fd.getName());
			bw.newLine();
			bw.write(String.valueOf(fd.getSize()));
			bw.newLine();
			bw.write(fd.getCrc());
			bw.newLine();
			bw.write(String.valueOf(fd.getChunkNumber()));
			bw.newLine();
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String[] getDescData(String fileName){		
		String[] res = new String[4]; 
		
		try {
			    BufferedReader reader = new BufferedReader(new FileReader(dirw+fileName)); 
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

}
