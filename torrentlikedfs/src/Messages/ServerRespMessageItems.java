package Messages;

public class ServerRespMessageItems {
	public final static ServerRespMessages ACK = new ServerRespMessages("OK");
	public final static ServerRespMessages NACK_REG_PEER = new ServerRespMessages("Peer already registered!");
}
