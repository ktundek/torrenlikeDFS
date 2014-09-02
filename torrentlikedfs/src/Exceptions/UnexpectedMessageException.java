package Exceptions;

public class UnexpectedMessageException extends Exception{
	private static final long serialVersionUID = 1L;
	private String expectedMessageType;

	public UnexpectedMessageException(String expectedMessageType) {
		super();
		this.expectedMessageType = expectedMessageType;
		unexpectedMsgToString();
	}
	
	private void unexpectedMsgToString(){
		System.out.println("EX: expected message type is: "+ expectedMessageType);	
	}
	
}
