package Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import Exceptions.UnableToReadFileException;
import Logger.Logging;

public class FileData implements Serializable,Constants{
	private String name;
	private long size; //in bytes
	private String path;	
	private String crc;
	
	public FileData(){
		
	}
	
	public FileData(File file) {
		try {
			if (!file.canRead())			
				throw new UnableToReadFileException(file.getCanonicalPath());			
			name = file.getName();			
			size = file.length();		
			path = file.getCanonicalPath();
			//hash = createChecksum(path);
			crc = createChecksum(path);
		} catch (UnableToReadFileException | IOException e) {			
			Logging.write(this.getClass().getName(), "FileData", "Unable to read file: "+file.getName() + ", "+e.getMessage());			
		}
	}
	
	// Calculate checksum for a file
	public String createChecksum(String filePath) throws UnsupportedEncodingException{
		FileInputStream fis = null;
		byte[] mdBytes = null;		
		
		try {
			fis = new FileInputStream(filePath);
			MessageDigest md = MessageDigest.getInstance("MD5");	    
		    byte[] dataBytes = new byte[1024]; // buffer			    
		    int read = 0; 		 	
		    
		    while ((read = fis.read(dataBytes)) != -1) {		    	
		    	md.update(dataBytes, 0, read);
		    }
		    fis.close();		    
		    mdBytes = md.digest();	
		    
		} catch (FileNotFoundException e) {
			Logging.write(this.getClass().getName(),"createChecksum", e.getMessage());		
		} catch (NoSuchAlgorithmException e) {
			Logging.write(this.getClass().getName(),"createChecksum", "NO SUCH ALOGORITHM! "+e.getMessage());			
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		String res="";
		for (int i = 0; i < mdBytes.length; i++) {			
			res+=Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1);
	    }			 		
			
		return res;
	}
	
	public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers            
            append(name).
            append(size).
            append(crc).
            toHashCode();
    }
	
	public boolean equals(Object obj){
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof FileData))
            return false;

        FileData fd = (FileData) obj;
        return new EqualsBuilder().            
            append(name, fd.name).
            append(size, fd.size).
            append(crc, fd.crc).
            isEquals();					
	}
	
	public int getChunkNumber(){
		if (this.getSize() <= CHUNK_SIZE) return 1;
		else
			if (this.getSize() % CHUNK_SIZE == 0) return (int) ((this.getSize()/CHUNK_SIZE));
			else return (int) ((this.getSize()/CHUNK_SIZE)+1);
			 
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

	public String getCrc() {
		return crc;
	}

	public void setCrc(String crc) {
		this.crc = crc;
	}	
		
}
