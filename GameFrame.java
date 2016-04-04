package game2048;

// Borders and Layout
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;

// Components
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

// Listeners and Events
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

// IO
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

// Utilities
import java.util.Random;

public class GameFrame extends JFrame
{
  private static final boolean CAN_SPAWN_FOUR = true;
  private static final int SIZE = 4;   // number of rows and columns
  
  // Displays the tiles on the board
  private JLabel[][] tiles_ = new JLabel[ SIZE ][ SIZE ];
  // Stores the actual values for each tile
  private int[][] tileValues_ = new int[ SIZE ][ SIZE ];
  
  private JLabel  scoreLabel_;      // Displays the player's score
  private int     score_;           // Records the player's score
  private int     nBlanks_;         // Counts the number of blank tiles
  private int     nMoves_;          // Counts the number of moves
  private int     largestTile_;     // Records the largest tile
  private boolean hasReached2048_;  
  
  /**
   * Constructor
   */
  GameFrame()
  {
    super ( "2048" );
    
    // Create tool panel
    JPanel toolPanel = new JPanel();
    toolPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
    toolPanel.setLayout( new BoxLayout( toolPanel, BoxLayout.LINE_AXIS ) );
    
    // Create "New Game" button
    JLabel resetButton = new JLabel( "New Game" );
    resetButton.addMouseListener( new MouseListener() {
      public void mouseClicked( MouseEvent event )
      {
        initializeBoard();
      } // end method mouseClicked
      public void mouseEntered(  MouseEvent event ) {}
      public void mouseExited(   MouseEvent event ) {}
      public void mousePressed(  MouseEvent event ) {}
      public void mouseReleased( MouseEvent event ) {}
    } );
    
    // Create leaderboard button
    JLabel leaderboardButton = new JLabel( "Leaderboard" );
    leaderboardButton.addMouseListener( new MouseListener() {
      public void mouseClicked( MouseEvent event )
      {
        new Leaderboard( GameFrame.this ).setVisible( true );
      } // end method mouseClicked
      public void mouseEntered(  MouseEvent event ) {}
      public void mouseExited(   MouseEvent event ) {}
      public void mousePressed(  MouseEvent event ) {}
      public void mouseReleased( MouseEvent event ) {}
    } );
    
    // Create label that displays the player's score
    scoreLabel_ = new JLabel();
    
    // Assemble tool panel
    toolPanel.add( Box.createHorizontalStrut( 5 ) );
    toolPanel.add( resetButton );
    toolPanel.add( Box.createHorizontalStrut( 5 ) );
    toolPanel.add( new JLabel( "|") );
    toolPanel.add( Box.createHorizontalStrut( 5 ) );
    toolPanel.add( leaderboardButton );
    toolPanel.add( Box.createHorizontalGlue() );
    toolPanel.add( scoreLabel_ );
    toolPanel.add( Box.createHorizontalStrut( 5 ) );
    
    // Create board panel
    JPanel boardPanel = new JPanel();
    boardPanel.setBorder( BorderFactory.createEmptyBorder( 20, 20, 20, 20 ) );
    boardPanel.setBackground( new Color( 187, 173, 160 ) );
    boardPanel.setLayout( new GridLayout( 4, 4, 20, 20 ) );
    
    // Create label array to represent tiles
    for ( int i = 0; i < tiles_.length; i++ )
      for ( int j = 0; j < tiles_[ i ].length; j++ )
        tiles_[ i ][ j ]
            = new JLabel( createImageIcon( tileValues_[ i ][ j ] ) );
    
    // Add tiles to board
    for ( JLabel[] tileArray: tiles_ )
      for ( JLabel tile : tileArray )
        boardPanel.add( tile );
      
    // Reset game data
    initializeBoard();
      
    // Set frame properties
    add( toolPanel, BorderLayout.PAGE_START );
    add( boardPanel, BorderLayout.CENTER );    
    setDefaultCloseOperation( EXIT_ON_CLOSE );
    setLocationByPlatform( true );
    setResizable( false );
    pack();
  } // end constructor
  
  /**
   * Creates an ImageIcon using an image named ###.png, where ### corresponds
   * to a value that can appear on the game. The value of ### is taken from
   * the tile values.
   *
   * @param value   The value of the tile, which constitutes the filename
   *                of the image to be used.
   * @return        The ImageIcon to represent the tiles 
   */
  private ImageIcon createImageIcon( int value )
  {
    // Get the image file path
    String path = "images\\" + value + ".png";
    java.net.URL imageUrl = GameFrame.class.getResource( path );
    
    // Return the ImageIcon
    if ( imageUrl != null )
      return new ImageIcon( imageUrl );
    else {
      System.err.println( "Error. \'" + path + "\' not found." );
      return null;
    } // end else
  } // end method createImageIcon
  
