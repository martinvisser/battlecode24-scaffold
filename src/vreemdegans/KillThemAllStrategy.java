package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import static vreemdegans.Movement.*;

public class KillThemAllStrategy implements Strategy {
    @Override
    public void execute(RobotController rc) throws GameActionException {
        // check if we can get some bread
        // check if i'm around a lot of damaged allies, if so do a heal
        // if not, search for enemies and attack them
        // if none found nearby move towards enemy spawn point

        rc.setIndicatorString("Hunting");

        gotoCrumbIfPossible(rc);
        attackIfPossible(rc);
        moveToHuntIfPossible(rc);
        healIfPossible(rc);
        placeTrapIfPossible(rc);
        moveToEnemySpawn(rc);
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
