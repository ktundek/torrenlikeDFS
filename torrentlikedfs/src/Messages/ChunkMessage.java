package Messages;

import java.io.Serializable;

import Client.PeerData;



public abstract class ChunkMessage implements Serializable {
	private PeerData peer;
	
	public ChunkMessage(PeerData peer)
	{
		this.peer = peer;
	}

	public PeerData getPeerInfo() {
		return peer;
	}
}