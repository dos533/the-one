package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Lunch extends RoomBase{
    public Lunch() {
        super(RoomType.Lunch, new Timeframe(30, 300), true);
        roomDoors.put(RoomType.Magistrale, new Coord(350, 234.25));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(360, 236.25),
                new Coord(360, 266.25),
                new Coord(310, 260),
                new Coord(310, 226.25),
                new Coord(360, 236.25)
        );
    }
}
