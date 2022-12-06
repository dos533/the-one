package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Cafe extends RoomBase{

    public Cafe() {
        super(RoomType.Cafe, new Timeframe(300, 1000), true);
        roomDoors.put(RoomType.Magistrale, new Coord(90, 100));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(80, 100),
                new Coord( 80, 150),
                new Coord(100, 150),
                new Coord(100, 100),
                new Coord(80, 100)
        );
    }
}
