package org.artisan.model;

import javafx.scene.paint.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CurveSetTest {

  private CurveSet set;

  @BeforeEach
  void setUp() {
    set = CurveSet.createDefault();
  }

  @Test
  void getAllVisibleReturnsOnlyVisibleCurves() {
    CurveDefinition visibleBt =
        CurveDefinition.builder().name("BT").visible(true).build();
    CurveDefinition hiddenEt =
        CurveDefinition.builder().name("ET").visible(false).build();
    CurveSet s =
        set.withBt(visibleBt)
            .withEt(hiddenEt)
            .withDeltaBt(CurveDefinition.builder().name("ΔBT").visible(true).build())
            .withDeltaEt(CurveDefinition.builder().name("ΔET").visible(false).build());

    List<CurveDefinition> visible = s.getAllVisible();

    assertEquals(2, visible.size());
    assertTrue(visible.stream().anyMatch(c -> "BT".equals(c.getName())));
    assertTrue(visible.stream().anyMatch(c -> "ΔBT".equals(c.getName())));
    assertFalse(visible.stream().anyMatch(c -> "ET".equals(c.getName())));
    assertFalse(visible.stream().anyMatch(c -> "ΔET".equals(c.getName())));
  }

  @Test
  void withBtReturnsNewInstance() {
    CurveDefinition bt = CurveDefinition.builder().name("BT").color(Color.RED).build();
    CurveSet updated = set.withBt(bt);

    assertNotSame(set, updated);
    assertEquals(bt, updated.getBt());
    assertNotSame(set.getBt(), updated.getBt());
  }

  @Test
  void addExtraCurveBeyondLimitThrows() {
    CurveSet s =
        set.addExtraCurve(CurveDefinition.builder().name("E1").build())
            .addExtraCurve(CurveDefinition.builder().name("E2").build())
            .addExtraCurve(CurveDefinition.builder().name("E3").build())
            .addExtraCurve(CurveDefinition.builder().name("E4").build());

    assertEquals(4, s.getExtraCurves().size());
    assertThrows(
        IllegalStateException.class,
        () -> s.addExtraCurve(CurveDefinition.builder().name("E5").build()));
  }

  @Test
  void getCurveByNameFoundAndNotFound() {
    CurveDefinition bt = CurveDefinition.builder().name("BT").build();
    CurveDefinition et = CurveDefinition.builder().name("ET").build();
    CurveSet s = set.withBt(bt).withEt(et);

    assertTrue(s.getCurveByName("BT").isPresent());
    assertEquals(bt, s.getCurveByName("BT").get());
    assertTrue(s.getCurveByName("ET").isPresent());
    assertEquals(et, s.getCurveByName("ET").get());
    assertTrue(s.getCurveByName("Unknown").isEmpty());
    assertTrue(s.getCurveByName(null).isEmpty());
  }

  @Test
  void getCurveByNameFindsDeltaAndExtra() {
    CurveDefinition deltaBt = CurveDefinition.builder().name("ΔBT").build();
    CurveDefinition extra = CurveDefinition.builder().name("Extra1").build();
    CurveSet s =
        set.withDeltaBt(deltaBt).addExtraCurve(extra);

    assertTrue(s.getCurveByName("ΔBT").isPresent());
    assertTrue(s.getCurveByName("Extra1").isPresent());
  }
}
