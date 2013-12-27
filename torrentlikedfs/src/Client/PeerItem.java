package Client;

import java.io.Serializable;

public class PeerItem implements Serializable{
	private PeerData peerData;
	private int port;
	
	public PeerItem(PeerData peerData, int port) {
		super();
		this.peerData = peerData;
		this.port = port;
	}

	public PeerData getPeerData() {
		return peerData;
	}

	public void setPeerData(PeerData peerData) {
		this.peerData = peerData;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}	

}
