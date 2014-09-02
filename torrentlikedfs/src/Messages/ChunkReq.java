package Messages;

import java.io.Serializable;


import Client.PeerData;
import Common.FileData;

//Ez az osztaly a https://code.google.com/p/simpletorrentlikep2p/ cimen talalhato 
//ChunkReq osztaly alapjan keszult

public class ChunkReq extends ChunkMessage implements Serializable{	
	private static final long serialVersionUID = 1L;
	private FileData fd;
	private int chunkNr;
	
	public ChunkReq(PeerData peer) {
		super(peer);		
	}

	public FileData getFd() {
		return fd;
	}

	public void setFd(FileData fd) {
		this.fd = fd;
	}

	public int getChunkNr() {
		return chunkNr;
	}

	public void setChunkNr(int chunkNr) {
		this.chunkNr = chunkNr;
	}	
}
