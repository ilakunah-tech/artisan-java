package org.artisan.view;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.artisan.controller.AppController;
import org.artisan.controller.AppSettings;
import org.artisan.controller.RoastSession;
import org.artisan.controller.Sample;
import org.artisan.device.DevicePort;
import org.artisan.device.StubDevice;
import org.artisan.model.ArtisanTime;
import org.artisan.model.AxisConfig;
import org.artisan.model.ColorConfig;
import org.artisan.model.CurveSet;
import org.artisan.model.Sampling;

/**
 * Main application window: toolbar (ON/OFF, event buttons), chart, status bar.
 * Applies AtlantaFX Primer Dark theme. Wires Sampling to chart update on JavaFX thread.
 */
public final class MainWindow extends Application {

  private AppController appController;
  private AppSettings appSettings;
  private Label statusBar;
  private Label elapsedLabel;
  private boolean samplingOn;

  @Override
  public void start(Stage primaryStage) {
    appSettings = AppSettings.load();

    RoastSession session = new RoastSession();
    ArtisanTime timeclock = new ArtisanTime();
    Sampling sampling = new Sampling(timeclock);
    DevicePort device = new StubDevice();
    ColorConfig colorConfig = new ColorConfig(appSettings.isDarkTheme()
        ? ColorConfig.Theme.DARK : ColorConfig.Theme.LIGHT);
    AxisConfig axisConfig = new AxisConfig();
    RoastChartController chartController = new RoastChartController(
        session.getCanvasData(), colorConfig, axisConfig);

    appController = new AppController(
        session, sampling, device, chartController, axisConfig, colorConfig, CurveSet.createDefault());

    appController.setOnSampleConsumer(s -> Platform.runLater(() -> {
      if (appController.getChartController() != null) {
        appController.getChartController().onSample(s.timeSec(), s.bt(), s.et());
      }
    }));

    BorderPane root = new BorderPane();
    root.getStylesheets().add("io/github/mkpaz/atlantafx/base/theme/primer-dark.css");

    HBox toolbar = new HBox(10);
    Button onOff = new Button("ON");
    onOff.setOnAction(e -> toggleSampling());
    Button charge = new Button("CHARGE");
    charge.setOnAction(e -> appController.onChargeButton());
    Button dryEnd = new Button("DRY END");
    dryEnd.setOnAction(e -> appController.onDryEndButton());
    Button fcStart = new Button("FC START");
    fcStart.setOnAction(e -> appController.onFcStartButton());
    Button fcEnd = new Button("FC END");
    fcEnd.setOnAction(e -> appController.onFcEndButton());
    Button drop = new Button("DROP");
    drop.setOnAction(e -> appController.onDropButton());
    Button coolEnd = new Button("COOL END");
    coolEnd.setOnAction(e -> appController.onCoolEndButton());
    toolbar.getChildren().addAll(onOff, charge, dryEnd, fcStart, fcEnd, drop, coolEnd);

    Node chartView = chartController.getView();
    chartController.startUpdateTimer();

    statusBar = new Label("BT: —  ET: —  RoR: —");
    elapsedLabel = new Label("Elapsed: 0 s");
    HBox status = new HBox(20, statusBar, elapsedLabel);

    AnimationTimer statusTimer = new AnimationTimer() {
      @Override
      public void handle(long now) {
        if (appController.getSession().getCanvasData().getTimex().isEmpty()) {
          statusBar.setText("BT: —  ET: —  RoR: —");
        } else {
          var tx = appController.getSession().getCanvasData().getTimex();
          var t1 = appController.getSession().getCanvasData().getTemp1();
          var t2 = appController.getSession().getCanvasData().getTemp2();
          var d1 = appController.getSession().getCanvasData().getDelta1();
          int i = tx.size() - 1;
          double bt = i < t2.size() ? t2.get(i) : 0;
          double et = i < t1.size() ? t1.get(i) : 0;
          double ror = i < d1.size() ? d1.get(i) : 0;
          statusBar.setText(String.format("BT: %.1f  ET: %.1f  RoR: %.1f", bt, et, ror));
        }
        if (samplingOn) {
          double elapsed = appController.getSampling().getElapsedMs() / 1000.0;
          elapsedLabel.setText(String.format("Elapsed: %.0f s", elapsed));
        }
      }
    };
    statusTimer.start();

    root.setTop(new VBox(toolbar));
    root.setCenter(chartView);
    root.setBottom(status);

    Scene scene = new Scene(root, 1000, 600);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Artisan");
    primaryStage.setOnCloseRequest(e -> {
      appController.stopSampling();
      appSettings.save();
    });
    primaryStage.show();
  }

  private void toggleSampling() {
    samplingOn = !samplingOn;
    if (samplingOn) {
      appController.startSampling();
    } else {
      appController.stopSampling();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
