package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackgroundProfileTest {

  @Test
  void fromCanvasDataCopiesDataChangesToOriginalDoNotAffectBackground() {
    CanvasData canvas = new CanvasData();
    canvas.addDataPoint(0.0, 20.0, 180.0);
    canvas.addDataPoint(60.0, 100.0, 200.0);
    canvas.setDelta1(List.of(1.0, 1.5));
    canvas.setDelta2(List.of(0.5, 1.0));

    BackgroundProfile bg = BackgroundProfile.fromCanvasData(canvas, "Ref");

    assertEquals(2, bg.getTimex().size());
    assertEquals(0.0, bg.getTimex().get(0));
    assertEquals(60.0, bg.getTimex().get(1));
    assertEquals(180.0, bg.getTemp1().get(0));
    assertEquals(200.0, bg.getTemp1().get(1));
    assertEquals(20.0, bg.getTemp2().get(0));
    assertEquals(100.0, bg.getTemp2().get(1));
    assertEquals(1.0, bg.getDelta1().get(0));
    assertEquals(0.5, bg.getDelta2().get(0));
    assertEquals("Ref", bg.getProfileName());
    assertFalse(bg.isEmpty());

    // Mutate original; background must be unchanged
    canvas.clear();
    canvas.addDataPoint(100.0, 50.0, 150.0);

    assertEquals(2, bg.getTimex().size());
    assertEquals(0.0, bg.getTimex().get(0));
    assertEquals(1, canvas.getTimex().size());
    assertEquals(100.0, canvas.getTimex().get(0));
  }

  @Test
  void isEmptyOnEmptyCanvasData() {
    CanvasData empty = new CanvasData();
    BackgroundProfile bg = BackgroundProfile.fromCanvasData(empty, "Empty");

    assertTrue(bg.isEmpty());
    assertTrue(bg.getTimex().isEmpty());
    assertTrue(bg.getTemp1().isEmpty());
    assertTrue(bg.getTemp2().isEmpty());
  }

  @Test
  void fromCanvasDataNullYieldsEmptyProfile() {
    BackgroundProfile bg = BackgroundProfile.fromCanvasData(null, "Null");

    assertTrue(bg.isEmpty());
    assertEquals("Null", bg.getProfileName());
  }
}
