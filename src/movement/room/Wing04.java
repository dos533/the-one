package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing04 extends RoomBase{

    public Wing04() {
        super(RoomType.Wing04, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(503, 235));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(506, 220),
                new Coord(536, 220),
                new Coord(536, 350),
                new Coord(500, 350),
                new Coord(500, 250),
                new Coord(506, 220)
        );
    }
}
