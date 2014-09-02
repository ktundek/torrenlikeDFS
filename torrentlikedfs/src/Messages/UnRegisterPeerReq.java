package Messages;

import java.io.Serializable;

import Client.PeerItem;



public class UnRegisterPeerReq implements Serializable{
	private static final long serialVersionUID = 1L;
	private PeerItem peerItem;

	public UnRegisterPeerReq(PeerItem peerItem) {
		super();
		this.peerItem = peerItem;
	}

	public PeerItem getPeerItem() {
		return peerItem;
	}

	public void setPeerdata(PeerItem peerItem) {
		this.peerItem = peerItem;
	}	

}
