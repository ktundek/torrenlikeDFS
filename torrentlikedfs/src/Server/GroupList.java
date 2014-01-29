package Server;

import java.util.Vector;

import Client.Group;

public class GroupList {
	private Vector<Group> groupList;

	public GroupList() {
		super();
		groupList = new Vector<Group>();
	}
	
	public Vector<Group> getGroupList(){
		return groupList;
	}
	
	public void addItem(Group group){
		groupList.add(group);
	}
	
	public void deleteItem(Group group){
		groupList.remove(group);
	}
	
	public boolean contains(Group group){
		if (groupList.contains(group)) return true;
		else return false;
	}
	
	public boolean containsPeer(Group group){
		boolean cont = false;
		for (int i=0; i<groupList.capacity(); i++){
			if (groupList.get(i).getPeer().equals(group.getPeer())) cont = true;
		}
		return cont;
	}
	
	public int getSize(){
		return groupList.size();
	}
	
	public void toStringGroup(){
		for (int i=0; i<groupList.size(); i++){
			for (int j=0; j<groupList.get(i).getFileList().getSize(); j++){
				System.out.println(i+". Peer, File data:"+ 
						groupList.get(i).getFileList().getItem(j).getName()+", "+
						groupList.get(i).getFileList().getItem(j).getSize());
			}
		}
	}
}
