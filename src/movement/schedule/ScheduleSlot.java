package movement.schedule;

import movement.room.RoomBase;


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
