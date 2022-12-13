package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Lecture2 extends RoomBase {


    public Lecture2() {
        super(RoomType.LectureHall02, new Timeframe(300, 1000), false);
        roomDoors.put(RoomType.Magistrale, new Coord(485, 248.125));
    }

    @Override
    public List<Coord> GetPolygon() {

        return Arrays.asList(
                new Coord(470, 246.25),
                new Coord(500, 250),
                new Coord(500, 280),
                new Coord(470, 280),
                new Coord(470, 246.25)
        );
    }
}
