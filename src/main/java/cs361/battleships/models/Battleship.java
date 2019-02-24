package cs361.battleships.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.mchange.v1.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Battleship extends Ship {
    private boolean ccHit;//FIXME: Add a battleshipcchit to atackStatus that we can check to see if it's been hit

    public Battleship(){
        this.kind = "BATTLESHIP";
        size = 4;
        ccHit = false;
    }

    @Override
    public Square getCC(){
        return occupiedSquares.get(2);
    }

    @Override
    public List<Result> sinkMe(){// Return a list of hits on the ship
        List<Result> HitMe = new ArrayList<>();
        if(ccHit){// Second hit sinks the ship
            Result tmpR;
            for (int i = 0; i < occupiedSquares.size();i++){
                tmpR = this.attack(occupiedSquares.get(i).getRow(),occupiedSquares.get(i).getColumn());
                if(tmpR.getResult() != AtackStatus.INVALID){
                    HitMe.add(tmpR);
                }
            }
        }
        else {
            ccHit = true;
        }
        return HitMe;
    }

    @Override
    public boolean getCChit(){
        return ccHit;
    }
}
