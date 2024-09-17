package B;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;

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

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this robot, and to get
     *           information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
//        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
//        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // Make sure you spawn your robot in before you attempt to take any actions!
                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
                if (!rc.isSpawned()) {
                    spawn(rc);
                } else {
                    decide(rc);

                    switch (strategy) {
                        case RANDOM:
                            random(rc);
                            break;
                        case CAPTURE:
                            capture(rc);
                            break;
                        case GO_HOME:
                            goHome(rc);
                            break;
                        case HUNT:
                            hunt(rc);
                            break;
                    }

                    // We can also move our code into different methods or classes to better organize it!
                    updateEnemyRobots(rc);
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
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

    static Strategy strategy = Strategy.RANDOM;

    private static void hunt(RobotController rc) throws GameActionException {
        // check if we can get some bread
        // check if i'm around a lot of damaged allies, if so do a heal
        // if not, search for enemies and attack them
        // if none found nearby move towards enemy spawn point

        gotoCrumbIfPossible(rc);
        healIfPossible(rc);
        attackIfPossible(rc);
        placeTrapIfPossible(rc);
        moveToEnamySpawn(rc);
    }

    private static void placeTrapIfPossible(RobotController rc) throws GameActionException {
        if (rc.canBuild(TrapType.EXPLOSIVE, rc.getLocation())) {
            rc.build(TrapType.EXPLOSIVE, rc.getLocation());
        }
    }

    private static void gotoCrumbIfPossible(RobotController rc) throws GameActionException {
        MapLocation[] crumbs = rc.senseNearbyCrumbs(-1);
        MapLocation closestCrumb = Arrays.stream(crumbs).min(Comparator.comparingInt(x -> rc.getLocation().distanceSquaredTo(x))).orElse(null);
        if (crumbs.length > 0) {
            moveTo(rc, closestCrumb);
        }
    }

    private static void healIfPossible(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(4, rc.getTeam());
        if (nearbyAllies.length > 0) {
            RobotInfo target = chooseHealTarget(rc, nearbyAllies);
            if (target != null) {
                rc.heal(nearbyAllies[0].getLocation());
            }
        }
    }

    private static void decide(RobotController rc) throws GameActionException {
        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
            strategy = Strategy.RANDOM;
            return;
        }

        if (rc.hasFlag()) {
            strategy = Strategy.GO_HOME;
        } else {
            strategy = Strategy.CAPTURE;
        }
    }

    private static void spawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        // Pick a random spawn location to attempt spawning in.
        MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
    }

    private static void random(RobotController rc) throws GameActionException {
        // Move and attack randomly if no objective.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        } else if (rc.canAttack(nextLoc)) {
            rc.attack(nextLoc);
        }

        // Rarely attempt placing traps behind the robot.
        MapLocation prevLoc = rc.getLocation().subtract(dir);
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
            rc.build(TrapType.EXPLOSIVE, prevLoc);
    }

    private static void capture(RobotController rc) throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())) {
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");

            strategy = Strategy.GO_HOME;
            return;
        }

        // have a small chance of attacking if we are going for the flag and see an enemy
        if (rng.nextInt() % 5 < 2) {
            attackIfPossible(rc);
        }


        // Move towards the enemy flag.
        // check if i can see a flag, if not request latest general location of flags to target
        FlagInfo[] enemyFlags = rc.senseNearbyFlags(1000, rc.getTeam().opponent());

        if (enemyFlags.length > 0) {
            FlagInfo flagLocation = enemyFlags[0]; // TODO make all of them go for different flags
            moveTo(rc, flagLocation.getLocation());
        } else {
            MapLocation[] broadcastedFlags = rc.senseBroadcastFlagLocations();
            if (broadcastedFlags.length == 0) {

                return;
            }

            moveTo(rc, broadcastedFlags[0]);
        }
    }

    private static void moveToEnamySpawn(RobotController rc) throws GameActionException {
//        MapLocation[] enemySpawnLocs = rc.getAllySpawnLocations()
//        MapLocation firstLoc = enemySpawnLocs[0];
//        moveTo(rc, firstLoc);
    }

    private static RobotInfo chooseHealTarget(RobotController rc, RobotInfo[] nearbyAllies) {
        Stream<RobotInfo> healableRobots = Arrays.stream(nearbyAllies).filter(x -> rc.canHeal(x.getLocation()));
        // sort them by health and return lowest one

        return healableRobots.min(Comparator.comparingInt(x -> x.getHealth())).orElse(null);
    }

    enum Strategy {
        RANDOM,
        CAPTURE,
        GO_HOME,
        HUNT,
    }

    private static void attackIfPossible(RobotController rc) throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(4, rc.getTeam().opponent());
        if (enemyRobots.length > 0) {
            RobotInfo target = chooseAttackTarget(rc, enemyRobots);
            if (target != null) {
                rc.attack(enemyRobots[0].getLocation());
            }
        }
    }

    private static RobotInfo chooseAttackTarget(RobotController rc, RobotInfo[] enemyRobots) {
        Stream<RobotInfo> attackableRobots = Arrays.stream(enemyRobots).filter(x -> rc.canAttack(x.getLocation()));
        // sort them by health and return lowest one

        return attackableRobots.min(Comparator.comparingInt(x -> x.getHealth())).orElse(null);
    }

    private static void moveTo(RobotController rc, MapLocation location) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);
        if (rc.canMove(dir)) rc.move(dir);
        else {
            final Direction nextDirection = getNextDirection(directions, dir, rc, 0);
            if (nextDirection != null) {
                rc.move(nextDirection);
            }
        }
    }

    private static void goHome(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation firstLoc = spawnLocs[0];
//        Direction dir = rc.getLocation().directionTo(new MapLocation(0, 22));
        moveTo(rc, firstLoc);
//
//        // If we are holding an enemy flag, singularly focus on moving towards
//        // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
//        // to make sure setup phase has ended.
////        if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
//        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
//        MapLocation firstLoc = spawnLocs[0];
//        Direction dir = rc.getLocation().directionTo(new MapLocation(0, 22));
//        if (rc.canMove(dir)) rc.move(dir);
//        else {
//            final Direction nextDirection = getNextDirection(directions, dir, rc, 0);
//            if (nextDirection != null) {
//                System.out.println("direction: " + nextDirection);
//                rc.move(nextDirection);
//            }
//        }
//        }
    }

    private static Direction getNextDirection(Direction[] directions, Direction current, RobotController rc, int attempts) {
        if (attempts > directions.length) {
            return null;
        }
        int currentIndex = getIndexOf(directions, current);
        int nextIndex = (currentIndex + 1) % directions.length;
        final Direction direction = directions[nextIndex];
        if (rc.canMove(direction)) {
            return direction;
        } else {
            return getNextDirection(directions, direction, rc, ++attempts);
        }
    }

    private static int getIndexOf(Direction[] array, Direction value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1; // Return -1 if the value is not found
    }

    private static void updateEnemyRobots(RobotController rc) throws GameActionException {
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0) {
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++) {
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyRobots.length)) {
                rc.writeSharedArray(0, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}
