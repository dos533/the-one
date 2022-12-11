package movement.room;

import core.Coord;
import util.PolygonUtils;
import util.Timeframe;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RoomBase {

    public static Map<RoomType, RoomBase> AllRooms = Stream.of(new Object[][]{
            {RoomType.Subway, new Subway()},
            {RoomType.Outside, new Outside()},
            {RoomType.Magistrale, new Magistrale()},
            {RoomType.LectureHall01, new Lecture1()},
            {RoomType.Cafe, new Cafe()},
    }).collect(Collectors.toMap(data -> (RoomType) data[0], data -> (RoomBase) data[1]));

    private static Random rand = new Random();

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

    public Set<RoomType> GetNeighbors() {
        return roomDoors.keySet();
    }

    public RoomBase GetRandomNeighboringRoom() {
        //this looks like hacky java... but im pretty sure its correct
        RoomType[] neighbors = roomDoors.keySet().toArray(new RoomType[0]);

        //create array containing self
        RoomType[] selfIncludedNeighbors = new RoomType[neighbors.length + 1];
        for(int i = 0; i < neighbors.length; i++) {
            selfIncludedNeighbors[i] = neighbors[i];
        }
        selfIncludedNeighbors[neighbors.length] = _type;

        RoomType randomType = selfIncludedNeighbors[rand.nextInt(selfIncludedNeighbors.length)];

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
        Lunch,
        VendingMachine,
        LectureHall01,
        LectureHall02,
        LectureHall03,
        Outside,
        Subway,
        CarPark,
    }

    public static List<RoomType> LunchOptions = Arrays.asList(RoomType.Cafe, RoomType.Lunch, RoomType.VendingMachine);
    public static List<RoomType> LectureRooms = Arrays.asList(RoomType.LectureHall01, RoomType.LectureHall02, RoomType.LectureHall03);
    public static List<RoomType> GatheringRooms = Arrays.asList(RoomType.Magistrale);
    public static List<RoomType> EntranceAndExitOptions = Arrays.asList(RoomType.Subway, RoomType.CarPark);

    public static String getRoomCategory(RoomType roomType){
        if (LunchOptions.contains(roomType)) return "LunchOptions";
        if (LectureRooms.contains(roomType)) return "LectureRooms";
        if (GatheringRooms.contains(roomType)) return "GatheringRooms";
        if (EntranceAndExitOptions.contains(roomType)) return "EntranceAndExitOptions";
        return null;
    }


    public static RoomType GetRandomLunchOption() {
        return LunchOptions.get(rand.nextInt(LunchOptions.size()));
    }

    public static RoomType GetRandomLectureRoom() {
        return LectureRooms.get(rand.nextInt(LectureRooms.size()));
    }

    public static RoomType GetRandomGatheringRoom() {
        return RoomType.Magistrale;
    }

    public static RoomType GetRandomEntranceAndExitOption() {
        return EntranceAndExitOptions.get(rand.nextInt(EntranceAndExitOptions.size()));
    }

}

