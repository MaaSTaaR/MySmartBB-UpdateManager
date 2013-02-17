public interface FTPListener 
{
	public void connecting();
	public void connected();
	public void loggedin();
	public void dir_found();
	public void update_finished();
	public void cant_connect();
	public void incorrect_username_or_password();
	public void dir_doesnt_exist();
	
}
