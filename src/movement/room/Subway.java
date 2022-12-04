package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Subway extends RoomBase{

    public Subway() {
        super(RoomType.Subway, new Timeframe(300, 1000));
        roomDoors.put(RoomType.Outside, new Coord(180, 100));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(175, 100),
                new Coord( 175, 130),
                new Coord(200, 130),
                new Coord(200, 100),
                new Coord(175, 100)
        );
    }
}
