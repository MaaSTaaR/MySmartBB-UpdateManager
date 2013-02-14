import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommitCompare;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

public class GitHubClient 
{
	private String username;
	private String repo;
	private String base_commit;
	private String latest_commit_sha;
	private String patch_url = null;
	private boolean update_exists = false;
	private int updates_number;
	private List<CommitFile> patch_files;
	
	GitHubClient( String username, String repo )
	{
		this.username = username;
		this.repo = repo;
	}
	
	public void setBaseCommit( String commit )
	{
		this.base_commit = commit;
	}
	
	public void checkUpdates() throws IOException
	{	
		RepositoryService sRepo = new RepositoryService();
		CommitService sCommit = new CommitService();
		
		// ... //
		
		Repository repo = sRepo.getRepository( this.username, this.repo );
		
		Commit latest_commit = sCommit.getCommits( repo ).get( 0 ).getCommit();
		
		String latest_sha = latest_commit.getUrl().replace( "https://api.github.com/repos/" + this.username + "/" + this.repo + "/git/commits/", "" );
		
		// ... //
		
		if ( !latest_sha.equals( base_commit ) )
		{
			RepositoryCommitCompare patch = sCommit.compare( repo, base_commit, latest_sha );
			
			this.update_exists = true;
			this.updates_number = patch.getTotalCommits();
			this.patch_url = patch.getPatchUrl();
			this.latest_commit_sha = latest_sha;
			this.patch_files = patch.getFiles();
		}
	}
	
	private InputStream getFileRawStream( String local_filename )
	{
		Iterator<CommitFile> it = this.patch_files.iterator();
		
		// An ugly linear search :-(
		while ( it.hasNext() )
		{
			CommitFile commit_file = it.next();
			
			String[] commit_filename_array = commit_file.getFilename().split( "/" );
			
			String commit_filename = commit_filename_array[ commit_filename_array.length - 1 ];
			
			//System.out.println( "Commit File Name : " + commit_filename + " : " + local_filename );
			
			if ( commit_filename.equals( local_filename ) )
			{
				System.out.println( commit_filename + " Found" );
				URL raw_url = null;
				InputStream stream = null;
				try {
					raw_url = new URL( commit_file.getRawUrl() );
					
					stream = raw_url.openStream();
					
					return stream;
				} 
				catch (MalformedURLException e) { e.printStackTrace(); }
				catch (IOException e) { e.printStackTrace(); }
			}
		}
		
		return null;
	}
	
	public String getFileRaw( String local_filename )
	{
		InputStream stream = this.getFileRawStream( local_filename );
		
		if ( stream == null )
			return null;
		
		return new Scanner( stream, "UTF-8" ).useDelimiter( "\\A" ).next();
	}
	
	public BufferedImage getImageRaw( String local_filename )
	{
		InputStream stream = this.getFileRawStream( local_filename );
		
		if ( stream == null )
			return null;
		
		BufferedImage image = null;
		try {
			image = ImageIO.read( stream );
		} catch (IOException e) { e.printStackTrace(); }
		
		return image;
	}
	
	public String getPatchURL()
	{
		return this.patch_url;
	}
	
	public boolean updateExists()
	{
		return this.update_exists;
	}
	
	public int getUpdatesNumber()
	{
		return this.updates_number;
	}
	
	public String getLatestCommitSHA()
	{
		return this.latest_commit_sha;
	}
}
