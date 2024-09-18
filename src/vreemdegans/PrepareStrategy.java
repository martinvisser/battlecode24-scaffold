package vreemdegans;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;

import static vreemdegans.ActiveStrategy.HIDE_THE_FLAG;
import static vreemdegans.Movement.*;
import static vreemdegans.RobotPlayer.directions;
import static vreemdegans.RobotPlayer.rng;

public class PrepareStrategy implements Strategy {
    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
//        if (isAtSpawnLocation(rc) && rc.canPickupFlag(rc.getLocation())) {
//            HIDE_THE_FLAG.setAndExecute(rc, turnCounter);
//            return;
//        }

        rc.setIndicatorString("Preparing");
        gotoCrumbIfPossible(rc);

        // last 50 turns, start preparing
        if (turnCounter <= 150) {
            exploreMove(rc);
            randomMove(rc);
        }

        moveToEnemySpawn(rc);
        fillNearbyWithWater(rc);

//        Direction dir = directions[rng.nextInt(directions.length)];

        // Rarely attempt placing traps behind the robot.
//        MapLocation prevLoc = rc.getLocation().subtract(dir);
//        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1) {
//            rc.build(TrapType.EXPLOSIVE, prevLoc);
//        }
    }

    private boolean isAtSpawnLocation(RobotController rc) {
        return Arrays.stream(rc.getAllySpawnLocations()).anyMatch(loc -> loc.equals(rc.getLocation()));
    }
}
