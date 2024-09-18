package vreemdegans;

import battlecode.common.*;

import static vreemdegans.Movement.*;

public class PrepareStrategy implements Strategy {
    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        // Move and attack randomly if no objective.
        gotoCrumbIfPossible(rc);

        // last 50 turns, start preparing
        if (turnCounter <= 150) {
            exploreMove(rc);
            randomMove(rc);
        }

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
