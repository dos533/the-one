package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Outside extends RoomBase{

    public Outside() {
        super(RoomType.Outside, new Timeframe(30, 100), false);
        roomDoors.put(RoomType.Magistrale, new Coord(100, 50));
        roomDoors.put(RoomType.Subway, new Coord(180, 100));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(100, 10),
                new Coord( 100, 100),
                new Coord(200, 100),
                new Coord(200, 10),
                new Coord(100, 10)
        );
    }
}
