package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;
import java.util.Comparator;

import static vreemdegans.Movement.moveTo;
import static vreemdegans.RobotPlayer.closestSpawnLoc;
import static vreemdegans.RobotPlayer.homeDir;

public class BringBackTheGoodiesStrategy implements Strategy {
    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation currentLocation = rc.getLocation();
        if (closestSpawnLoc == null) {
            closestSpawnLoc = Arrays.stream(spawnLocs)
                    .min(Comparator.comparingInt(loc -> loc.distanceSquaredTo(currentLocation)))
                    .orElseGet(() -> spawnLocs[0]);

            rc.setIndicatorString("Going home to: " + closestSpawnLoc);
        }
        if (homeDir == null || !rc.canMove(homeDir)) {
            homeDir = rc.getLocation().directionTo(closestSpawnLoc);
        }

        // Check if a closest spawn location was found
        moveTo(rc, closestSpawnLoc);
    }
}
