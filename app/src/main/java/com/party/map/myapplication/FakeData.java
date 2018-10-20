package com.party.map.myapplication;

public final class FakeData {

    static class Alarm{
        public String heure;
        public boolean enabled;

        public Alarm(String heure, boolean enabled) {
            this.heure = heure;
            this.enabled = enabled;
        }
    }

    public static Alarm[] getFakeAlarm() {
        return new Alarm[] {
                new Alarm("9 : 8", false),
                new Alarm("7 : 0", true),
                new Alarm("9 : 20", false),
                new Alarm("8 : 3", true),
                new Alarm("4 : 15", false),
                new Alarm("7 : 45", true),
                new Alarm("17 : 00", false),


        };
    }
}