  /** 
   * Resets everything
   */
  private void initializeBoard()
  {
    // Set the tile values to 0
    for ( int i = 0; i < SIZE; i++ ) 
      for ( int j = 0; j < SIZE; j++ )
        tileValues_[ i ][ j ] = 0;
        
    // Reset game data
    nBlanks_ = SIZE * SIZE;
    hasReached2048_ = false;
    nMoves_ = 0;
    largestTile_ = 0;
    score_ = 0;
    scoreLabel_.setText( "Score: 0" );
    
    // Spawn two random tiles
    spawnNewTile( !CAN_SPAWN_FOUR );
    spawnNewTile( CAN_SPAWN_FOUR );
    
    // Create the frame's key listener
    if ( getKeyListeners().length == 0 )
      addKeyListener( new KeyListener() {
        // Respond upon release of arrow keys
        public void keyReleased( KeyEvent event )
        {
          // Move according to key press
          move( event.getKeyCode() );
          refreshBoard();
          
          // Tell the player he reached 2048 when he does
          if ( ( !hasReached2048_ ) && ( largestTile_ == 2048 ) ) {
            hasReached2048_ = true;
            JOptionPane.showMessageDialog(
                GameFrame.this,
                String.format( 
                    "You made it in %,d moves.%nGo try for a higher score!",
                    nMoves_ ),
                "You reached 2048!",
                JOptionPane.PLAIN_MESSAGE );
          } // end if
          
          // Tell the player there are no more moves
          if ( !canMove() ) {
            removeKeyListener( this );
            
            // Display the player's game data and get the player's name
            JOptionPane.showMessageDialog(
                GameFrame.this,
                String.format(
                    "You made %,d moves.%n"
                    + "Largest tile: %d%n"
                    + "Your score: %,d", nMoves_, largestTile_, score_ ),
                    "No more moves man",
                JOptionPane.PLAIN_MESSAGE );
        
            // Record the data
            Path filePath = Paths.get( GameLauncher.RECORD_FILE_PATH );
            String record
                = String.format( "%d %d %d%n", nMoves_, largestTile_, score_ );
                    
            // Record score to a file
            try ( RandomAccessFile raf
                      = new RandomAccessFile( filePath.toFile(), "rw" ) ) {
              raf.seek( filePath.toFile().length() );
              raf.writeBytes( record );
            } // end try
            catch ( IOException exception ) {
              System.err.format( "IOException: %s%n", exception );
            } // end catch
            
            // Show leaderboard
            new Leaderboard( GameFrame.this ).setVisible( true );
          } // end if
        } // end method keyReleased
        public void keyPressed( KeyEvent event ) {}
        public void keyTyped( KeyEvent event ) {}
      } );
    
    refreshBoard();
  } // end method initializeBoard
  
  /**
   * Merges two identical tiles when tiles move.
   *
   * @param keyCode   The equivalent numeric key code of a key press.
   *                  This is needed to know where the new tile ends up.
   * @param row       The horizontal position of the first tile.
   * @param col       The vertical popsition of the first tile.
   * @param n         Depending on keyCode, this could represent the
   *                  horizontal or vertical position of the second tile.
   */
  private void mergeTiles( int keyCode, int row, int col, int n )
  {
    boolean isHorizontal
        = ( ( keyCode == KeyEvent.VK_LEFT )
            || ( keyCode == KeyEvent.VK_RIGHT ) );
    boolean isVertical
        = ( ( keyCode == KeyEvent.VK_UP ) || ( keyCode == KeyEvent.VK_DOWN ) );
        
    // Horizontal merge
    if ( isHorizontal ) {
      if ( tileValues_[ row ][ col ] == tileValues_[ row ][ n ] ) {
        // "Merge" the two tiles_
        tileValues_[ row ][ col ] *= 2;
        tileValues_[ row ][ n ] = 0;
        updateScore( tileValues_[ row ][ col ] );
      } // end if
    } // end if
    // Vertical merge
    else if ( isVertical ) {
      if ( tileValues_[ row ][ col ] == tileValues_[ n ][ col ] ) {
        // "Merge" the two tiles_
        tileValues_[ row ][ col ] *= 2;
        tileValues_[ n ][ col ] = 0;
        updateScore( tileValues_[ row ][ col ] );
      } // end if
    } // end else-if
  } // end method mergeTiles
  
