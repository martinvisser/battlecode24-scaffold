package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;
import java.util.Comparator;

import static vreemdegans.Movement.moveTo;

public class BringItBack {
    static void goHome(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation currentLocation = rc.getLocation();
        if (RobotPlayer.closestSpawnLoc == null) {
            RobotPlayer.closestSpawnLoc = Arrays.stream(spawnLocs)
                    .min(Comparator.comparingInt(loc -> loc.distanceSquaredTo(currentLocation)))
                    .orElseGet(() -> spawnLocs[0]);

            rc.setIndicatorString("Going home to: " + RobotPlayer.closestSpawnLoc);
        }
        if (RobotPlayer.homeDir == null || !rc.canMove(RobotPlayer.homeDir)) {
            RobotPlayer.homeDir = rc.getLocation().directionTo(RobotPlayer.closestSpawnLoc);
        }

        // Check if a closest spawn location was found
        moveTo(rc, RobotPlayer.closestSpawnLoc);
    }
}
