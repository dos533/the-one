package movement.schedule;

import core.Settings;
import movement.room.RoomBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class Schedule {

    private static final int NR_LECTURES_MIN = 1;
    private static final int NR_LECTURES_MAX = 4;

    // time in hours
    private static final int START_TIME_MIN = 8;
    private static final int START_TIME_MAX = 18;

    ArrayList<ScheduleSlot> slots;
    RoomBase.RoomType exitRoom;

    static ArrayList<Lecture> lectures;

    static {
        Settings s = new Settings("Schedule");
        lectures = new ArrayList<>();

        Random rng = new Random(s.getInt("RNG_SEED"));

        int nrLectures = RoomBase.LectureRooms.size() * 4;
        // this might never stop if generating non-overlapping lectures is not possible
        generateNextLecture: while (lectures.size() < nrLectures) {

            double time = rng.nextInt(START_TIME_MAX - START_TIME_MIN + 1) + START_TIME_MIN;
            RoomBase.RoomType room = RoomBase.GetRandomLectureRoom();

            // always 2h at the moment
            double duration = 2;

            for (int j = 0; j < lectures.size(); j++) {
                Lecture l = lectures.get(j);
                // skip if overlaps with other lecture in the same room
                if (l.room == room && time + duration > l.time && time < l.time + l.duration) {
                    continue generateNextLecture;
                }
            }

            lectures.add(new Lecture(time, duration, room));

        }

        System.out.println("GENERATED LECTURES "+lectures);
    }

    public Schedule(ArrayList<ScheduleSlot> slots) {
        this.slots = slots;
        this.exitRoom = RoomBase.GetRandomEntranceAndExitOption();
    }

    public static Schedule fromSeed(int seed) {

        Random rng = new Random(seed);

        ArrayList<ScheduleSlot> slots = new ArrayList<>();

        int nrLectures = rng.nextInt(NR_LECTURES_MAX - NR_LECTURES_MIN + 1) + NR_LECTURES_MIN;

        ArrayList<Lecture> chooseFromLectures = new ArrayList<>(lectures);

        // first pick all lectures
        pickNextLecture: while (slots.size() < nrLectures && chooseFromLectures.size() > 0) {

            int index = rng.nextInt(chooseFromLectures.size());
            Lecture lecture = chooseFromLectures.get(index);

            for (int j = 0; j < slots.size(); j++) {
                ScheduleSlot slot = slots.get(j);
                // skip if overlaps with other lecture
                if (lecture.time + lecture.duration >= slot.time && lecture.time <= slot.time + slot.duration) {
                    chooseFromLectures.remove(index);
                    continue pickNextLecture;
                }
            }

            // add some randomness aka people coming/leaving early/late

            // lets people come around quarter past the hour +- 10 mins
            double time = lecture.time + 0.25 + (rng.nextDouble() - 0.5) * 0.2;

            // lets people leave around quarter to the hour +- 10 mins
            double duration = lecture.duration - 0.5 + (rng.nextDouble() - 0.5) * 0.2;

            slots.add(new ScheduleSlot(time, duration, lecture.room));
        }

        slots.sort(Comparator.comparingDouble(o -> o.time));

        boolean hadLunch = false;

        // second fill slots between lecture
        int i = 0;
        while (i < slots.size()-1) {

            ScheduleSlot l1 = slots.get(i);
            ScheduleSlot l2 = slots.get(i+1);

            double start = l1.time + l1.duration;
            double gap = l2.time - start;

            // go for lunch only if at least 20 mins time and after 10:30

            if (!hadLunch && start >= 10.5 && gap > 0.2) {
                double gettingLunchDuration = 0.1;

                // getting lunch (10 mins)
                RoomBase.RoomType room = RoomBase.GetRandomLunchOption();
                slots.add(++i, new ScheduleSlot(start, gettingLunchDuration, room));
                hadLunch = true;

                // eating lunch (rest available time)
                RoomBase.RoomType room2 = RoomBase.GetRandomGatheringRoom();
                slots.add(++i, new ScheduleSlot(start + gettingLunchDuration, gap - gettingLunchDuration, room2));

            } else {
                // just hangout for the available time
                RoomBase.RoomType room = RoomBase.GetRandomGatheringRoom();
                slots.add(++i, new ScheduleSlot(start, gap, room));
            }
            i++;
        }

        System.out.println("GENERATED SCHEDULE: "+slots);

        return new Schedule(slots);
    }

    public RoomBase getNextRoom(double currentTime) {

        double currentTimeAsHourOfDay = 7 + (currentTime / 60 / 60);

        if (currentTimeAsHourOfDay < slots.get(0).time) {
            return getExitRoom();
        }

        for (ScheduleSlot slot : slots) {
            if (currentTimeAsHourOfDay < slot.time + slot.duration) {
                return getRoomForSlot(slot);
            }
        }

        return getExitRoom();
    }

    private RoomBase getExitRoom() {
        return RoomBase.AllRooms.get(exitRoom);
    }

    private RoomBase getRoomForSlot(ScheduleSlot slot) {
        return RoomBase.AllRooms.get(slot.room);
    }

    public double getNextSlotTime(double currentTime) {
        double currentTimeAsHourOfDay = 7 + (currentTime / 60 / 60);

        for (ScheduleSlot slot : slots) {
            if (currentTimeAsHourOfDay < slot.time) {
                return slot.time;
            }
        }

        return Double.MAX_VALUE;
    }
}

class Lecture {

    public double time;
    public double duration;
    public RoomBase.RoomType room;

    public Lecture(double time, double duration, RoomBase.RoomType room) {
        this.time = time;
        this.duration = duration;
        this.room = room;
    }

    @Override
    public String toString() {
        return "Lecture{" +
                "time=" + time +
                ", duration=" + duration +
                ", room=" + room +
                '}';
    }
}

class ScheduleSlot {

    public double time;
    public double duration;
    public RoomBase.RoomType room;

    public ScheduleSlot(double time, double duration, RoomBase.RoomType room) {
        this.time = time;
        this.duration = duration;
        this.room = room;
    }

    @Override
    public String toString() {
        return "ScheduleSlot{" +
                "time=" + time +
                ", duration=" + duration +
                ", room=" + room +
                '}';
    }
}