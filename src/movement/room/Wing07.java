package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing07 extends RoomBase{
    public Wing07() {
        super(RoomType.Wing07, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(390, 150));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(340, 150),
                new Coord(340, 50),
                new Coord(370, 50),
                new Coord(370, 150),
                new Coord(340, 150)
        );
    }
}
