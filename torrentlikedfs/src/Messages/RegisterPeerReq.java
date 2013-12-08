package Messages;

import java.io.Serializable;

import Client.PeerData;

public class RegisterPeerReq implements Serializable {
	private PeerData peerData;

	public RegisterPeerReq(PeerData peerData) {	
		this.peerData = peerData;
	}

	public PeerData getPeerData() {
		return peerData;
	}

	public void setPeerData(PeerData peerData) {
		this.peerData = peerData;
	} 
		
}
