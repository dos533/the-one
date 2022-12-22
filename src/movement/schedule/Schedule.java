package movement.schedule;

import core.DTNHost;
import core.Settings;
import movement.room.RoomBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class Schedule {

    private static final int START_OF_DAY = 8;
    private static final int END_OF_DAY = 20;

    private static final int NR_LECTURES_MIN = 1;
    private static final int NR_LECTURES_MAX = 3;

    // time in hours
    private static final int START_TIME_MIN = 8;
    private static final int START_TIME_MAX = 18;

    private static final double FIVE_MINS = 1.0 / 12.0;
    private static final double TEN_MINS = 1.0 / 6.0;
    private static final double QUARTER_HOUR = 1.0 / 4.0;
    private static final double TWENTY_MINS = 1.0 / 3.0;
    private static final double HALF_HOUR = 1.0 / 2.0;


    private static double STUDENT_PROB_EAT;
    private static double STUDENT_PROB_HANGOUT;
    private static double STUDENT_PROB_CHAIR;

    private static double PROF_PROB_EAT;
    private static double PROF_PROB_HANGOUT;
    private static double PROF_PROB_CHAIR;

    private static double VISITOR_PROB_EAT;
    private static double VISITOR_PROB_HANGOUT;
    private static double VISITOR_PROB_CHAIR;

    ArrayList<ScheduleSlot> slots;
    RoomBase.RoomType exitRoom;

    static ArrayList<Lecture> lectures;

    static {
        Settings s = new Settings("Schedule");
        lectures = new ArrayList<>();

        Random rng = new Random(s.getInt("RNG_SEED"));

        double[] studentProbs = s.getCsvDoubles("STUDENT_PROB" ,3);
        STUDENT_PROB_EAT = studentProbs[0];
        STUDENT_PROB_HANGOUT = studentProbs[1];
        STUDENT_PROB_CHAIR = studentProbs[2];

        System.out.println(STUDENT_PROB_CHAIR);

        assert(STUDENT_PROB_EAT + STUDENT_PROB_CHAIR + STUDENT_PROB_HANGOUT == 1);

        double[] profProbs = s.getCsvDoubles("PROF_PROB" ,3);
        PROF_PROB_EAT = profProbs[0];
        PROF_PROB_HANGOUT = profProbs[1];
        PROF_PROB_CHAIR = profProbs[2];

        assert(PROF_PROB_EAT + PROF_PROB_HANGOUT + PROF_PROB_CHAIR == 1);

        double[] visitorProbs = s.getCsvDoubles("VISITOR_PROB" ,3);
        VISITOR_PROB_EAT = visitorProbs[0];
        VISITOR_PROB_HANGOUT = visitorProbs[1];
        VISITOR_PROB_CHAIR = visitorProbs[2];

        assert(PROF_PROB_EAT + PROF_PROB_HANGOUT + PROF_PROB_CHAIR == 1);

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

    public static Schedule empty() {
        return new Schedule(new ArrayList<>());
    }

    public static Schedule generateForHost(DTNHost host) {
        int seed = host.getAddress();
        if (host.groupId.equals("student")) {
            return Schedule.forStudent(seed);
        } else if (host.groupId.equals("professor")) {
            return Schedule.forProfessor(seed);
        } else if (host.groupId.equals("barista")) {
            return Schedule.forBarista(seed);
        } else if (host.groupId.equals("visitor")) {
            return Schedule.forVisitor(seed);
        } else if (host.groupId.equals("cleaner")) {
            return Schedule.forCleaner(seed);
        } else {
                return Schedule.empty();

        }
    }

    public static Schedule forStudent(int seed) {
        Random rng = randomForSeed(seed);

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
            double time = lecture.time + QUARTER_HOUR + (rng.nextDouble() - 0.5) * TWENTY_MINS;

            // lets people leave around quarter to the hour +- 10 mins
            double duration = lecture.duration - HALF_HOUR + (rng.nextDouble() - 0.5) * TWENTY_MINS;

            slots.add(new ScheduleSlot(time, duration, lecture.room));
        }

        slots.sort(Comparator.comparingDouble(o -> o.time));

        // second fill slots between lectures
        int i = 0;
        while (i < slots.size()-1) {

            ScheduleSlot l1 = slots.get(i);
            ScheduleSlot l2 = slots.get(i+1);

            double start = l1.time + l1.duration;
            double gap = l2.time - start;

            if (gap == 0) {
                i++;
                continue;
            }

            double choice = rng.nextDouble();

            if (choice <= STUDENT_PROB_EAT && gap >= FIVE_MINS) {
                // choice 1: get something to eat/drink

                // getting lunch takes between 5 and 15 mins
                double duration = durationBetween(rng, FIVE_MINS, QUARTER_HOUR, gap);

                RoomBase.RoomType foodRoom = RoomBase.GetRandomLunchOption();

                slots.add(++i, new ScheduleSlot(start, duration, foodRoom));

            } else if (choice <= STUDENT_PROB_EAT + STUDENT_PROB_CHAIR && gap >= QUARTER_HOUR) {
                // choice 2: go to a chair for a seminar or study

                // between 15 mins and 2 hours
                double duration = durationBetween(rng, QUARTER_HOUR, 2, gap);

                RoomBase.RoomType seminarRoom = RoomBase.GetRandomWing();

                slots.add(++i, new ScheduleSlot(start, duration, seminarRoom));

            } else {
                // choice 3: hang out somewhere

                // between 10 mins and 1 hour
                double duration = durationBetween(rng, 10, 1, gap);

                RoomBase.RoomType room = RoomBase.GetRandomGatheringRoom();

                slots.add(++i, new ScheduleSlot(start, duration, room));
            }
        }

        System.out.println("Student Schedule "+seed+": "+slots);

        return new Schedule(slots);
    }

    public static Schedule forProfessor(int seed) {
        Random rng = randomForSeed(seed);

        ArrayList<ScheduleSlot> slots = new ArrayList<>();

        int nrLectures = rng.nextInt(NR_LECTURES_MAX - NR_LECTURES_MIN + 1) + NR_LECTURES_MIN;

        ArrayList<Lecture> chooseFromLectures = new ArrayList<>(lectures);

        // first pick all lectures
        pickNextLecture: while (slots.size() < nrLectures && chooseFromLectures.size() > 0) {

            int index = rng.nextInt(chooseFromLectures.size());
            Lecture lecture = chooseFromLectures.get(index);

            if (lecture.professor != -1) {
                chooseFromLectures.remove(index);
                continue;
            }

            for (int j = 0; j < slots.size(); j++) {
                ScheduleSlot slot = slots.get(j);
                // skip if overlaps with other lecture
                if (lecture.time + lecture.duration >= slot.time && lecture.time <= slot.time + slot.duration) {
                    chooseFromLectures.remove(index);
                    continue pickNextLecture;
                }
            }

            // set the professor for this lecture
            lecture.professor = seed;

            // add some randomness - professors always come early and leave late

            // lets professors come around quarter past the hour - 10 mins
            double time = lecture.time + 0.25 - rng.nextDouble() * 0.1;

            // lets professors leave around quarter to the hour + 10 mins
            double duration = lecture.duration - 0.5 + rng.nextDouble() * 0.1;

            slots.add(new ScheduleSlot(time, duration, lecture.room));
        }

        slots.sort(Comparator.comparingDouble(o -> o.time));

        // the home chair of the professor
        RoomBase.RoomType chair = RoomBase.GetRandomWing();

        // second fill slots between lectures
        int i = 0;
        while (i < slots.size()-1) {

            ScheduleSlot l1 = slots.get(i);
            ScheduleSlot l2 = slots.get(i+1);

            double start = l1.time + l1.duration;
            double gap = l2.time - start;

            i++;

            if (gap == 0) {
                continue;
            }

            double choice = rng.nextDouble();

            if (choice <= PROF_PROB_EAT && gap >= FIVE_MINS) {
                // choice 1: get something to eat/drink

                // getting lunch takes between 5 and 15 mins
                double duration = durationBetween(rng, FIVE_MINS, QUARTER_HOUR, gap);

                RoomBase.RoomType foodRoom = RoomBase.GetRandomLunchOption();

                slots.add(i, new ScheduleSlot(start, duration, foodRoom));

            } else if (choice <= PROF_PROB_EAT + PROF_PROB_HANGOUT && gap >= FIVE_MINS) {
                // choice 2: hang out somewhere

                // between 5 and 30 mins
                double duration =durationBetween(rng,  FIVE_MINS, HALF_HOUR, gap);

                RoomBase.RoomType room = RoomBase.GetRandomGatheringRoom();

                slots.add(i, new ScheduleSlot(start, duration, room));

            } else {
                // choice 3: be in the chair

                // between 10 mins and 1 hour
                double duration = durationBetween(rng, 10, 1, gap);

                slots.add(i, new ScheduleSlot(start, duration, chair));
            }
        }

        System.out.println("Professor Schedule "+seed+": "+slots);

        return new Schedule(slots);
    }


    public static Schedule forBarista(int seed) {
        Random rng = randomForSeed(seed);

        ArrayList<ScheduleSlot> slots = new ArrayList<>();

        RoomBase.RoomType workplace = RoomBase.GetRandomLunchOption();

        // Somewhere between 8 and 14
        double shiftStart = START_OF_DAY + rng.nextDouble()*4;

        // Somewhere between 4 and 8 hours
        double shiftDuration = Math.min(4 + rng.nextDouble()*4, END_OF_DAY - shiftStart - QUARTER_HOUR);

        // Somewhere in the middle of the shift
        double breakStart = shiftStart + shiftDuration/2 + (rng.nextDouble()-0.5);

        // Somewhere between 20 and 40 mins
        double breakDuration = 0.2 + rng.nextDouble()*0.2;

        double breakEnd = breakStart + breakDuration;
        double shiftEnd = shiftStart + shiftDuration;

        slots.add(new ScheduleSlot(shiftStart, breakStart-shiftStart, workplace));

        slots.add(new ScheduleSlot(breakStart, breakDuration, RoomBase.GetRandomGatheringRoom()));

        slots.add(new ScheduleSlot(breakEnd, shiftEnd-breakEnd, workplace));

        System.out.println("Barista Schedule "+seed+": "+slots);

        return new Schedule(slots);
    }

    public static Schedule forVisitor(int seed) {
        Random rng = randomForSeed(seed);

        ArrayList<ScheduleSlot> slots = new ArrayList<>();

        // Somewhere between 8 and 18
        double visitStart = START_OF_DAY + rng.nextDouble()*10;

        // Somewhere between 2 and 6 hours
        double visitDuration = Math.min(2 + rng.nextDouble()*4, END_OF_DAY - visitStart - QUARTER_HOUR);

        double visitEnd = visitStart + visitDuration;

        double time = visitStart;

        while (time < visitEnd) {

            double gap = visitEnd - time;

            double choice = rng.nextDouble();

            if (choice <= VISITOR_PROB_EAT && gap >= FIVE_MINS) {
                // choice 1: get something to eat/drink

                // getting lunch takes between 5 and 15 mins
                double duration =durationBetween(rng,  FIVE_MINS , QUARTER_HOUR, gap);

                RoomBase.RoomType foodRoom = RoomBase.GetRandomLunchOption();

                slots.add(new ScheduleSlot(time, duration, foodRoom));
                time += duration;

            } else if (choice <= VISITOR_PROB_EAT + VISITOR_PROB_CHAIR && gap >= TEN_MINS) {
                // choice 2: visit a chair

                // between 10 and 30 mins
                double duration =durationBetween(rng,  TEN_MINS, HALF_HOUR, gap);

                RoomBase.RoomType chair = RoomBase.GetRandomWing();

                slots.add(new ScheduleSlot(time, duration, chair));
            } else {
                // choice 2: hang out somewhere

                // between 10 and 30 mins
                double duration = durationBetween(rng, 10,  HALF_HOUR, gap);

                RoomBase.RoomType room = RoomBase.GetRandomGatheringRoom();

                slots.add(new ScheduleSlot(time, duration, room));
                time += duration;

            }

        }

        System.out.println("Visitor Schedule "+seed+": "+slots);

        return new Schedule(slots);
    }

    public static Schedule forCleaner(int seed) {
        Random rng = randomForSeed(seed);

        ArrayList<ScheduleSlot> slots = new ArrayList<>();

        // early shift: between 8 and 10
        // late shift: between 18 and 20;
        boolean earlyShift = rng.nextInt(100) % 2 == 0;

        double shiftStart = earlyShift ? START_OF_DAY + rng.nextDouble()*QUARTER_HOUR : END_OF_DAY - 2 - rng.nextDouble()*QUARTER_HOUR;

        double shiftEnd = earlyShift ? START_OF_DAY + 2 + rng.nextDouble()*QUARTER_HOUR : END_OF_DAY - FIVE_MINS - rng.nextDouble()*QUARTER_HOUR;

        double time = shiftStart;
        RoomBase room = RoomBase.AllRooms.get(RoomBase.RoomType.Magistrale);

        // go through neighboring rooms randomly
        while (time < shiftEnd) {
            double gap = shiftEnd - time;

            double duration = durationBetween(rng, FIVE_MINS, HALF_HOUR, gap);

            slots.add(new ScheduleSlot(time, duration, room.GetRoomType()));

            time += duration;
            room = room.GetRandomNeighboringRoom();
        }

        System.out.println("CLEANER Schedule "+seed+": "+slots);

        return new Schedule(slots);
    }

    private static double durationBetween(Random rng, double low, double high, double max) {
        double duration = low + rng.nextDouble() * high;
        return Math.min(duration, max);
    }

    private static Random randomForSeed(int seed) {
        Random rng = new Random(seed * 12345);

        // weird fix since the first value appears to be less random
        rng.nextDouble();

        return rng;
    }


    public boolean isFinishedAtTime(double currentTime) {
        if(slots.isEmpty()) {
            return false;
        }
        ScheduleSlot s = slots.get(slots.size() - 1);
        return START_OF_DAY + (currentTime / 60 / 60) > s.time + s.duration;
    }

    public RoomBase getNextRoom(double currentTime) {

        if (slots.isEmpty()) {
            return getExitRoom();
        }


        double currentTimeAsHourOfDay = START_OF_DAY + (currentTime / 60 / 60);

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
        double currentTimeAsHourOfDay = START_OF_DAY + (currentTime / 60 / 60);

        for (ScheduleSlot slot : slots) {
            if (currentTimeAsHourOfDay < slot.time) {
                return (slot.time - START_OF_DAY) * 3600;
            } else if (currentTimeAsHourOfDay < slot.time + slot.duration) {
                return (slot.time + slot.duration - START_OF_DAY) * 3600;
            }
        }

        return (END_OF_DAY - START_OF_DAY) * 3600;
    }

    @Override
    public String toString() {
        return "{" +
                "slots=" + slots +
                ", exitRoom=" + exitRoom +
                '}';
    }
}
