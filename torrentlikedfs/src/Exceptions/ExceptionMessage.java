package Exceptions;

import javax.swing.JOptionPane;

public class ExceptionMessage {

	public ExceptionMessage(){}
	
	public static void messageBox(String infoMessage) { 
		JOptionPane.showMessageDialog(null, infoMessage, "Error",JOptionPane.ERROR_MESSAGE); 
	}
}
