package Messages;

import java.io.Serializable;

import Common.FileDataListClient;

public abstract class ServerListResp implements Serializable{
	private ServerRespMessages msg;
	private Object obj;

	public ServerListResp(ServerRespMessages msg, Object obj) {
		super();
		this.msg = msg;
		this.obj = obj;
	}

	public ServerRespMessages getMsg() {
		return msg;
	}

	public void setMsg(ServerRespMessages msg) {
		this.msg = msg;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}	
}
