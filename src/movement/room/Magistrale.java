package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Magistrale extends RoomBase{


    public Magistrale() {
        super(RoomType.Magistrale, new Timeframe(300, 1000), true);
        roomDoors.put(RoomType.Outside, new Coord(100, 50));
        roomDoors.put(RoomType.LectureHall01, new Coord(30, 100));
        roomDoors.put(RoomType.Cafe, new Coord(90, 100));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(10, 10),
                new Coord( 10, 100),
                new Coord(100, 100),
                new Coord(100, 10),
                new Coord(10, 10)
        );
    }
}
