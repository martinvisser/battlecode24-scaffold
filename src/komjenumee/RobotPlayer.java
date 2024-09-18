package komjenumee;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Random;
//https://releases.battlecode.org/specs/battlecode24/3.0.5/specs.md.html

public strictfp class RobotPlayer {

    static final Random rng = new Random(6147);
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    private static AttackPhase attackPhase = new AttackPhase();
    private static SetupPhase setupPhase = new SetupPhase();
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        while (true) {
            if (!rc.isSpawned()) {
                MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                // Pick a random spawn location to attempt spawning in.
                MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
                if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
            } else {
                if(isSetupRounds(rc)){
                    setupPhase.run(rc);
                }else{
                    attackPhase.run(rc);
                }
            }

            // Make sure you spawn your robot in before you attempt to take any actions!
            // Robots not spawned in do not have vision of any tiles and cannot perform any actions.

            Clock.yield();
        }


        // iemand bezig:
        // ja: volg
        // nee: ga door
        // exacte locatie flag?
        // ja: beweeg erheen
        // nee: beweeg naar globale richting
        // heb ik een flag, ga naar homebase
        // is er een enemy in de buurt: kill it
    }

    private static boolean isSetupRounds(RobotController rc) {
        return rc.getRoundNum() <= GameConstants.SETUP_ROUNDS;
    }

}