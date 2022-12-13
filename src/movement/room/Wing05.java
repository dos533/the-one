package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing05 extends RoomBase{
    public Wing05() {
        super(RoomType.Wing05, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(435, 150));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(420, 150),
                new Coord(420, 50),
                new Coord(450, 50),
                new Coord(450, 150),
                new Coord(420, 150)
        );
    }
}
