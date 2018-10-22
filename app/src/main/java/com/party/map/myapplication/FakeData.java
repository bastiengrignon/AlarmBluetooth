package com.party.map.myapplication;

final class FakeData {

    static class Alarm{
        String heure;
        public boolean enabled;

        Alarm(String heure, boolean enabled) {
            this.heure = heure;
            this.enabled = enabled;
        }
    }

    static Alarm[] getFakeAlarm() {
        return new Alarm[] {
                new Alarm("09 : 08", false),
                new Alarm("07 : 00", true),
                new Alarm("09 : 20", false),
                new Alarm("08 : 03", true),
                new Alarm("04 : 15", false),
                new Alarm("07 : 45", true),
                new Alarm("17 : 00", false),


        };
    }
}
