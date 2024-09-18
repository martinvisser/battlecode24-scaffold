package vreemdegans;

import battlecode.common.*;

import java.util.*;

import static battlecode.common.GlobalUpgrade.ATTACK;
import static vreemdegans.ActiveStrategy.*;
import static vreemdegans.Discovery.storeEnemySpawn;
import static vreemdegans.Discovery.updateAllyFlagLocationAndHuntTarget;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random();

    /**
     * Array containing all the possible movement directions.
     */
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

    static ActiveStrategy activeStrategy = PREPARE;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this robot, and to get
     *           information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        storeEnemySpawn(rc);

        ActiveStrategy alwaysStrategy = null;

        if (rng.nextInt(10) == 1) { // 10% chance to hunt
            alwaysStrategy = GENERIC;
//            System.out.println("Hunter");
            rc.setIndicatorString("Hunter");
        } else if (rc.readSharedArray(Discovery.BuildersCount) < 3) {
            alwaysStrategy = BUILD;
//            System.out.println("Builder");
            rc.setIndicatorString("Builder");
            int builders = rc.readSharedArray(Discovery.BuildersCount);
            rc.writeSharedArray(Discovery.BuildersCount, builders + 1);
        }

        while (true) {
            turnCount += 1;
            tryBuyGlobalUpgrade(rc); // only do every 600 turns

            try {
                if (!rc.isSpawned()) {
                    spawn(rc);
                } else {
                    updateAllyFlagLocationAndHuntTarget(rc, turnCount);
                    pickStrategy(rc, alwaysStrategy);
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
//                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
//                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    static MapLocation closestSpawnLoc;
    static Direction homeDir;

    private static void pickStrategy(RobotController rc, ActiveStrategy alwaysStrategy) throws GameActionException {
        if (alwaysStrategy != null) {
            alwaysStrategy.execute(rc, turnCount);
            return;
        }

        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
            activeStrategy = PREPARE;
        } else if (rc.hasFlag()) {
            activeStrategy = GO_HOME;
        } else {
            activeStrategy = CAPTURE;
        }

        rc.setIndicatorString("Active strategy: " + activeStrategy);

        activeStrategy.execute(rc, turnCount);
    }

    private static void tryBuyGlobalUpgrade(RobotController rc) throws GameActionException {
        if (rc.canBuyGlobal(ATTACK)) {
            rc.buyGlobal(ATTACK);
        }
    }

    private static void spawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        // Pick a random spawn location to attempt spawning in.
        MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
    }
}
