package vreemdegans;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static vreemdegans.Discovery.*;
import static vreemdegans.RobotPlayer.directions;
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
            moveToNext(dir, rc, 0);
        }
        fillNearbyWithWater(rc);
    }

    static void fillNearbyWithWater(RobotController rc) throws GameActionException {
        MapInfo[] nearbyMapInfos = rc.senseNearbyMapInfos(1);
        for (MapInfo mapInfo : nearbyMapInfos) {
            if (rc.canFill(mapInfo.getMapLocation())) {
                rc.fill(mapInfo.getMapLocation());
            }
        }
    }

    private static void moveToNext(Direction current, RobotController rc, int attempts) throws GameActionException {
        final Direction nextDirection = findDirection(current, rc, attempts);
        if (nextDirection != null) rc.move(nextDirection);
        else Movement.randomMove(rc);
    }

    private static Direction findDirection(Direction current, RobotController rc, int attempts) throws GameActionException {
        if (attempts < Direction.allDirections().length) {
            Direction nextDirection = current.rotateRight();
            final MapLocation newLocation = rc.getLocation().add(nextDirection).add(nextDirection);
            if (rc.onTheMap(newLocation) && rc.sensePassability(newLocation) && rc.canMove(nextDirection)) {
                return nextDirection;
            }
            return findDirection(nextDirection, rc, attempts + 1);
        }
        return null;
    }

    static void gotoCrumbIfPossible(RobotController rc) throws GameActionException {
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        MapLocation closestCrumb = Arrays.stream(crumbs).min(Comparator.comparingInt(x -> rc.getLocation().distanceSquaredTo(x))).orElse(null);
        if (crumbs.length > 0) {
            rc.setIndicatorString("Moving to closest crumb: " + closestCrumb);
            moveTo(rc, closestCrumb);
        }
    }

    static void randomMove(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    static void exploreMove(RobotController rc) throws GameActionException {
        // move towards an area that brings you as far as possible from the closest few allies
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, rc.getTeam());
        if (nearbyAllies.length > 0) {
            MapLocation closestAlly = Arrays.stream(nearbyAllies).min(Comparator.comparingInt(x -> rc.getLocation().distanceSquaredTo(x.getLocation()))).orElse(null).getLocation();
            Direction dir = rc.getLocation().directionTo(closestAlly).opposite();
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    static void moveToEnemySpawn(RobotController rc) throws GameActionException {//        MapLocation[] enemySpawnLocs = rc.getAllySpawnLocations()
        // choose one of the 3 enemy spawn locations and move there

        List<MapLocation> mapLocations = new ArrayList<>();
        mapLocations.add(new MapLocation(rc.readSharedArray(EnemySpawn1LocationX), rc.readSharedArray(EnemySpawn1LocationY)));
        mapLocations.add(new MapLocation(rc.readSharedArray(EnemySpawn2LocationX), rc.readSharedArray(EnemySpawn2LocationY)));
        mapLocations.add(new MapLocation(rc.readSharedArray(EnemySpawn3LocationX), rc.readSharedArray(EnemySpawn3LocationY)));

        MapLocation target = mapLocations.get(rng.nextInt(mapLocations.size()));

        moveTo(rc, target);
    }

    static void moveToHuntIfPossible(RobotController rc) throws GameActionException {
        if (rc.readSharedArray(HunterTargetX) != 0 && rc.readSharedArray(HunterTargetY) != 0) {
            MapLocation target = new MapLocation(rc.readSharedArray(HunterTargetX), rc.readSharedArray(HunterTargetY));
            try {
                moveTo(rc, target);
            } catch (GameActionException e) {
                // ignore
            }
        }
    }

    static void moveToOurFlag(RobotController rc) throws GameActionException {
        ArrayList<MapLocation> flags = alliedFlags(rc);
        // find the closest flag and move there
        MapLocation closestFlag = flags.stream().min(
                Comparator.comparingInt(x -> rc.getLocation().distanceSquaredTo(x))
        ).orElse(null);

        // randomize the flag location a bit
        if (closestFlag != null) {
            int dx = rng.nextInt(7) - 3;
            int dy = rng.nextInt(7) - 3;
            closestFlag = new MapLocation(closestFlag.x + dx, closestFlag.y + dy);
        }

        if (closestFlag != null) {
            moveTo(rc, closestFlag);
        }
    }
}
