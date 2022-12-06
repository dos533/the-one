package movement.schedule;

import java.util.ArrayList;
import java.util.Random;

public class Schedule {


    private static final int NR_LECTURES_MIN = 1;
    private static final int NR_LECTURES_MAX = 4;

    // time in hours
    private static final int START_TIME_MIN = 8;
    private static final int START_TIME_MAX = 12;

    private static final int WAIT_TIME_MIN = 0;
    private static final int WAIT_TIME_MAX = 2;

    ArrayList<ScheduleItem> items;

    public Schedule(ArrayList<ScheduleItem> items) {
        this.items = items;
    }

    public static Schedule fromSeed(int seed) {

        Random rng = new Random(seed);
        ArrayList<ScheduleItem> items = new ArrayList<>();


        int nrLectures = rng.nextInt(NR_LECTURES_MAX - NR_LECTURES_MIN + 1) + NR_LECTURES_MIN;

        // schedule always starts with a lecture

        int nextTime = rng.nextInt(START_TIME_MAX - START_TIME_MIN + 1) + START_TIME_MIN;

        // TODO: generate duration and place
        Lecture firstLecture = new Lecture(nextTime, 1, 1);
        items.add(firstLecture);

        for (int i = 1; i < nrLectures; i++) {

            int waitTime = rng.nextInt(WAIT_TIME_MAX - WAIT_TIME_MIN + 1) + WAIT_TIME_MIN;
            nextTime = nextTime + waitTime;

            // TODO: generate duration and place
            Lecture nextLecture = new Lecture(nextTime, 1, 1);
            items.add(nextLecture);
        }

        // TODO: generate other items, like lunch and study time

        return new Schedule(items);
    }

}




class ScheduleItem {

    /*

    Lecture:
      - fixed place and time
      - find route to destination and move along path
      - don't respect encounters
      - stay at lecture until over

    Lunch:
      - fixed time, variable place
      - scheduled in between lectures, around noon, when enough time
      - pick place at random
      - find route to destination and move along path
      - respect encounters only by other nodes in lunch mode - opt. change destination
      - stay there while waiting & eating

    Study:
      - fixed place, variable time
      - scheduled in between lectures, when enough time
      - pick time at random
      - find route to destination and move along path
      - respect encounters
      - stay there until time over

    Roam:
      - variable place, variable time
      - scheduled to fill gaps in schedule
      - pick place at random, full available time
      - find route to destination and move along path
      - respect encounters
      - stay there until time over





     */



}


class Lecture extends ScheduleItem {

    private int time;
    private int duration;
    private int place;

    public Lecture(int time, int duration, int place) {
        this.time = time;
        this.duration = duration;
        this.place = place;
    }

    public int getTime() {
        return time;
    }

    public int getDuration() {
        return duration;
    }

    public int getPlace() {
        return place;
    }
}
