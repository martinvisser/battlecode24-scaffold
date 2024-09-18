package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public interface Strategy {
    void execute(RobotController rc, int turnCounter) throws GameActionException;
}
