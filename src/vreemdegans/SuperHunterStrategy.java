package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import static vreemdegans.Movement.gotoCrumbIfPossible;
import static vreemdegans.Movement.moveToEnemySpawn;

public class SuperHunterStrategy extends GeneralStrategy {
    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        // check if we can get some bread
        // check if i'm around a lot of damaged allies, if so do a heal
        // if not, search for enemies and attack them
        // if none found nearby move towards enemy spawn point

        rc.setIndicatorString("Hunting");

        gotoCrumbIfPossible(rc);
        attackIfPossible(rc);
        healIfPossible(rc);
        moveToEnemySpawn(rc);
    }
}
