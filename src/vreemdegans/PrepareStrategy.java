package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import java.util.Arrays;

import static vreemdegans.Movement.*;

public class PrepareStrategy implements Strategy {
    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        rc.setIndicatorString("Preparing");
        gotoCrumbIfPossible(rc);

        // last 50 turns, start preparing
        if (turnCounter <= 150) {
            exploreMove(rc);
            randomMove(rc);
        }

        moveToEnemySpawn(rc);
        fillNearbyWithWater(rc);
    }

    private boolean isAtSpawnLocation(RobotController rc) {
        return Arrays.stream(rc.getAllySpawnLocations()).anyMatch(loc -> loc.equals(rc.getLocation()));
    }
}
