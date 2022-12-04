package movement.room;

import core.Coord;
import util.PolygonUtils;
import util.Timeframe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RoomBase {

    public static Map<RoomType, RoomBase> AllRooms = Stream.of(new Object[][]{
            {RoomType.Subway, new Subway()},
            {RoomType.Outside, new Outside()},
            {RoomType.Magistrale, new Magistrale()}
    }).collect(Collectors.toMap(data -> (RoomType) data[0], data -> (RoomBase) data[1]));

    private RoomType _type = RoomType.None;
    private boolean _doRandomWalk;
    protected Map<RoomType, Coord> roomDoors;
    private Timeframe _timeframe;

    public RoomBase(RoomType type)  {
        this(type, new Timeframe(), false);

    }
    public RoomBase(RoomType type, Timeframe timeframe) {
        this(type, timeframe, false);
    }
    public RoomBase(RoomType type, boolean doRandomWalk) {
        this(type, new Timeframe(), doRandomWalk);
    }
    public RoomBase(RoomType type, Timeframe timeframe, boolean doRandomWalk) {
        _type = type;
        roomDoors = new HashMap<>();
        _timeframe = timeframe;
        _doRandomWalk = doRandomWalk;
    }

    public RoomType GetRoomType() {
        return _type;
    }

    public boolean GetDoRandomWalk() {
        return _doRandomWalk;
    }

    public Timeframe GetTimeInRoom() {
        return _timeframe;
    }

    public abstract List<Coord> GetPolygon();

    public Coord GetDoorToRoom(RoomType room) {
        return roomDoors.get(room);
    }

    public RoomBase GetRandomNeighboringRoom() {
        Random rand = new Random();
        //this looks like hacky java... but im pretty sure its correct
        RoomType[] neighbors = roomDoors.keySet().toArray(new RoomType[0]);
        StringBuilder sb = new StringBuilder();
        for(RoomType t : neighbors){
            sb.append(t);
        }
        System.out.println(sb.toString());

        RoomType randomType = neighbors[rand.nextInt(0, roomDoors.size())];
        return RoomBase.AllRooms.get(randomType);
    }

    public boolean IsInside(Coord c) {
        return PolygonUtils.IsInside(GetPolygon(), c);
    }

    public Coord GetCenter() {
        double x = 0;
        double y = 0;
        List<Coord> polygon = GetPolygon();
        for (Coord coord : polygon) {
            x += coord.getX();
            y += coord.getY();
        }

        return new Coord(x / polygon.size(), y / polygon.size());
    }


    public enum RoomType {
        None,
        Magistrale,
        Wing02,
        Wing04,
        Wing05,
        Wing06,
        Wing07,
        Wing08,
        Wing09,
        Wing10,
        Wing11,
        Wing13,
        Library,
        Cafe,
        LectureHall01,
        LectureHall02,
        LectureHall03,
        Outside,
        Subway
    }

}


