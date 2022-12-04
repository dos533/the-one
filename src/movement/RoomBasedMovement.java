package movement;

import core.Coord;
import core.Settings;
import movement.room.RoomBase;
import util.PolygonUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class RoomBasedMovement extends MovementModel implements SwitchableMovement{

    private Coord _lastWaypoint;

    private RoomBase _currentRoom;
    private RoomBase _nextRoom;

    private boolean _isEnabled;

    //TODO add schedule
    //private Schedule _schedule

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

        RoomBase previousRoom = null;
        if(_currentRoom != null) {
            previousRoom = _currentRoom;
        }
        _currentRoom = _nextRoom;

        //TODO get next room based on schedule at some point
        //for now just select a random neighboring room
        RoomBase nextRoom = _currentRoom.GetRandomNeighboringRoom();

        System.out.println("next room: " + nextRoom.GetRoomType());

        //Path p = new Path(generateSpeed());
        Path p = new Path(0.1);
        p.addWaypoint(_lastWaypoint.clone());

        if(previousRoom != null && nextRoom.GetRoomType() != previousRoom.GetRoomType()) {
            p.addWaypoint(nextRoom.GetDoorToRoom(_currentRoom.GetRoomType()));
        }

        if(!nextRoom.GetDoRandomWalk() && PolygonUtils.IsInside(nextRoom.GetPolygon(), this._lastWaypoint)) {
            return null;
        }

        Coord c = PolygonUtils.RandomPointInside(nextRoom.GetPolygon());
        p.addWaypoint(c);
        _lastWaypoint = c;
        System.out.println("Moving to " + nextRoom.GetRoomType());
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
            _nextRoom = RoomBase.AllRooms.get(RoomBase.RoomType.Outside);
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
