package vreemdegans;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static vreemdegans.DiscoverEnemies.*;
import static vreemdegans.RobotPlayer.rng;

class Movement {

     static void moveTo(RobotController rc, MapLocation location) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);
        rc.setIndicatorLine(rc.getLocation(), location, 0, 255, 0);
        if (rc.canMove(dir)) {
            rc.move(dir);
        } else if (rc.canFill(location)) {
            rc.fill(location);
        } else {
            final Direction nextDirection = getNextDirection(dir, rc);
            if (nextDirection != null) {
                rc.move(nextDirection);
            }

            // if we are stuck, try to fill water or another land action
            fillNearbyWithWater(rc);
        }
    }

    private static Direction getNextDirection(Direction current, RobotController rc) throws GameActionException {
        if (rc.canMove(current.rotateRight())) {
            return current.rotateRight();
        } else if (rc.canMove(current.rotateRight().rotateRight())) {
            return current.rotateRight().rotateRight();
        } else if (rc.canMove(current.rotateRight().rotateRight().rotateRight())) {
            return current.rotateRight().rotateRight().rotateRight();
        } else if (rc.canMove(current.rotateLeft())) {
            return current.rotateLeft();
        } else if (rc.canMove(current.rotateLeft().rotateLeft())) {
            return current.rotateLeft().rotateLeft();
        } else if (rc.canMove(current.rotateLeft().rotateLeft().rotateLeft())) {
            return current.rotateLeft().rotateLeft().rotateLeft();
        } else if (rc.canMove(current.opposite())) {
            return current.opposite();
        } else {
            final MapInfo[] a = rc.senseNearbyMapInfos(2);
            return Arrays.stream(a)
                    .filter(mapInfo -> mapInfo.isPassable())
                    .map(mapInfo -> rc.getLocation().directionTo(mapInfo.getMapLocation()))
                    .filter(dir -> dir != current)
                    .filter(dir -> rc.canMove(dir))
                    .findAny()
                    .orElse(null);
        }
    }

    static void gotoCrumbIfPossible(RobotController rc) throws GameActionException {
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        MapLocation closestCrumb = Arrays.stream(crumbs).min(Comparator.comparingInt(x -> rc.getLocation().distanceSquaredTo(x))).orElse(null);
        if (crumbs.length > 0) {
            rc.setIndicatorString("Moving to closest crumb: " + closestCrumb);
            moveTo(rc, closestCrumb);
        }
    }

    static void fillNearbyWithWater(RobotController rc) throws GameActionException {
        MapInfo[] nearbyMapInfos = rc.senseNearbyMapInfos(1);
        for (MapInfo mapInfo : nearbyMapInfos) {
            if (rc.canFill(mapInfo.getMapLocation())) {
                rc.fill(mapInfo.getMapLocation());
            }
        }
    }

    static void moveToEnemySpawn(RobotController rc) throws GameActionException {//        MapLocation[] enemySpawnLocs = rc.getAllySpawnLocations()
        rc.setIndicatorString("Moving to enemy spawn");
        // choose one of the 3 enemy spawn locations and move there

        ArrayList<MapLocation> mapLocations = new ArrayList<>();
        mapLocations.add(new MapLocation(rc.readSharedArray(Spawn1LocationX), rc.readSharedArray(Spawn1LocationY)));
        mapLocations.add(new MapLocation(rc.readSharedArray(Spawn2LocationX), rc.readSharedArray(Spawn2LocationY)));
        mapLocations.add(new MapLocation(rc.readSharedArray(Spawn3LocationX), rc.readSharedArray(Spawn3LocationY)));

        MapLocation target = mapLocations.get(rng.nextInt(mapLocations.size()));

        // take average location of all enemy spawn locations
        int x = 0;
        int y = 0;
        for (MapLocation mapLocation : mapLocations) {
            x += mapLocation.x;
            y += mapLocation.y;
        }
        target = new MapLocation(x / mapLocations.size(), y / mapLocations.size());

        moveTo(rc, target);
    }
}
