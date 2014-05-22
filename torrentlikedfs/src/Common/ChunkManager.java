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
import org.apache.commons.io.IOUtils;

import Client.PeerData;
import Client.PeerHandler;
import Messages.ChunkListReq;
import Messages.ChunkListResp;
import Messages.ChunkReq;
import Messages.ChunkResp;
import Messages.RegisterChunkReq;
import Messages.RegisterChunkResp;
import Messages.RegisterGroupReq;
import Messages.RegisterPeerChunk;
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
	//private Map <FileData, ChunkInfo>  availableFiles = null;
	//private Map <String, PeerList> chunkOwners = null;

	// for tracker's file's state
	//private Map<FileData, ChunkInfo> fileChunks=null;	// file chunk map
	//private Map<String, FileData> files = null; // <fileCrc, fileData>
	private Map<String, ChunkInfo> fileChunks=null;	// <fileCrc, ChunkInfo>
	private Map<String, PeerList> chunkOwners = null; // <chunkName, PeerList>

	public ChunkManager(String dirr, String dirw, Object obj, Object serverObj){
		//fileChunks = Collections.synchronizedMap(new HashMap<FileData, ChunkInfo>());
		//files = Collections.synchronizedMap(new HashMap<String, FileData>());
		fileChunks = Collections.synchronizedMap(new HashMap<String, ChunkInfo>());
		chunkOwners = Collections.synchronizedMap(new HashMap<String, PeerList>());
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

	//-------------------------------------- Common methods --------------------------------------//

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

	//private synchronized byte[] readChunk(String fileName, long fileSize, int chunkIndex) {
	private synchronized byte[] readChunk(FileData fd, int chunkIndex) {
		String fileName = fd.getName();
		long fileSize = fd.getSize();		
		FileInputStream fis = null;
		String size = String.valueOf(fileSize);
		byte[] buffer = null;

		try
		{
			//File f = new File(dirw + fileName + size + getCrcChar(fd)+"_" + chunkIndex + ".chnk");
			String chname = getChunkPath(fd, chunkIndex);
			File f = new File(chname);
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

	protected synchronized void writeChunk(FileData file, int chunkIndex, byte[] data) {
		FileOutputStream fos = null;		
		String fileName = file.getName();
		String filesize = String.valueOf(file.getSize());

		try
		{	
			System.out.println("WRITE: "+ dirw + fileName + filesize +getCrcChar(file)+ "_" + chunkIndex + ".chnk");
			//File outf = new File(dirw + fileName + filesize +getCrcChar(file)+ "_" + chunkIndex + ".chnk");
			//getCrcChar(file);
			String chname = getChunkPath(file, chunkIndex);
			File outf = new File(chname);
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

	protected synchronized byte[] getChunkData(FileData file, int chunkIndex) {		
		FileInputStream fin = null;
		String fileName = file.getName();
		long filesize =  file.getSize();
		//chunkIndex++;
		int index = chunkIndex+1;
		byte[] data = null;
		byte[] newChunk = null;

		try
		{
			File inf = new File(dirr + fileName);
			if (inf.exists()){
				fin = new FileInputStream(inf);				

				data = new byte[index*Constants.CHUNK_SIZE];
				long readStart = (index-1)*Constants.CHUNK_SIZE;			
				int byteNr = fin.read(data);
				newChunk = new byte[(int)(byteNr-readStart)];

				for (int i=0; i<(byteNr-readStart);i++) newChunk[i] = data[(int) readStart+i];

				log("Write size:" + newChunk.length);
			}
			else{ // when the requested file is a chunk
				newChunk = getChunk(file, chunkIndex);
			}
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

	protected synchronized byte[] getChunk(FileData file, int chunkIndex){
		FileInputStream fin = null;		
		byte[] data = null;		

		try {

			//fin = new FileInputStream(dirw+fileName+filesize+getCrcChar(file)+"_"+chunkIndex+".chnk");
			String chname = getChunkPath(file, chunkIndex);
			fin = new FileInputStream(chname);
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

	public synchronized boolean mergeChunks(FileData filedata) {
		//if (!isAllChunksCompleted(filedata)) return;
		//ChunkInfo ci = chunkInfoMap.get(filedata);
		boolean ok = false;
		long filesize =  filedata.getSize();

		FileOutputStream fos = null;
		FileInputStream fis = null;
		byte[] buffer = new byte[Constants.CHUNK_SIZE];
		try
		{
			fos = new FileOutputStream(dirr + filedata.getName());
			for (int i=0;i<filedata.getChunkNumber() ;i++)
			{
				// Read each chunk
				//fis = new FileInputStream(dirw + filedata.getName()+filesize+getCrcChar(filedata)+ "_" + i + ".chnk");
				String chname = getChunkPath(filedata, i);
				fis = new FileInputStream(chname);
				int readSize = fis.read(buffer);
				if (readSize<=0) log("Warning: read size is 0 (File:" + filedata.getName() + ",Chunk:" + i + ")");
				fis.close();

				// Write the chunk
				fos.write(buffer, 0, readSize);
			}
			ok = verifyCrc(filedata.getName(), filedata.getCrc());
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
		return ok;
	}
	
	// After we have all chunks we have to delete them and the description file too
	public synchronized void deleteChunks(FileData filedata) {					
		//delete description file
		String descname = dirw + filedata.getCrc()+ ".dsc";
		File file1 = new File(descname);		
		if (!file1.delete()) {System.out.println("CHUNKMANAGER: deleteChunks: The description file: "+descname+" wasn't deleted!");}
		
		//delete chunks
		for (int i=0;i<filedata.getChunkNumber() ;i++)
		{				
			String chname = getChunkPath(filedata, i);
			System.out.println("Delete: "+chname);
			File file2 = new File(chname);
			if (!file2.delete()){System.out.println("CHUNKMANAGER: deleteChunks: The file: "+chname+" wasn't deleted!");}			
		}			

	}
	
	public synchronized boolean verifyCrc(String fileName, String crc){
		boolean ok = false;
		File file = new File(dirr + fileName);
		FileData fd = new FileData(file);
		if (fd.getCrc().equals(fileName)) ok = true;
		return ok;
	}
	
	public void writeoutfileChunk(String key, FileData fd){
		int a = fileChunks.size();
		System.out.println(fd.getName()+"FILECHUNKS size:"+a);
		ChunkInfo ci = fileChunks.get(key);
		for (int i=0;i<ci.nrChunks();i++){
			System.out.print(ci.getState(i)+", ");
		}		
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

	protected synchronized boolean isAllChunksCompleted(FileData fd)
	{
		ChunkInfo ci = fileChunks.get(fd.getCrc());
		if (ci == null) return false;

		for (int i=0;i<ci.nrChunks();i++)
		{
			if (ci.getState(i) != ChunkState.COMPLETED) return false;
		}		
		return true; 
	}

	public synchronized void createFileDescriptor(FileData fd){
		FileWriter fw = null;
		BufferedWriter bw = null;
		File file = null;
		try { 
			//File file = new File(dirw+fd.getName()+fd.getSize()+".dsc");			
			file = new File(dirw+fd.getCrc()+".dsc");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(fd.getName());
			bw.newLine();
			bw.write(String.valueOf(fd.getSize()));
			bw.newLine();
			bw.write(fd.getCrc());
			bw.newLine();
			bw.write(String.valueOf(fd.getChunkNumber()));
			bw.newLine();		
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				bw.close();
				fw.close();				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}

	public synchronized String[] getDescData(String fileName){		
		String[] res = new String[4];
		BufferedReader reader=null;
		//FileInputStream reader = null;
		FileReader fr = null;

		try {
			fr = new FileReader(dirw+fileName);
			reader = new BufferedReader(fr);
			//reader = new FileInputStream(dirw+fileName);
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
		finally{
			/*if (reader!=null)				
				//reader.close();
				IOUtils.closeQuietly(reader);*/			
			try {
				reader.close();
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return res;
	}

	public synchronized String getChunkPath(FileData fd, int chunkIndex){				
		String fileName = getChunkName(fd, chunkIndex);
		String name = dirw+fileName;
		//System.out.println("DIR: "+name);
		return name;
	}
	
	public synchronized String getChunkName(FileData fd, int chunkIndex){
		String filesize = String.valueOf(fd.getSize());
		return fd.getName()+filesize+getCrcChar(fd)+"_"+chunkIndex+".chnk";
	}
	
 	public synchronized String getCrcChar(FileData fd){
		String c = fd.getCrc().substring(0, 5);		
		return c;
	}
	
	//-------------------------------------- End Common methods --------------------------------------//

	//-------------------------------------- Peer methods --------------------------------------//
	public void setPeerHandler(PeerHandler phandler){
		this.phandler = phandler;
	}

	// peer register its own files
	public void initializePeerFileList(FileDataListClient fdl){		
		for (int i=0; i<fdl.getSize();i++){
			FileData fd = fdl.getItem(i);
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());
			for (int j=0; j<ci.nrChunks(); j++) ci.setState(j, ChunkState.COMPLETED);
			fileChunks.put(fd.getCrc(), ci);
		}
	}
	
	// peer registers its own chunks
	public void initializePeerChunkList(Map<String, ChunkInfo> chunks){					
		Iterator<Entry<String, ChunkInfo>> it = chunks.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			ChunkInfo value = (ChunkInfo) pairs.getValue();
			
			if (!fileChunks.containsKey(key)){				
				fileChunks.put(key, value);
			}
			else{
				ChunkInfo ci = fileChunks.get(key);
				for (int i=0;i<ci.nrChunks(); i++){
					if (ci.getState(i).equals(ChunkState.EMPTY)&&
							value.getState(i).equals(ChunkState.COMPLETED))
						ci.setState(i, ChunkState.COMPLETED);
				}
			}
		}
	}
	
	// when the peer gets a ChunkResp
	public synchronized void onChunkRespPeer(ChunkResp resp){
		FileData fd = resp.getFd();		
		int chunkNr = resp.getChunkNr();
		byte[] data = resp.getData();
		//chunkNr--; //because chunk numbers are from 1 not from 0

		System.out.println("CHUNKMANAGER: onChunkRespPeer: the respons: "+resp.getFd().getName() +", chunknr: "+resp.getChunkNr());
		//if (!fileChunks.containsKey(fd)){
		if (!fileChunks.containsKey(fd.getCrc())){
			System.out.println("First CHUNK PEER");
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());		
			ci.setState(chunkNr, ChunkState.COMPLETED);
			//fileChunks.put(fd, ci);
			fileChunks.put(fd.getCrc(), ci);
			writeChunk(fd, chunkNr, data);
			registerPeerChunks(this.peer, getChunkName(fd, chunkNr), chunkNr);
			createFileDescriptor(fd);
			writeoutfileChunk(fd.getCrc(), fd);
		}
		else{
			System.out.println("Even more PEER chunks!");
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci.getState(chunkNr)==ChunkState.EMPTY){
				ci.setState(chunkNr, ChunkState.COMPLETED);
				writeChunk(fd, chunkNr, data);
				registerPeerChunks(this.peer, getChunkName(fd, chunkNr), chunkNr);
			}
			writeoutfileChunk(fd.getCrc(), fd);
		}		
		if (isAllChunksCompleted(fd)){
			System.out.println("Peer has all file chunks! :D");
			if (mergeChunks(fd)){
				if (tsc!=null) tsc.addFile(fd);
				//we should delete the chunks
				//deleteChunks(fd);
			}
			else{} //ertesiteni kell a klienst, hogy hiba tortent a file osszerakasaban			
		}		
	}
	
	// the peer registers the obtained chunks
	public synchronized void registerPeerChunks(PeerData pd, String chunkName, int chunkNr){
		RegisterPeerChunk rpc = new RegisterPeerChunk(pd, chunkName, chunkNr);
		phandler.sendMessage(rpc);
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

	//-------------------------------------- End Peer methods --------------------------------------//	

	//-------------------------------------- TrackerServer methods --------------------------------------//	

	// when the TrackerChunkManager gets a new chunk
	public synchronized void onChunkRespTracker(ChunkResp resp){
		System.out.println("ON CHUNK RESPONSE TRACKER!");
		FileData fd = resp.getFd();		
		int chunkNr = resp.getChunkNr();
		byte[] data = resp.getData();		
		
		System.out.println("onChunkRespTracker kiiratas: ");
		writeoutfileChunks();
		System.out.println("onChunkRespTracker kiiratas vege!");
		
		if (!fileChunks.containsKey(fd.getCrc())){
			//files.put(fd.getCrc(), fd);
			System.out.println("First CHUNK");
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());		
			ci.setState(chunkNr, ChunkState.COMPLETED);	
			
			fileChunks.put(fd.getCrc(), ci);			
			PeerList pl = new PeerList();
			pl.addItem(resp.getPeerInfo());
			chunkOwners.put(getChunkName(fd, chunkNr), pl);
			writeChunk(fd, chunkNr, data);
			createFileDescriptor(fd);
			
		}
		else{
			System.out.println("Even more chunks!");
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci.getState(chunkNr)==ChunkState.EMPTY){
				ci.setState(chunkNr, ChunkState.COMPLETED);
				writeChunk(fd, chunkNr, data);
			}
			String qwer =  getChunkName(fd, chunkNr);
			//System.out.println("QWER: "+qwer);
			//writeOutChunkOwnerKeys();
			if (chunkOwners.containsKey(qwer)){
				System.out.println("CONTAINS CHUNKNAME!!!");
				PeerList pl = chunkOwners.get(getChunkName(fd, chunkNr));
				pl.addItem(resp.getPeerInfo());				
			}
			else{
				PeerList pl = new PeerList();
				pl.addItem(resp.getPeerInfo());
				chunkOwners.put(getChunkName(fd, chunkNr), pl);
			}
			//writeoutfileChunk(fd.getCrc(), fd);
		}	
		writeOutChunkOwner();
		if (isAllChunksCompleted(fd)){
			//System.out.println("Tracker has all file chunks!");
			if (mergeChunks(fd)){
				tsc.addFile(fd);
				// we should delete the chunks
				//deleteChunks(fd);
			}
			else{} // hiba a file osszerakasaban
		}		
	}

	// the server registers the peers files
	public synchronized void registerPeerFiles(RegisterGroupReq rgr){
		System.out.println("CHUNKMANAGER: registerPeerFiles");
		PeerData peerdata = rgr.getGroup().getPeerData();
		FileDataListClient fdl = rgr.getGroup().getFileList();

		for (int i=0; i<fdl.getSize(); i++){
			FileData fd = fdl.getItem(i);
			System.out.println("CHUNKMANAGER: registerPeerFiles : the key: "+fd.getCrc());
			writeoutfileChunks();
			if (fileChunks.containsKey(fd.getCrc())){
				//System.out.println("CHUNKMANAGER: registerPeerFiles: Crc: "+fd.getCrc());
				for(int j=0; j<fd.getChunkNumber(); j++){
					String chunkName = getChunkName(fd, j);
					if (chunkOwners.containsKey(chunkName)){
						//System.out.println("CHUNKMANAGER: registerPeerFiles: chunkName: "+chunkName);
						PeerList pl = chunkOwners.get(chunkName);
						pl.addItem(peerdata);
					}
				}
			}
		}


	}
	
	// the server registers its own files
	public synchronized void registerTrackerFiles(FileDataListClient fdl){
		System.out.println("CHUNKMANAGER: registerTrackerFiles");		

		for (int i=0; i<fdl.getSize(); i++){
			FileData fd = fdl.getItem(i);
			if (!fileChunks.containsKey(fd.getCrc())){
				ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());
				for (int k=0;k<ci.nrChunks();k++) ci.setState(k, ChunkState.COMPLETED);
				fileChunks.put(fd.getCrc(), ci);
				System.out.println("CHUNKMANAGER: registerTrackerFiles: Crc: "+fd.getCrc());
				for(int j=0; j<fd.getChunkNumber(); j++){
					String chunkName = getChunkName(fd, j);
					PeerList pl = new PeerList(); // an empty PeerList
					chunkOwners.put(chunkName, pl);					
				}
			}
		}
	}
	
	// the server registers its own chunks
	public synchronized void registerTrackerChunk(RegisterChunkReq rcr){
		Map<String, ChunkInfo> chunks = rcr.getChunks();
		Map<String, FileData> files = rcr.getFiles();
		Iterator<Entry<String, ChunkInfo>> it = chunks.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			ChunkInfo value = (ChunkInfo) pairs.getValue();
			
			if (!fileChunks.containsKey(key)){
				fileChunks.put(key, value);
				FileData fd = files.get(key);
				for (int i=0; i<value.nrChunks(); i++){
					if (value.getState(i).equals(ChunkState.COMPLETED)){
						String chunkName = getChunkName(fd, i);
						PeerList pl = new PeerList(); // empty PeerList 
						chunkOwners.put(chunkName, pl);
					}
				}
			}
			else{ // this should not occur
				System.out.println("There is a problem....this should not occur!");
			}
		}
	}
	
	// the server registers the peers chunks
	public synchronized RegisterChunkResp processRegisterChunkRequest(RegisterChunkReq rcr){
		Map <String, ChunkInfo> res = Collections.synchronizedMap(new HashMap<String, ChunkInfo>());
		Map <String, ChunkInfo> req = rcr.getChunks();
		Map <String, FileData> reqfiles = rcr.getFiles();
		PeerData pd = rcr.getPeer();
		System.out.println("RegisterChunkReq PeedData: "+ pd.getInetAddress()+" : "+pd.getPort());

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
				System.out.println("a KLIENSNEL meglevo chunkok: VALUE");
				for (int k=0; k<value.nrChunks(); k++)
					System.out.print(value.getState(k)+", ");
				System.out.println("a TRACKERNEL meglevo chunkok: CI");
				for (int k=0; k<ci.nrChunks(); k++)
					System.out.print(ci.getState(k)+", ");

				ChunkInfo newci = new ChunkInfo(ci.nrChunks());
				for (int i=0;i<ci.nrChunks();i++)
					if(value.getState(i).equals(ChunkState.DOWNLOADING) && 
							ci.getState(i).equals(ChunkState.EMPTY)){
						newci.setState(i, ChunkState.DOWNLOADING); // the peer has to upload these chunks to the server							
					}
				res.put(key, newci);
				System.out.println("a kliensnek feltoltesre visszakuldott chunkok: NEWCI");
				for (int k=0; k<newci.nrChunks(); k++)
					System.out.print(newci.getState(k)+", ");				
				
				// be kell irnunk a mappekbe, h milyen chunkkok mely klienseknel talalhatok meg				
				System.out.println("be kell irnunk a mappekbe, h milyen chunkkok mely klienseknel talalhatok meg");
				for (int i=0; i<value.nrChunks();i++){
					if (value.getState(i).equals(ChunkState.DOWNLOADING) &&
							ci.getState(i).equals(ChunkState.COMPLETED)){
						FileData fd = reqfiles.get(key);
						//System.out.println("Filedata: "+ fd.getName());
						//System.out.println("ChunkName: "+ getChunkName(fd, i));
						PeerList pl = chunkOwners.get(getChunkName(fd, i));
						if (pl==null) System.out.println("ChunkManager.processRegisterChunkRequest : it is null");
						else {
							//System.out.println("it's size is: "+pl.size()+" and the peers data is: "+pd.getInetAddress()+", port: "+pd.getPort());
							pl.addItem(pd);
						}
						//pl.addItem(pd);
					}
				}
				writeOutChunkOwner();
			}	        
		}
		RegisterChunkResp resp = new RegisterChunkResp(res, rcr.getPeer());
		return resp;
	}			

	// the server registers chunks obtained by peers
	public synchronized void registerObtainedChunk(RegisterPeerChunk rpc){
		System.out.println("------OBTAINED CHUNK------");		
		PeerData pd = rpc.getPeer();
		String key = rpc.getChunkName();
		PeerList pl = chunkOwners.get(key);
		pl.addItem(pd);
		writeOutChunkOwner();
	}
	
	public synchronized ChunkListResp onChunkListRequest(ChunkListReq req){
		// megnezni, h a chunkkok mely klienseknel van es osszeallitani egy listat
		// ha nincs egy kliensnel sem, akkor a szervertol kell kerni
		PeerData peer = req.getPeer();
		FileData fd = req.getFileData();
		String crc = fd.getCrc();
		int chunkNr = fd.getChunkNumber();		
		
		Map<String, PeerList> chunkList = new HashMap<String, PeerList>();
		
		if (fileChunks.containsKey(crc)){
			for (int i=0; i<chunkNr; i++){
				String chunkName = getChunkName(fd, i);
				if (chunkOwners.containsKey(chunkName)){
					PeerList pl = chunkOwners.get(chunkName);
					chunkList.put(chunkName, pl);
				}
				else chunkList.put(chunkName, null); // if none of the peers has the chunk
			}
		}
		writeOutChunkList(chunkList);
		ChunkListResp resp = new ChunkListResp(peer, chunkList, fd);
		return resp;
	}
	
	// delete peers file registration from chunkOwners
	public synchronized void deletePeer(PeerData peerData){
		Iterator<Entry<String, PeerList>> it = chunkOwners.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			PeerList value = (PeerList) pairs.getValue();
			if (value.contains(peerData))
				value.removeItem(peerData);
		}
	}

	public synchronized void writeoutfileChunks(){
		System.out.println("Write ou filechunks content, peer side: ");
		Iterator<Entry<String, ChunkInfo>> it = fileChunks.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			System.out.println("the key:" + key);

			ChunkInfo value = fileChunks.get(key);
			System.out.println("ChunkList size: "+value.nrChunks());
			for (int k=0; k<value.nrChunks(); k++){
				System.out.println("        "+value.getState(k)+" ");			
			}
		}
	}

	public synchronized void writeOutChunkList(Map<String, PeerList> list){							
		Iterator<Entry<String, PeerList>> it = list.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			System.out.println("the key:" + key);

			PeerList value = list.get(key);
			System.out.println("ChunkList size: "+value.size());
			for (int k=0; k<value.size(); k++){
				System.out.println("        "+value.getPeerData(k)+" ");			
			}
		}
	}
	
	public synchronized void writeOutChunkOwner(){							
		Iterator<Entry<String, PeerList>> it = chunkOwners.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			System.out.println("the key:" + key);
			
			PeerList value = chunkOwners.get(key);
			if (value!=null){
			System.out.println("PeerList size: "+value.size());
			for (int k=0; k<value.size(); k++){
				System.out.println("        "+value.getPeerData(k)+" ");			
			}
			}
		}
	}

	public synchronized void writeOutChunkOwnerKeys(){
		Iterator<Entry<String, PeerList>> it = chunkOwners.entrySet().iterator();		
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			System.out.println("the key:" + key);	
		}
	}
}
	//-------------------------------------- End TrackerServer methods --------------------------------------//	
