import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.PatchApplyException;
import org.eclipse.jgit.api.errors.PatchFormatException;

public class Main 
{
	private static File main_dir;
	private static GitHubClient client;
	private static boolean ready = true;
	private static Preferences prefs;
	private static Updater updater;
	private static final String prefs_path = "MySmartBB_UM";
	private static ResourceBundle messages;
	private static boolean rtl = false;
	private static boolean prefsExist = false;
	
	public static void main( String[] args ) throws BackingStoreException 
	{		
		client = new GitHubClient( "MaaSTaaR", "MySmartBB" );
		updater = new Updater( client );
		
		if ( Preferences.userRoot().nodeExists( prefs_path ) )
		{
			prefs = Preferences.userRoot().node( prefs_path );
			
			Locale currLocale = null;
			String language = prefs.get( "language", "en" );
			
			System.out.println( "Language : " + language );
			
			if ( language.equals( "en" ) )
			{
				currLocale = new Locale( "en", "US" );
			}
			else
			{
				currLocale = new Locale( "ar", "KW" );
				rtl = true;
			}
			
			messages = ResourceBundle.getBundle( "languages/messages", currLocale );
			
			prefsExist = true;
		}
		else
		{
			prefs = Preferences.userRoot().node( prefs_path );
			
			prefs.flush();
		}
		
		MainWindow.show();
		
		checkIfSystemReady();
	}
	
	public static void checkIfSystemReady() throws BackingStoreException
	{
		if ( prefsExist )
		{	
			setMainDir();
			
			try {
				checkMainDir();
			} catch (IOException e) { e.printStackTrace(); }
		}
		else
		{
			ready = false;
			MainWindow.setStatus( getMessage( "please_set_required_information" ) );
		}
		
		if ( !ready )
		{
			MainWindow.disableAllWidgetsButSetting();
		}
		else
		{
			MainWindow.enableAllWidgets();
			MainWindow.setStatus( getMessage( "ready" ) );
		}
	}
	
	private static void checkMainDir() throws IOException 
	{
		if ( !main_dir.isDirectory() )
		{
			ready = false;
			MainWindow.setStatus( getMessage( "choose_local_mysmartbb_dir" ) );
		}
		else
		{	
			File local_commit_sha_file = new File( main_dir.getAbsolutePath() + "/commit.txt" );
			
			if ( !local_commit_sha_file.isFile() )
			{
				ready = false;
				MainWindow.setStatus( getMessage( "check_commit_file" ) );
			}
			else
			{
				BufferedReader buffer = new BufferedReader( new FileReader( main_dir.getAbsolutePath() + "/commit.txt" ) );
				
				String commit_sha = buffer.readLine();
				
				buffer.close();
				
				if ( commit_sha.isEmpty() )
				{
					ready = false;
					MainWindow.setStatus( getMessage( "commit_file_empty" ) );
				}
				else
				{
					ready = true;
					client.setBaseCommit( commit_sha );
				}
			}
		}
	}
	
	private static void setMainDir()
	{
		String main_dir_path = prefs.get( "main_dir", "" );
		
		System.out.println( "Main Dir : " + main_dir_path );
		
		main_dir = new File( main_dir_path );
	}
	
	public static Preferences getPreferences()
	{
		return prefs;
	}
	
	public static GitHubClient getClient()
	{
		return client;
	}
	
	public static String getMainDir()
	{
		return main_dir.getAbsolutePath() + "/";
	}
	
	public static Updater getUpdater()
	{
		return updater;
	}
	
	public static String getMessage( String key )
	{
		return messages.getString( key );
	}
	
	public static boolean isRTL()
	{
		return rtl;
	}
}
