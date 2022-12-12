package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Outside extends RoomBase{

    public Outside() {
        super(RoomType.Outside, new Timeframe(30, 100), false);
        roomDoors.put(RoomType.Magistrale, new Coord(485, 400));
        roomDoors.put(RoomType.Subway, new Coord(485, 100));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(470, 150),
                new Coord(470, 100),
                new Coord(500, 100),
                new Coord(500, 150),
                new Coord(470, 150)
        );
    }
}
