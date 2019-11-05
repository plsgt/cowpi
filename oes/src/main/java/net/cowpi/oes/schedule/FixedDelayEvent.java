package net.cowpi.oes.schedule;

import java.util.concurrent.TimeUnit;

public interface FixedDelayEvent extends Runnable {

    long getDelay();

    TimeUnit getTimeUnit();

}
