package Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import Client.PeerData;
import Client.PeerHandler;
import Messages.ChunkReq;
import Messages.ChunkResp;
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
	//private Map <FileData, ChunkList>  availableFiles = null;
	//private Map <Chunk, PeerList> chunkOwners = null;
	
	// for tracker's file's state
	private Map<FileData, ChunkInfo> fileChunks=null;	// file chunk map
		
	public ChunkManager(String dirr, String dirw, Object obj, Object serverObj){
		fileChunks = Collections.synchronizedMap(new HashMap<FileData, ChunkInfo>());
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
			fileChunks.put(fd, ci);
		}		
	}
	
	public void printlnFileChunks(){
		//Iterator it = fileChunks.
		
	}
	
	private synchronized byte[] readChunk(String fileName, int chunkIndex) {
		FileInputStream fis = null;
		byte[] buffer = null;
		
		try
		{
			File f = new File(dirw + fileName + "_" + chunkIndex + ".chnk");
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
		
		try
		{			
			File outf = new File(dirw + fileName + "_" + chunkIndex + ".chnk");						
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
			for (int j=1; j<=chunknr;j++){
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
	
	private synchronized boolean isAllChunksCompleted(FileData fd)
	{
		ChunkInfo ci = fileChunks.get(fd);
		if (ci == null) return false;
		
		for (int i=0;i<ci.nrChunks();i++)
		{
			if (ci.getState(i) != ChunkState.COMPLETED) return false;
		}		
		return true; 
	}
	
	public void registerPeerFiles(RegisterGroupReq rgr){
		PeerData pd = rgr.getGroup().getPeerData();
		FileDataListClient fdl = rgr.getGroup().getFileList();
		
		//if ()
		
		
	}
	
	// when the ChunkManager gets a new chunk
	public synchronized void onChunkResp(ChunkResp resp){
		FileData fd = resp.getFd();		
		int chunkNr = resp.getChunkNr();
		byte[] data = resp.getData();
		chunkNr--; //because chunk numbers are from 1 not from 0
		
		if (!fileChunks.containsKey(fd)){
			System.out.println("First CHUNK");
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());		
			ci.setState(chunkNr, ChunkState.COMPLETED);
			fileChunks.put(fd, ci);
			writeChunk(fd, chunkNr, data);
		}
		else{
			System.out.println("Even more chunks!");
			ChunkInfo ci = fileChunks.get(fd);
			if (ci.getState(chunkNr)==ChunkState.EMPTY){
				ci.setState(chunkNr, ChunkState.COMPLETED);
				writeChunk(fd, chunkNr, data);
			}			
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
		
		if (fileChunks.containsKey(fd)){
			System.out.println("van file");
			ChunkInfo ci = fileChunks.get(fd);
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

}
