package Server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import Client.PeerData;
import Client.PeerItem;



public class PeerItemList{
	//private Vector<PeerItem> peerItemList;
	private ArrayList<PeerItem> peerItemList;

	public PeerItemList() {
		super();	
		//peerItemList = new Vector<PeerItem>();
		peerItemList = new ArrayList<PeerItem>();
	}
	

	//public Vector<PeerItem> getPeerDataList() {
	public ArrayList<PeerItem> getPeerDataList() {
		return peerItemList;
	}

	//public void setPeerDataList(Vector<PeerItem> peerItemList) {
	public void setPeerDataList(ArrayList<PeerItem> peerItemList) {
		this.peerItemList = peerItemList;
	}
	
	public void addItem(PeerItem pi){
		peerItemList.add(pi);
	}
	
	public void deleteItem(PeerItem pi){
		peerItemList.remove(pi);
	}
	
	public void deleteItem(int i){
		peerItemList.remove(i);
	}
	
	public boolean contains(PeerItem pi){
		if (peerItemList.contains(pi)) return true;
		else return false;
	}
	
	public boolean containsPeerData(PeerData peerData){
		boolean cont = false;
		for (int i=0; i<peerItemList.size(); i++){
			if (peerItemList.get(i).getPeerData().equals(peerData)) cont = true;
		}
		return cont;
	}
	
	public boolean containsPeerItem(PeerItem pi){
		boolean cont = false;
		PeerData pd = pi.getPeerData();
		PeerData peerData = null;
		PeerItem peerItem = null;
		for (int i=0; i<peerItemList.size(); i++){
			peerData = peerItemList.get(i).getPeerData();
			peerItem = peerItemList.get(i);
			if (peerData.equals(pd) && peerItem.getPort() == pi.getPort())
				cont = true;
		}
		return cont;
	}
	
	public int containsPeerItemIndex(PeerItem pi){
		int ind = -1;
		PeerData pd = pi.getPeerData();
		PeerData peerData = null;
		PeerItem peerItem = null;
		for (int i=0; i<peerItemList.size(); i++){
			peerData = peerItemList.get(i).getPeerData();
			peerItem = peerItemList.get(i);
			if (peerData.equals(pd) && peerItem.getPort() == pi.getPort())
				ind = i;
		}
		return ind;
	}
	
	public int getSize(){
		return peerItemList.size();
	}
	
	public PeerItem getIndex(int i){
		return peerItemList.get(i);
	}
	
	public void toStringList(){
		for (int i=0; i<peerItemList.size(); i++){
			System.out.println(i+". Peer innetAddress: "+peerItemList.get(i).getPeerData().getInetAddress()
					+", local port: "+ peerItemList.get(i).getPeerData().getPort()
					+" and port: "+peerItemList.get(i).getPort());
		}
	}
}
