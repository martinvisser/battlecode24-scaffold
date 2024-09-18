package komjenumee;

import battlecode.common.Direction;
import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import static komjenumee.RobotPlayer.directions;
import static komjenumee.RobotPlayer.rng;

public class AttackPhase {

    Pathfinder pathfinder = new Pathfinder();
    final int FLUSH_CACHE_ROUNDS = 50;


    public void run(RobotController rc) throws GameActionException {
        if(rc.hasFlag()){
            // Run forest, run
            runForest(rc);
        }else{
            MapLocation location = getFlagLocationFromOther(rc);
            if (location == null) {
                // niemand gaat naar flag, we doen het zelf wel weer
                location = searchFlag(rc);
            }
            move(rc, location);
            processAfterMove(rc);
        }
    }

    private void processAfterMove(RobotController rc) throws GameActionException {
        MapLocation current = rc.getLocation();
        if(rc.canPickupFlag(current)){
            rc.pickupFlag(current);
            resetTarget(rc);
        }else{
            attack(rc);
            fill(rc);
        }
    }

    private void attack(RobotController rc) throws GameActionException {
        RobotInfo[] ris = rc.senseNearbyRobots(rc.getLocation(), 1, rc.getTeam().opponent());
        for (RobotInfo ri : ris) {
            if (rc.canAttack(ri.location)) {
                rc.attack(ri.location);
                log(rc,"ATTACKKKING");
            }
        }
    }

    private void fill(RobotController rc) throws GameActionException {
        for(Direction dir : directions){
            if(rc.canFill(rc.getLocation().add(dir))){
                rc.fill(rc.getLocation().add(dir));
            }
        }

    }
    private static void log(RobotController rc, String string) {
        rc.setIndicatorString(string);
//        System.out.println(string + rc.getTeam() + rc.getID() + rc.getLocation());
    }
    public void runForest(RobotController rc) throws GameActionException {
        MapLocation to = findNearest(rc);
        pathfinder.move(rc, to);
    }

    private MapLocation findNearest(RobotController robotController){
        MapLocation []mls = robotController.getAllySpawnLocations();
        MapLocation current = robotController.getLocation();
        int distance = Integer.MAX_VALUE;
        MapLocation closest = null;
        for(MapLocation loc : mls){
            int curDistance =loc.distanceSquaredTo(current);
            if(distance > curDistance){
                distance = curDistance;
                closest = loc;
            }
        }
        return closest;
    }



    public void move(RobotController rc, MapLocation location) throws GameActionException {
        boolean moved=  pathfinder.move(rc, location);
        if(!moved){

            Direction dir = directions[rng.nextInt(directions.length)];
            if(rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    public MapLocation searchFlag(RobotController rc) throws GameActionException {
        MapLocation loc = getAccurateFlagLocation(rc);
        if (loc == null) {
            loc = getApproximateFlagLocation(rc);
            if(loc!=null) {
                rc.setIndicatorDot(loc,0,255,0);
            }

        } else {
            rc.setIndicatorDot(loc,255,0,0);
            writeExact(rc, loc);
        }
        return loc;
    }


    public MapLocation getAccurateFlagLocation(RobotController rc) throws GameActionException {
        FlagInfo[] fis = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        for (FlagInfo flag : fis) {
            if(!flag.isPickedUp()){
                return flag.getLocation();
            }
        }
        return null;
    }

    public MapLocation getApproximateFlagLocation(RobotController rc) {
        MapLocation[] mls = rc.senseBroadcastFlagLocations();
        return mls.length > 0 ? mls[0] : null;
    }

    public MapLocation getFlagLocationFromOther(RobotController rc) throws GameActionException {
        if (rc.canWriteSharedArray(0, 1)) {
            int prevCacheRound = rc.readSharedArray(1);
            if (prevCacheRound + FLUSH_CACHE_ROUNDS > rc.getRoundNum()) {
                int locationValue = rc.readSharedArray(0);
                if (locationValue == 0) {
                    return null;
                } else {
                    return extractLocation(locationValue);
                }
            }else{
                resetTarget(rc);
                return null;
            }

        } else {
            return null;
        }
    }

    private void resetTarget(RobotController rc) throws GameActionException {

        pathfinder.flushcache();
        rc.writeSharedArray(1, 0);
    }

    public MapLocation extractLocation(int value){
        String val = "" + value;
        int index = val.lastIndexOf("0", val.length() -2);
        String first = val.substring(0, index);
        String last = val.substring(index);
        int x = Integer.parseInt(first);
        int y = Integer.parseInt(last);
        return new MapLocation(x, y);
    }

    public void writeExact(RobotController rc, MapLocation ml) {
        String loc = ""+ ml.x;
        loc = loc + "0";
        loc += ml.y;
        try {
            rc.writeSharedArray(0, Integer.parseInt(loc));
            rc.writeSharedArray(1, rc.getRoundNum());
        } catch (GameActionException e) {
//            System.err.println("writing to array fialed: " + e.getMessage());
        }
    }

}
