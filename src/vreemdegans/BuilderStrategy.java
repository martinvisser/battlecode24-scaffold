package vreemdegans;

import battlecode.common.*;

import java.util.ArrayList;

import static vreemdegans.Discovery.alliedFlags;
import static vreemdegans.GeneralStrategy.attackIfPossible;
import static vreemdegans.Movement.moveToOurFlag;
import static vreemdegans.Movement.randomMove;
import static vreemdegans.RobotPlayer.rng;

public class BuilderStrategy implements Strategy {
    static void tryPlaceTrapCloseToFlag(RobotController rc) throws GameActionException {
        ArrayList<MapLocation> flags = alliedFlags(rc);
        if (flags.isEmpty()) {
            return;
        }

        TrapType targetTraptype = TrapType.values()[rng.nextInt(TrapType.values().length)];
        if (targetTraptype == TrapType.NONE) {
            targetTraptype = TrapType.EXPLOSIVE;
        }

        // check if any location is close to a flag and if we can place a trap there
        for (MapInfo location : rc.senseNearbyMapInfos(2)) {
            if (location.getTrapType() == TrapType.NONE) {
                for (MapLocation flag : flags) {
                    if (location.getMapLocation().distanceSquaredTo(flag) <= 5) {
                        if (rc.canBuild(targetTraptype, location.getMapLocation())) {
                            rc.build(targetTraptype, location.getMapLocation());
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        rc.setIndicatorString("Building");

        attackIfPossible(rc);
        tryPlaceTrapCloseToFlag(rc);
        moveToOurFlag(rc);
        randomMove(rc);
    }
}
