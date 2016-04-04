package game2048;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

public class GameLauncher
{
  static final String RECORD_FILE_PATH = "game2048\\leaderboard.2048";

  private static void createGui()
  {
    try {
      for ( UIManager.LookAndFeelInfo info :
            UIManager.getInstalledLookAndFeels() )
        if ( info.getName().equals( "Windows" ) ) {
          UIManager.setLookAndFeel( info.getClassName() );
          break;
        } // end if
    } // end try
    catch ( Exception e ) {
      System.err.println( e + " : " + e.getMessage() );
    } // end catch
    
    // new Leaderboard( new GameFrame() ).setVisible( true );
    new GameFrame().setVisible( true );
  } // end method createGui

  public static void main( String[] args )
  {
    SwingUtilities.invokeLater( new Runnable() {
      public void run()
      {
        createGui();
      } // end method run
    } );
  } // end main
} // end class