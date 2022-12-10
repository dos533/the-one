package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Lecture1 extends RoomBase{

    public Lecture1() {
        super(RoomType.LectureHall01, new Timeframe(300, 1000), true);
        roomDoors.put(RoomType.Magistrale, new Coord(30, 100));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(10, 100),
                new Coord( 10, 150),
                new Coord(50, 150),
                new Coord(50, 100),
                new Coord(10, 100)
        );
    }
}
