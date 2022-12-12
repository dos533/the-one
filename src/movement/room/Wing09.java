package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing09 extends RoomBase{
    public Wing09() {
        super(RoomBase.RoomType.Wing09, new Timeframe(30, 300), false);
        roomDoors.put(RoomBase.RoomType.Magistrale, new Coord(275, 150));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(260, 150),
                new Coord(260, 50),
                new Coord(290, 50),
                new Coord(290, 150),
                new Coord(260, 150)
        );
    }
}
