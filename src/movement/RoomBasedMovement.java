package movement;

import core.Coord;
import core.Settings;
import core.SimClock;
import movement.room.RoomBase;
import util.PolygonUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class RoomBasedMovement extends MovementModel implements SwitchableMovement{

    private Coord _lastWaypoint;

    private RoomBase _currentRoom;
    private RoomBase _nextRoom;

    private boolean _isEnabled;

    private double _nextMoveTime = 0;

    //TODO add schedule
    //private Schedule _schedule


    public RoomBase.RoomType GetCurrentRoom() {
        return _currentRoom.GetRoomType();
    }

    private boolean _scheduleFinished = false;

    public RoomBasedMovement(Settings settings) {
        super(settings);
        this._currentRoom = RoomBase.AllRooms.get(RoomBase.RoomType.Subway);
        this._isEnabled = true;
    }

    public RoomBasedMovement(RoomBasedMovement prototype) {
        super(prototype);
        this._currentRoom = prototype._currentRoom;
        this._isEnabled = true;
    }

    @Override
    public boolean isActive() {
        return _isEnabled;
    }

    @Override
    public Path getPath() {
        if(_currentRoom.GetRoomType() == RoomBase.RoomType.Subway && _scheduleFinished) {
            this._isEnabled = false;
        }

        final double currentTime = SimClock.getTime();

        //dont move if we are waiting in the current room
        if(_nextMoveTime > currentTime) {
            return null;
        }

        RoomBase previousRoom = _currentRoom;
        _currentRoom = _nextRoom;

        //TODO get next room based on schedule at some point
        //for now just select a random neighboring room
        RoomBase nextRoom = _currentRoom.GetRandomNeighboringRoom();

        Path p = new Path(generateSpeed());
        p.addWaypoint(_lastWaypoint.clone());

        System.out.println(_currentRoom.GetRoomType() + " -> " + nextRoom.GetRoomType() );

        if(nextRoom.GetRoomType() != _currentRoom.GetRoomType()) {
            Coord doorCoord = _currentRoom.GetDoorToRoom(nextRoom.GetRoomType());
            p.addWaypoint(doorCoord.clone());
        }

        //TODO do this via schedule instead
        _nextMoveTime = currentTime + nextRoom.GetTimeInRoom().Random();

        if(!nextRoom.GetDoRandomWalk() && nextRoom.GetRoomType() == _currentRoom.GetRoomType()) {
            _nextRoom = nextRoom;
            return null;
        }
        Coord c = PolygonUtils.RandomPointInside(nextRoom.GetPolygon());
        p.addWaypoint(c);
        _lastWaypoint = c;
        _nextRoom = nextRoom;

        return p;
    }

    @Override
    public Coord getInitialLocation() {
        if (_lastWaypoint == null) {
            //start at subway exit
            System.out.println("spawning at subway");
            _lastWaypoint = PolygonUtils.RandomPointInside(RoomBase.AllRooms.get(RoomBase.RoomType.Subway).GetPolygon());
            _currentRoom = RoomBase.AllRooms.get(RoomBase.RoomType.Subway);
            _nextRoom = RoomBase.AllRooms.get(RoomBase.RoomType.Subway);
        }

        return _lastWaypoint.clone();
    }

    @Override
    public MovementModel replicate() {
        return new RoomBasedMovement(this);
    }

    @Override
    public void setLocation(Coord lastWaypoint) {
        //why can I not break a foreach?
        AtomicBoolean roomFound = new AtomicBoolean(false);
        RoomBase.AllRooms.forEach((roomType, roomBase) -> {
            if(roomBase.IsInside(lastWaypoint)) {
                _lastWaypoint = lastWaypoint;
                _currentRoom = roomBase;
                roomFound.set(true);
            }
        });

        if(!roomFound.get()) {
            //Could not find a room.... what to do now?
        }
    }

    @Override
    public Coord getLastLocation() {
        if (_lastWaypoint != null) {
            return _lastWaypoint.clone();
        } else {
            return null;
        }
    }

    @Override
    public boolean isReady() {
        return false;
    }
}
