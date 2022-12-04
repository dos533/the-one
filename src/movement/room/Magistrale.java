package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Magistrale extends RoomBase{


    public Magistrale() {
        super(RoomType.Magistrale, new Timeframe(300, 1000));
        roomDoors.put(RoomType.Outside, new Coord(100, 50));
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
