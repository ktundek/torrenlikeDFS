package Client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import Common.FileData;

public class PeerGUI{
	private Peer peer;
	private JFrame frame;
	private JPanel panel_up, panel_down;
	private JLabel label_p, label_s; 
	private JTable table_p, table_s;
	private JButton button;
	
	public PeerGUI(Peer peer){
		this.peer = peer;
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
		table_p = new JTable((TableModel) peer.buildTable());
		table_p.setSize(190, 100);
		JScrollPane sp = new JScrollPane(table_p);
		sp.setPreferredSize(new Dimension(300, 200));
		panel_up.add(sp, BorderLayout.CENTER);
		
		// lower part
		label_s.setText("Available files on server:");
		panel_down.add(label_s, BorderLayout.NORTH);		
		button = new JButton("Download");
		button.setSize(new Dimension(50, 5));		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!(table_s.getSelectedRows().length==0)){
					int selectedRowIndex = table_s.getSelectedRow();
					//int selectedColumnIndex = table_s.getSelectedColumn();
					String fName = table_s.getModel().getValueAt(selectedRowIndex, 0).toString();
					long fSize = Long.parseLong(table_s.getModel().getValueAt(selectedRowIndex, 1).toString());
					String fCrc = table_s.getModel().getValueAt(selectedRowIndex, 2).toString();
					
					FileData fd = new FileData();
					fd.setName(fName);
					fd.setSize(fSize);
					fd.setCrc(fCrc);
					peer.sendChunkListRequest(fd);
				}
				else {
					// TODO
					// ertesiteni kell a peert, h valasszon ki egy sort
				}
			}});
		panel_down.add(button, BorderLayout.SOUTH);


		frame.setVisible(true);
		frame.setTitle("Peer");
		frame.setSize(500,500);
		frame.setLocation(50,100);
	}
		
	
	public void buildServerTable(DefaultTableModel dtm){
		createDialog();
		table_s = new JTable(dtm);
		panel_down.add(new JScrollPane(table_s), BorderLayout.CENTER);
		
		//table_s.repaint();		
	}
	
	// insert a new row or update an existing one
	public void peerTableRows(Vector<Object> rowData){
		String fileName = rowData.get(0).toString();
		//String fileSize = "";
		String value = rowData.get(1).toString();
		System.out.println("PEER GUI: peerTableRows: file:"+fileName+", update value:"+value);
		boolean find = false;
		DefaultTableModel dtm = (DefaultTableModel) table_p.getModel();
		int lenght = dtm.getRowCount();
		for (int i=0;i<lenght;i++){
			if (dtm.getValueAt(i, 0).equals(fileName)){ // update
				dtm.setValueAt(value, i, 1);
				find = true;
			}				
		}
		if (!find){ // insert
			dtm.addRow(rowData);
		}
	}
	
	
	public static void main(String argv[]){
		Peer peer = new Peer();
		PeerGUI gui = new PeerGUI(peer);
		peer.setGUI(gui);
		//gui.createDialog();
	}

}
