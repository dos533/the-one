package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Lecture3 extends RoomBase{
    public Lecture3() {
        super(RoomType.LectureHall03, new Timeframe(300, 1000), false);
        roomDoors.put(RoomType.Magistrale, new Coord(425, 240.625));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(440, 242.5),
                new Coord(440, 272.5),
                new Coord(410, 272.5),
                new Coord(410, 238.75),
                new Coord(440, 242.5)
        );
    }
}
