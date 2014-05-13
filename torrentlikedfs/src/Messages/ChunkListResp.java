package Messages;

import java.io.Serializable;
import java.util.Map;

import Client.PeerData;
import Common.PeerList;

public class ChunkListResp extends ChunkMessage implements Serializable{
	protected Map<String, PeerList> chunkList = null;
	
	public ChunkListResp(PeerData peer, Map<String, PeerList> chunkList) {
		super(peer);
		this.chunkList = chunkList;
	}

	public Map<String, PeerList> getChunkList() {
		return chunkList;
	}

	public void setChunkList(Map<String, PeerList> chunkList) {
		this.chunkList = chunkList;
	}		

}
