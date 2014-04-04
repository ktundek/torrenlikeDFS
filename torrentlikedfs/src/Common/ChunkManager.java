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
	private String dir = "";
	
	//public ChunkManager(String dir, PeerData peer){
	public ChunkManager(String dir, Object obj){
		fileChunks = Collections.synchronizedMap(new HashMap<FileData,ChunkInfo>());
		if (obj!=null && obj instanceof PeerData) this.peer = (PeerData) obj; 		
		this.dir = dir;
		
		addInitialFiles(dir);
		checkAndCreateDir(dir);
		checkAndCreateDir(dir+"Chunk");
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
			File f = new File(dir + fileName + "_" + chunkIndex + ".chnk");
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

	private synchronized void writeChunk(String fileName, int chunkIndex, byte[] data) {
		FileOutputStream fos = null;

		try
		{
			File f = new File(dir + fileName + "_" + chunkIndex + ".chnk");
			fos = new FileOutputStream(f);
			log("Write size:" + data.length);
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
	
	//private ChunkMessage pocess
}
