package com.example.jyn.remotemeeting.Otto;

/**
 * Created by JYN on 2017-11-17.
 */

public class Event {

    public static class FragmentActivityMessage {
        private String message;

        public FragmentActivityMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }


    public static class ActivityFragmentMessage {
        private String message;

        public ActivityFragmentMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
