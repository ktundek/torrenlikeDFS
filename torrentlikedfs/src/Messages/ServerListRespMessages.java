package Messages;

import java.io.Serializable;

public class ServerListRespMessages extends ServerRespMessages implements Serializable {
	//private ServerRespMessages msg = null;	
	private String msg = null;
	private Object obj = null;
	
	public ServerListRespMessages(String msg, Object obj) {
		super(null);
		this.msg = msg;
		this.obj = obj;
	}	
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
		
}
