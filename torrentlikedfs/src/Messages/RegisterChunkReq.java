package Messages;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;


import Client.PeerData;
import Common.ChunkInfo;
import Common.FileData;

public class RegisterChunkReq implements Serializable{
	private static final long serialVersionUID = 1L;
	private Hashtable<String, ChunkInfo> chunks;
	private Hashtable<String, FileData> files;
	private PeerData peer;

	public RegisterChunkReq(Hashtable<String, ChunkInfo> chunks, PeerData peer, Hashtable<String, FileData> files) {		
		this.peer = peer;
		this.files = files;
		this.chunks = chunks; 
	}

	public Map<String, ChunkInfo> getChunks() {
		return chunks;
	}

	public void setChunks(Hashtable<String, ChunkInfo> chunks) {
		this.chunks = chunks;
	}

	public PeerData getPeer() {
		return peer;
	}

	public void setPeer(PeerData peer) {
		this.peer = peer;
	}

	public Map<String, FileData> getFiles() {
		return files;
	}

	public void setFiles(Hashtable<String, FileData> files) {
		this.files = files;
	}					
}