  /**
   * Moves the tiles according to the key press.
   *
   * @param keyCode   The equivalent numeric key code of a key press.
   *                  This is needed to know where the tiles end up.
   */
  private void move( int keyCode )
  {
    boolean hasMoved = false;
    
    if ( keyCode == KeyEvent.VK_LEFT ) {
      // Scan from top
      for ( int row = 0; row < SIZE; row++ ) {
        // Scan from left
        for ( int col = 0; col < SIZE - 1; col++ ) {
          // Scan to the right for tile with same value as test tile
          int n = col + 1;
          while ( n < SIZE - 1 && tileValues_[ row ][ n ] == 0 )
            n++;

          if ( tileValues_[ row ][ col ] == tileValues_[ row ][ n ] ) {
            mergeTiles( keyCode, row, col, n );
            
            if ( tileValues_[ row ][ col ] != 0 ) {
              hasMoved = true;
              nBlanks_++;
            } // end if
          } // end if
        } // end for
        
        // Pack tiles to the left
        for ( int i = 0; i < SIZE; i++ )
          if ( tileValues_[ row ][ i ] != 0 ) {
            boolean isMovable = false;
            // Scan for the leftmost empty tile
            int n = i;
            while ( n > 0 && tileValues_[ row ][ n - 1 ] == 0 ) {
              n--;
              isMovable = true;
            } // end while
              
            // Copy the test tile to the empty tile
            if ( isMovable ) {
              tileValues_[ row ][ n ] = tileValues_[ row ][ i ];
              tileValues_[ row ][ i ] = 0;
              hasMoved = true;
            } // end if
          } // end if
            
      } // end for
    } // end if
    else if ( keyCode == KeyEvent.VK_RIGHT ) {
      // Scan from top
      for ( int row = 0; row < SIZE; row++ ) {
        // Scan from right
        for ( int col = SIZE - 1; col > 0; col-- ) {
          // Scan to the left for tile with same value as test tile
          int n = col - 1;
          while ( n > 0 && tileValues_[ row ][ n ] == 0 )
            n--;

          // Check if tile to the left matches the test tile
          if ( tileValues_[ row ][ col ] == tileValues_[ row ][ n ] ) {
            mergeTiles( keyCode, row, col, n );
            
            if ( tileValues_[ row ][ col ] != 0 ) {
              hasMoved = true;
              nBlanks_++;
            } // end if
          } // end if
        } // end for
          
        // Pack tiles to the right
        for ( int i = SIZE - 1; i >= 0; i-- )
          if ( tileValues_[ row ][ i ] != 0 ) {
            boolean isMovable = false;
            // Scan for rightmost empty tile
            int n = i;
            while ( n < SIZE - 1 && tileValues_[ row ][ n + 1 ] == 0 ) {
              n++;
              isMovable = true;
            } // end while
              
            // Copy the test tile to the empty tile
            if ( isMovable ) {
              tileValues_[ row ][ n ] = tileValues_[ row ][ i ];
              tileValues_[ row ][ i ] = 0;
              hasMoved = true;
            } // end if
          } // end if
        
      } // end for
    } // end else-if
    else if ( keyCode == KeyEvent.VK_UP ) {
      // Scan from left
      for ( int col = 0; col < SIZE; col++ ) {
        // Scan from top
        for ( int row = 0; row < SIZE - 1; row++ ) {
          // Scan to the bottom for tile with same value as test tile
          int n = row + 1;
          while ( n < SIZE - 1 && tileValues_[ n ][ col ] == 0 )
            n++;

          // Check if tile to the right matches the test tile
          if ( tileValues_[ row ][ col ] == tileValues_[ n ][ col ] ) {
            // "Merge" the two tiles_
            mergeTiles( keyCode, row, col, n );
            
            if ( tileValues_[ row ][ col ] != 0 ) {
              hasMoved = true;
              nBlanks_++;
            } // end if
          } // end if
        } // end for
        
        // Pack tiles to the top
        for ( int i = 0; i < SIZE; i++ )
          if ( tileValues_[ i ][ col ] != 0 ) {
            boolean isMovable = false;
            // Scan for the topmost empty tile
            int n = i;
            while ( n > 0 && tileValues_[ n - 1 ][ col ] == 0 ) {
              n--;
              isMovable = true;
            } // end while
              
            // Copy the test tile to the empty tile
            if ( isMovable ) {
              tileValues_[ n ][ col ] = tileValues_[ i ][ col ];
              tileValues_[ i ][ col ] = 0;
              hasMoved = true;
            } // end if
          } // end if
            
        } // end for
    } // end else-f
    else if ( keyCode == KeyEvent.VK_DOWN ) {
      // Scan from left
      for ( int col = 0; col < SIZE; col++ ) {
        // Scan from bottom
        for ( int row = SIZE - 1; row > 0; row-- ) {
          // Scan to the top for tile with same value as test tile
          int n = row - 1;
          while ( n > 0 && tileValues_[ n ][ col ] == 0 )
            n--;

          // Check if tile to the top matches the test tile
          if ( tileValues_[ row ][ col ] == tileValues_[ n ][ col ] ) {
            // "Merge" the two tiles_
            mergeTiles( keyCode, row, col, n );
            
            if ( tileValues_[ row ][ col ] != 0 ) {
              hasMoved = true;
              nBlanks_++;
            } // end if
          } // end if
        } // end for
        
        // Pack tiles_ to the top
        for ( int i = SIZE - 1; i >= 0; i-- )
          if ( tileValues_[ i ][ col ] != 0 ) {
            boolean isMovable = false;
            // Scan for the topmost empty tile
            int n = i;
            while ( n < SIZE - 1 && tileValues_[ n + 1 ][ col ] == 0 ) {
              n++;
              isMovable = true;
            } // end while
              
            // Copy the test tile to the empty tile
            if ( isMovable ) {
              tileValues_[ n ][ col ] = tileValues_[ i ][ col ];
              tileValues_[ i ][ col ] = 0;
              hasMoved = true;
            } // end if
          } // end if
            
      } // end for
    } // end else-if
    
    if ( hasMoved ) {
      spawnNewTile( CAN_SPAWN_FOUR );
      nMoves_++;
    } // end if
  } // end method move
  
