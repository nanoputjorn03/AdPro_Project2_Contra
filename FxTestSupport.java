package se233.adpro2;

import javafx.application.Platform;

public final class FxTestSupport {
    private static volatile boolean started = false;

    public static synchronized void initFx() {
        if (started) return;
        try {
            Platform.startup(() -> {});   // start JavaFX toolkit once
        } catch (IllegalStateException already) {
            // FX already started – ignore
        }
        started = true;
    }
}
