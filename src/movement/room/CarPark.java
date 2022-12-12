package movement.room;

import core.Coord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarPark extends RoomBase{
    public CarPark() {
        super(RoomType.CarPark, false);
        roomDoors.put(RoomType.Magistrale, new Coord(100, 175));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(100, 165),
                new Coord(100, 185),
                new Coord(50, 185),
                new Coord(50, 165),
                new Coord(100, 165)
        );
    }
}
