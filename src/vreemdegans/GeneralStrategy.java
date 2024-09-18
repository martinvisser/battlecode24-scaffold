package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import static vreemdegans.Movement.*;

public class GeneralStrategy implements Strategy {
    private static RobotInfo chooseAttackTarget(RobotController rc, RobotInfo[] enemyRobots) {
        RobotInfo[] attackableRobots = Arrays.stream(enemyRobots).filter(x -> rc.canAttack(x.getLocation())).toArray(RobotInfo[]::new);

        // if anybody has the flag, target him!
        RobotInfo flagHolder = Arrays.stream(attackableRobots).filter(x -> x.hasFlag()).findFirst().orElse(null);
        if (flagHolder != null) {
            return flagHolder;
        }

        // sort them by health and return lowest one
        return Arrays.stream(attackableRobots).min(Comparator.comparingInt(x -> x.getHealth())).orElse(null);
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

    public static void healIfPossible(RobotController rc) throws GameActionException {
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

    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        // check if we can get some bread
        // check if i'm around a lot of damaged allies, if so do a heal
        // if not, search for enemies and attack them
        // if none found nearby move towards enemy spawn point

        rc.setIndicatorString("General");

        gotoCrumbIfPossible(rc);
        attackIfPossible(rc);
        moveToHuntIfPossible(rc);
        healIfPossible(rc);
        moveToEnemySpawn(rc);
    }
}
