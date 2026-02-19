package org.artisan.device;

/**
 * DeviceChannel that reads BT/ET from a Siemens S7 PLC via S7ClientInterface.
 * Uses S7ClientStub by default when no real Moka7 client is available.
 */
public final class S7DeviceChannel implements DeviceChannel {

    private final S7ClientInterface client;
    private S7Config config;
    private boolean open;

    public S7DeviceChannel() {
        this(new S7ClientStub());
    }

    public S7DeviceChannel(S7ClientInterface client) {
        this.client = client != null ? client : new S7ClientStub();
    }

    /**
     * Starts the channel by connecting to the PLC with the given config.
     * Must be called before read(). Config is stored for read().
     */
    public void start(S7Config cfg) {
        if (cfg == null) return;
        this.config = cfg;
        open = client.connect(cfg);
    }

    /**
     * Stops the channel and disconnects.
     */
    public void stop() {
        client.disconnect();
        open = false;
    }

    @Override
    public void open() throws DeviceException {
        if (config == null) {
            config = new S7Config();
            S7Config.loadFromPreferences(config);
        }
        open = client.connect(config);
        if (!open) {
            throw new DeviceException("S7 connect failed");
        }
    }

    @Override
    public void close() {
        stop();
    }

    @Override
    public boolean isOpen() {
        return open && client.isConnected();
    }

    @Override
    public SampleResult read() throws DeviceException {
        if (!isOpen() || config == null) {
            throw new DeviceException("S7 channel not open");
        }
        float bt = client.readFloat(config.getBtDbNumber(), config.getBtDbOffset());
        float et = client.readFloat(config.getEtDbNumber(), config.getEtDbOffset());
        return SampleResult.now(bt, et);
    }

    @Override
    public String getDescription() {
        return config != null ? "S7 " + config.getHost() + ":" + config.getPort() : "S7 PLC";
    }
}
