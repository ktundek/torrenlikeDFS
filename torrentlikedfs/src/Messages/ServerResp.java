package Messages;

import java.io.Serializable;

public abstract class ServerResp implements Serializable {	
	private static final long serialVersionUID = 1L;
	private ServerRespMessages msg;

	public ServerResp(ServerRespMessages msg) {
		super();
		this.msg = msg;
	}

	public ServerRespMessages getMsg() {
		return msg;
	}

	public void setMsg(ServerRespMessages msg) {
		this.msg = msg;
	}
		
}
