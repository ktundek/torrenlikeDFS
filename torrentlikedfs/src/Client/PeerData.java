package Client;

import java.io.Serializable;
import java.net.*;


public class PeerData implements Serializable{
	private int port;
	private InetAddress inetAddress;	

	public PeerData(int port, InetAddress inetAddress) {
		super();
		this.port = port;
		this.inetAddress = inetAddress;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}
	
	public boolean equals(Object object){
		PeerData pd = (PeerData) object;
		
		if (this==object) return true;
		if (!(object instanceof PeerData))
			return false;
		if (this.getInetAddress().equals(pd) && this.port == pd.port)
			return true;
		else
			return false;
	}
}
