package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

class Discovery {
    static final int EnemySpawn1LocationX = 1;
    static final int EnemySpawn1LocationY = 2;
    static final int EnemySpawn2LocationX = 3;
    static final int EnemySpawn2LocationY = 4;
    static final int EnemySpawn3LocationX = 5;
    static final int EnemySpawn3LocationY = 6;
    static final int HunterTargetX = 7;
    static final int HunterTargetY = 8;
    static final int AllyFlag1LocationX = 9;
    static final int AllyFlag1LocationY = 10;
    static final int AllyFlag2LocationX = 11;
    static final int AllyFlag2LocationY = 12;
    static final int AllyFlag3LocationX = 13;
    static final int AllyFlag3LocationY = 14;
    static final int HidersCount = 15;


    static void updateAllyFlagLocation(RobotController rc) throws GameActionException {
        MapLocation flag1Location = new MapLocation(rc.readSharedArray(AllyFlag1LocationX), rc.readSharedArray(AllyFlag1LocationY));
        MapLocation flag2Location = new MapLocation(rc.readSharedArray(AllyFlag2LocationX), rc.readSharedArray(AllyFlag2LocationY));
        MapLocation flag3Location = new MapLocation(rc.readSharedArray(AllyFlag3LocationX), rc.readSharedArray(AllyFlag3LocationY));

        MapInfo[] nearby1MapInfos = rc.senseNearbyMapInfos(flag1Location);
        for (MapInfo mapInfo : nearby1MapInfos) {
            rc.canPickupFlag()

            if (mapInfo) {
                rc.writeSharedArray(AllyFlag1LocationX, mapInfo.getMapLocation().x);
                rc.writeSharedArray(AllyFlag1LocationY, mapInfo.getMapLocation().y);
            }
        }
    }


    static void storeEnemySpawn(RobotController rc) throws GameActionException {
        // try to find the last known location of the dropped flags, if it's found and not stored, store it
        MapLocation[] mapLocations = rc.senseBroadcastFlagLocations();
        if (mapLocations.length < 1) {
            return;
        }

        if (rc.readSharedArray(EnemySpawn1LocationX) != 0
                && rc.readSharedArray(EnemySpawn1LocationY) != 0
                && rc.readSharedArray(EnemySpawn2LocationX) != 0
        ) {
            return;
        }

        for (MapLocation mapLocation : mapLocations) {
            if (rc.readSharedArray(EnemySpawn1LocationX) == 0 && rc.readSharedArray(EnemySpawn1LocationY) == 0) {
                rc.writeSharedArray(EnemySpawn1LocationX, mapLocation.x);
                rc.writeSharedArray(EnemySpawn1LocationY, mapLocation.y);
            } else if (rc.readSharedArray(EnemySpawn2LocationX) == 0 && rc.readSharedArray(EnemySpawn2LocationY) == 0) {
                rc.writeSharedArray(EnemySpawn2LocationX, mapLocation.x);
                rc.writeSharedArray(EnemySpawn2LocationY, mapLocation.y);
            } else if (rc.readSharedArray(EnemySpawn3LocationX) == 0 && rc.readSharedArray(EnemySpawn3LocationY) == 0) {
                rc.writeSharedArray(EnemySpawn3LocationX, mapLocation.x);
                rc.writeSharedArray(EnemySpawn3LocationY, mapLocation.y);
            }
        }

        System.out.println("spawn 1 " + rc.readSharedArray(EnemySpawn1LocationX) + " " + rc.readSharedArray(EnemySpawn1LocationY));
        System.out.println("spawn 2 " + rc.readSharedArray(EnemySpawn2LocationX) + " " + rc.readSharedArray(EnemySpawn2LocationY));
        System.out.println("spawn 3 " + rc.readSharedArray(EnemySpawn3LocationX) + " " + rc.readSharedArray(EnemySpawn3LocationY));
    }
}
