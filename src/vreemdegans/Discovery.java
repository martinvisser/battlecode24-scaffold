package vreemdegans;

import battlecode.common.FlagInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static battlecode.common.GameConstants.SETUP_ROUNDS;

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
    static final int Hiders = 15;
    static final int LastChangedTurn = 16;

    static void updateAllyFlagLocationAndHuntTarget(RobotController rc, int roundCount) throws GameActionException {
        MapLocation flag1Location = new MapLocation(rc.readSharedArray(AllyFlag1LocationX), rc.readSharedArray(AllyFlag1LocationY));
        MapLocation flag2Location = new MapLocation(rc.readSharedArray(AllyFlag2LocationX), rc.readSharedArray(AllyFlag2LocationY));
        MapLocation flag3Location = new MapLocation(rc.readSharedArray(AllyFlag3LocationX), rc.readSharedArray(AllyFlag3LocationY));

        FlagInfo[] flagInfos = rc.senseNearbyFlags(-1, rc.getTeam());
        // find the old location that is closest to the new location and update that one with the new one
        // check at every point if it has actually changed and if so update the changed boolean
        boolean changed = false;
        MapLocation newHuntLocation = null;
        for (FlagInfo flagInfo : flagInfos) {
            if (
                    (flag1Location.x == 0 && flag1Location.y == 0) ||
                            (flagInfo.getLocation().distanceSquaredTo(flag1Location) < flagInfo.getLocation().distanceSquaredTo(flag2Location) &&
                                    flagInfo.getLocation().distanceSquaredTo(flag1Location) < flagInfo.getLocation().distanceSquaredTo(flag3Location))
            ) {

                if (flag1Location.x != flagInfo.getLocation().x || flag1Location.y != flagInfo.getLocation().y) {
                    rc.writeSharedArray(AllyFlag1LocationX, flagInfo.getLocation().x);
                    rc.writeSharedArray(AllyFlag1LocationY, flagInfo.getLocation().y);
                    changed = true;
                    newHuntLocation = flagInfo.getLocation();
                }
            } else if ((flag2Location.x == 0 && flag2Location.y == 0) ||
                    (flagInfo.getLocation().distanceSquaredTo(flag2Location) < flagInfo.getLocation().distanceSquaredTo(flag1Location)
                            && flagInfo.getLocation().distanceSquaredTo(flag2Location) < flagInfo.getLocation().distanceSquaredTo(flag3Location))) {

                if (flag2Location.x != flagInfo.getLocation().x || flag2Location.y != flagInfo.getLocation().y) {
                    rc.writeSharedArray(AllyFlag2LocationX, flagInfo.getLocation().x);
                    rc.writeSharedArray(AllyFlag2LocationY, flagInfo.getLocation().y);
                    changed = true;
                    newHuntLocation = flagInfo.getLocation();
                }
            } else if ((flag3Location.x == 0 && flag3Location.y == 0) ||
                    (flagInfo.getLocation().distanceSquaredTo(flag3Location) < flagInfo.getLocation().distanceSquaredTo(flag1Location)
                            && flagInfo.getLocation().distanceSquaredTo(flag3Location) < flagInfo.getLocation().distanceSquaredTo(flag2Location))) {
                if (flag3Location.x != flagInfo.getLocation().x || flag3Location.y != flagInfo.getLocation().y) {
                    rc.writeSharedArray(AllyFlag3LocationX, flagInfo.getLocation().x);
                    rc.writeSharedArray(AllyFlag3LocationY, flagInfo.getLocation().y);
                    changed = true;
                    newHuntLocation = flagInfo.getLocation();
                }
            }
        }

        if (changed) {
            rc.writeSharedArray(LastChangedTurn, roundCount);
        }

        if (changed && roundCount > SETUP_ROUNDS) {
//            System.out.println("New hunt target: " + newHuntLocation);
            rc.writeSharedArray(HunterTargetX, newHuntLocation.x);
            rc.writeSharedArray(HunterTargetY, newHuntLocation.y);
        }

        // if we have not changed the target for a while, we can pick a new one
        int turnsChangedAgo = roundCount - rc.readSharedArray(LastChangedTurn);
        if (turnsChangedAgo > 3) {
            rc.writeSharedArray(HunterTargetX, 0);
            rc.writeSharedArray(HunterTargetY, 0);
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
