package org.artisan.model;

import javafx.scene.paint.Color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurveDefinitionTest {

  @Test
  void builderConstructsCorrectly() {
    CurveDefinition c =
        CurveDefinition.builder()
            .name("BT")
            .color(Color.BLUE)
            .visible(true)
            .lineWidth(2.0f)
            .style(CurveStyle.DASHED)
            .build();

    assertEquals("BT", c.getName());
    assertEquals(Color.BLUE, c.getColor());
    assertTrue(c.isVisible());
    assertEquals(2.0f, c.getLineWidth());
    assertEquals(CurveStyle.DASHED, c.getStyle());
  }

  @Test
  void defaultsApplied() {
    CurveDefinition c = CurveDefinition.builder().build();

    assertEquals("", c.getName());
    assertEquals(Color.BLACK, c.getColor());
    assertTrue(c.isVisible());
    assertEquals(1.0f, c.getLineWidth());
    assertEquals(CurveStyle.SOLID, c.getStyle());
  }

  @Test
  void partialBuilderUsesDefaults() {
    CurveDefinition c = CurveDefinition.builder().name("ET").visible(false).build();

    assertEquals("ET", c.getName());
    assertFalse(c.isVisible());
    assertEquals(Color.BLACK, c.getColor());
    assertEquals(1.0f, c.getLineWidth());
    assertEquals(CurveStyle.SOLID, c.getStyle());
  }
}
