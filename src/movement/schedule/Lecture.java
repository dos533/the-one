package movement.schedule;

import movement.room.RoomBase;

class Lecture {

    public double time;
    public double duration;
    public RoomBase.RoomType room;
    public int professor;

    public Lecture(double time, double duration, RoomBase.RoomType room) {
        this.time = time;
        this.duration = duration;
        this.room = room;
        this.professor = -1;
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

