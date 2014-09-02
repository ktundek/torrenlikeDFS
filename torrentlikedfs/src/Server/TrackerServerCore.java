package Server;

import Client.PeerData;
import Client.PeerItem;
import Common.FileData;
import Common.FileDataListClient;
import Common.FileDataListServer;
import Common.Group;
import Common.GroupList;
import Logger.Logging;
import Messages.RegisterGroupReq;
import Messages.RegisterGroupResp;
import Messages.RegisterPeerReq;
import Messages.ServerListRespMessages;
import Messages.ServerRespMessageItems;
import Messages.ServerRespMessages;
import Messages.UnRegisterPeerReq;

public class TrackerServerCore {
	private PeerItemList peerIList = null;
	private GroupList groupList = null;
	private FileDataListServer fileList = new FileDataListServer();  // list of files on the server;
	
	public TrackerServerCore() {
		peerIList = new PeerItemList();
		groupList = new GroupList();
	}

	public synchronized ServerRespMessages registerPeer(RegisterPeerReq rpr, int port){
		ServerRespMessages msg = ServerRespMessageItems.ACK;
		PeerData peerData = rpr.getPeerData();
		PeerItem peerItem = new PeerItem(peerData, port);
						
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
		int index = peerIList.containsPeerItemIndex(peerItem);
		if (index!= -1) {
			peerIList.deleteItem(index);
			groupList.deleteItem(peerItem.getPeerData());
			peerIList.toStringList();
			groupList.toStringGroup();
			Logging.write(this.getClass().getName(), "unregisterPeer", "Deleted client: "+peerItem.getPeerData().getInetAddress());
			
		}
		else Logging.write(this.getClass().getName(), "unregisterPeer", 
				"Can not find the Peer ("+peerItem.getPeerData().getInetAddress()+") in the list!"); 				
	}
	
	public synchronized ServerListRespMessages registerGroup(RegisterGroupReq rgr){				
		ServerListRespMessages msg= new ServerListRespMessages("", null);
		msg.setMsg("OK");		
		Group group = rgr.getGroup();
		FileDataListClient clientFileList = new FileDataListClient();		
		
		if (groupList.containsGroup(group)){   
			msg.setMsg("NACK"); // the group already exists
		}		
		else{	
			groupList.addItem(group);
			groupList.toStringGroup();						
			clientFileList = fileList.getNotIncludedFileList(group.getFileList());															
			
			msg.setObj(clientFileList);			
				
		}			
		return msg;
	}	
	
	public int getNrRegisteredPeer(){		
		return peerIList.getSize();		
	}
	
	public synchronized void addFile(FileData filedata){
		fileList.addItem(filedata);		
	}
	
	public synchronized void addFileList(FileDataListClient fdl){
		fileList.addFileList(fdl);
	}
	
	public synchronized FileDataListClient getNonExistetnFiles(Group group){
		FileDataListClient fdl = null;		
		return fdl;
	}
}
