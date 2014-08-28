package Messages;

import java.io.Serializable;

import Client.PeerData;
import Client.PeerItem;



public class RegisterPeerReq implements Serializable {
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
