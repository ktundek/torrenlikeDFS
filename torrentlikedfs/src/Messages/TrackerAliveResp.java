package Messages;

import java.io.Serializable;

public class TrackerAliveResp extends ServerResp implements Serializable{
	private ServerRespMessages msg;
	
	public TrackerAliveResp(ServerRespMessages msg) {
		super(msg);		
		this.msg = msg;
	}
	
	public ServerRespMessages getServerRespMessages(){
		return msg;
	}
}
