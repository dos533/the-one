package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing06 extends RoomBase{
    public Wing06() {
        super(RoomType.Wing06, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(455, 244.375));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(470, 246.25),
                new Coord(470, 346.25),
                new Coord(440, 346.25),
                new Coord(440, 242.5),
                new Coord(470, 246.25)
        );
    }
}
