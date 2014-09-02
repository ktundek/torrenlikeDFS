package Messages;

import java.io.Serializable;

import Client.PeerData;


public class RegisterPeerReq implements Serializable {
	private static final long serialVersionUID = 1L;
	private PeerData peerData;

	public RegisterPeerReq(PeerData peerData) {	
		this.peerData = peerData;
	}

	public PeerData getPeerData() {
		return peerData;
	}

	public void setPeerItem(PeerData peerData) {
		this.peerData = peerData;
	} 
		
}
