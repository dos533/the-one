package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing08 extends RoomBase{
    public Wing08() {
        super(RoomType.Wing08, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(375, 234.375));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(390, 236.25),
                new Coord(390, 336.25),
                new Coord(360, 336.25),
                new Coord(360, 232.5),
                new Coord(390, 236.25)
        );
    }
}
