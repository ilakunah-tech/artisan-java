package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ComputedProfileInformationTest {

    @Test
    void gettersSetters() {
        ComputedProfileInformation c = new ComputedProfileInformation();
        assertNull(c.getChargeEt());
        c.setChargeEt(100.0);
        assertEquals(100.0, c.getChargeEt());
        c.setTotalTime(600.0);
        assertEquals(600.0, c.getTotalTime());
    }

    @Test
    void equalsAndHashCode() {
        ComputedProfileInformation a = new ComputedProfileInformation();
        a.setChargeEt(100.0);
        a.setTotalTime(600.0);
        ComputedProfileInformation b = new ComputedProfileInformation();
        b.setChargeEt(100.0);
        b.setTotalTime(600.0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.setTotalTime(601.0);
        assertNotEquals(a, b);
    }
}
