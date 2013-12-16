package Server;

import Client.PeerData;
import Client.PeerDataList;
import Messages.RegisterPeerReq;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.UnRegisterPeerReq;

public class TrackerServerCore {
	private PeerDataList peerdlist = null;
	
	public TrackerServerCore() {
		peerdlist = new PeerDataList();
	}

	public synchronized ServerRespMessages registerPeer(RegisterPeerReq rpr){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		PeerData peerData = rpr.getPeerData();
		
		if (peerdlist.contains(peerData)){
			msg = ServerRespMessageItems.NACK_REG_PEER;
		}
		else{
			peerdlist.addItem(peerData);
		}	
		return msg;
	}
	
	public synchronized void unregisterPeer(UnRegisterPeerReq unrpr){		
		PeerData peerData = unrpr.getPeerdata();
		if (peerdlist.contains(peerData)){
			peerdlist.deleteItem(peerData);
		}else System.out.println("User wasn't registerd!");
	}
}