  /**
   * Spawn a 2-tile or a 4-tile on a random blank tile.
   *  
   * @param canSpawnFour  Specifies if the game should try to spawn a 
   *                      4-tile or not
   */
  private void spawnNewTile( boolean canSpawnFour )
  {
    // 10% chance to spawn a 4-tile
    final double CHANCE_TO_SPAWN_4 = 0.9;
    
    Random random = new Random();
    double spawnDice;
    
    // Determine if tile has a chance to spawn a 4
    if ( canSpawnFour )
      spawnDice = random.nextDouble();
    else
      spawnDice = 0.0;
    
    // Choose a random tile from the board
    int row    = random.nextInt( SIZE );
    int column = random.nextInt( SIZE );
    
    // Continue choosing a tile until the chosen tile is empty
    while ( tileValues_[ row ][ column ] != 0 ) {
      row    = random.nextInt( SIZE );
      column = random.nextInt( SIZE );
    } // end while
    
    // Spawn a 2 or 4 on the board
    if ( spawnDice <= CHANCE_TO_SPAWN_4 )
      tileValues_[ row ][ column ] = 2;
    else
      tileValues_[ row ][ column ] = 4;
      
    // Reduce the number of blanks by 1
    nBlanks_--;
  } // end method spawnNewTile
  
  /** 
   * Update the tiles on the board with the current tile values
   */
  private void refreshBoard()
  {
    for ( int i = 0; i < tiles_.length; i++ )
      for ( int j = 0; j < tiles_[ i ].length; j++ )
        tiles_[ i ][ j ].setIcon( createImageIcon( tileValues_[ i ][ j ] ) );
        
  } // end method refreshBoard
  
  /**
   * Update the score. Also store the largest tile present.
   *
   * @param points    Number of points to add to the score
   */
  private void updateScore( int points )
  {
    score_ += points;
    scoreLabel_.setText( String.format( "Score: %,d", score_ ) );
    
    if ( points > largestTile_ )
      largestTile_ = points;
  } // end updateScore
  
  /**
   * Checks if there are possible moves. The game ends when there are no more
   * possible moves.
   *
   * @return    True if movement is still possible.
   */
  private boolean canMove()
  {
    if ( nBlanks_ == 0 ) {
      // Check for presence of pairs horizontally
      for ( int row = 0; row < SIZE; row++ )
        for ( int col = 0; col < SIZE - 1; col++ )
          if ( tileValues_[ row ][ col ] == tileValues_[ row ][ col + 1 ] )
            return true;
            
      // Check for presence of pairs vertically
      for ( int col = 0; col < SIZE; col++ )
        for ( int row = 0; row < SIZE - 1; row++ )
          if ( tileValues_[ row ][ col ] == tileValues_[ row + 1 ][ col ] )
            return true;
            
      // Return false if the above loops failed to look for pairs, in
      // which case the game ends.
      return false;
    } // end if
    else
      // Return true if there are still empty tiles ( of course )
      return true;
    
  } // end method canMove
} // end class