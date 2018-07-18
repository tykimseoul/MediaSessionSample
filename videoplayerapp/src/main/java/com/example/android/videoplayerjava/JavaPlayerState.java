package com.example.android.videoplayerjava;

public class JavaPlayerState {
    private int window = 0;
    private long position = 0L;
    private boolean whenReady = true;

    public JavaPlayerState() {
    }

    public int getWindow() {
        return window;
    }

    public long getPosition() {
        return position;
    }

    public boolean isWhenReady() {
        return whenReady;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public void setWhenReady(boolean whenReady) {
        this.whenReady = whenReady;
    }
}
