package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProfileDataTest {

    @Test
    void sizeAndLists() {
        ProfileData p = new ProfileData();
        assertEquals(0, p.size());
        p.getTimex().add(0.0);
        p.getTimex().add(60.0);
        p.getTemp1().add(20.0);
        p.getTemp1().add(100.0);
        assertEquals(2, p.size());
        p.setTitle("Test Roast");
        assertEquals("Test Roast", p.getTitle());
    }

    @Test
    void setTimexNotNull() {
        ProfileData p = new ProfileData();
        p.setTimex(null);
        assertNotNull(p.getTimex());
        assertEquals(0, p.getTimex().size());
    }

    @Test
    void computed() {
        ProfileData p = new ProfileData();
        ComputedProfileInformation comp = new ComputedProfileInformation();
        comp.setDropTime(480.0);
        p.setComputed(comp);
        assertEquals(480.0, p.getComputed().getDropTime());
    }
}
