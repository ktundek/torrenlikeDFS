package Messages;

import java.io.Serializable;

import javax.swing.table.DefaultTableModel;

import Client.PeerData;



public class GetFilesResp extends ChunkMessage implements Serializable{	
	private static final long serialVersionUID = 1L;
	private DefaultTableModel dtm = null;
	
	public GetFilesResp(PeerData peer) {
		super(peer);		
	}

	public DefaultTableModel getDtm() {
		return dtm;
	}

	public void setDtm(DefaultTableModel dtm) {
		this.dtm = dtm;
	}	

}
