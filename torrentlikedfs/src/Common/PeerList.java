package Common;

import java.io.Serializable;
import java.util.Vector;

import Client.PeerData;

public class PeerList implements Serializable{
	private Vector<PeerData> peerList;

	public PeerList() {
		super();
		peerList = new Vector<PeerData>();
	}
	
	public void addItem(PeerData pd){
		peerList.add(pd);
	}
	
	public void removeItem(PeerData pd){
		peerList.remove(pd);
	}
	
	public int size(){
		return peerList.size();
	}
	
	public String getPeerData(int ind){
		PeerData pd = peerList.get(ind);
		return (String) pd.getInetAddress().toString()+ " : " + pd.getPort();
	}
}
