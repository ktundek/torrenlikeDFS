package Exceptions;

// Ez az osztaly a linken elerheto osztaly masolata:
//https://code.google.com/p/simpletorrentlikep2p/source/browse/trunk/src/P2PMessage/src/cnt5106c/UnableToReadFileException.java
public class UnableToReadFileException extends Exception {
	private static final long serialVersionUID = 1L;
	public UnableToReadFileException(String filepath)
	{
		this.filepath = filepath;
	}
	private String filepath = "";
	public String getMessage()
	{
		return "Can't read file : " +  this.filepath;
	}
}
