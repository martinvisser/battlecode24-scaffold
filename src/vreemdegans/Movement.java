package vreemdegans;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static vreemdegans.Discovery.*;
import static vreemdegans.RobotPlayer.directions;
import static vreemdegans.RobotPlayer.rng;

class Movement {

    static void moveTo(RobotController rc, MapLocation location) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);
        rc.setIndicatorLine(rc.getLocation(), location, 0, 255, 0);

//        // RobotPlayer.lastDirections contains less than 2, add the direction
//        // Otherwise remove the first element and add the new direction
//        if (RobotPlayer.lastDirections.size() < 2) {
//            RobotPlayer.lastDirections.add(dir);
//        } else {
//            RobotPlayer.lastDirections.remove();
//            RobotPlayer.lastDirections.add(dir);
//        }
//
//        Direction newDir = Arrays.stream(directions)
//                .filter(d -> RobotPlayer.lastDirections.stream().noneMatch(ld -> ld == d))
//                .filter(d -> rc.canMove(d))
//                .findAny()
//                .orElse(dir);

        if (rc.canMove(dir)) {
            rc.move(dir);
        } else if (rc.canFill(location)) {
            rc.fill(location);
        } else {
            final Direction nextDirection = getNextDirection(dir, rc);
            if (nextDirection != null) {
                rc.move(nextDirection);
            }
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

        ArrayList<MapLocation> mapLocations = new ArrayList<>();
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
            int dx = rng.nextInt(3);
            int dy = rng.nextInt(3);
            int dxMinus = rng.nextInt(3);
            int dyMinus = rng.nextInt(3);
            closestFlag = new MapLocation(closestFlag.x + dx - dxMinus, closestFlag.y + dy - dyMinus);
        }

        if (closestFlag != null) {
            moveTo(rc, closestFlag);
        }
    }
}
