public abstract class ApplyPatchListener 
{
	protected String filename;
	
	public void setFilename( String filename ) 
	{
		this.filename = filename;
	}
	
	public abstract void started();
	public abstract void skipped();
	public abstract void failed();
	public abstract void done();
}