package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing12 extends RoomBase{
    public Wing12() {
        super(RoomType.Wing12, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(390, 203.125));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(150, 206.25),
                new Coord(150, 256.25),
                new Coord(100, 256.25),
                new Coord(100, 200),
                new Coord(150, 206.25)
        );
    }
}
