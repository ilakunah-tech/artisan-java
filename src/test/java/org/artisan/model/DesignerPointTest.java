package org.artisan.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DesignerPointTest {

    @Test
    void compareTo_byTimeSec() {
        DesignerPoint a = new DesignerPoint(10, 200);
        DesignerPoint b = new DesignerPoint(20, 200);
        assertTrue(a.compareTo(b) < 0);
    }
}

