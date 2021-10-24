package ex0.algo;

import ex0.Building;
import ex0.CallForElevator;
import ex0.Elevator;
import ex0.simulator.Builging_A;
import ex0.simulator.ElevetorCallList;

import java.util.ArrayList;

public class MyElev implements ElevatorAlgo{
    public static final int UP = 1, LEVEL = 0, DOWN = -1, ERROR = -2;
    private Building _building;
    private ElevetorCallList [] list = new ElevetorCallList[_building.numberOfElevetors()];
    public MyElev(Building b) {
        _building = b;
    }
    @Override
    public Building getBuilding() {
        return _building;
    }

    @Override
    public String algoName() {
        return "ex0_MyElevator";
    }

    @Override
    public int allocateAnElevator(CallForElevator c) {
        //check if there is an elevator at the same floor
        for(int i=0; i<_building.numberOfElevetors();i++){
            Elevator curr=_building.getElevetor(i);
            if(curr.getState()==LEVEL&&curr.getPos()==c.getSrc()){
                list[i].add(c);
               return i;
            }
        }
        //check if there is an elevator going to the same dest, if there is more then one, send the nearest
        boolean flag=false;
        int nearest=0;
        for(int i=0;i<_building.numberOfElevetors();i++){
            Elevator curr=_building.getElevetor(i);
            CallForElevator other=list[i].get(0);
            if(other.getDest()==c.getDest()&&other.getType()==c.getType()&&curr.getPos()<c.getSrc()){
                flag=true;
                if(distance(i,c.getSrc())>distance(nearest,c.getSrc())){
                    nearest=i;
                }
            }
        }
        if(flag){
            list[nearest].add(0,c);
            return nearest;
        }
        flag=false;
        int closest=0;
        //check if there is an elevator that is going to the src floor and moving to the same direction
        if(c.getType()==UP){
            for(int i=0; i<_building.numberOfElevetors();i++){
                Elevator curr=_building.getElevetor(i);
                if(curr.getState()==UP&&curr.getPos()<c.getSrc()&&distance(i,c.getSrc())<distance(closest,c.getSrc())){
                    closest=i;
                    flag=true;
                }
            }
        }
        else{
            for(int i=0; i<_building.numberOfElevetors();i++){
                Elevator curr=_building.getElevetor(i);
                if(curr.getState()==DOWN&&curr.getPos()>c.getSrc()&&distance(i,c.getSrc())<distance(closest,c.getSrc())){
                    closest=i;
                    flag=true;
                }
            }
        }
        if(flag) {
            list[closest].add(c);
            return closest;
        }
        //check what is the closest elevator that doesn't move
        for(int i=0; i<_building.numberOfElevetors();i++){
            Elevator curr=_building.getElevetor(i);
            if(curr.getState()==LEVEL&&distance(i,c.getSrc())<distance(closest,c.getSrc())) {
                closest = i;
                flag=true;
            }
        }
        if(flag) {
            list[closest].add(c);
            return closest;
        }
        //check if there is an elevator that has less than 3 calls and it's last dest is the closest to this src floor
        for(int i=0;i<_building.numberOfElevetors();i++){
            int curr_length=list[i].size();
            int closest_length=list[closest].size();
            int curr_dist=Math.abs(list[i].get(curr_length-1).getDest()-c.getSrc());
            int closest_dist=Math.abs(list[closest].get(closest_length-1).getDest()-c.getSrc());
            if(curr_length<=2&&curr_dist<closest_dist){
                closest=i;
            }
        }
        return closest;
    }

    /*
    this method checks how much time it will take for an elevator to reach a certain floor
     */
    private double distance(int index, int floor){
        Elevator curr=_building.getElevetor(index);
        int count=count_stops(index,floor);
        double start= curr.getStartTime();
        double stop= curr.getStopTime();
        double close=curr.getTimeForClose();
        double open= curr.getTimeForOpen();
        double travelTime=curr.getSpeed()/(Math.abs(curr.getPos()-floor));
        if(curr.getState()==LEVEL){
            return start+stop+close+open+travelTime;
        }
        else if(curr.getState()==UP||curr.getState()==DOWN){
            return stop+open+travelTime+count*(start+stop+close+open);
        }
        else{
            return Double.MAX_VALUE;
        }
    }

    /*
    count the stops that an elevator needs to do before it gets to a certain floor
     */
    private int count_stops(int index, int floor){
        int direction, count=0;
        Elevator curr=_building.getElevetor(index);
        if(curr.getPos()<floor){
            direction=UP;
            for(int j=0;j<list[index].size()&&list[index].get(j).getType()==direction&&curr.getPos()<floor;j++){
                count++;
            }
        } else{
            direction=DOWN;
            for(int j=0;j<list[index].size()&&list[index].get(j).getType()==direction&&curr.getPos()>floor;j++){
                count++;
            }
        }
        return count;
    }

    @Override
    public void cmdElevator(int elev) {

    }
}
