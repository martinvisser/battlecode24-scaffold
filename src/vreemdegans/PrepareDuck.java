package vreemdegans;

import battlecode.common.*;

import static vreemdegans.DuckHunt.gotoCrumbIfPossible;
import static vreemdegans.DuckHunt.moveToEnemySpawn;

class PrepareDuck {
    static void prepare(RobotController rc) throws GameActionException {
        // Move and attack randomly if no objective.
        gotoCrumbIfPossible(rc);
        moveToEnemySpawn(rc);

        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        } else if (rc.canAttack(nextLoc)) {
            rc.attack(nextLoc);
        }

        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(dir);
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && RobotPlayer.rng.nextInt() % 37 == 1)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
    }
}
