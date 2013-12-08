package Client;

import java.util.Vector;

public class PeerDataList {
	private Vector<PeerData> peerDataList;

	public PeerDataList() {
		super();	
		peerDataList = new Vector<PeerData>();
	}
	

	public Vector<PeerData> getPeerDataList() {
		return peerDataList;
	}

	public void setPeerDataList(Vector<PeerData> peerDataList) {
		this.peerDataList = peerDataList;
	}
	
	public void addItem(PeerData pd){
		peerDataList.add(pd);
	}
	
	public void deleteItem(PeerData pd){
		peerDataList.remove(pd);		
	}
	
	public boolean contains(PeerData pd){
		if (peerDataList.contains(pd)) return true;
		else return false;
	}
}
