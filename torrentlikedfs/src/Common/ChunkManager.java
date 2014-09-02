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
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


import Client.PeerData;
import Client.PeerHandler;
import Exceptions.ExceptionMessage;
import Logger.Logging;
import Messages.ChunkListReq;
import Messages.ChunkListResp;
import Messages.ChunkReq;
import Messages.ChunkResp;
import Messages.RegisterChunkReq;
import Messages.RegisterChunkResp;
import Messages.RegisterGroupReq;
import Messages.RegisterPeerChunk;
import Messages.ServerFilesUpdate;
import Server.TrackerServer;
import Server.TrackerServerCore;

public class ChunkManager {	
	private PeerData peer = null;
	private TrackerServer trackerserver = null;
	private TrackerServerCore tsc = null;
	private PeerHandler phandler = null;
	private String dirr = ""; //read from this directory
	private String dirw = ""; //write into this directory
	
	private Map<String, ChunkInfo> fileChunks=null;	// <fileCrc, ChunkInfo>
	private Map<String, PeerList> chunkOwners = null; // <chunkName, PeerList>

	public ChunkManager(String dirr, String dirw, Object obj, Object serverObj){		
		fileChunks = Collections.synchronizedMap(new HashMap<String, ChunkInfo>());
		chunkOwners = Collections.synchronizedMap(new HashMap<String, PeerList>());
		
		if (obj!=null && obj instanceof PeerData) this.peer = (PeerData) obj;
		else if (obj!=null && obj instanceof TrackerServer) this.trackerserver = (TrackerServer) obj; 
		if (serverObj!=null && serverObj instanceof TrackerServerCore) this.tsc = (TrackerServerCore) serverObj;
		
		this.dirr = dirr;
		this.dirw = dirw;
		
		checkAndCreateDir(dirr);
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

	protected synchronized void writeChunk(FileData file, int chunkIndex, byte[] data) {
		FileOutputStream fos = null;		
		String fileName = file.getName();
		String filesize = String.valueOf(file.getSize());

		try
		{	
			Logging.write(this.getClass().getName(), "writeChunk", "WRITE: "+ dirw + fileName + filesize +getCrcChar(file)+ "_" + chunkIndex + ".chnk");					
			String chname = getChunkPath(file, chunkIndex);
			File outf = new File(chname);
			fos = new FileOutputStream(outf);					
			fos.write(data);					
		}
		catch(FileNotFoundException e)
		{
			Logging.write(this.getClass().getName(), "writeChunk", e.getMessage());
		}
		catch (IOException e)
		{
			Logging.write(this.getClass().getName(), "writeChunk", e.getMessage());
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
			Logging.write(this.getClass().getName(), "getChunkData", e.getMessage());
		}
		catch (IOException e)
		{
			Logging.write(this.getClass().getName(), "getChunkData", e.getMessage());
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
			String chname = getChunkPath(file, chunkIndex);
			fin = new FileInputStream(chname);
			int count = fin.available();
			data = new byte[count];
			fin.read(data);

		} catch (FileNotFoundException e) {
			Logging.write(this.getClass().getName(), "getChunk", e.getMessage());
		} catch (IOException e) {
			Logging.write(this.getClass().getName(), "getChunk", e.getMessage());
		}
		finally{
			if (fin != null) try {fin.close();} catch (IOException e) {}
		}
		return data;
	}

	public synchronized boolean mergeChunks(FileData filedata) {		
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
				String chname = getChunkPath(filedata, i);
				fis = new FileInputStream(chname);
				int readSize = fis.read(buffer);
				if (readSize<=0) {
					Logging.write(this.getClass().getName(), "mergeChunks", "Warning: read size is 0 (File:" + filedata.getName() + ",Chunk:" + i + ")");					
				}
				fis.close();

				// Write the chunk
				fos.write(buffer, 0, readSize);
			}
			ok = verifyCrc(filedata.getName(), filedata.getCrc());
		}		
		catch (FileNotFoundException e)
		{
			Logging.write(this.getClass().getName(), "mergeChunks", e.getMessage());
		}
		catch (IOException e)
		{
			Logging.write(this.getClass().getName(), "mergeChunks", e.getMessage());
		}
		finally
		{
			if (fis != null) try {fis.close();} catch (IOException e) {}
			if (fos != null) try {fos.close();} catch (IOException e) {}
		}
				
