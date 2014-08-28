package Messages;

import java.io.Serializable;

import javax.swing.table.DefaultTableModel;

import Client.PeerData;



public class GetFilesResp extends ChunkMessage implements Serializable{
	private DefaultTableModel dtm = null;
	
	public GetFilesResp(PeerData peer) {
		super(peer);
		// TODO Auto-generated constructor stub
	}

	public DefaultTableModel getDtm() {
		return dtm;
	}

	public void setDtm(DefaultTableModel dtm) {
		this.dtm = dtm;
	}	

}
