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
	
	public PeerData getItem(int ind){
		return peerList.get(ind);
	}
	
	public boolean contains(PeerData peer){
		boolean ok = false;
		for (int i=0; i<peerList.size(); i++){
			PeerData pd = peerList.get(i);
			if (pd.getInetAddress().equals(peer.getInetAddress()) &&
					pd.getPort()==peer.getPort()) ok = true;
		}
		return ok;
	}
	
	public String getPeerData(int ind){
		PeerData pd = peerList.get(ind);
		return (String) pd.getInetAddress().toString()+ " : " + pd.getPort();
	}
}
