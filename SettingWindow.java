import java.awt.MouseInfo;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class SettingWindow 
{
	private static Window mainWin;
	private static JPanel mainPane;
	private static JTextField pathTxt;
	private static JTextField ftpServerTxt;
	private static JTextField ftpUsernameTxt;
	private static Preferences prefs;
	private static JTextField ftpDirTxt;
	private static JComboBox languagesList;
	private static JTextField urlTxt;

	public static void show()
	{
		prefs = Main.getPreferences();
		
		setNativeLookAndFeel();
		createSettingWindow();
		
		pathTxt.setText( prefs.get( "main_dir", "" ) );
		ftpServerTxt.setText( prefs.get( "ftp_server", "" ) );
		ftpUsernameTxt.setText( prefs.get( "ftp_username", "" ) );
		ftpDirTxt.setText( prefs.get( "ftp_dir", "." ) );
		urlTxt.setText( prefs.get( "forum_url", "http://" ) );
		
		String language = prefs.get( "language", "en" );
		
		if ( language.equals( "en" ) )
			languagesList.setSelectedItem( "English" );
		else if ( language.equals( "ar" ) )
			languagesList.setSelectedItem( "Arabic" );
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
	
	private static void createSettingWindow() 
	{
		createMainPane();
		
		// ... //
		
		mainWin = new JFrame( Main.getMessage( "setting" ) );
		
		mainWin.setSize( 420, 650 );
		
		// Shows the window near to the mouse cursor
		mainWin.setLocation( MouseInfo.getPointerInfo().getLocation() );
		
		((JFrame) mainWin).setContentPane( mainPane );
		
		// ... //
		
		createWidgets();
		
		// ... //
		
		mainWin.setVisible( true );
	}
	
	private static void createMainPane() 
	{
		mainPane = new JPanel();
		
		mainPane.setLayout( new BoxLayout( mainPane, BoxLayout.PAGE_AXIS ) );
		
		mainPane.setComponentOrientation( MainWindow.globalOrientation );
		
		mainPane.setVisible( true );
	}
	
	private static void createWidgets() 
	{
		createMainDirPane();
		createFTPSettingPane();
		createForumURLPane();
		createInterfaceLanguagePane();
		createApplyBtn();
	}

	private static void createMainDirPane() 
	{
		JPanel mainDirPane = new JPanel();
		mainDirPane.setComponentOrientation( MainWindow.globalOrientation );
		mainDirPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "local_mysmartbb_dir" ) ));

		pathTxt = new JTextField( 15 );
		JButton btnBrowseFiles = new JButton( Main.getMessage( "browse" ) );
		
		pathTxt.setEditable( false );
		
		btnBrowseFiles.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) 
			{
				JFileChooser fc = new JFileChooser();
				
				fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				
				int ret = fc.showOpenDialog( mainPane );
				
				if ( ret == JFileChooser.APPROVE_OPTION )
				{
					File dir = fc.getSelectedFile();
					
					pathTxt.setText( dir.getAbsolutePath() );
				}
			}
		});
		
		mainDirPane.add( pathTxt );
		mainDirPane.add( btnBrowseFiles );
		
		mainPane.add( mainDirPane );
	}

	private static void createFTPSettingPane() 
	{
		JPanel ftpSettingPane = new JPanel();
		ftpSettingPane.setComponentOrientation( MainWindow.globalOrientation );
		ftpSettingPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "ftp_server_info" ) ) );
		
		// ... //
		
		JPanel ftpServerPane = new JPanel();
		ftpServerPane.setComponentOrientation( MainWindow.globalOrientation );
		
		JLabel ftpServerLbl = new JLabel( Main.getMessage( "ftp_server" ) );
		ftpServerTxt = new JTextField( 15 );
		
		ftpServerPane.add( ftpServerLbl );
		ftpServerPane.add( ftpServerTxt );
		
		// ... //
		
		JPanel ftpUsernamePane = new JPanel();
		ftpUsernamePane.setComponentOrientation( MainWindow.globalOrientation );
		
		JLabel ftpUsernameLbl = new JLabel( Main.getMessage( "username" ) );
		ftpUsernameTxt = new JTextField( 15 );
		
		ftpUsernamePane.add( ftpUsernameLbl );
		ftpUsernamePane.add( ftpUsernameTxt );
		
		// ... //
		
		JPanel ftpDirPane = new JPanel();
		ftpDirPane.setComponentOrientation( MainWindow.globalOrientation );
		
		JLabel ftpDirLbl = new JLabel( Main.getMessage( "server_mysmartbb_dir" ) );
		ftpDirTxt = new JTextField( 15 );
		
		ftpDirPane.add( ftpDirLbl );
		ftpDirPane.add( ftpDirTxt );
		
		// ... //
		
		ftpSettingPane.add( ftpServerPane );
		ftpSettingPane.add( ftpUsernamePane );
		ftpSettingPane.add( ftpDirPane );
		
		mainPane.add( ftpSettingPane );
	}
	
	private static void createForumURLPane() 
	{
		JPanel urlPane = new JPanel();
		urlPane.setComponentOrientation( MainWindow.globalOrientation );
		urlPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "forum_url" ) ));

		urlTxt = new JTextField( 15 );
		
		urlPane.add( urlTxt );
		
		mainPane.add( urlPane );
	}
	
	private static void createInterfaceLanguagePane() 
	{
		JPanel interfaceLangPane = new JPanel();
		interfaceLangPane.setComponentOrientation( MainWindow.globalOrientation );
		interfaceLangPane.setBorder( BorderFactory.createTitledBorder( Main.getMessage( "interface_language" ) ));
		
		// ... //
		
		JLabel noteLbl = new JLabel( Main.getMessage( "restart_required" ) );
		
		languagesList = new JComboBox();
		
		languagesList.addItem( "Arabic" );
		languagesList.addItem( "English" );
		
		interfaceLangPane.add( noteLbl );
		interfaceLangPane.add( languagesList );
		
		// ... //
		
		mainPane.add( interfaceLangPane );
	}
	
	private static void createApplyBtn() 
	{
		JButton applyBtn = new JButton( Main.getMessage( "save" ) );
		
		applyBtn.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) 
			{
				String language = null;
				
				if ( languagesList.getSelectedItem().equals( "English" ) )
					language = new String( "en" );
				else if ( languagesList.getSelectedItem().equals( "Arabic" ) )
					language = new String( "ar" );
				
				prefs.put( "main_dir", pathTxt.getText() );
				prefs.put( "ftp_server", ftpServerTxt.getText() );
				prefs.put( "ftp_username", ftpUsernameTxt.getText() );
				prefs.put( "ftp_dir", ftpDirTxt.getText() );
				prefs.put( "language", language );
				
				try {
					prefs.flush();
				} catch (BackingStoreException e1) { e1.printStackTrace(); }
				
				try {
					Main.checkIfSystemReady();
					mainWin.dispose();
				} catch (BackingStoreException e1) { e1.printStackTrace(); }
			}
		});
		
		mainPane.add( applyBtn );
	}
}
