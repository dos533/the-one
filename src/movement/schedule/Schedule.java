package movement.schedule;

import core.Settings;
import movement.MovementModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

public class Schedule {

    private static final int NR_LECTURES_MIN = 1;
    private static final int NR_LECTURES_MAX = 4;

    // time in hours
    private static final int START_TIME_MIN = 8;
    private static final int START_TIME_MAX = 16;

    private static final int WAIT_TIME_MIN = 0;
    private static final int WAIT_TIME_MAX = 2;

    ArrayList<ScheduleItem> items;

    static ArrayList<Lecture> lectures;

    static {
        Settings s = new Settings("Schedule");
        lectures = new ArrayList<>();

        ArrayList<Integer> lectureRooms = new ArrayList<>();
        lectureRooms.add(1);
        lectureRooms.add(2);
        lectureRooms.add(3);
        lectureRooms.add(4);

        Random rng = new Random(s.getInt("RNG_SEED"));

        // this might never stop if generating non-overlapping lectures is not possible
        generateNextLecture: while (lectures.size() < 20) {

            int time = rng.nextInt(START_TIME_MAX - START_TIME_MIN + 1) + START_TIME_MIN;
            int room = rng.nextInt(lectureRooms.size());

            double duration = 1.5;

            for (int j = 0; j < lectures.size(); j++) {
                var l = lectures.get(j);
                // skip if overlaps with other lecture in the same room
                if (l.room == room && time + duration >= l.time && time <= l.time + l.duration) {
                    continue generateNextLecture;
                }
            }

            lectures.add(new Lecture(time, duration, room));
        }
    }

    public Schedule(ArrayList<ScheduleItem> items) {
        this.items = items;
    }

    public static Schedule fromSeed(int seed) {

        Random rng = new Random(seed);

        ArrayList<ScheduleItem> items = new ArrayList<>();

        int nrLectures = rng.nextInt(NR_LECTURES_MAX - NR_LECTURES_MIN + 1) + NR_LECTURES_MIN;

        var chooseFromLectures = new ArrayList<>(lectures);

        pickNextLecture: while (items.size() < nrLectures && chooseFromLectures.size() > 0) {

            var index = rng.nextInt(chooseFromLectures.size());
            var lecture = chooseFromLectures.get(index);

            for (int j = 0; j < items.size(); j++) {
                var item = items.get(j);
                // skip if overlaps with other lecture
                if (lecture.time + lecture.duration >= item.time && lecture.time <= item.time + item.duration) {
                    chooseFromLectures.remove(index);
                    continue pickNextLecture;
                }
            }

            items.add(new ScheduleItem(RoomType.lecture, lecture.time, lecture.duration, lecture.room));
        }

        items.sort(Comparator.comparingDouble(o -> o.time));

        boolean hadLunch = false;

        int i = 0;
        while (i < items.size()-1) {

            var l1 = items.get(i);
            var l2 = items.get(i+1);

            var start = l1.time + l1.duration;
            var gap = l2.time - start;

            if (!hadLunch && gap > 0.5) {

                // choose room for lunch
                items.add(i+1, new ScheduleItem(RoomType.eating, start, gap, 0));
                hadLunch = true;
            } else {
                // choose room for hangout
                items.add(i+1, new ScheduleItem(RoomType.hangout, start, gap, 0));
            }
            i += 2;

        }

        return new Schedule(items);
    }

}

enum RoomType {
    lecture, eating, hangout, outside
}

class Lecture {

    public double time;
    public double duration;
    public int room;

    public Lecture(double time, double duration, int room) {
        this.time = time;
        this.duration = duration;
        this.room = room;
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

    public RoomType type;
    public double time;
    public double duration;
    public int room;

    public ScheduleItem(RoomType type, double time, double duration, int room) {
        this.type = type;
        this.time = time;
        this.duration = duration;
        this.room = room;
    }
}
