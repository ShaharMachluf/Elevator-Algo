/**/package ex0.algo;

import ex0.Building;
import ex0.CallForElevator;
import ex0.Elevator;

import java.util.ArrayList;

public class MyElev implements ElevatorAlgo {
    public static final int UP = 1, LEVEL = 0, DOWN = -1, ERROR = -2;
    public static final int INIT=0, GOING2SRC=1, GOIND2DEST=2, DONE=3; //CMD
    private Building _building;
    private int elevCol;
    public ArrayList<CallForElevator>[] list;

    public MyElev(Building b) {
        _building = b;
        elevCol = _building.numberOfElevetors();
        list = new ArrayList[elevCol];
        setList(list);//reset the list
    }

    /**
     * init the call list of each elevator in the array
     * @param list array of array lists for elevator calls
     */
    public void setList(ArrayList<CallForElevator>[] list) {
        for (int i = 0; i < elevCol; i++) {
            this.list[i] = new ArrayList<CallForElevator>();

        }
    }


    @Override
    public Building getBuilding() {
        return _building;
    }

    @Override
    public String algoName() {
        return "our algo";
    }

    /**
     * This method is the main optimal allocation (aka load-balancing) algorithm for allocating the
     * "best" elevator for a call (over all the elevators in the building).
     *
     * @param c the call for elevator (src, dest)
     * @return the index of the elevator to which this call was allocated to.
     **/



    @Override
    public int allocateAnElevator(CallForElevator c) {
        //check if there is an elevator at the same floor
        for (int i = 0; i < _building.numberOfElevetors(); i++) {
            Elevator curr = _building.getElevetor(i);
            if (curr.getState() == LEVEL && curr.getPos() == c.getSrc() && list[i].size()<4) {
                list[i].add(c);
                return i;
            }
        }
        //check if there is an elevator going to the same dest, if there is more then one, send the nearest
        boolean flag = false;
        int nearest = rand(0,_building.numberOfElevetors());
        for (int i = 0; i < _building.numberOfElevetors(); i++) {
            Elevator curr = _building.getElevetor(i);
            if(!list[i].isEmpty()) {
                CallForElevator other = list[i].get(0);
                //check if the elevator meets the condition
                if (other.getDest() == c.getDest() && other.getType() == c.getType()&&c.getType() == UP&&curr.getPos() < c.getSrc()) {
                    //compare between the current elevator and nearest one from before
                    if (distance(i, c.getSrc()) < distance(nearest, c.getSrc())&&distance(i, c.getSrc())>1&&list[i].size()<2) {
                        flag = true;
                        nearest = i;
                    }
                }
                //check if the elevator meets the condition
                if (other.getDest() == c.getDest() && other.getType() == c.getType()&&c.getType() == DOWN&&curr.getPos() > c.getSrc()){
                    //compare between the current elevator and nearest one from before
                    if (distance(i, c.getSrc()) < distance(nearest, c.getSrc())&&distance(i, c.getSrc())>1&&list[i].size()<2) {
                        nearest = i;
                        flag = true;
                    }


                }
            }
        }

        if (flag) {
            list[nearest].add(0, c);
            return nearest;
        }
        flag = false;
        int closest = rand(0, _building.numberOfElevetors()-1);
        //check if there is an elevator that is going to the src floor and moving to the same direction
        if (c.getType() == UP) {
            for (int i = 0; i < _building.numberOfElevetors(); i++) {
                Elevator curr = _building.getElevetor(i);
                if(!list[i].isEmpty()) {
                    CallForElevator other = list[i].get(0);
                    //check if the elevator meets the conditions and compare to the closest one from before
                    if (curr.getState() == UP && curr.getPos() < c.getSrc() && distance(i, c.getSrc()) < distance(closest, c.getSrc()) && other.getSrc() <= c.getSrc() && other.getDest() >= c.getSrc()&&distance(i, c.getSrc())>1&&list[i].size()<2) {
                        closest = i;
                        flag = true;
                    }
                }
            }
        } else {
            for (int i = 0; i < _building.numberOfElevetors(); i++) {
                Elevator curr = _building.getElevetor(i);
                if (!list[i].isEmpty()) {
                    CallForElevator other = list[i].get(0);
                    //check if the elevator meets the conditions and compare to the closest one from before
                    if (curr.getState() == DOWN && curr.getPos() > c.getSrc() && distance(i, c.getSrc()) < distance(closest, c.getSrc()) && other.getSrc() >= c.getSrc() && other.getDest() <= c.getSrc()&&distance(i, c.getSrc())>1&&list[i].size()<2) {
                        closest = i;
                        flag = true;
                    }
                }
            }
        }
        if (flag) {
            list[closest].add(0,c);
            return closest;
        }
        //check what is the closest elevator that doesn't move
        for (int i = 0; i < _building.numberOfElevetors(); i++) {
            Elevator curr = _building.getElevetor(i);
            //check if the elevator meets the conditions and compare to the closest one from before
            if (curr.getState() == LEVEL && distance(i, c.getSrc()) < distance(closest, c.getSrc())&&list[i].size()<4) {
                closest = i;
                flag = true;
            }
        }
        if (flag) {
            list[closest].add(c);
            return closest;
        }
        //check if there is an elevator that has less than 3 calls and it's last dest is the closest to this src floor
        for (int i = 0; i < _building.numberOfElevetors(); i++) {
            int curr_length = list[i].size();
            int closest_length = list[closest].size();
            //check if the elevator meets the conditions
            if(!list[i].isEmpty()&&!list[closest].isEmpty()) {
                int curr_dist = Math.abs(list[i].get(curr_length - 1).getDest() - c.getSrc());
                int closest_dist = Math.abs(list[closest].get(closest_length - 1).getDest() - c.getSrc());
                //compare the elevator to the closest one from before
                if (curr_length <= 2 && curr_dist < closest_dist) {
                    closest = i;
                }
            }
        }
        list[closest].add(c);
        return closest;
    }

