package Messages;

import java.io.Serializable;

import Client.PeerData;

//Ez az osztaly a https://code.google.com/p/simpletorrentlikep2p/ cimen talalhato 
//ChunkMessage osztaly alapjan keszult

public abstract class ChunkMessage implements Serializable {	
	private static final long serialVersionUID = 1L;
	private PeerData peer;
	
	public ChunkMessage(PeerData peer)
	{
		this.peer = peer;
	}

	public PeerData getPeerInfo() {
		return peer;
	}
}