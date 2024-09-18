package komjenumee;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.HashSet;
import java.util.Set;

import static komjenumee.RobotPlayer.directions;

public class Pathfinder {
    static Set<MapLocation> cache = new HashSet<>();


    public boolean move(RobotController robotController, MapLocation flagLocation) throws GameActionException {
        return  move(robotController, flagLocation, false);
    }

    private boolean move(RobotController robotController, MapLocation flagLocation, boolean canTravelAgain) throws GameActionException {
        // kijk van alle buren de cost
        // move naar laagste cost
        MapLocation botlocation = robotController.getLocation();

        int currentLowestCost = Integer.MAX_VALUE;
        Direction best = null;
        for (Direction dir : directions) {
            MapLocation neighbour = botlocation.add(dir);
            int cost = calculateCost(robotController, neighbour, flagLocation, canTravelAgain);
            if (cost < currentLowestCost) {
                currentLowestCost = cost;
                best = dir;
            }
        }
        if (best != null) {
            MapLocation moveToLocation = robotController.getLocation().add(best);
            cache.add(moveToLocation);
            robotController.setIndicatorLine(botlocation, moveToLocation, 255, 255, 0);
            robotController.move(best);
            return true;
        } else {

            if(!canTravelAgain){
                // only try once
                return move(robotController, flagLocation, true);

            }else{
                robotController.setIndicatorString("WHELP");
                return false;
            }
        }
    }

    public int calculateCost(RobotController robotController, MapLocation neighbour, MapLocation flagLocation,
                                    boolean canTravelAgain) {
        Direction direction = robotController.getLocation().directionTo(neighbour);
        boolean canmove = robotController.canMove(direction);
        boolean alreadyVisited = cache.contains(neighbour) && !canTravelAgain ;
        if (!canmove || alreadyVisited || neighbour == null || flagLocation == null) {
            return Integer.MAX_VALUE;
        } else {
            return neighbour.distanceSquaredTo(flagLocation);
        }
    }
    public void flushcache(){
        cache.clear();

    }
}
