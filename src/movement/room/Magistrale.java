package movement.room;

import core.Coord;
import util.Timeframe;

import java.util.Arrays;
import java.util.List;

public class Magistrale extends RoomBase{


    public Magistrale() {
        super(RoomType.Magistrale, new Timeframe(300, 1000), true);
        roomDoors.put(RoomType.Outside, new Coord(485, 150));
        roomDoors.put(RoomType.CarPark, new Coord(100, 175));
        roomDoors.put(RoomType.LectureHall01, new Coord(515, 175));
        roomDoors.put(RoomType.LectureHall02, new Coord(485, 248.125));
        roomDoors.put(RoomType.LectureHall03, new Coord(425, 240.625));
        roomDoors.put(RoomType.Lunch, new Coord(350, 234.25));
        roomDoors.put(RoomType.Cafe, new Coord(390, 230));
        roomDoors.put(RoomType.Wing04, new Coord(503, 235));
        roomDoors.put(RoomType.Wing05, new Coord(435, 150));
        roomDoors.put(RoomType.Wing06, new Coord(455, 244.375));
        roomDoors.put(RoomType.Wing07, new Coord(390, 150));
        roomDoors.put(RoomType.Wing08, new Coord(375, 234.375));
        roomDoors.put(RoomType.Wing09, new Coord(275, 150));
        roomDoors.put(RoomType.Wing10, new Coord(295, 224.375));
        roomDoors.put(RoomType.Wing11, new Coord(195, 150));
        roomDoors.put(RoomType.Wing12, new Coord(390, 203.125));
        roomDoors.put(RoomType.Wing13, new Coord(115, 150));
    }

    @Override
    public List<Coord> GetPolygon() {
        return Arrays.asList(
                new Coord(100, 150),
                /*
                new Coord(180, 150),
                new Coord(260, 150),
                new Coord(340, 150),
                new Coord(420, 150),
                new Coord(500, 150),
                */

                new Coord(520, 150),
                new Coord(500, 250),
                /*
                new Coord(470, 246.25),
                new Coord(390, 236.25),
                new Coord(310, 226.25),
                new Coord(230, 216.25),
                new Coord(150, 206.25),
                 */
                new Coord(100, 200),
                // new Coord(100, 165),
                new Coord(100, 150)
        );

    }
}
