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
import Server.TrackerServer;

public class ChunkManager {
	private Map<FileData, ChunkInfo> fileChunks=null;
	private PeerData peer = null;
	private TrackerServer trackers = null;
	private PeerHandler phandler = null;
	private String dirr = ""; //read from this directory
	private String dirw = ""; //write into this directory
	
	//public ChunkManager(String dir, PeerData peer){
	public ChunkManager(String dirr, String dirw, Object obj){
		fileChunks = Collections.synchronizedMap(new HashMap<FileData,ChunkInfo>());
		if (obj!=null && obj instanceof PeerData) this.peer = (PeerData) obj; 		
		//this.phandler = phandler;
		this.dirr = dirr;
		this.dirw = dirw;
		
		addInitialFiles(dirr);
		checkAndCreateDir(dirr);
		
		addInitialFiles(dirw);
		checkAndCreateDir(dirw);
		//checkAndCreateDir(dir+"Chunk");
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
		//System.out.println(fileChunks);
		
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
	
	public synchronized void writeChunk(FileData file, int chunkIndex, byte[] data) {
		FileOutputStream fos = null;
		//FileInputStream fin = null;
		String fileName = file.getName();
		//long filesize =  file.getSize();
		//byte[] data = null;
		
		try
		{
			//File inf = new File(dir + fileName);
			File outf = new File(dirw + fileName + "_" + chunkIndex + ".chnk");			
			//fin = new FileInputStream(inf);
			fos = new FileOutputStream(outf);
			
			//data = new byte[chunkIndex*Constants.CHUNK_SIZE];
			//long readStart = (chunkIndex-1)*Constants.CHUNK_SIZE;
			//int rest = (int) (fin.available() -readStart);
			//int byteNr = fin.read(data);
			//byte[] newChunk = new byte[(int)(byteNr-readStart)];
			
			//for (int i=0; i<(byteNr-readStart);i++) newChunk[i] = data[(int) readStart+i];
			
			//if (readStart + Constants.CHUNK_SIZE <= filesize){				
				//fin.read(data);				
				//data = new byte[Constants.CHUNK_SIZE];
				//fin.read(data, (int) readStart, Constants.CHUNK_SIZE);										
			//}
			//else{
				//fin.read(data);
				//data = new byte[rest+1];
				//fin.read(data, (int) readStart, rest);							
			//}
			
			//log("Write size:" + data.length);
			fos.write(data);

			//log("Write size:" + newChunk.length);
			//fos.write(newChunk);
			
						
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
			//if (fin != null) try {fin.close();} catch (IOException e) {}
		}
	}

	//private synchronized void writeChunk(FileData file, int chunkIndex, byte[] data) {
	private synchronized byte[] getChunkData(FileData file, int chunkIndex) {
		//FileOutputStream fos = null;
		FileInputStream fin = null;
		String fileName = file.getName();
		long filesize =  file.getSize();
		byte[] data = null;
		byte[] newChunk = null;
		
		try
		{
			File inf = new File(dirr + fileName);
			//File outf = new File(dir + fileName + "_" + chunkIndex + ".chnk");			
			fin = new FileInputStream(inf);
			//fos = new FileOutputStream(outf);
			
			data = new byte[chunkIndex*Constants.CHUNK_SIZE];
			long readStart = (chunkIndex-1)*Constants.CHUNK_SIZE;
			//int rest = (int) (fin.available() -readStart);
			int byteNr = fin.read(data);
			newChunk = new byte[(int)(byteNr-readStart)];
			
			for (int i=0; i<(byteNr-readStart);i++) newChunk[i] = data[(int) readStart+i];
			
			//if (readStart + Constants.CHUNK_SIZE <= filesize){				
				//fin.read(data);				
				//data = new byte[Constants.CHUNK_SIZE];
				//fin.read(data, (int) readStart, Constants.CHUNK_SIZE);										
			//}
			//else{
				//fin.read(data);
				//data = new byte[rest+1];
				//fin.read(data, (int) readStart, rest);							
			//}
			
			//log("Write size:" + data.length);
			//fos.write(data);

			log("Write size:" + newChunk.length);
			//fos.write(newChunk);												
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
			//if (fos != null) try {fos.close();} catch (IOException e) {}
			if (fin != null) try {fin.close();} catch (IOException e) {}
		}
		return newChunk;
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
	
	public void processFileListChunkReq(FileDataListClient fileList){
		ChunkResp resp =null;// = new ChunkResp(peer);
		byte[] chunk;
		byte[] data = new byte[Constants.CHUNK_SIZE];
		FileData fd = null;
		int chunknr = 0;
		
		for (int i=0; i<fileList.getSize();i++){
			fd = fileList.getItem(i);
			chunknr = fd.getChunkNumber();
			for (int j=1; j<=chunknr;j++){
				chunk = getChunkData(fd, j);				
				//chunk = readChunk(fd.getName(), j);
				//writeChunk(fd, j, chunk);
				
				resp = new ChunkResp(peer);
				resp.setChunkNr(j);
				resp.setData(chunk);
				resp.setFd(fd);
				phandler.sendMessage(resp);
			}
				
		}
		System.out.println("CHUNKMANAGER: processFileListChunkReq");
		//phandler.sendMessage(resp);
	}
	
	//private ChunkMessage pocess
}
