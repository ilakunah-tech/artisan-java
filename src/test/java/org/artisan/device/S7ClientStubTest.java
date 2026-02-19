package org.artisan.device;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class S7ClientStubTest {

    @Test
    void connect_returnsTrue() {
        S7ClientStub stub = new S7ClientStub();
        S7Config cfg = new S7Config();
        assertTrue(stub.connect(cfg));
    }

    @Test
    void isConnected_returnsTrueAfterConnect() {
        S7ClientStub stub = new S7ClientStub();
        stub.connect(new S7Config());
        assertTrue(stub.isConnected());
    }

    @Test
    void readFloat_returnsFiniteValue() {
        S7ClientStub stub = new S7ClientStub();
        stub.connect(new S7Config());
        float v = stub.readFloat(1, 0);
        assertTrue(Float.isFinite(v));
    }

    @Test
    void disconnect_doesNotThrow() {
        S7ClientStub stub = new S7ClientStub();
        stub.connect(new S7Config());
        assertDoesNotThrow(stub::disconnect);
        assertFalse(stub.isConnected());
    }
}
