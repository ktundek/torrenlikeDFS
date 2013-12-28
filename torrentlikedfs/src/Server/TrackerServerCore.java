package Server;

import Client.Group;
import Client.PeerData;
import Client.PeerItem;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.UnRegisterPeerReq;

public class TrackerServerCore {
	private PeerItemList peerIList = null;
	private GroupList groupList = null;
	
	public TrackerServerCore() {
		peerIList = new PeerItemList();
		groupList = new GroupList();
	}

	public synchronized ServerRespMessages registerPeer(RegisterPeerReq rpr, int port){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		PeerData peerData = rpr.getPeerData();
		PeerItem peerItem = new PeerItem(peerData, port);
		
		//if (peerIList.contains(peerItem) || peerIList.containsPeerData(peerItem.getPeerData())){
		if (peerIList.contains(peerItem)){
			msg = ServerRespMessageItems.NACK_REG_PEER;
		}
		else{
			peerIList.addItem(peerItem);
			peerIList.toStringList();
		}	
		return msg;
	}
	
	public synchronized void unregisterPeer(PeerItem peerItem){				
		//System.out.println("PEER ITEM to DELETE:" + peerItem.getPort()+", "+ peerItem.getPeerData().getInetAddress());//+", "+peerItem.getPeerData().getPort());
		//System.out.println("MEMBERS in the LIST:");
		peerIList.toStringList();
		for (int i=0; i<peerIList.getSize(); i++){
			if (peerIList.getIndex(i).getPeerData().getSerialversionuid()==peerItem.getPeerData().getSerialversionuid()
					&& peerIList.getIndex(i).getPort()==peerItem.getPort()){
				peerIList.deleteItem(i);
				System.out.println("USER WAS DELETED!");
			}
		}
		/*if (peerIList.contains(peerItem)){
			peerIList.deleteItem(peerItem);
			// if user was part of a group
			peerIList.toStringList();
		}else System.out.println("User wasn't registerd!");*/
	}
	
	public synchronized ServerRespMessages registerGroup(RegisterGroupReq rgr){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		Group group = rgr.getGroupitem();
		if (groupList.contains(group)){
			msg = ServerRespMessageItems.NACK_REG_GROUP;
		}		
		else{	
			groupList.addItem(group);
		}
		return msg;
	}
	
	public int getNrRegisteredPeer(){		
		return peerIList.getSize();		
	}
}
