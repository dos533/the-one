package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Lecture1 extends RoomBase{

    public Lecture1() {
        super(RoomType.LectureHall01, new Timeframe(300, 1000), false);
        roomDoors.put(RoomType.Magistrale, new Coord(515, 175));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(520, 150),
                new Coord(580, 150),
                new Coord(570, 200),
                new Coord( 510, 200),
                new Coord(520, 150)
        );
    }
}

