package Messages;

import java.io.Serializable;

public class TrackerAliveResp extends ServerResp implements Serializable{
	private static final long serialVersionUID = 1L;
	private ServerRespMessages msg;
	
	public TrackerAliveResp(ServerRespMessages msg) {
		super(msg);		
		this.msg = msg;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}
}
