package Exceptions;

import javax.swing.JOptionPane;

public class ExceptionMessage {

	public ExceptionMessage(){}
	
	// for errors
	public static void messageBox(String infoMessage) { 
		JOptionPane.showMessageDialog(null, infoMessage, "Error",JOptionPane.ERROR_MESSAGE); 
	}
	
	public static void infoBox(String infoMessage) { 
		JOptionPane.showMessageDialog(null, infoMessage); 
	}
	
	public static int optionBox(String infoMesssage){
		return JOptionPane.showConfirmDialog(null, infoMesssage, "Choose", JOptionPane.YES_NO_OPTION);
	}
}
