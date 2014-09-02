package Messages;

import java.io.Serializable;
import java.util.Map;


import Client.PeerData;
import Common.ChunkInfo;

public class RegisterChunkResp extends ChunkMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, ChunkInfo> chunks;	
	
	public RegisterChunkResp(Map<String, ChunkInfo> chunks, PeerData peer) {
		super(peer);
		this.chunks = chunks;
	}

	public Map<String, ChunkInfo> getChunks() {
		return chunks;
	}

	public void setChunks(Map<String, ChunkInfo> chunks) {
		this.chunks = chunks;
	}	
}
