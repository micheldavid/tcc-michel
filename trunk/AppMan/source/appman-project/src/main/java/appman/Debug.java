/*
 * Created on 25/11/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package appman;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @author lucasa
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Debug
{
	private static boolean print = true;
	
	public static void debug(Object str, boolean b)
	{
		if(b == true)
		{
			System.out.println(str.toString());
		}

		String filepath = "appman.log";
		File file = new File(filepath);
		try
		{
				if( !file.exists() )
				{
					file.createNewFile();
				}
				OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file, true));
				output.write("\n"+str.toString());
				output.flush();
				output.close();
		} catch (Exception e)
		{
			System.out.println("Debug Error on debug to File ["+filepath+"]: " + e.getMessage());
		}
	}
	public static void debug(Object str)
	{		
		Debug.debug(str, print);
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
