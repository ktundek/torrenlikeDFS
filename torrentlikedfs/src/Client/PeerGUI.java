package Client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import Common.FileData;
import Exceptions.ExceptionMessage;

public class PeerGUI{
	private Peer peer;
	private JFrame frame;
	private JPanel panel_up, panel_down;
	private JLabel label_p, label_s; 
	private JTable table_p, table_s;
	private JButton button_down, button_up;
	private JFileChooser chooser;
	
	public PeerGUI(){	
		createDialog();
	}
	
	public void createDialog(){
		frame = new JFrame();
		label_p = new JLabel();
		label_s = new JLabel();
		panel_up = new JPanel();
		panel_down = new JPanel();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());				
		
		frame.add(panel_up, BorderLayout.NORTH);
		frame.add(panel_down, BorderLayout.CENTER);
		
		panel_up.setLayout(new BorderLayout());
		panel_down.setLayout(new BorderLayout());
		
		// upper part
		label_p.setText("My files:");
		panel_up.add(label_p, BorderLayout.NORTH);
				
		DefaultTableModel dtm = new DefaultTableModel(); 		
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("File");
		columnNames.add("Size in bytes");
		columnNames.add("%");				
		dtm.setColumnIdentifiers(columnNames);		
				
		table_p = new JTable(dtm);
		table_p.setSize(190, 100);
		JScrollPane sp = new JScrollPane(table_p);
		sp.setPreferredSize(new Dimension(300, 200));
		panel_up.add(sp, BorderLayout.CENTER);
				
		button_up = new JButton("Upload new file");
		button_up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooser = new JFileChooser();
				chooser.setDialogTitle("Choose a file");
				int returnVal = chooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION){					
					 peer.copyFile(chooser.getSelectedFile(), chooser.getSelectedFile().getName());
				}
			}
		});
		
		panel_up.add(button_up, BorderLayout.SOUTH);
		
		// lower part
		label_s.setText("Available files on server:");
		panel_down.add(label_s, BorderLayout.NORTH);
		
		DefaultTableModel dtm_s = new DefaultTableModel(); 		
		Vector<String> columnNames_s = new Vector<String>();
		columnNames_s.add("File");
		columnNames_s.add("Size in bytes");
		columnNames_s.add("Crc");				
		dtm_s.setColumnIdentifiers(columnNames_s);
		
		table_s = new JTable(dtm_s);
		panel_down.add(new JScrollPane(table_s), BorderLayout.CENTER);
		
		button_down = new JButton("Download");
		button_down.setSize(new Dimension(50, 5));		
		button_down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!(table_s.getSelectedRows().length==0)){
					int selectedRowIndex = table_s.getSelectedRow();					
					String fName = table_s.getModel().getValueAt(selectedRowIndex, 0).toString();
					long fSize = Long.parseLong(table_s.getModel().getValueAt(selectedRowIndex, 1).toString());
					String fCrc = table_s.getModel().getValueAt(selectedRowIndex, 2).toString();
					
					FileData fd = new FileData();
					fd.setName(fName);
					fd.setSize(fSize);
					fd.setCrc(fCrc);
					peer.sendChunkListRequest(fd);
				}
				else { // notify the client that he/she has to select a file
					ExceptionMessage.infoBox("Select a file!");					
				}
			}});
		panel_down.add(button_down, BorderLayout.SOUTH);


		frame.setVisible(true);
		frame.setTitle("Peer");
		frame.setSize(500,500);
		frame.setLocation(50,100);
	}
		
	public void setPeer(Peer peer){
		this.peer = peer;
	}
	
	public void builPeerTable(){		
		DefaultTableModel dtm = peer.buildTable();		
		Vector<Vector<Object>> data = dtm.getDataVector();
		Iterator it = data.iterator();
		while(it.hasNext()){
			Vector<Object> row = (Vector<Object>) it.next();			
			peerTableRows(row);
		}		
	}

	public void buildServerTable(DefaultTableModel dtm){		
		Vector<Vector<Object>> data = dtm.getDataVector();
		Iterator it = data.iterator();
		while(it.hasNext()){
			Vector<Object> row = (Vector<Object>) it.next();			
			serverTableRows(row);
		}
	}
	
	// insert a new row or update an existing one
	public void peerTableRows(Vector<Object> rowData){
		String fileName = rowData.get(0).toString();		
		String value = rowData.get(2).toString();		
		boolean find = false;
		DefaultTableModel dtm = (DefaultTableModel) table_p.getModel();
		int lenght = dtm.getRowCount();
		for (int i=0;i<lenght;i++){
			if (dtm.getValueAt(i, 0).equals(fileName)){ // update
				dtm.setValueAt(value, i, 2);
				find = true;
			}				
		}
		if (!find){ // insert
			dtm.addRow(rowData);
		}
	}
	
	// add a new line to server's table
	public void serverTableRows(Vector<Object> rowData){
		DefaultTableModel dtm = (DefaultTableModel) table_s.getModel();
		dtm.addRow(rowData);
	}
	
	
	public static void main(String argv[]){
		PeerGUI gui = new PeerGUI();
		Peer peer = new Peer(gui);
		gui.setPeer(peer);
		gui.builPeerTable();		
	}

}
