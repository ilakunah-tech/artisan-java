package org.artisan.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackgroundProfileTest {

  @Test
  void holdsProfileDataAndMetadata() {
    ProfileData pd = new ProfileData();
    pd.setTimex(List.of(0.0, 60.0));
    pd.setTemp1(List.of(180.0, 200.0)); // ET
    pd.setTemp2(List.of(20.0, 100.0));  // BT
    pd.setDelta1(List.of(1.0, 1.5));
    pd.setDelta2(List.of(0.5, 1.0));

    BackgroundProfile bg = new BackgroundProfile(pd, "Ref", true, 12.0);

    assertFalse(bg.isEmpty());
    assertEquals("Ref", bg.getTitle());
    assertTrue(bg.isVisible());
    assertEquals(12.0, bg.getAlignOffset());
    assertEquals(2, bg.getProfileData().getTimex().size());
  }

  @Test
  void isEmptyWhenNoProfileData() {
    BackgroundProfile bg = new BackgroundProfile();
    assertTrue(bg.isEmpty());
  }

  @Test
  void isEmptyWhenTimexEmpty() {
    ProfileData pd = new ProfileData();
    BackgroundProfile bg = new BackgroundProfile(pd, "Empty", true, 0.0);
    assertTrue(bg.isEmpty());
  }
}
