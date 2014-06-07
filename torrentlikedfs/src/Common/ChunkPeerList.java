package Common;

import java.io.Serializable;

public class ChunkPeerList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ChunkName;
	private PeerList peerList;
	
	public ChunkPeerList(String chunkName, PeerList peerList) {
		super();
		ChunkName = chunkName;
		this.peerList = peerList;
	}
	
	public String getChunkName() {
		return ChunkName;
	}
	public void setChunkName(String chunkName) {
		ChunkName = chunkName;
	}
	public PeerList getPeerList() {
		return peerList;
	}
	public void setPeerList(PeerList peerList) {
		this.peerList = peerList;
	}
	
	
}
