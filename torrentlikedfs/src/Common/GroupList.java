package Common;

import java.util.Vector;

import Client.PeerData;
import Client.PeerItem;

public class GroupList {
	private Vector<Group> groupList;

	public GroupList() {
		super();
		groupList = new Vector<Group>();
	}
	
	public Vector<Group> getGroupList(){
		return groupList;
	}
	
	public Group getIndex(int ind){
		return groupList.get(ind);
	}
	
	public void addItem(Group group){
		groupList.add(group);
	}
	
	public void deleteItem(Group group){
		groupList.remove(group);
	}
	
	public void deleteItem(PeerData peerData){
		for (int i=0; i<groupList.size(); i++){				
			if (groupList.get(i).getPeerData().equals(peerData)) 
				groupList.remove(i);
		}
	}
	
	// Each Peer can take part in only one Group. 
	// If there exists a Group with the same PeerData it will return true;
	public boolean containsGroup(Group group){		
		boolean cont = false;
		PeerData peerData = group.getPeerData();				
		for (int i=0; i<groupList.size(); i++){				
			if (groupList.get(i).getPeerData().equals(peerData)) cont = true;
		}
		return cont;
	}		
	
	
	public int getSize(){
		return groupList.size();
	}
	
	public void toStringGroup(){
		for (int i=0; i<groupList.size(); i++){
			for (int j=0; j<groupList.get(i).getFileList().getSize(); j++){
				System.out.print(i+". Peer, File data:");
				groupList.get(i).getFileList().toStringFileDataList();
						//groupList.get(i).getFileList().getItem(j).getName()+", "+
						//groupList.get(i).getFileList().getItem(j).getSize());
			}
		}
	}
}
