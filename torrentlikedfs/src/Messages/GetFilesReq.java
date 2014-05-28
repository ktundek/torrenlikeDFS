package Messages;

import java.io.Serializable;

import Client.PeerData;

public class GetFilesReq extends ChunkMessage implements Serializable{

	public GetFilesReq(PeerData peer) {
		super(peer);
		// TODO Auto-generated constructor stub
	}

}
