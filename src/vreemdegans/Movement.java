package vreemdegans;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

import static vreemdegans.DuckHunt.fillNearbyWithWater;

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
}
