package org.artisan.controller;

/**
 * One temperature sample (time, BT, ET) for passing to chart or alarms.
 */
public record Sample(double timeSec, double bt, double et) {}
