package cs361.battleships.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Board {

	@JsonProperty private List<Ship> ships;
	@JsonProperty private List<Result> attacks;
	@JsonProperty private int numSonar;//FIXME: Add a sonar result type that we count to see how may sonar we have made

	/*
	DO NOT change the signature of this method. It is used by the grading scripts.
	 */
	public Board() {
		ships = new ArrayList<>();
		attacks = new ArrayList<>();
		numSonar = 0;
	}

	/*
	DO NOT change the signature of this method. It is used by the grading scripts.
	 */
	public boolean placeShip(Ship ship, int x, char y, boolean isVertical) {
		if (ships.size() >= 3) {
			return false;
		}
		if (ships.stream().anyMatch(s -> s.getKind().equals(ship.getKind()))) {
			return false;
		}
		final Ship placedShip;
		if(ship.getKind().equals("MINESWEEPER")){
			placedShip = new Minesweeper();
		}
		else if(ship.getKind().equals("BATTLESHIP")){
			placedShip = new Battleship();
		}
		else if(ship.getKind().equals("DESTROYER")){
			placedShip = new Destroyer();
		}
		else {
			return false;
		}
		placedShip.place(y, x, isVertical);
		if (ships.stream().anyMatch(s -> s.overlaps(placedShip))) {
			return false;
		}
		if (placedShip.getOccupiedSquares().stream().anyMatch(s -> s.isOutOfBounds())) {
			return false;
		}
		ships.add(placedShip);
		return true;
	}

	/*
	DO NOT change the signature of this method. It is used by the grading scripts.
	 */
	public Result attack(int x, char y, boolean useSonar) {
		Result attackResult = attack(new Square(x, y),useSonar);
		attacks.add(attackResult);
		return attackResult;
	}

	public void sonarAtk(int x,char y){//FIXME: Write test functionss
		Square tmpS = new Square(x,y);
		Result tmpR = attack(tmpS,true);
		if(tmpR.getResult() == AtackStatus.HIT){// Center of the sonar
			tmpR.getLocation().nohit();
			tmpR.setShip(null);
			tmpR.setResult(AtackStatus.SONARHIT);
			attacks.add(tmpR);
		}
		else if(tmpR.getResult() == AtackStatus.MISS){
			tmpR.setResult(AtackStatus.SONARMISS);
			attacks.add(tmpR);
		}

		//If it's not hit or miss then it's invalid and out of bounds in witch case we don't care
	}

	public void sonar(int x, char y){//FIXME: Write test functions
		if(numSonar < 2){
			for(int i = 0; i < attacks.size(); i++){// Search for the first sunk ship
				if(attacks.get(i).getResult() == AtackStatus.SUNK){
					int tmp = (int) y;
					sonarAtk(x,y);// Center
					sonarAtk(x+1,y);// Up one
					sonarAtk(x+2,y);// Up two
					sonarAtk(x-1,y);// Down one
					sonarAtk(x-2,y);// Down two
					sonarAtk(x,(char)(tmp+1));// Right one
					sonarAtk(x,(char)(tmp+2));// Right two
					sonarAtk(x,(char)(tmp-1));// Left one
					sonarAtk(x,(char)(tmp-2));// Left two
					sonarAtk(x+1,(char)(tmp+1));// Top right
					sonarAtk(x+1,(char)(tmp-1));// Top left
					sonarAtk(x-1,(char)(tmp+1));// Bottom right
					sonarAtk(x-1,(char)(tmp-1));// Bottom left
					numSonar++;
					return;
				}
			}
		}
	}

	private Result attack(Square s,boolean useSonar) {
		if(!useSonar){
			int numItems = attacks.size();
			for(int i = 0; i < numItems; i++){
				if(attacks.get(i).getResult() == AtackStatus.SONARHIT || attacks.get(i).getResult() == AtackStatus.SONARMISS){
					attacks.remove(i);
					i--;
					numItems--;
				}
			}
		}

		var shipsAtLocation = ships.stream().filter(ship -> ship.isAtLocation(s)).collect(Collectors.toList());
		if (shipsAtLocation.size() == 0) {
			if (attacks.stream().anyMatch(r -> r.getLocation().equals(s))) {
				var attackResult = new Result(s);
				attackResult.setResult(AtackStatus.INVALID);
				return attackResult;
			}
			var attackResult = new Result(s);
			return attackResult;
		}
		if(!useSonar){
			var hitShip = shipsAtLocation.get(0);
			var attackResult = hitShip.attack(s.getRow(), s.getColumn());
			if(hitShip.getCC().equals(s)){//If we've hit the cc
				List<Result> tmpL = hitShip.sinkMe();
				if(tmpL.size() != 0){
					for(int i = 0; i < tmpL.size();i++){
						attacks.add(tmpL.get(i));// Add attacks for all the other squares
					}
					attackResult = tmpL.get(tmpL.size()-1);//Grab the sunk result
				}
			}
			if (attackResult.getResult() == AtackStatus.SUNK) {
				if (ships.stream().allMatch(ship -> ship.isSunk())) {
					attackResult.setResult(AtackStatus.SURRENDER);
				}
			}
			return attackResult;
		}
		else {
			var attackResult = new Result(s);
			attackResult.setResult(AtackStatus.HIT);
			return attackResult;
		}

	}

	List<Ship> getShips() {
		return ships;
	}

	List<Result> getAtacks(){//Test function
		return attacks;
	}

	void addToAtacks(Result tmp){//Test helper function
		attacks.add(tmp);
	}
}
