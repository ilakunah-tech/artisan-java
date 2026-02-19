package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for RoastSimulatorChannel.
 */
class RoastSimulatorChannelTest {

    @Test
    void open_isOpen_returnsTrue() {
        RoastSimulatorChannel ch = new RoastSimulatorChannel(new SimulatorConfig());
        assertFalse(ch.isOpen());
        ch.open();
        assertTrue(ch.isOpen());
        ch.close();
    }

    @Test
    void read_returnsBtAbove25AfterStart() throws DeviceException {
        SimulatorConfig config = new SimulatorConfig();
        config.setBtStartTemp(25.0);
        RoastSimulatorChannel ch = new RoastSimulatorChannel(config);
        ch.open();
        var result = ch.read();
        ch.close();
        assertNotNull(result);
        assertTrue(result.bt() >= 24.0 && result.bt() <= 30.0, "BT after first read should be near start (25): " + result.bt());
    }

    @Test
    void read_btIncreasesOverTime() throws DeviceException {
        RoastSimulatorChannel ch = new RoastSimulatorChannel(new SimulatorConfig());
        ch.open();
        double bt0 = ch.read().bt();
        double bt9 = bt0;
        for (int i = 0; i < 9; i++) {
            bt9 = ch.read().bt();
        }
        ch.close();
        assertTrue(bt9 > bt0, "BT should increase over 10 reads: bt0=" + bt0 + " bt9=" + bt9);
    }

    @Test
    void read_etCloseToBtPlusOffset() throws DeviceException {
        SimulatorConfig config = new SimulatorConfig();
        config.setEtOffset(10.0);
        config.setNoiseAmplitude(0);
        RoastSimulatorChannel ch = new RoastSimulatorChannel(config);
        ch.open();
        for (int i = 0; i < 5; i++) {
            var r = ch.read();
            double diff = Math.abs(r.et() - r.bt() - 10.0);
            assertTrue(diff < 5.0, "|ET - BT - offset| should be < 5: et=" + r.et() + " bt=" + r.bt() + " diff=" + diff);
        }
        ch.close();
    }

    @Test
    void close_isOpen_returnsFalse() {
        RoastSimulatorChannel ch = new RoastSimulatorChannel(new SimulatorConfig());
        ch.open();
        assertTrue(ch.isOpen());
        ch.close();
        assertFalse(ch.isOpen());
    }
}
