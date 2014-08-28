package Messages;

import java.io.Serializable;
import java.util.Map;


import Client.PeerData;
import Common.FileData;
import Common.PeerList;

public class ChunkListResp extends ChunkMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Map<String, PeerList> chunkList = null;
	protected FileData fileData = null;
	
	public ChunkListResp(PeerData peer, Map<String, PeerList> chunkList, FileData fileData) {
		super(peer);
		this.chunkList = chunkList;
		this.fileData = fileData;  
	}

	public Map<String, PeerList> getChunkList() {
		return chunkList;
	}

	public void setChunkList(Map<String, PeerList> chunkList) {
		this.chunkList = chunkList;
	}

	public FileData getFileData() {
		return fileData;
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}			

}
