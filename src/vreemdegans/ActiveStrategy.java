package vreemdegans;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

enum ActiveStrategy {
    PREPARE(new PrepareStrategy()),
    CAPTURE(new CaptureStrategy()),
    GO_HOME(new BringBackTheGoodiesStrategy()),
    SUPER_HUNT(new SuperHunterStrategy()),
    GENERIC(new GeneralStrategy()),
    BUILD(new BuilderStrategy());

    private final Strategy strategy;

    ActiveStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    void execute(RobotController rc, int turnCounter) throws GameActionException {
        strategy.execute(rc, turnCounter);
    }

    public void setAndExecute(RobotController rc, int turnCounter) {
        RobotPlayer.activeStrategy = this;
        try {
            this.execute(rc, turnCounter);
        } catch (GameActionException e) {
            // Do nothing
        }
    }
}
