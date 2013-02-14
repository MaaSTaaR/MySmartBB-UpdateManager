import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.eclipse.jgit.api.ApplyResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.PatchApplyException;
import org.eclipse.jgit.api.errors.PatchFormatException;

public class Updater 
{
	private GitHubClient client;
	private List<File> updated_files;
	private boolean ready_to_upload = false;
	private List<File> failed_files;
	private List<File> downloaded_files;
	
	Updater( GitHubClient client )
	{
		this.client = client;
		this.downloaded_files = new ArrayList<File>();
	}
	
	public boolean updateLocalVersion() throws IOException
	{
		if ( client.updateExists() )
		{
			String main_dir = Main.getMainDir();
			URL patch_url = new URL( client.getPatchURL() );
			
			System.out.println( client.getPatchURL() );
			
			MySmartApply app = new MySmartApply( main_dir );
			
			app.setPatch( patch_url.openStream() );
			
			ApplyResult result = null;
			
			try {
				result = app.call();
			} 
			catch (PatchFormatException e) { e.printStackTrace(); } 
			catch (PatchApplyException e) { e.printStackTrace(); } 
			catch (GitAPIException e) { e.printStackTrace(); }
			finally { }
			
			BufferedWriter buffer = new BufferedWriter( new FileWriter( main_dir + "commit.txt" ) );
			
			buffer.write( client.getLatestCommitSHA() );
			
			buffer.close();
			
			updated_files = result.getUpdatedFiles();
			failed_files = app.getFailedFiles();
			
			System.out.println( "Failed Files Size : " + failed_files.isEmpty() );
			
			// The applying process failed for some files. 
			// We need to download a complete copy of these files and change the current with the new one. 
			if ( !failed_files.isEmpty() )
			{
				Iterator<File> it = failed_files.iterator();
				
				while ( it.hasNext() )
				{
					File curr_file = it.next();
					String curr_filename = curr_file.getName();
					
					if ( curr_filename.endsWith( ".png" ) || curr_filename.endsWith( ".jpg" ) || 
							curr_filename.endsWith( ".jpeg" ) || curr_filename.endsWith( ".gif" ) )
					{
						String format = null;
						
						if ( curr_filename.endsWith( ".png" ) )
							format = new String( "png" );
						else if ( curr_filename.endsWith( ".jpg" ) || curr_filename.endsWith( ".jpeg" ) )
							format = new String( "jpg" );
						else if ( curr_filename.endsWith( ".gif" ) )
							format = new String( "gif" );
						
						BufferedImage new_content = client.getImageRaw( curr_filename );
						
						if ( new_content == null )
							continue;
						
						ImageIO.write( new_content, format, curr_file );
					}
					else
					{
						String new_content = client.getFileRaw( curr_filename );
						
						if ( new_content == null )
							continue;
						
						BufferedWriter writer = new BufferedWriter( new FileWriter( curr_file ) );
						
						writer.write( new_content );
						
						writer.close();
					}
					
					
					downloaded_files.add( curr_file );
				}
			}
			
			ready_to_upload = true;
		}
		
		return true;
	}
	
	public void uploadUpdatedFiles()
	{
		// ...
	}
	
	public List<File> getUpdatedFiles()
	{
		return updated_files;
	}
	
	public String[] getUpdatedFilesArray()
	{
		String[] files = new String[ updated_files.size() ];
		int k = 0;
		
		for ( File file : updated_files )
			files[ k++ ] = file.getAbsolutePath();
		
		return files;
	}
	
	public List<File> getDownloadedFiles()
	{
		return downloaded_files;
	}
	
	public String[] getDownloadedFilesArray()
	{
		String[] files = new String[ downloaded_files.size() ];
		int k = 0;
		
		for ( File file : downloaded_files )
			files[ k++ ] = file.getAbsolutePath();
		
		return files;
	}
}
