package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Arrays;
import java.util.Comparator;

import static vreemdegans.ActiveStrategy.PREPARE;
import static vreemdegans.Discovery.Hiders;

public class HideTheFlagStrategy implements Strategy {
    @Override
    public void execute(RobotController rc, int turnCounter) throws GameActionException {
        final MapLocation loc = rc.getLocation();
        if (rc.readSharedArray(Hiders) >= 1 && !RobotPlayer.isHider) {
            System.out.println("No flag or enough hiders, going to prepare");
            PREPARE.setAndExecute(rc, turnCounter);
            return;
        }

        try {
            RobotPlayer.isHider = true;
            if (!rc.hasFlag()) {
                rc.pickupFlag(loc);
            }

            final int mapHeight = rc.getMapHeight();
            final int mapWidth = rc.getMapWidth();

            // find closest corner based on distance to the corner
            final MapLocation[] corners = new MapLocation[]{
                    new MapLocation(0, 0),
                    new MapLocation(mapWidth - 1, 0),
                    new MapLocation(0, mapHeight - 1),
                    new MapLocation(mapWidth - 1, mapHeight - 1)
            };
            final MapLocation closestCorner = Arrays.stream(corners)
                    .min(Comparator.comparingInt(location -> loc.distanceSquaredTo(location)))
                    .orElseGet(() -> corners[0]);
            Movement.moveTo(rc, closestCorner);
            rc.setIndicatorString("Hiding the flag in corner: " + closestCorner);

            if (loc.distanceSquaredTo(closestCorner) < 3 && rc.canDropFlag(loc)) {
                rc.dropFlag(loc);
                rc.setIndicatorString("Flag hidden");
                rc.writeSharedArray(Hiders, rc.readSharedArray(Hiders) + 1);
                RobotPlayer.activeStrategy = PREPARE;
                RobotPlayer.isHider = false;
            } else {
                Movement.randomMove(rc);
            }
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
