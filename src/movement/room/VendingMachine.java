package movement.room;

import core.Coord;

import java.util.Arrays;
import java.util.List;

public class VendingMachine extends RoomBase{
    public VendingMachine() {
        super(RoomType.VendingMachine, false);
        roomDoors.put(RoomType.Magistrale, new Coord(380, 265));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(390, 260),
                new Coord(390, 270),
                new Coord(380, 270),
                new Coord(380, 260),
                new Coord(390, 260)
        );
    }
}
