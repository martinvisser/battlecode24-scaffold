package B;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.*;
import java.util.stream.Stream;

import static battlecode.common.GlobalUpgrade.ATTACK;

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

    static final int Spawn1LocationX = 1;
    static final int Spawn1LocationY = 2;
    static final int Spawn2LocationX = 3;
    static final int Spawn2LocationY = 4;
    static final int Spawn3LocationX = 5;
    static final int Spawn3LocationY = 6;


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

    static Strategy strategy = Strategy.PREPARE;

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

        storeEnemySpawn(rc);

        while (true) {
            turnCount += 1;
            tryBuyGlobalUpgrade(rc); // only do every 600 turns


            try {
                if (!rc.isSpawned()) {
                    spawn(rc);
                } else {
                    decide(rc);

                    switch (strategy) {
                        case PREPARE:
                            prepare(rc);
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

    static MapLocation closestSpawnLoc;
    static Direction homeDir;

    private static void decide(RobotController rc) throws GameActionException {
        if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
            strategy = Strategy.PREPARE;
            return;
        }

        if (rc.hasFlag()) {
            strategy = Strategy.GO_HOME;
        } else {
            strategy = Strategy.CAPTURE;
        }

        rc.setIndicatorString("strategy: " + strategy);
    }

    private static void tryBuyGlobalUpgrade(RobotController rc) throws GameActionException {
        if (rc.canBuyGlobal(ATTACK)) {
            rc.buyGlobal(ATTACK);
        }
    }

    private static void storeEnemySpawn(RobotController rc) throws GameActionException {
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

    private static void hunt(RobotController rc) throws GameActionException {
        // check if we can get some bread
        // check if i'm around a lot of damaged allies, if so do a heal
        // if not, search for enemies and attack them
        // if none found nearby move towards enemy spawn point

        rc.setIndicatorString("Hunting");

        gotoCrumbIfPossible(rc);
        healIfPossible(rc);
        attackIfPossible(rc);
        placeTrapIfPossible(rc);
        moveToEnemySpawn(rc);
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
            rc.setIndicatorString("Moving to closest crumb: " + closestCrumb);
            moveTo(rc, closestCrumb);
        }
    }

    private static void healIfPossible(RobotController rc) throws GameActionException {
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

    private static void prepare(RobotController rc) throws GameActionException {
        // Move and attack randomly if no objective.
        gotoCrumbIfPossible(rc);
        moveToEnemySpawn(rc);

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

    private static void spawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        // Pick a random spawn location to attempt spawning in.
        MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
    }

    private static void moveTo(RobotController rc, MapLocation location) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(location);
        rc.setIndicatorLine(rc.getLocation(), location, 0, 255, 0);
        if (rc.canMove(dir)) {
            rc.move(dir);
        } else if (rc.canFill(location)) {
            rc.fill(location);
        } else {
            final Direction nextDirection = getNextDirection(dir, rc);
            if (nextDirection != null) {
                rc.move(nextDirection);
            }

            // if we are stuck, try to fill water or another land action
            fillNearbyWithWater(rc);
        }
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

        // filter out picked up flags
        enemyFlags = Arrays.stream(enemyFlags).filter(x -> !x.isPickedUp()).toArray(FlagInfo[]::new);

        if (enemyFlags.length > 0) {
            FlagInfo flagInfo = enemyFlags[rng.nextInt(enemyFlags.length)]; // TODO make all of them go for different flags
            final MapLocation location = flagInfo.getLocation();
            moveTo(rc, location);
        } else {
            MapLocation[] broadcastedFlags = rc.senseBroadcastFlagLocations();
            if (broadcastedFlags.length == 0) {
                hunt(rc);
                return;
            }

            final MapLocation broadcastedFlag = broadcastedFlags[rng.nextInt(broadcastedFlags.length)];
            moveTo(rc, broadcastedFlag);
        }
    }

    private static void moveToEnemySpawn(RobotController rc) throws GameActionException {//        MapLocation[] enemySpawnLocs = rc.getAllySpawnLocations()
        rc.setIndicatorString("Moving to enemy spawn");
        // choose one of the 3 enemy spawn locations and move there

        ArrayList<MapLocation> mapLocations = new ArrayList<>();
        mapLocations.add(new MapLocation(rc.readSharedArray(Spawn1LocationX), rc.readSharedArray(Spawn1LocationY)));
        mapLocations.add(new MapLocation(rc.readSharedArray(Spawn2LocationX), rc.readSharedArray(Spawn2LocationY)));
        mapLocations.add(new MapLocation(rc.readSharedArray(Spawn3LocationX), rc.readSharedArray(Spawn3LocationY)));

        MapLocation target = mapLocations.get(rng.nextInt(mapLocations.size()));

        // take average location of all enemy spawn locations
        int x = 0;
        int y = 0;
        for (MapLocation mapLocation : mapLocations) {
            x += mapLocation.x;
            y += mapLocation.y;
        }
        target = new MapLocation(x / mapLocations.size(), y / mapLocations.size());

        moveTo(rc, target);
    }

    private static RobotInfo chooseHealTarget(RobotController rc, RobotInfo[] nearbyAllies) {
        Stream<RobotInfo> healableRobots = Arrays.stream(nearbyAllies).filter(x -> rc.canHeal(x.getLocation()));
        // sort them by health and return lowest one

        return healableRobots.min(Comparator.comparingInt(x -> x.getHealth())).orElse(null);
    }

    private static void attackIfPossible(RobotController rc) throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(6, rc.getTeam().opponent());
        if (enemyRobots.length > 0) {
            RobotInfo target = chooseAttackTarget(rc, enemyRobots);
            if (target != null) {
                try {
                    rc.attack(enemyRobots[0].getLocation());
                    return;
                } catch (GameActionException e) {
                    // ignore
                }
            }
            moveTo(rc, enemyRobots[0].getLocation());
        }
    }

    private static RobotInfo chooseAttackTarget(RobotController rc, RobotInfo[] enemyRobots) {
        Stream<RobotInfo> attackableRobots = Arrays.stream(enemyRobots).filter(x -> rc.canAttack(x.getLocation()));
        // sort them by health and return lowest one

        return attackableRobots.min(Comparator.comparingInt(x -> x.getHealth())).orElse(null);
    }

    enum Strategy {
        PREPARE,
        CAPTURE,
        GO_HOME,
        HUNT,
    }

    private static void fillNearbyWithWater(RobotController rc) throws GameActionException {
        MapInfo[] nearbyMapInfos = rc.senseNearbyMapInfos(1);
        for (MapInfo mapInfo : nearbyMapInfos) {
            if (rc.canFill(mapInfo.getMapLocation())) {
                rc.fill(mapInfo.getMapLocation());
            }
        }
    }

    private static void goHome(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation currentLocation = rc.getLocation();
        if (closestSpawnLoc == null) {
            closestSpawnLoc = Arrays.stream(spawnLocs)
                    .min(Comparator.comparingInt(loc -> loc.distanceSquaredTo(currentLocation)))
                    .orElseGet(() -> spawnLocs[0]);

            rc.setIndicatorString("Going home to: " + closestSpawnLoc);
        }
        if (homeDir == null || !rc.canMove(homeDir)) {
            homeDir = rc.getLocation().directionTo(closestSpawnLoc);
        }

        // Check if a closest spawn location was found
        moveTo(rc, closestSpawnLoc);
    }

    private static Direction getNextDirection(Direction current, RobotController rc) throws GameActionException {
        if (rc.canMove(current.rotateRight())) {
            return current.rotateRight();
        } else if (rc.canMove(current.rotateRight().rotateRight())) {
            return current.rotateRight().rotateRight();
        } else if (rc.canMove(current.rotateRight().rotateRight().rotateRight())) {
            return current.rotateRight().rotateRight().rotateRight();
        } else if (rc.canMove(current.rotateLeft())) {
            return current.rotateLeft();
        } else if (rc.canMove(current.rotateLeft().rotateLeft())) {
            return current.rotateLeft().rotateLeft();
        } else if (rc.canMove(current.rotateLeft().rotateLeft().rotateLeft())) {
            return current.rotateLeft().rotateLeft().rotateLeft();
        } else if (rc.canMove(current.opposite())) {
            return current.opposite();
        } else {
            final MapInfo[] a = rc.senseNearbyMapInfos(2);
            return Arrays.stream(a)
                    .filter(mapInfo -> mapInfo.isPassable())
                    .map(mapInfo -> rc.getLocation().directionTo(mapInfo.getMapLocation()))
                    .filter(dir -> dir != current)
                    .filter(dir -> rc.canMove(dir))
                    .findAny()
                    .orElse(null);
        }
    }

    private static void updateEnemyRobots(RobotController rc) throws GameActionException {
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0) {
//            rc.setIndicatorString("There are nearby enemy robots! Scary!");
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
