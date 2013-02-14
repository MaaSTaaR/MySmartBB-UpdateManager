import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.awt.ComponentOrientation;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.egit.github.core.client.RequestException;

public class MainWindow 
{
	private static JFrame mainWin;
	private static JPanel mainPane;
	private static JButton updateBtn;
	private static JButton checkingBtn;
	private static JButton settingBtn;
	private static JLabel statusLbl;
	private static JLabel updateStatusLbl;
	private static JList updatedFilesList;
	private static JScrollPane updatedFilesScrollPane;
	private static JButton uploadBtn;
	private static JList downloadedFilesList;
	private static JScrollPane downloadedFilesScrollPane;
	public static ComponentOrientation globalOrientation = ComponentOrientation.RIGHT_TO_LEFT;
	
	public static void show()
	{
		if ( !Main.isRTL() )
			globalOrientation = ComponentOrientation.LEFT_TO_RIGHT;
		
		setNativeLookAndFeel();
		createMainWindow();
	}

	private static void setNativeLookAndFeel() 
	{
		try 
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		} catch (ClassNotFoundException e) {} 
		catch (InstantiationException e) {} 
		catch (IllegalAccessException e) {} 
		catch (UnsupportedLookAndFeelException e) {}
	}
	
	private static void createMainWindow() 
	{
		createMainPane();
		
		// ... //
		
		mainWin = new JFrame( Main.getMessage( "mysmartbb_update_manager" ) );
		
		mainWin.setSize( 500, 220 );
		mainWin.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		// Shows the window near to the mouse cursor
		mainWin.setLocation( MouseInfo.getPointerInfo().getLocation() );
		
		mainWin.setContentPane( mainPane );
		
		// ... //
		
		createWidgets();
		
		// ... //
		
		mainWin.setVisible( true );
	}

	private static void createMainPane() 
	{
		mainPane = new JPanel();
		
		mainPane.setLayout( new BoxLayout( mainPane, BoxLayout.PAGE_AXIS ) );
		
		mainPane.setComponentOrientation( globalOrientation );
		
		mainPane.setVisible( true );
	}
	
	private static void createWidgets() 
	{
		createOperationsPane();
		createUpdateStatusPane();
		createStatusPane();
		createUpdatedFilesPane();
		createDownloadedFilesPane();
	}
	
	private static void createOperationsPane() 
	{
		JPanel operationsPane = new JPanel();
		
		operationsPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "operations" ) ) );
		operationsPane.setComponentOrientation( globalOrientation );
		
		checkingBtn = new JButton( Main.getMessage( "check_updates" ) );
		settingBtn = new JButton( Main.getMessage( "setting" ) );
		
		operationsPane.add( checkingBtn );
		operationsPane.add( settingBtn );
		
		settingBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) 
			{
				SettingWindow.show();
			}
		});
		
		checkingBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				MainWindow.setUpdateStatus( Main.getMessage( "seeking_updates" ) );
				
				Thread worker = new Thread() {
					public void run()
					{
						try {
							Main.getClient().checkUpdates();
						} 
						catch (IOException e) { e.printStackTrace(); }
						
						SwingUtilities.invokeLater( new Runnable() {
							@Override
							public void run() 
							{
								if ( Main.getClient().updateExists() )
								{
									checkingBtn.setEnabled( false );
									updateBtn.setVisible( true );
									
									MainWindow.setUpdateStatus( Main.getMessage( "new_updates" ) + " " + Main.getClient().getUpdatesNumber() );
								}
								else
								{
									MainWindow.setUpdateStatus( Main.getMessage( "no_updates" ) );
								}
							}
						} );
					}
				};
				
				worker.start();
			}
		});
		
		mainPane.add( operationsPane );
	}
	
	private static void createUpdateStatusPane() 
	{
		JPanel updateStatusPane = new JPanel();
		
		updateStatusPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "update_status" ) ) );
		updateStatusPane.setComponentOrientation( globalOrientation );
		
		updateStatusLbl = new JLabel( Main.getMessage( "no_update_info_available" )  );
		updateBtn = new JButton( Main.getMessage( "update" ) );
		
		updateBtn.setVisible( false );
		
		updateBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e )
			{
				updateBtn.setEnabled( false );
				
				MainWindow.setStatus( Main.getMessage( "updating_files" ) );
				
				Thread worker = new Thread() {
					public void run()
					{
						try {
							Main.getUpdater().updateLocalVersion();
						} catch (IOException e) { e.printStackTrace(); }
						
						SwingUtilities.invokeLater( new Runnable() {
							@Override
							public void run() 
							{
								MainWindow.setStatus( Main.getMessage( "local_mysmartbb_updated" ) );
								
								uploadFiles();
							}
						} );
					}
				};
				
				worker.start();
			}
		});
		
		updateStatusPane.add( updateStatusLbl );
		updateStatusPane.add( updateBtn );
		
		mainWin.add( updateStatusPane );
	}
	
	private static void createStatusPane() 
	{
		JPanel statusPane = new JPanel();
		
		statusPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "system_messages" ) ) );
		statusPane.setComponentOrientation( globalOrientation );
		
		statusLbl = new JLabel( "" );

		statusPane.add( statusLbl );
		
		mainWin.add( statusPane );
	}
	
	private static void createUpdatedFilesPane() 
	{
		updatedFilesList = new JList();
		
		updatedFilesScrollPane = new JScrollPane( updatedFilesList );
		updatedFilesScrollPane.setComponentOrientation( globalOrientation );
		updatedFilesScrollPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "patched_files" ) ) );
		
		updatedFilesScrollPane.setVisible( false );
		
		// ... //
		
		uploadBtn = new JButton( Main.getMessage( "upload_files" ) );
		
		uploadBtn.setVisible( false );
		
		uploadBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) 
			{
				uploadFiles();
			}
		});
		
		// ... //
		
		mainPane.add( updatedFilesScrollPane );
		mainPane.add( uploadBtn );
	}
	
	private static void createDownloadedFilesPane() 
	{
		downloadedFilesList = new JList();
		
		downloadedFilesScrollPane = new JScrollPane( downloadedFilesList );
		downloadedFilesScrollPane.setComponentOrientation( globalOrientation );
		downloadedFilesScrollPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "replaced_files" ) ));
		
		downloadedFilesScrollPane.setVisible( false );
		
		mainPane.add( downloadedFilesScrollPane );
	}
	
	private static void uploadFiles()
	{
		final String ftp_server, ftp_username, ftp_dir;
		
		mainWin.setSize( 500, 350 );
		updatedFilesScrollPane.setVisible( true );
		
		updatedFilesList.setListData( Main.getUpdater().getUpdatedFilesArray() );
		
		String[] downloaded_files = Main.getUpdater().getDownloadedFilesArray();
		
		if ( downloaded_files.length > 0 )
		{
			mainWin.setSize( 500, 450 );
			downloadedFilesScrollPane.setVisible( true );
			
			downloadedFilesList.setListData( downloaded_files );
		}
		
		ftp_server = Main.getPreferences().get( "ftp_server", "" );
		ftp_username = Main.getPreferences().get( "ftp_username", "" );
		ftp_dir = Main.getPreferences().get( "ftp_dir", "." );
		
		if ( ftp_server.isEmpty() || ftp_username.isEmpty() )
		{	
			MainWindow.setStatus( Main.getMessage( "set_ftp_info" ) );
			uploadBtn.setVisible( true );
		}
		else
		{
			JPasswordField passwordTxt = new JPasswordField();
			JLabel passwordLbl = new JLabel( Main.getMessage( "enter_ftp_password" ) );
			
			int response = JOptionPane.showConfirmDialog( null, new Object[] { passwordLbl, passwordTxt }, Main.getMessage( "ftp_password" ), JOptionPane.OK_CANCEL_OPTION );
			
			if ( response == JOptionPane.OK_OPTION )
			{
				final String ftp_password = String.valueOf( passwordTxt.getPassword() );
				
				Thread worker = new Thread()
				{
					public void run()
					{
						//System.out.println( "Uploading ... : " + ftp_password );
						
						MainWindow.setStatus( Main.getMessage( "ftp_client_connecting" ) );
						
						FTPClient ftp = new FTPClient();
						
						try {
							ftp.connect( ftp_server );
							
							MainWindow.setStatus( Main.getMessage( "ftp_client_connected" ) );
							
							ftp.login( ftp_username, ftp_password );
							
							MainWindow.setStatus( Main.getMessage( "ftp_client_loggedin" ) );
							
							ftp.changeDirectory( ftp_dir );
							
							MainWindow.setStatus( Main.getMessage( "ftp_client_mysmartbb_dir_found" ) );
							
							// ... //
							
							Updater updater = Main.getUpdater();
							
							List<File> files = updater.getUpdatedFiles();
							
							files.addAll( updater.getDownloadedFiles() );
							
							Iterator<File> it = files.iterator();
							
							while ( it.hasNext() )
							{
								ftp.changeDirectory( ftp_dir );
								
								File file = it.next();
								
								String path = file.getAbsolutePath();
								
								if ( path.contains( "/setup/" ) )
									continue;
								
								path = path.replace( Main.getMainDir(), "" );
								path = path.replace( file.getName(), "" );
								
								String dirs[] = path.split( File.separator );
								
								System.out.println( "File : " + file.getName() );
								
								for ( String dir : dirs )
								{
									System.out.println( dir );
									
									try {
										ftp.changeDirectory( dir );
									}
									catch (FTPException e)
									{
										if ( e.getCode() == 550 )
										{
											ftp.createDirectory( dir );
											ftp.changeDirectory( dir );
										}
										else
										{
											e.printStackTrace();
										}
									}
									finally { }
								}
								
								try {
									System.out.println( "File == " + file.getAbsolutePath() );
									
									ftp.upload( file, new TransferListener( file.getName() ) );
								} 
								catch (FTPDataTransferException e) { e.printStackTrace(); } 
								catch (FTPAbortedException e) { e.printStackTrace(); }
							}
							
							// ... //
							
							ftp.disconnect( true );
							
							Main.getClient().setBaseCommit( Main.getClient().getLatestCommitSHA() );
							
							MainWindow.setStatus( Main.getMessage( "update_succeed" ) );
							uploadBtn.setVisible( false );
							checkingBtn.setEnabled( true );
							updateBtn.setVisible( false );
							MainWindow.setUpdateStatus( Main.getMessage( "update_succeed" ) );
						}
						catch (ConnectException e) 
						{ 
							uploadBtn.setVisible( true );
							MainWindow.setStatus( Main.getMessage( "ftp_client_couldnt_connect" ) );
						}
						catch (IllegalStateException e) { e.printStackTrace(); } 
						catch (IOException e) { e.printStackTrace(); } 
						catch (FTPIllegalReplyException e) { e.printStackTrace(); }
						catch (FTPException e) 
						{
							uploadBtn.setVisible( true );
							
							if ( e.getCode() == 530 )
								MainWindow.setStatus( Main.getMessage( "ftp_client_incorrect_username_password" ) );
							else if ( e.getCode() == 550 )
								MainWindow.setStatus( Main.getMessage( "ftp_client_mysmartbb_dir_doesnt_exist" ) );
							else
								e.printStackTrace(); 
						}
						finally { }
					}
				};
				
				worker.start();
			}
			else
			{
				uploadBtn.setVisible( true );
			}
		}
	}
	
	public static void disableAllWidgetsButSetting()
	{
		checkingBtn.setEnabled( false );
		updateBtn.setEnabled( false );
		
		settingBtn.setEnabled( true );
	}
	
	public static void enableAllWidgets()
	{
		checkingBtn.setEnabled( true );
		updateBtn.setEnabled( true );
		
		settingBtn.setEnabled( true );
	}
	
	public static void setStatus( String message )
	{
		statusLbl.setText( message );
	}
	
	public static void setUpdateStatus( String message )
	{
		updateStatusLbl.setText( message );
	}
}

class TransferListener implements FTPDataTransferListener
{
	private String filename;
	
	TransferListener( String filename )
	{
		this.filename = filename;
	}
	
	@Override
	public void aborted() 
	{
	}

	@Override
	public void completed() 
	{
		MainWindow.setStatus( Main.getMessage( "ftp_client_uploaded" ) + " " + filename );
	}

	@Override
	public void failed() 
	{	
	}

	@Override
	public void started()
	{
		MainWindow.setStatus( Main.getMessage( "ftp_client_upload_started" ) + " " + filename );
	}

	@Override
	public void transferred( int bytes ) 
	{
		MainWindow.setStatus( Main.getMessage( "ftp_client_uploading" ) + " " + filename );
	}	
}