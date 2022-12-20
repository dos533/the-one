package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Library extends RoomBase{

    public Library() {
        super(RoomType.Library, new Timeframe(300, 1000), false);
        roomDoors.put(RoomType.Magistrale, new Coord(150, 206.25));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(150,  206.25),
                new Coord(150, 256.25),
                new Coord( 100, 256.25),
                new Coord( 100, 200),
                new Coord(150, 206.25)
        );
    }
}
