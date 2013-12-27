package Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import Exceptions.UnableToReadFileException;

public class FileData {
	private String name;
	private long size;
	private String path;
	private byte[] hash;
	
	public FileData(File file) throws IOException, UnableToReadFileException{
		if (!file.canRead())
			throw new UnableToReadFileException(file.getCanonicalPath());
		name = file.getName();			
		size = file.length();		
		path = file.getCanonicalPath();
		hash = createChecksum(path);
	}
	
	// Calculate checksum for a file
	public byte[] createChecksum(String filePath){
		FileInputStream fis = null;
		byte[] mdBytes = null;
		
		try {
			fis = new FileInputStream(filePath);
			MessageDigest md = MessageDigest.getInstance("MD5");	    
		    byte[] dataBytes = new byte[1024]; // buffer			    
		    int read = 0; 
		 
		    while (read != -1) {
		    	read = fis.read(dataBytes);
		    	md.update(dataBytes, 0, read);
		    }
		    fis.close();
		    mdBytes = md.digest();		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NO SUCH ALOGORITHM!");
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		return mdBytes;
	}
	
	public boolean equals(Object obj){
		boolean eq = false;
		if ((obj !=null) && (obj instanceof FileData)){
			FileData fd = (FileData) obj;
			if ((this.name.equals(fd.name)) && (this.size==fd.size)){
				eq = true;
			}
		}
		return eq;	
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}
		
}
