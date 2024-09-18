package komjenumee;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;

import java.util.ArrayList;
import java.util.List;

import static komjenumee.RobotPlayer.directions;
import static komjenumee.RobotPlayer.rng;

public class SetupPhase {
    private List<MapLocation> stuns = new ArrayList();
    //private Direction cantMoveDirection = null;

    public void run(RobotController rc) throws GameActionException {

        /*if (rc.canPickupFlag(rc.getLocation())) {
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");
        }*/


        // If we are holding an enemy flag, singularly focus on moving towards
        // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
        // to make sure setup phase has ended.
        /*if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            MapLocation firstLoc = spawnLocs[0];
            Direction dir = rc.getLocation().directionTo(firstLoc);
            if (rc.canMove(dir)) rc.move(dir);
        }*/

        //try to move in the direction of the flag
        /*MapLocation[] mls = rc.senseBroadcastFlagLocations();
        Direction broadcastDir = null;
        if (mls.length > 0) {
            MapLocation ml = mls[0];
            broadcastDir = rc.getLocation().directionTo(ml);
            //if (rc.canMove(dir)) rc.move(dir);
        }*/
        //Direction[] test = Arrays.stream(directions).filter(d -> !d.equals(cantMoveDirection)).toArray(Direction[]::new);

        //Direction dir = test[rng.nextInt(test.length)];
        Direction dir = directions[rng.nextInt(directions.length)];
        //if (broadcastDir != null) dir = broadcastDir;

        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
            log(rc, "Moved!");
        } else {
            log(rc, "Can't move!");
            //save the direction that we can't move
            //cantMoveDirection = dir; //bad results

        }


        if (rc.canHeal(nextLoc)) {
            rc.heal(nextLoc);
            log(rc, "Healing!");
        } else if (rc.canDig(nextLoc)) {
            rc.dig(nextLoc);
            log(rc, "Dug!");
        } else if (rc.canBuild(TrapType.EXPLOSIVE, nextLoc)) {
            rc.build(TrapType.EXPLOSIVE, nextLoc);
            log(rc, "Built a EXPLOSIVE!");
        } else if (rc.canBuild(TrapType.STUN, nextLoc)) {
            rc.build(TrapType.STUN, nextLoc);
            log(rc, "Built a STUN!");
            stuns.add(nextLoc);
        } else if (rc.canBuild(TrapType.WATER, nextLoc)) {
            rc.build(TrapType.WATER, nextLoc);
            log(rc, "Built a WATER!");
        } else if (rc.canFill(nextLoc)) {
            rc.fill(nextLoc);
            log(rc, "Filled a hole!");
        } else if (rc.canAttack(nextLoc)) {
            rc.attack(nextLoc);
            log(rc, "Take that! Damaged an enemy that was in our way!");
        }
        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(dir);
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
        // We can also move our code into different methods or classes to better organize it!
        updateEnemyRobots(rc);
    }


    private static void log(RobotController rc, String string) {
        //rc.setIndicatorString(string);
        //System.out.println(string + rc.getTeam() + rc.getID() + rc.getLocation());
    }


    public static void updateEnemyRobots(RobotController rc) throws GameActionException {
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.

        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0) {
            rc.setIndicatorString("There are nearby enemy robots! Scary!" + enemyRobots.length);
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++) {
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)) {
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
                //System.out.println(numEnemies + " enemy robots seen!");
            }
        }
    }
}
