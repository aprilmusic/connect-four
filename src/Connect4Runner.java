import java.util.*;

import javax.swing.JOptionPane;

import info.gridworld.grid.*;
import info.gridworld.world.*;

public class Connect4Runner {
	private List<Location> FourInARowlocs = new ArrayList<Location>();
	private boolean gameOver = false;
	private  final int NUM_ROWS ;
	private  final int NUM_COLS ;
	private int[][] grid;
	private boolean redsTurn;
	private int highlightedColumn = 0;
	Location dropLoc= new Location(0,highlightedColumn);
	Location mostRecent = new Location(0, highlightedColumn);

	private World world = new World() {
		@Override
		public boolean keyPressed(String key, Location loc) {
			System.out.println(key);
			if (key.equals("RIGHT")){
				right();
			}
			if (key.equals("LEFT")){
				left();
			}
			if (key.equals("SPACE")){
				
				drop();
				
				if (gameOver()){
					String result = "";
					if (redsTurn){
						result = "Black";
						for (Location l: FourInARowlocs){
							world.add(l, new WinningBlackChecker());
						}
					}
					else{
						result = "Red";
						for (Location l: FourInARowlocs){
							world.add(l, new WinningRedChecker());
						}
					}
					JOptionPane.showMessageDialog(null, "Game Over! " + result + " wins!");
					System.exit(0);
				}
			}
			
			return true;
		}
	};

	public Connect4Runner(int rows, int cols) {
		this.NUM_ROWS=rows;
		this.NUM_COLS=cols;
	}

	/**
	 * Try to drop a checker in the highlighted col.  If a checker can be dropped in that
	 * col (that column is not full), then a checker of the current player's color is 
	 * "dropped" as low as possible in that col.  The turn is advanced.  If that col
	 * is full, then the attempt is ignored
	 */
	protected void tryMove() {


		redsTurn = !redsTurn;
	}

	private void drop() {

		if (redsTurn){
			if (lowest(highlightedColumn) == null){
				
				return;
			}
			dropLoc = lowest(highlightedColumn);
			mostRecent = new Location(dropLoc.getRow(), dropLoc.getCol());
			grid[dropLoc.getRow()-1][dropLoc.getCol()] = 1;
			clearFaintCheckers();
			if (lowest(highlightedColumn) == null){
				redsTurn = !redsTurn;
				showHighlightedSpot();
				updateGrid();
				return;
			}
			dropLoc = lowest(dropLoc.getCol());
			grid[dropLoc.getRow()-1][dropLoc.getCol()] = 4;

		}
		else{
			if (lowest(highlightedColumn) == null){
				return;
			}
			dropLoc = lowest(highlightedColumn);
			mostRecent = new Location(dropLoc.getRow(), dropLoc.getCol());
			grid[dropLoc.getRow()-1][dropLoc.getCol()] = 2;
			clearFaintCheckers();
			if (lowest(highlightedColumn) == null){
				redsTurn = !redsTurn;
				showHighlightedSpot();
				updateGrid();
				return;
			}
			dropLoc = lowest(dropLoc.getCol());
			grid[dropLoc.getRow()-1][dropLoc.getCol()] = 3;
		}
		redsTurn = !redsTurn;
		showHighlightedSpot();
		updateGrid();
		
	}
	
	private void right(){
		if (highlightedColumn <NUM_COLS-1){
			clearFaintCheckers();
			highlightedColumn ++;
			dropLoc = lowest(highlightedColumn);
			clearTopRow();
			if (redsTurn){
				showHighlightedSpot();
				if (dropLoc != null)
					grid[dropLoc.getRow()-1][dropLoc.getCol()] = 3;
				
			}
			else{
				showHighlightedSpot();
				if (dropLoc != null)
					grid[dropLoc.getRow()-1][dropLoc.getCol()] = 4;
			}
		}
		showHighlightedSpot();
		updateGrid();
	}
	
	private void left(){
		if (highlightedColumn >0){
			clearFaintCheckers();
			highlightedColumn --;
			dropLoc = lowest(highlightedColumn);
			clearTopRow();
			if (redsTurn){
				if (dropLoc != null)
					grid[dropLoc.getRow()-1][dropLoc.getCol()] = 3;
			}
			else{
				if (dropLoc != null)
					grid[dropLoc.getRow()-1][dropLoc.getCol()] = 4;
			}
		}
		showHighlightedSpot();
		updateGrid();
	}
	
	private void showInfo() {
		JOptionPane.showMessageDialog(null, "GAME OVER!! Hit 'R' key to restart");
		
	}

	public static void main(String[] args) {
		new Connect4Runner(6,7).start();
	}

	/**
	 * Shows the current players checker above the board and a faint image of the
	 * checker where it would be placed if dropped.
	 */
	protected void showHighlightedSpot() {
		if (redsTurn){
			clearTopRow();
			world.add(new Location(0,highlightedColumn), new RedCheckerFaint());
			
		}
		else{
			clearTopRow();
			world.add(new Location(0,highlightedColumn), new BlackCheckerFaint());
		}
	}

	private Location lowest(int col) {
		int row = NUM_ROWS;
		while((grid[row-1][col] == 1||grid[row-1][col] == 2) && row-1>0){
			row --;
		}
		if (row == 1 && (grid[row-1][col] == 1|| grid[row-1][col] == 2)){
			return null;
		}
		return new Location(row,col);
	}