		return ok;
	}
	
	// After we have all chunks we have to delete them and the description file too
	public synchronized void deleteChunks(FileData filedata) {					
		//delete description file
		String descname = dirw + filedata.getCrc()+ ".dsc";
		File file1 = new File(descname);		
		if (!file1.delete()) {
			Logging.write(this.getClass().getName(), "deleteChunks", "The description file: "+descname+" wasn't deleted!");		
		}
		
		//delete chunks
		for (int i=0;i<filedata.getChunkNumber() ;i++)
		{				
			String chname = getChunkPath(filedata, i);
			System.out.println("Delete: "+chname);
			File file2 = new File(chname);
			if (!file2.delete()){				
				Logging.write(this.getClass().getName(), "deleteChunks", "The file: "+chname+" wasn't deleted!");
			}			
		}			

	}
	
	public synchronized void deleteFile(FileData fd){
		String fileName = dirr + fd.getName();
		File file1 = new File(fileName);		
		if (!file1.delete()) {
			Logging.write(this.getClass().getName(), "deleteFile", "The file: "+fileName+" wasn't deleted!");		
		}
	}
	
	public synchronized boolean verifyCrc(String fileName, String crc){
		boolean ok = false;
		File file = new File(dirr + fileName);
		FileData fd = new FileData(file);
		if (fd.getCrc().equals(crc)) ok = true;
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
		Logging.write(this.getClass().getName(), "onChunkReq", fd.getName()+", chunk: "+ reqChunkNr);		

		if (fileChunks.containsKey(fd.getCrc())){			
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci!=null){				
				if (ci.getState(reqChunkNr) == ChunkState.COMPLETED){					
					resp = new ChunkResp(peer);
					resp.setFd(fd);
					resp.setChunkNr(reqChunkNr);
					resp.setData(getChunkData(fd, reqChunkNr));
				}					
			}
		}						
		return resp;	// the response is null if the server doesn't have the file or the chunk is EMPTY
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
			Logging.write(this.getClass().getName(), "createFileDescriptor", e.getMessage());
		}
		finally{
			try {
				bw.close();
				fw.close();					
			} catch (IOException e) {
				Logging.write(this.getClass().getName(), "createFileDescriptor", e.getMessage());
			}			
		}
	}

	public synchronized String[] getDescData(String fileName){		
		String[] res = new String[4];
		BufferedReader reader=null;		
		FileReader fr = null;

		try {
			fr = new FileReader(dirw+fileName);
			reader = new BufferedReader(fr);			
			String line = null;
			int ind = 0;
			while ((line = reader.readLine()) != null) {			
				res[ind] = line;				
				ind++;
			}				
		} catch (IOException x) {
			Logging.write(this.getClass().getName(), "getDescData", x.getMessage());
		}
		finally{				
			try {
				reader.close();
				fr.close();
			} catch (IOException e) {				
				Logging.write(this.getClass().getName(), "getDescData", e.getMessage());
			}
		}

		return res;
	}

	public synchronized String getChunkPath(FileData fd, int chunkIndex){				
		String fileName = getChunkName(fd, chunkIndex);
		String name = dirw+fileName;		
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

	public ChunkInfo getFileChunkInfo(FileData fd){		
		return fileChunks.get(fd.getCrc());
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
			
		if (!fileChunks.containsKey(fd.getCrc())){		// the peer gets the first chunk of ths file 	
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());		
			ci.setState(chunkNr, ChunkState.COMPLETED);			
			fileChunks.put(fd.getCrc(), ci);
			writeChunk(fd, chunkNr, data);
			registerPeerChunks(this.peer, getChunkName(fd, chunkNr), chunkNr);
			createFileDescriptor(fd);
			updatePeerFileList(fd);
			writeoutfileChunk(fd.getCrc(), fd);
		}
		else{ // the rest of the chunks
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci.getState(chunkNr)==ChunkState.EMPTY){
				ci.setState(chunkNr, ChunkState.COMPLETED);
				writeChunk(fd, chunkNr, data);
				registerPeerChunks(this.peer, getChunkName(fd, chunkNr), chunkNr);
				updatePeerFileList(fd);
			}
			writeoutfileChunk(fd.getCrc(), fd);
		}		
		if (isAllChunksCompleted(fd)){ // Peer has all file chunks			
			if (mergeChunks(fd)){
				if (tsc!=null) tsc.addFile(fd);
				//we should delete the chunks
				deleteChunks(fd);
			}
			else{
				int ans = ExceptionMessage.optionBox("Error merging the "+fd.getName()+" chunk(s)! Do you want to delete it?");
				if (ans==JOptionPane.YES_OPTION){deleteFile(fd); deleteChunks(fd);}
			}
			// TODO
			//ertesiteni kell a klienst, hogy hiba tortent a file osszerakasaban			
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
	}
	
	public synchronized void sendNewFileChunks(FileData fd){
		ChunkResp resp =null;
		byte[] chunk;
		for (int i=0; i<fd.getChunkNumber();i++){
			chunk = getChunkData(fd, i);								

			resp = new ChunkResp(peer);
			resp.setChunkNr(i);
			resp.setData(chunk);
			resp.setFd(fd);
			phandler.sendMessage(resp);
		}
	}

	public synchronized void processChunkListReq(RegisterChunkResp rcr){		
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

	// fill Peer's table
	public synchronized DefaultTableModel getFiles(){		
		DefaultTableModel dtm = new DefaultTableModel();
		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); 		

		// names of columns
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("File");
		columnNames.add("Size in bytes");
		columnNames.add("%");

		// whole files
		File folder = new File(dirr);
		File[] fileList = folder.listFiles();
		FileData fd = null;
		int per = 0;

		if (fileList!=null)
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isFile()){
				fd = new FileData(fileList[i]);
				Vector<Object> row = new Vector<Object>(); 
				per = getDownloadPercentage(fd);				
				row.add(fd.getName());
				row.add(fd.getSize());
				row.add(per);				
				data.add(row);
			}
		}

		// chunks
		File folder2 = new File(dirw);
		File[] fileList2 = folder2.listFiles();
		FileData fd2 = null;
		Vector<Object> row = new Vector<Object>(); 
		String [] desc = null;
		per = 0;
		
		if (fileList2!=null)
		for (int i=0; i<fileList2.length;i++){
			if (getFileExtention(fileList2[i].getName()).equals("dsc")){
				row = new Vector<Object>();

				desc = getDescData(fileList2[i].getName()); // filename, size, crc, nrofchunks
				String fName = desc[0];
				String fSize = desc[1];
				String fCrc = desc[2];							

				fd2 = new FileData();
				fd2.setName(fName);
				fd2.setSize(Long.valueOf(fSize));
				fd2.setCrc(fCrc);

				per = getDownloadPercentage(fd2);						
				row.add(fName);
				row.add(fSize);
				row.add(per);

				data.add(row);
			}
		}

		dtm.setDataVector(data, columnNames);
		return dtm;
	}

	// update peer's file list
	public synchronized void updatePeerFileList(FileData fd){
		Vector<Object> row = new Vector<Object>();
		int per = getDownloadPercentage(fd);				
		row.add(fd.getName());
		row.add(fd.getSize());
		row.add(per);
		phandler.updatePeerTable(row);
	}	
	

	public String getFileExtention(String fileName){
		String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		return ext;
	}
	
	// when user uploads a new file
	public void addNewFile(FileData fd){		
		if (!fileChunks.containsKey(fd.getCrc())){			
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());
			for (int i=0; i<fd.getChunkNumber();i++){
				ci.setState(i, ChunkState.COMPLETED);
			}			
			fileChunks.put(fd.getCrc(), ci);
			updatePeerFileList(fd);			
		}
		else{
			// TODO kellene szolni a peernek h mar van ilyen fajlja
		}
	}	

	public int getDownloadPercentage(FileData fileData)
	{
		ChunkInfo ci = fileChunks.get(fileData.getCrc());
		int num_completed = 0;
		for(int i=0;i<ci.nrChunks();i++)
		{
			if (ci.getState(i).equals(ChunkState.COMPLETED)) num_completed++;
		}

		return 100 * num_completed / ci.nrChunks();
	}

	//-------------------------------------- End Peer methods --------------------------------------//	

	//-------------------------------------- TrackerServer methods --------------------------------------//	

	// when the TrackerChunkManager gets a new chunk
	public synchronized void onChunkRespTracker(ChunkResp resp){
		System.out.println("ON CHUNK RESPONSE TRACKER!");
		FileData fd = resp.getFd();		
		int chunkNr = resp.getChunkNr();
		byte[] data = resp.getData();		
		
		if (!fileChunks.containsKey(fd.getCrc())){ // First chunk			
			ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());		
			ci.setState(chunkNr, ChunkState.COMPLETED);	
			
			fileChunks.put(fd.getCrc(), ci);			
			PeerList pl = new PeerList();
			pl.addItem(resp.getPeerInfo());
			chunkOwners.put(getChunkName(fd, chunkNr), pl);
			writeChunk(fd, chunkNr, data);
			createFileDescriptor(fd);
			
		}
		else{  // the rest of the chunks
			ChunkInfo ci = fileChunks.get(fd.getCrc());
			if (ci.getState(chunkNr)==ChunkState.EMPTY){
				ci.setState(chunkNr, ChunkState.COMPLETED);
				writeChunk(fd, chunkNr, data);
			}
			String qwer =  getChunkName(fd, chunkNr);
			
			if (chunkOwners.containsKey(qwer)){				
				PeerList pl = chunkOwners.get(getChunkName(fd, chunkNr));
				if (!pl.contains(resp.getPeerInfo()))
					pl.addItem(resp.getPeerInfo());				
			}
			else{
				PeerList pl = new PeerList();
				pl.addItem(resp.getPeerInfo());
				chunkOwners.put(getChunkName(fd, chunkNr), pl);
			}
		}			
		if (descFileExists(fd)){
			if (isAllChunksCompleted(fd)){ // Tracker has all file chunks
				if (mergeChunks(fd)){
					tsc.addFile(fd);
					// we should delete the chunks
					deleteChunks(fd);
					trackerserver.notifyTrackerItems(getFileDataForTableRow(fd)); // Notify all peers that there is a new file on the server
				}
				else{ // error merging the file
					Logging.write(this.getClass().getName(), "onChunkRespTracker", "Error in merging the file.");
				}						
			}
		}
	}

	// the server registers the peers files
	public synchronized void registerPeerFiles(RegisterGroupReq rgr){		
		PeerData peerdata = rgr.getGroup().getPeerData();
		FileDataListClient fdl = rgr.getGroup().getFileList();

		for (int i=0; i<fdl.getSize(); i++){
			FileData fd = fdl.getItem(i);			
			//writeoutfileChunks();
			if (fileChunks.containsKey(fd.getCrc())){				
				for(int j=0; j<fd.getChunkNumber(); j++){
					String chunkName = getChunkName(fd, j);
					if (chunkOwners.containsKey(chunkName)){						
						PeerList pl = chunkOwners.get(chunkName);
						pl.addItem(peerdata);
					}
				}
			}
		}
	}
	
	// the server registers its own files
	public synchronized void registerTrackerFiles(FileDataListClient fdl){		

		for (int i=0; i<fdl.getSize(); i++){
			FileData fd = fdl.getItem(i);
			if (!fileChunks.containsKey(fd.getCrc())){
				ChunkInfo ci = new ChunkInfo(fd.getChunkNumber());
				for (int k=0;k<ci.nrChunks();k++) ci.setState(k, ChunkState.COMPLETED);
				fileChunks.put(fd.getCrc(), ci);				
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
				Logging.write(this.getClass().getName(), "registerTrackerChunk","There is a problem....this should not occur!");				
			}
		}
	}
	
	// the server registers the peers chunks
	public synchronized RegisterChunkResp processRegisterChunkRequest(RegisterChunkReq rcr){
		Map <String, ChunkInfo> res = Collections.synchronizedMap(new HashMap<String, ChunkInfo>());
		Map <String, ChunkInfo> req = rcr.getChunks();
		Map <String, FileData> reqfiles = rcr.getFiles();
		PeerData pd = rcr.getPeer();		
		Logging.write(this.getClass().getName(), "processRegisterChunkRequest", "Register peer's("+pd.getInetAddress()+") chhunks");

		Iterator<Entry<String, ChunkInfo>> it = req.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			ChunkInfo value = req.get(key);
			if (!fileChunks.containsKey(key)){
				res.put(key, value);				
			}
			else{	        	
				ChunkInfo ci = fileChunks.get(key);				
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
				for (int i=0; i<value.nrChunks();i++){
					if (value.getState(i).equals(ChunkState.DOWNLOADING) &&
							ci.getState(i).equals(ChunkState.COMPLETED)){
						FileData fd = reqfiles.get(key);						
						PeerList pl = chunkOwners.get(getChunkName(fd, i));
						if (pl==null) System.out.println("ChunkManager.processRegisterChunkRequest : it is null");
						else {
							//System.out.println("it's size is: "+pl.size()+" and the peers data is: "+pd.getInetAddress()+", port: "+pd.getPort());
							pl.addItem(pd);
						}
						//pl.addItem(pd);
					}
				}
				//writeOutChunkOwner();
			}	        
		}
		RegisterChunkResp resp = new RegisterChunkResp(res, rcr.getPeer());
		return resp;
	}			

	// the server registers chunks obtained by peers
	public synchronized void registerObtainedChunk(RegisterPeerChunk rpc){		
		PeerData pd = rpc.getPeer();
		String key = rpc.getChunkName();
		PeerList pl = chunkOwners.get(key);
		pl.addItem(pd);
		//writeOutChunkOwner();
	}
	
	// here we create a list of peers who have chunks of a given file
	public synchronized ChunkListResp onChunkListRequest(ChunkListReq req){
		// megnezni, h a chunkkok mely klienseknel van es osszeallit egy peer listat
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
					pl.removeItem(peer); 
					chunkList.put(chunkName, pl);
				}
				else chunkList.put(chunkName, null); // if none of the peers has the chunk
			}
		}
		ChunkListResp resp = new ChunkListResp(peer, chunkList, fd);
		return resp;
	}
	
	// delete peers file registration from chunkOwners
	public synchronized void deletePeer(PeerData peerData){
		Iterator<Entry<String, PeerList>> it = chunkOwners.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();			
			PeerList value = (PeerList) pairs.getValue();			
			while (value.contains(peerData)){				
				value.removeItem(peerData);				
			}
		}
	}	
	
	// returns true if the specifies dsc file exists, else returns false
	public synchronized boolean descFileExists(FileData fd){
		boolean exists = false;
		File f = new File(dirw+fd.getCrc().toString()+".dsc");
		if(f.exists()) exists = true;		
		return exists;
	}
	
	
	public synchronized DefaultTableModel getFilesServer(){
		DefaultTableModel dtm = new DefaultTableModel();
		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); 		

		// names of columns
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("File");
		columnNames.add("Size in bytes");
		columnNames.add("Crc");

		// whole files
		File folder = new File(dirr);
		File[] fileList = folder.listFiles();
		FileData fd = null;
		int per = 0;

		if (fileList!=null)
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isFile()){
				fd = new FileData(fileList[i]); 
				Vector<Object> row = new Vector<Object>(); 	
				row.add(fd.getName());
				row.add(fd.getSize());
				row.add(fd.getCrc());				
				data.add(row);
			}
		}		

		dtm.setDataVector(data, columnNames);
		return dtm;
	}	

	// when the server gets a new file, we have to notify the peers
	public synchronized ServerFilesUpdate getFileDataForTableRow(FileData fd){
		Vector<Object> row = new Vector<Object>();			
		row.add(fd.getName());
		row.add(fd.getSize());
		row.add(fd.getCrc());
		ServerFilesUpdate msgUpdate = new ServerFilesUpdate(null);
		msgUpdate.setRow(row);
		return msgUpdate;
	}

	/*public synchronized void writeoutfileChunks(){		
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
	}*/

	/*public synchronized void writeOutChunkList(Map<String, PeerList> list){							
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
	}*/
	
	/*public synchronized void writeOutChunkOwner(){							
		Iterator<Entry<String, PeerList>> it = chunkOwners.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			
			PeerList value = chunkOwners.get(key);
			if (value!=null){
				String msg = "PeerList: size: "+ +value.size();				
				for (int k=0; k<value.size(); k++){
					System.out.println("        "+value.getPeerData(k)+" ");
					msg = msg + " " + value.getPeerData(k);
				}
				Logging.write(this.getClass().getName(), "writeOutChunkOwner", msg);
			}
		}
	}*/

	/*public synchronized void writeOutChunkOwnerKeys(){
		Iterator<Entry<String, PeerList>> it = chunkOwners.entrySet().iterator();		
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			String key = (String) pairs.getKey();
			System.out.println("the key:" + key);	
		}
	}*/
}
	//-------------------------------------- End TrackerServer methods --------------------------------------//	
