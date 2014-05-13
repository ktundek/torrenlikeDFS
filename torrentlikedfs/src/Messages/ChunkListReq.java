package Messages;

import java.io.Serializable;

import Client.PeerData;
import Common.FileData;

public class ChunkListReq implements Serializable{
	private FileData fileData;
	private PeerData peer;
	
	public ChunkListReq(FileData fileData, PeerData peer) {
		super();
		this.fileData = fileData;
		this.peer = peer;
	}

	public FileData getFileData() {
		return fileData;
	}

	public void setFileData(FileData fileData) {
		this.fileData = fileData;
	}

	public PeerData getPeer() {
		return peer;
	}

	public void setPeer(PeerData peer) {
		this.peer = peer;
	}		
	
}