	private void clearFaintCheckers() {
		for(int r=0; r<this.NUM_ROWS;r++) {
			for(int c=0; c<this.NUM_COLS;c++) {
				if(grid[r][c] == 3||grid[r][c] == 4){
					grid[r][c] = 0;
				}
				
			}
		}
		updateGrid();
	}

	private void clearTopRow() {
		for(int c=0;c<this.NUM_COLS;c++) {
			world.remove(new Location (0,c));
		}
	}

	private void start() {
		gameOver = false;
		world.setGrid(new BoundedGrid(NUM_ROWS+1, NUM_COLS));
		grid = new int[NUM_ROWS][NUM_COLS];
		for (int r = 0; r<NUM_ROWS; r++){
			for (int c = 0; c<NUM_COLS; c++){
				if (grid[r][c] == 0){
					world.add(new Location(r+1,c), new EmptySquare());
					
				}
			}
		}
		//clearGrid();
		world.show();// shows the world
		pickRandomColorToStart();
		this.showHighlightedSpot();
		
		
	}
	
	
	/**
	 * Check to see if there are any 4-in-a-rows on the board.  This could
	 * be done more efficiently if the location of the latest checker is remembered.
	 * @return true if game is over.
	 */
	private boolean gameOver() {
		
		return (fourHor(mostRecent) || fourVert(mostRecent) || fourDiag(mostRecent) || fourDiag2(mostRecent));
	}
	
	private boolean fourDiag(Location rec){
		int recRow = rec.getRow()-1;
		int recCol = rec.getCol();
		int cur = -3;
		while (recRow + cur < 0 || recCol + cur < 0){
			cur ++;
		}
		int count = 0;
		for (; cur < 4 && recCol + cur < NUM_COLS && recRow + cur < NUM_ROWS; cur ++){
			if (grid[recRow + cur][recCol + cur] == grid[recRow][recCol]){
				count ++;
			}
			else{
				count = 0;
			}
			if (count == 4){
				for (int i=0; i<4; i++){
					FourInARowlocs.add(new Location (recRow + cur - i + 1, recCol + cur - i));
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean fourDiag2(Location rec){
		int recRow = rec.getRow()-1;
		int recCol = rec.getCol();
		int cur = -3;
		while (recRow - cur >= NUM_ROWS || recCol + cur < 0){
			cur ++;
		}
		int count = 0;
		for (; cur < 4 && recCol + cur >= 0 && recCol + cur < NUM_COLS && recRow - cur < NUM_ROWS && recRow - cur >=0; cur ++){
			if (grid[recRow - cur][recCol + cur] == grid[recRow][recCol]){
				count ++;
			}
			else{
				count = 0;
			}
			if (count == 4){
				for (int i=0; i<4; i++){
					FourInARowlocs.add(new Location (recRow - cur + i + 1, recCol + cur - i));
				}
				return true;
			}
		}
		return false;
	}

	private boolean fourHor(Location rec) {
		int recRow = rec.getRow();
		int recCol = rec.getCol();
		int cur = recCol - 3;
		if (cur < 0){
			cur = 0;
		}
		int count = 0;
		for (; cur < recCol + 4 && cur < NUM_COLS; cur ++){
			if (grid[recRow-1][cur] == grid[recRow-1][recCol]){
				count ++;
			}
			else{
				count = 0;
			}
			if (count == 4){
				for (int i=0; i<4; i++){
					FourInARowlocs.add(new Location (recRow, cur - i));
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean fourVert(Location rec){
		int recRow = rec.getRow();
		int recCol = rec.getCol();
		int cur = recRow - 3;
		if (cur < 1){
			cur = 1;
		}
		int count = 0;
		for (; cur < recRow + 4 && cur < NUM_ROWS + 1; cur ++){
			if (grid[cur-1][recCol] == grid[recRow-1][recCol]){
				count ++;
			}
			else{
				count = 0;
			}
			if (count == 4){
				for (int i=0; i<4; i++){
					FourInARowlocs.add(new Location (cur - i, recCol));
				}
				return true;
			}
		}
		return false;
	}
	



	// Randomly choose between the red and black player.
	private void pickRandomColorToStart() {
		this.redsTurn=Math.random()>=.5;
	}

	// clear out the grid so a new game can be started.  Basically go to each 
	// location and remove the object there.
	private void clearGrid() {
		for(int r=1; r<this.NUM_ROWS+1;r++) {
			for(int c=0; c<this.NUM_COLS;c++) {
				world.add(new Location(r,c), new EmptySquare());
			}
		}
	}

	private void updateGrid(){
		for (int r = 0; r < grid.length; r++){
			for (int c = 0; c< grid[0].length; c++){
				switch (grid[r][c]){
				case 0:
					world.add(new Location(r+1,c), new EmptySquare());
					break;
				case 1:
					world.add(new Location(r+1,c), new RedChecker());
					break;
				case 2:
					world.add(new Location(r+1,c), new BlackChecker());
					break;
				case 3:
					world.add(new Location(r+1,c), new RedCheckerFaint());
					break;
				case 4:
					world.add(new Location(r+1,c), new BlackCheckerFaint());
					break;
				default:
					world.add(new Location (r+1,c), new EmptySquare());
				}

			}
		}
	}


}
