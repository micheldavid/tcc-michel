/*
 * Created on 25/11/2004
 */
package appman;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * @author lucasa
 */
public class Debug
{
	private static final boolean print = true;
	private static final File file = new File("appman.log");

	public static void debug(Object str, boolean b) {
		Debug.debug(str, null, b);
	}

	public static void debug(Object str, Throwable th, boolean sysOut) {
		if(sysOut) {
			System.out.println(str);
			if (th != null) th.printStackTrace(System.out);
		}

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fwrite = new FileWriter(file, true);
			PrintWriter output = new PrintWriter(fwrite, true);
			output.write(str + "\n");
			output.close();
			fwrite.close();
		} catch (Exception e) {
			System.out.println("Debug Error on debug to File [" + file + " / " + str + "]: " + e.getMessage());
			e.printStackTrace(System.out);
		}
	}

	public static void debug(Object str) {
		Debug.debug(str, null, print);
	}

	public static void debug(Object str, Throwable th) {		
		Debug.debug(str, th, print);
	}

	public static void newDebugFile(String str, String filepath)
	{
		File file = new File(filepath);
		try
		{
				if( file.exists() )
				{
					file.delete();
				}
				file.createNewFile();
				
				OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file, true));
				output.write(str);
				output.flush();
				output.close();
		} catch (Exception e)
		{
			System.out.println("Debug Error on create new debug File ["+filepath+"]: " + e.getMessage());
		}
	}
	public static void debugToFile(Object str, String filepath, boolean b)
	{		
				File file = new File(filepath);
				try
				{
						if( !file.exists() )
						{
							file.createNewFile();
						}
						OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file, true));
						output.write(str.toString());
						output.flush();
						output.close();
						if(b == true)
						{
							System.out.println(str.toString());
						}
				} catch (Exception e)
				{
					System.out.println("Debug Error on debug to File ["+filepath+"]: " + e.getMessage());
				}
	}	
}
