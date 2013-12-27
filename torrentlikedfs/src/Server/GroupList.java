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
}
