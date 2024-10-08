package vreemdegans;

import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;

import static vreemdegans.ActiveStrategy.GO_HOME;
import static vreemdegans.ActiveStrategy.SUPER_HUNT;
import static vreemdegans.GeneralStrategy.attackIfPossible;
import static vreemdegans.Movement.moveTo;

public class CaptureStrategy implements Strategy {
    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())) {
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");

            GO_HOME.setAndExecute(rc, turnCounter);
            return;
        }

        // have a small chance of attacking if we are going for the flag and see an enemy
        if (RobotPlayer.rng.nextInt() % 5 < 3) {
            attackIfPossible(rc);
        }

        // Move towards the enemy flag.
        // check if i can see a flag, if not request latest general location of flags to target
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());

        // filter out picked up flags
        enemyFlags = Arrays.stream(enemyFlags).filter(x -> !x.isPickedUp()).toArray(FlagInfo[]::new);

        if (enemyFlags.length > 0) {
            FlagInfo flagInfo = enemyFlags[RobotPlayer.rng.nextInt(enemyFlags.length)]; // TODO make all of them go for different flags
            final MapLocation location = flagInfo.getLocation();
            moveTo(rc, location);
        } else {
            MapLocation[] broadcastedFlags = rc.senseBroadcastFlagLocations();
            if (broadcastedFlags.length == 0) {
                SUPER_HUNT.setAndExecute(rc, turnCounter);
                return;
            }

            final MapLocation broadcastedFlag = broadcastedFlags[RobotPlayer.rng.nextInt(broadcastedFlags.length)];
            moveTo(rc, broadcastedFlag);
        }
    }
}
