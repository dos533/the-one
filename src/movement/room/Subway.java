package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Subway extends RoomBase{

    public Subway() {
        super(RoomType.Subway, new Timeframe(300, 1000), false);
        roomDoors.put(RoomType.Outside, new Coord(485, 100));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(470, 100),
                new Coord(470, 80),
                new Coord(500, 80),
                new Coord(500, 100),
                new Coord(470, 100)
        );
    }
}
