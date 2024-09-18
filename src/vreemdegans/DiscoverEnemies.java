package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

class DiscoverEnemies {
    static final int Spawn1LocationX = 1;
    static final int Spawn1LocationY = 2;
    static final int Spawn2LocationX = 3;
    static final int Spawn2LocationY = 4;
    static final int Spawn3LocationX = 5;
    static final int Spawn3LocationY = 6;

    static void storeEnemySpawn(RobotController rc) throws GameActionException {
        // try to find the last known location of the dropped flags, if it's found and not stored, store it
        MapLocation[] mapLocations = rc.senseBroadcastFlagLocations();
        if (mapLocations.length < 1) {
            return;
        }

        if (rc.readSharedArray(Spawn1LocationX) != 0
                && rc.readSharedArray(Spawn1LocationY) != 0
                && rc.readSharedArray(Spawn2LocationX) != 0
        ) {
            return;
        }

        for (MapLocation mapLocation : mapLocations) {
            if (rc.readSharedArray(Spawn1LocationX) == 0 && rc.readSharedArray(Spawn1LocationY) == 0) {
                rc.writeSharedArray(Spawn1LocationX, mapLocation.x);
                rc.writeSharedArray(Spawn1LocationY, mapLocation.y);
            } else if (rc.readSharedArray(Spawn2LocationX) == 0 && rc.readSharedArray(Spawn2LocationY) == 0) {
                rc.writeSharedArray(Spawn2LocationX, mapLocation.x);
                rc.writeSharedArray(Spawn2LocationY, mapLocation.y);
            } else if (rc.readSharedArray(Spawn3LocationX) == 0 && rc.readSharedArray(Spawn3LocationY) == 0) {
                rc.writeSharedArray(Spawn3LocationX, mapLocation.x);
                rc.writeSharedArray(Spawn3LocationY, mapLocation.y);
            }
        }

        System.out.println("spawn 1 " + rc.readSharedArray(Spawn1LocationX) + " " + rc.readSharedArray(Spawn1LocationY));
        System.out.println("spawn 2 " + rc.readSharedArray(Spawn2LocationX) + " " + rc.readSharedArray(Spawn2LocationY));
        System.out.println("spawn 3 " + rc.readSharedArray(Spawn3LocationX) + " " + rc.readSharedArray(Spawn3LocationY));
    }
}
