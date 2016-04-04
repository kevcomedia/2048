package game2048;

// Layout
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;

// Components
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

// IO
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

// Utilities
import java.util.Arrays;

public class Leaderboard extends JDialog
{
  private static class Record
    implements Comparable< Record >
  {
    private int    nMoves_;
    private int    largestTile_;
    private int    score_;
    
    Record( int nMoves, int largestTile, int score )
    {
      nMoves_      = nMoves;
      largestTile_ = largestTile;
      score_       = score;
    } // end constructor
    
    public int compareTo( Record record )
    {
      if ( this.score_ != record.score_ )
        return -Integer.valueOf( score_ ).compareTo( record.score_ );
      else if ( this.largestTile_ != record.largestTile_ )
        return -Integer.valueOf( largestTile_ ).compareTo( record.largestTile_ );
      else
        return Integer.valueOf( nMoves_ ).compareTo( record.nMoves_ );
    } // end method compareTo
    
    private static Record toRecord( String record )
    {
      Scanner sc = new Scanner( record );
      
      int    nMoves      = sc.nextInt();
      int    largestTile = sc.nextInt();
      int    score       = sc.nextInt();
      
      return new Record( nMoves, largestTile, score );
    } // end method toRecord
    
    public String toString()
    {
      return String.format( "%,d %d %,d", nMoves_, largestTile_, score_ );
    } // end method toString
  } // end inner class Record

  private static final int MAX_RECORDS = 10;
  
  private Record[] scoreRecords_;
  
  Leaderboard( JFrame owner )
  {
    super ( owner, "2048 Leaderboard", true );
    
    // Create panels
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout( new BoxLayout( mainPanel, BoxLayout.PAGE_AXIS ) );
    // mainPanel.setPreferredSize( new Dimension( 300, 400 ) );
    
    // Get record file path
    Path filePath = Paths.get( GameLauncher.RECORD_FILE_PATH );
    
    // Count the number of scores from the file
    int nRecords = 0;
    try ( Scanner sc = new Scanner( filePath.toFile() ) ) {
      while ( sc.hasNextLine() ) {
        nRecords++;
        sc.nextLine();
      } // end while
    } // end try
    catch ( FileNotFoundException exception ) {}
    
    // Create array to store records
    scoreRecords_ = new Record[ nRecords ];
    
    // Store the records from the file to the array
    try ( Scanner sc = new Scanner( filePath.toFile() ) ) {
      for ( int i = 0; i < scoreRecords_.length; i++ )
        scoreRecords_[ i ] = Record.toRecord( sc.nextLine() );
    } // end try
    catch ( FileNotFoundException exception ) {}
    
    // Sort the array from record with highest score
    Arrays.sort( scoreRecords_ );
    
    // Create template panel to display top 10 scores
    class RecordPanel extends JPanel
    {
      RecordPanel( int rank, int nMoves, int largestTile, int score )
      {
        super ();
        setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
        
        // All-purpose label
        JLabel label;
        
        add( Box.createHorizontalStrut( 15 ) );
        
        // Label for rank
        label = new JLabel( String.format( "%d", rank ) );
        label.setHorizontalAlignment( SwingConstants.TRAILING );
        label.setPreferredSize( new Dimension( 25, 24 ) );
        add( label );
        add( Box.createHorizontalGlue() );
        add( Box.createHorizontalStrut( 20 ) );
        
        // Label for number of moves made
        label = new JLabel( String.format( "%,d", nMoves ) );
        label.setHorizontalAlignment( SwingConstants.TRAILING );
        label.setPreferredSize( new Dimension( 60, 24 ) );
        add( label );
        add( Box.createHorizontalStrut( 30 ) );

        // Label for largest tile
        label = new JLabel( String.format( "%d", largestTile ) );
        label.setHorizontalAlignment( SwingConstants.TRAILING );
        label.setPreferredSize( new Dimension( 60, 24 ) );
        add( label );
        add( Box.createHorizontalStrut( 30 ) );
        
        // Label for score
        label = new JLabel( String.format( "%,d", score ) );
        label.setHorizontalAlignment( SwingConstants.TRAILING );
        label.setPreferredSize( new Dimension( 40, 24 ) );
        add( label );
        add( Box.createHorizontalStrut( 35 ) );
      } // end constructor
    } // end local class RecordPanel
    
    // Create headers
    JPanel headerPanel = new JPanel();
    headerPanel.setLayout( new BoxLayout( headerPanel, BoxLayout.LINE_AXIS ) );
    JLabel header;
    headerPanel.add( Box.createHorizontalStrut( 15 ) );
    header = new JLabel( "Rank" );
    header.setPreferredSize( new Dimension( 25, 24 ) );
    headerPanel.add( header );
    headerPanel.add( Box.createHorizontalGlue() );
    headerPanel.add( Box.createHorizontalStrut( 20 ) );
    header = new JLabel( "Moves Made" );
    header.setHorizontalAlignment( SwingConstants.TRAILING );
    header.setPreferredSize( new Dimension( 60, 24 ) );
    headerPanel.add( header );
    headerPanel.add( Box.createHorizontalStrut( 30 ) );
    header = new JLabel( "Largest Tile" );
    header.setHorizontalAlignment( SwingConstants.TRAILING );
    header.setPreferredSize( new Dimension( 60, 24 ) );
    headerPanel.add( header );
    headerPanel.add( Box.createHorizontalStrut( 30 ) );
    header = new JLabel( "Score" );
    header.setHorizontalAlignment( SwingConstants.TRAILING );
    header.setPreferredSize( new Dimension( 40, 24 ) );
    headerPanel.add( header );
    headerPanel.add( Box.createHorizontalStrut( 35 ) );
    
    mainPanel.add( headerPanel );
    
    // Display top 10 scores
    if ( nRecords > MAX_RECORDS )
      for ( int i = 0; i < MAX_RECORDS; i++ ) {
        Record record = scoreRecords_[ i ];
        mainPanel.add(
            new RecordPanel(
                i + 1,
                record.nMoves_,
                record.largestTile_,
                record.score_ ) );
        mainPanel.add( Box.createVerticalStrut( 10 ) );
      } // end for
    else
      for ( int i = 0; i < scoreRecords_.length; i++ ) {
        Record record = scoreRecords_[ i ];
        mainPanel.add(
            new RecordPanel(
                i + 1,
                record.nMoves_,
                record.largestTile_,
                record.score_ ) );
        mainPanel.add( Box.createVerticalStrut( 10 ) );
      } // end for
    
    add( mainPanel );
    setResizable( false );
    setLocationRelativeTo( owner );
    pack();
  } // end constructor
  
/*
  public static void main( String[] args )
  {
    Leaderboard lb = new Leaderboard( null );
    lb.setVisible( true );
  } // end main
 */
 
 } // end class