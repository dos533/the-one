package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Cafe extends RoomBase{

    public Cafe() {
        super(RoomType.Cafe, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(390, 230));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(390, 236.25),
                new Coord(410, 238.75),
                new Coord(410, 220),
                new Coord(390, 220),
                new Coord(390, 236.25)
        );
    }
}
