package Messages;

import java.io.Serializable;

import Common.Group;

public class RegisterGroupReq implements Serializable{
	private static final long serialVersionUID = 1L;
	private Group group;

	public RegisterGroupReq(Group group) {
		super();
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}		
}