    /**
     * this method checks how much time it will take for an elevator to reach a certain floor
     * @param index the index representing a certain elevator
     * @param floor the source floor of the call
     * @return
     */
    private double distance ( int index, int floor){
        Elevator curr = _building.getElevetor(index);
        int count = count_stops(index, floor);
        double start = curr.getStartTime();
        double stop = curr.getStopTime();
        double close = curr.getTimeForClose();
        double open = curr.getTimeForOpen();
        double travelTime = curr.getSpeed() / (Math.abs(curr.getPos() - floor));
        if (curr.getState() == LEVEL) {
            return start + stop + close + open + travelTime;
        } else if (curr.getState() == UP || curr.getState() == DOWN) {
            return stop + open + travelTime + count * (start + stop + close + open);
        } else {
            return Double.MAX_VALUE;
        }
    }

    /**
     * count the stops that an elevator needs to do before it gets to a certain floor
     * @param index the index representing a certain elevator
     * @param floor the source floor of the call
     * @return
     */
    private int count_stops ( int index, int floor){
        int direction, count = 0;
        Elevator curr = _building.getElevetor(index);
        if (curr.getPos() < floor) {
            direction = UP;
            for (int j = 0; j < list[index].size() && list[index].get(j).getType() == direction && curr.getPos() < floor; j++) {
                count++;
            }
        } else {
            direction = DOWN;
            for (int j = 0; j < list[index].size() && list[index].get(j).getType() == direction && curr.getPos() > floor; j++) {
                count++;
            }
        }
        return count;
    }


    @Override
    public void cmdElevator ( int elev) {

        if (list[elev].isEmpty()) { //if there is no calls to this elevator
            return;
        }
        removeDone(list[elev]);
        Elevator curr = this.getBuilding().getElevetor(elev);


        if (!list[elev].isEmpty() ) {//&&list[elev].get(0).getState()==LEVEL ) {

            int state = list[elev].get(0).getState(); //INIT=0, GOING2SRC=1, GOIND2DEST=2, DONE=3;

            if (list[elev].size() > 1 && list[elev].get(0).getTime(GOING2SRC) > list[elev].get(1).getTime(GOING2SRC)) {
                OnTheWay(elev,curr);
            }

            if (state == GOING2SRC && curr.getPos() != list[elev].get(0).getSrc()) {//the state is going to src:
                int k = list[elev].get(0).getSrc();
                curr.goTo(k);
            } else {
                          /*  if (list[elev].size() >= 2) {
                                OnTheWay(elev, curr);
                            }*/
                int k = list[elev].get(0).getDest();
                curr.goTo(k);
            }
        }

    }

    private void OnTheWay(int elev,Elevator curr){
        CallForElevator A = list[elev].get(0);
        CallForElevator B = list[elev].get(1);
        if (curr.getState() == UP) {
            if (A.getState() == GOING2SRC && B.getState() == GOING2SRC) {
                curr.goTo(Integer.min(A.getSrc(), B.getSrc()));
            }
            if (A.getState() == GOIND2DEST && B.getState() == GOING2SRC) {
                curr.goTo(Integer.min(A.getDest(), B.getSrc()));
            }
            if (A.getState() == GOING2SRC && B.getState() == GOIND2DEST) {
                curr.goTo(Integer.min(A.getSrc(), B.getDest()));
            }
            if (A.getState() == GOIND2DEST && B.getState() == GOIND2DEST) {
                curr.goTo(Integer.min(A.getDest(), B.getDest()));
            }
        }
        if (curr.getState() == DOWN) {
            if (A.getState() == GOING2SRC && B.getState() == GOING2SRC) {
                curr.goTo(Integer.max(A.getSrc(), B.getSrc()));
            }
            if (A.getState() == GOIND2DEST && B.getState() == GOING2SRC) {
                curr.goTo(Integer.max(A.getDest(), B.getSrc()));
            }
            if (A.getState() == GOING2SRC && B.getState() == GOIND2DEST) {
                curr.goTo(Integer.max(A.getSrc(), B.getDest()));
            }
            if (A.getState() == GOIND2DEST && B.getState() == GOIND2DEST) {
                curr.goTo(Integer.max(A.getDest(), B.getDest()));
            }
        }
    }



    /**
     * return a random in [min,max)
     * @param min
     * @param max
     * @return
     */

    private static int rand ( int min, int max){
        if (max < min) {
            throw new RuntimeException("ERR: wrong values for range max should be >= min");
        }
        int ans = min;
        double dx = max - min;
        double r = Math.random() * dx;
        ans = ans + (int) (r);
        return ans;
    }

    private void removeDone(ArrayList<CallForElevator> l){
        for(int i=0;i<l.size();i++){
            if(l.get(0).getState()==DONE){
                l.remove(i);
            }
        }

    }


}////