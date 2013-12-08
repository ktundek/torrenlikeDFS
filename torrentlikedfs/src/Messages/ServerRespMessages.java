package Messages;

import java.io.Serializable;

public class ServerRespMessages implements Serializable{
	private String msg = "";	
	
	public ServerRespMessages(String msg) {
		super();
		this.msg = msg;
	}	
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
