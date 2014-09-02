package Messages;

import java.io.Serializable;

import Client.PeerData;



public class GetFilesReq extends ChunkMessage implements Serializable{
	private static final long serialVersionUID = 1L;

	public GetFilesReq(PeerData peer) {
		super(peer);		
	}

}
