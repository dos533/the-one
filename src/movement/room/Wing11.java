package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing11 extends RoomBase{
    public Wing11() {
        super(RoomType.Wing11, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(195, 150));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(180, 150),
                new Coord(180, 50),
                new Coord(210, 50),
                new Coord(210, 150),
                new Coord(180, 150)
        );
    }
}
