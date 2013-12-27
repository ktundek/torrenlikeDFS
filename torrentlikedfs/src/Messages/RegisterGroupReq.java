package Messages;

import Client.Group;

public class RegisterGroupReq {
	private Group groupitem;

	public RegisterGroupReq(Group groupitem) {
		super();
		this.groupitem = groupitem;
	}

	public Group getGroupitem() {
		return groupitem;
	}

	public void setGroupitem(Group groupitem) {
		this.groupitem = groupitem;
	}		
}
