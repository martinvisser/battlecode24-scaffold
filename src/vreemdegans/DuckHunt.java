package vreemdegans;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import static vreemdegans.DiscoverEnemies.*;
import static vreemdegans.Movement.moveTo;
import static vreemdegans.RobotPlayer.rng;

public class DuckHunt {
    static void hunt(RobotController rc) throws GameActionException {
        // check if we can get some bread
        // check if i'm around a lot of damaged allies, if so do a heal
        // if not, search for enemies and attack them
        // if none found nearby move towards enemy spawn point

        rc.setIndicatorString("Hunting");

        gotoCrumbIfPossible(rc);
        healIfPossible(rc);
        attackIfPossible(rc);
        placeTrapIfPossible(rc);
        moveToEnemySpawn(rc);
    }

    static void gotoCrumbIfPossible(RobotController rc) throws GameActionException {
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        MapLocation closestCrumb = Arrays.stream(crumbs).min(Comparator.comparingInt(x -> rc.getLocation().distanceSquaredTo(x))).orElse(null);
        if (crumbs.length > 0) {
            rc.setIndicatorString("Moving to closest crumb: " + closestCrumb);
            moveTo(rc, closestCrumb);
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

    private static RobotInfo chooseHealTarget(RobotController rc, RobotInfo[] nearbyAllies) {
        Stream<RobotInfo> healableRobots = Arrays.stream(nearbyAllies).filter(x -> rc.canHeal(x.getLocation()));
        // sort them by health and return lowest one

        return healableRobots.min(Comparator.comparingInt(x -> x.getHealth())).orElse(null);
    }

    static void attackIfPossible(RobotController rc) throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(6, rc.getTeam().opponent());
        if (enemyRobots.length > 0) {
            RobotInfo target = chooseAttackTarget(rc, enemyRobots);
            if (target != null) {
                try {
                    rc.attack(enemyRobots[0].getLocation());
                } catch (GameActionException e) {
                    // ignore
                }
            } else {
                moveTo(rc, enemyRobots[0].getLocation());
            }
        }
    }

    private static RobotInfo chooseAttackTarget(RobotController rc, RobotInfo[] enemyRobots) {
        Stream<RobotInfo> attackableRobots = Arrays.stream(enemyRobots).filter(x -> rc.canAttack(x.getLocation()));
        // sort them by health and return lowest one

        return attackableRobots.min(Comparator.comparingInt(x -> x.getHealth())).orElse(null);
    }

    static void fillNearbyWithWater(RobotController rc) throws GameActionException {
        MapInfo[] nearbyMapInfos = rc.senseNearbyMapInfos(1);
        for (MapInfo mapInfo : nearbyMapInfos) {
            if (rc.canFill(mapInfo.getMapLocation())) {
                rc.fill(mapInfo.getMapLocation());
            }
        }
    }

    private static void placeTrapIfPossible(RobotController rc) throws GameActionException {
        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())) {
            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
        }
    }

    private static void healIfPossible(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(4, rc.getTeam());
        if (nearbyAllies.length > 0) {
            RobotInfo target = chooseHealTarget(rc, nearbyAllies);
            if (target != null) {
                try {
                    rc.heal(target.getLocation());
                } catch (GameActionException e) {
                    // ignore
                }
            }
        }
    }
}
