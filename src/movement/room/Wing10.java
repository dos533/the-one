package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Wing10 extends RoomBase{
    public Wing10() {
        super(RoomType.Wing10, new Timeframe(30, 300), false);
        roomDoors.put(RoomType.Magistrale, new Coord(295, 224.375));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(310, 226.25),
                new Coord(280, 326.25),
                new Coord(280, 326.25),
                new Coord(310, 222.5),
                new Coord(310, 226.25)
        );
    }
}
