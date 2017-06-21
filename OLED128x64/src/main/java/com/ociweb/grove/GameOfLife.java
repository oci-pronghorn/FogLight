package com.ociweb.grove;

import com.ociweb.gl.api.StartupListener;
import com.ociweb.gl.api.TimeListener;
import com.ociweb.iot.grove.Grove_OLED_128x64;
import com.ociweb.iot.maker.FogCommandChannel;

import static com.ociweb.iot.grove.Grove_OLED_128x64_Constants.*;

public class GameOfLife implements StartupListener, TimeListener {
	private FogCommandChannel ch;
	private int[][] cur_state = new int[row_count][col_count];
	private int[][] next_state = new int[row_count][col_count];
	public GameOfLife(FogCommandChannel ch){
		this(ch, def_start);
	}


	public GameOfLife(FogCommandChannel ch, int[][] start_state){
		this.ch = ch;
		cur_state = start_state;
	}

	public void ageUniverse(){
		for (int row = 0; row < row_count; row ++){
			for (int col = 0; col < col_count; col++){
				ageCell(row,col);
			}
		}
		cur_state = next_state;
	}
	
	private void ageCell(int row, int col){
		int nb_alive = tallyAliveNeighbors(row,col);
		if (cur_state[row][col]==1){
			ageLivingCell(row,col,nb_alive);
		}
		else{
			ageDeadCell(row,col,nb_alive);
		}
		
	}
	private void ageLivingCell(int row, int col, int living_neighbors){
		switch(living_neighbors){
		
		//"Any live cell with fewer than two live neighbours dies, as if caused by underpopulation."-Wikipedia
		case 0: 
		case 1:
			next_state[row][col] = 0;
			break;
		
		//"Any live cell with two or three live neighbours lives on to the next generation."-Wikipedia
		case 2:
		case 3:
			next_state[row][col] = 1;
			break;
		
		//"Any live cell with more than three live neighbours dies, as if by overpopulation."-Wikipedia
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
			next_state[row][col] = 0;
			break;
			
		}
	}

	private void ageDeadCell(int row, int col, int living_neighbors){
		switch(living_neighbors){
		case 3:
			next_state[row][col] = 1;
			break;
		default:
			next_state[row][col] = 0;
			break;
		}
	}
	private int tallyAliveNeighbors(int row, int col){
		int tally = 0;
		for (int deltaRow = -1; deltaRow < 2; deltaRow++){
			for (int deltaCol = -1; deltaCol < 2; deltaCol++){
				//logic to guard against indexOutOfBound
				if (! (deltaRow == 0 && deltaCol ==0) ){
					if (isInRange(row+deltaRow, 0,row_count) && isInRange(col+deltaCol, 0, col_count)){
						tally += cur_state[row+deltaRow][col+deltaCol];
					}
				}
			}
		}
		return tally;
	}
	/**
	 * deterimes if a number is in [lowerbound, higherbound).
	 * @param num
	 * @param lower_bound is inclusive
	 * @param higher_bound is exclusive
	 * @return true if num is within the range, false otherwise
	 */
	private boolean isInRange(int num, int lower_bound, int higher_bound){
		return num >= lower_bound && num < higher_bound; 
	}

	@Override
	public void startup() {
		Grove_OLED_128x64.init(ch);
	}
	
	@Override
	public void timeEvent(long time, int iteration) {
		ageUniverse();
		Grove_OLED_128x64.displayImage(ch, cur_state);
	}
	
	private static int[][] def_start = testImage;

}
