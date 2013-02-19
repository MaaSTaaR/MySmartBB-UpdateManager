import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.awt.ComponentOrientation;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
	private static JPanel upgradePane;
	
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
		createMajorUpgradePane();
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
									
									System.out.println( "Needs Upgrade? " + Main.getClient().needsUpgrade() );
									
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
	
	private static void createMajorUpgradePane() 
	{
		upgradePane = new JPanel();
		
		upgradePane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "there_is_major_upgrade" ) ) );
		upgradePane.setComponentOrientation( globalOrientation );
		
		JLabel upgradeLbl = new JLabel( Main.getMessage( "visit_upgrade_url" ) );
		JTextField urlTxt = new JTextField( 35 );
		
		urlTxt.setText( Main.getPreferences().get( "forum_url", "http://" ) + "/setup/upgrade/" );
		
		upgradePane.add( upgradeLbl );
		upgradePane.add( urlTxt );
		
		upgradePane.setVisible( false );
		
		mainWin.add( upgradePane );
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
						FTPListener listener = new FTPListener() 
						{
							@Override
							public void connecting() 
							{
								MainWindow.setStatus( Main.getMessage( "ftp_client_connecting" ) );
							}

							@Override
							public void connected() 
							{
								MainWindow.setStatus( Main.getMessage( "ftp_client_connected" ) );
							}

							@Override
							public void loggedin() 
							{
								MainWindow.setStatus( Main.getMessage( "ftp_client_loggedin" ) );
							}

							@Override
							public void dir_found() 
							{
								MainWindow.setStatus( Main.getMessage( "ftp_client_mysmartbb_dir_found" ) );
							}

							@Override
							public void update_finished() 
							{
								MainWindow.setStatus( Main.getMessage( "update_succeed" ) );
								uploadBtn.setVisible( false );
								checkingBtn.setEnabled( true );
								updateBtn.setVisible( false );
								MainWindow.setUpdateStatus( Main.getMessage( "update_succeed" ) );
								
								if ( Main.getClient().needsUpgrade() )
								{
									mainWin.setSize( 500, 550 );
									
									upgradePane.setVisible( true );
								}
							}

							@Override
							public void cant_connect() 
							{
								uploadBtn.setVisible( true );
								MainWindow.setStatus( Main.getMessage( "ftp_client_couldnt_connect" ) );
							}

							@Override
							public void incorrect_username_or_password() 
							{
								uploadBtn.setVisible( true );
								MainWindow.setStatus( Main.getMessage( "ftp_client_incorrect_username_password" ) );
							}

							@Override
							public void dir_doesnt_exist() 
							{
								MainWindow.setStatus( Main.getMessage( "ftp_client_mysmartbb_dir_doesnt_exist" ) );
							}
							
						};
						
						Main.getUpdater().uploadUpdatedFiles( ftp_server, ftp_username, 
								ftp_password, ftp_dir, 
								listener );
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