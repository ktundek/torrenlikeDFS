package Messages;

import java.io.Serializable;

import Client.PeerData;



public class RegisterPeerChunk implements Serializable{
	private PeerData peer;
	private String chunkName;
	private int chunkNr;
	
	public RegisterPeerChunk(PeerData peer, String chunkName, int chunkNr) {
		super();
		this.peer = peer;
		this.chunkName = chunkName;
		this.chunkNr = chunkNr;
	}

	public PeerData getPeer() {
		return peer;
	}

	public void setPeer(PeerData peer) {
		this.peer = peer;
	}

	public String getChunkName() {
		return chunkName;
	}

	public void setChunkName(String chunkName) {
		this.chunkName = chunkName;
	}

	public int getChunkNr() {
		return chunkNr;
	}

	public void setChunkNr(int chunkNr) {
		this.chunkNr = chunkNr;
	}	
}
