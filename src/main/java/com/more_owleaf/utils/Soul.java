package com.more_owleaf.utils;

public class Soul implements ISoul {
    private int souls;

    public Soul(int initialSouls) {
        this.souls = initialSouls;
    }

    @Override
    public int getSouls() {
        return this.souls;
    }

    @Override
    public void setSouls(int souls) {
        this.souls = souls;
    }

    @Override
    public void addSouls(int souls) {
        this.souls += souls;
    }

    @Override
    public void removeSouls(int souls) {
        this.souls = Math.max(0, this.souls - souls);
    }
}