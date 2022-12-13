package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing13 extends RoomBase{
    public Wing13() {
        super(RoomType.Wing13, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(115, 150));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(100, 150),
                new Coord(100, 50),
                new Coord(130, 50),
                new Coord(130, 150),
                new Coord(100, 150)
        );
    }
}
