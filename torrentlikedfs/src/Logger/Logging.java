package Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logging {
	private static String filePath = "";

	public Logging(){
		createFolder();
	}
	
	public static void createFolder(){		
		String os = System.getProperty("os.name").toLowerCase();		
		String dir = "";
		if (os.contains("windows")){
			dir = "C:"+ File.separator+"Logs"+File.separator;			
		}
		if (os.contains("linux")){
			dir = System.getProperty("user.home") + File.separator+"Logs" + File.separator;		
		}
		
		File directory = new File(dir);
		if (!directory.exists())
		{			
			directory.mkdirs(); //create directory
		}
		
		
		
		String curDate = (getDateTime())[0].replace("/", "_");	// date	
		
		filePath = dir+ "logs" + curDate+".txt";
		File file = new File(filePath);
		if (!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void write(String myClass, String method, String msg){
		createFolder();
		PrintWriter writer;
		try {			
			writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
			writer.println(getDateTime()[1]+" "+myClass+" "+method+": "+msg);			
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();				
		}		
	}
	
	private static String[] getDateTime(){
		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		Date dateobj = new Date();
		String time = df.format(dateobj);
		String[] date = time.split(" ");
		return date;
	}
	
	/*public static String getMethodName(Object obj)
	{
		final int depth = 0;
		final StackTraceElement[] ste = obj. Thread.currentThread().getStackTrace();

		//System. out.println(ste[ste.length-depth].getClassName()+"#"+ste[ste.length-depth].getMethodName());
		// return ste[ste.length - depth].getMethodName();  //Wrong, fails for depth = 0
		return ste[ste.length - 1 - depth].getMethodName(); //Thank you Tom Tresansky
	}*/
}
