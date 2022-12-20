package movement;

import core.Coord;
import core.DTNHost;
import core.Settings;
import core.SimClock;
import movement.room.RoomBase;
import movement.schedule.Schedule;
import util.PolygonUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class RoomBasedMovement extends MovementModel implements SwitchableMovement{

    private Coord _lastWaypoint;

    private RoomBase _currentRoom;
    private RoomBase _nextRoom;

    private boolean _isEnabled;

    private double _nextMoveTime = 0;

    private Schedule _schedule;

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
    public void setHost(DTNHost host) {
        super.setHost(host);
        this.generateSchedule(); // Can only initialize after the host has been set
    }

    public RoomBase.RoomType getRoomType(){
        return _currentRoom.GetRoomType();
    }

    private void generateSchedule() {
        _schedule = Schedule.generateForHost(host);
    }

    @Override
    public Path getPath() {
        final double currentTime = SimClock.getTime();

        if(_schedule.isFinishedAtTime(currentTime) && RoomBase.EntranceAndExitOptions.contains( _nextRoom.GetRoomType())) {
            System.out.println("is called");
            _currentRoom = _nextRoom;
            this._isEnabled = false;
        }

        //dont move if we are waiting in the current room
        if(_nextMoveTime > currentTime) {
            return null;
        }

        _currentRoom = _nextRoom;

        RoomBase nextRoom = _schedule.getNextRoom(currentTime);

        if(nextRoom == null) {
            return null;
        }

        Path p = new Path(generateSpeed());
        p.addWaypoint(_lastWaypoint.clone());
        if(_currentRoom.GetRoomType() != nextRoom.GetRoomType())
            System.out.println(_currentRoom.GetRoomType() + " -> " + nextRoom.GetRoomType() );

        if(nextRoom.GetRoomType() != _currentRoom.GetRoomType()) {
            for (Coord c: GetPathToRoom(_currentRoom.GetRoomType(), nextRoom.GetRoomType())) {
                p.addWaypoint(c.clone());
                _lastWaypoint = c.clone();
            }
        }

        _nextMoveTime = _schedule.getNextSlotTime(currentTime);

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


    private Coord[] GetPathToRoom(RoomBase.RoomType current, RoomBase.RoomType target) {

        RoomBase.RoomType pred[] = new RoomBase.RoomType[RoomBase.RoomType.values().length];
        int dist[] = new int[RoomBase.RoomType.values().length];

        if(!BFS(current, target, pred, dist)) {
            System.out.println("Rooms " + current + " and " + target + " are not connected!");
        }

        LinkedList<RoomBase.RoomType> path = new LinkedList<RoomBase.RoomType>();
        RoomBase.RoomType crawl = target;
        path.add(crawl);

        while(pred[crawl.ordinal()] != null) {
            path.add(pred[crawl.ordinal()]);
            crawl = pred[crawl.ordinal()];
        }

        Coord doors[] = new Coord[path.size() - 1];
        RoomBase.RoomType pathArray[] = path.toArray(new RoomBase.RoomType[0]);
        for(int i = 0; i < path.size() - 1; i++ ){
            doors[i] = RoomBase.AllRooms.get(pathArray[i]).GetDoorToRoom(pathArray[i + 1]);
        }

        //reverse the order to get path from start to finish
        Collections.reverse(Arrays.asList(doors));

        return doors;

    }

    private static boolean BFS(RoomBase.RoomType current, RoomBase.RoomType dest, RoomBase.RoomType[] pred, int[] dist) {
        LinkedList<RoomBase.RoomType> queue = new LinkedList<RoomBase.RoomType>();

        boolean visited[] = new boolean[RoomBase.RoomType.values().length];

        for(int i = 0; i < RoomBase.RoomType.values().length; i++) {
            visited[i] = false;
            dist[i] = Integer.MAX_VALUE;
            pred[i] = null;
        }

        visited[current.ordinal()] = true;
        dist[current.ordinal()] = 0;
        queue.add(current);


        //nested mess... but its just BFS so im not going to denest it
        while(!queue.isEmpty()) {
            RoomBase room = RoomBase.AllRooms.get(queue.remove());
            RoomBase.RoomType neighbors[] = room.GetNeighbors().toArray(new RoomBase.RoomType[0]);
            for( int i = 0; i < neighbors.length; i++) {
                if(!visited[neighbors[i].ordinal()]) {
                    visited[neighbors[i].ordinal()] = true;
                    dist[neighbors[i].ordinal()] = dist[room.GetRoomType().ordinal()] + 1;
                    pred[neighbors[i].ordinal()] = room.GetRoomType();
                    queue.add(neighbors[i]);

                    //room found
                    if(neighbors[i] == dest) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public Coord getInitialLocation() {
        if (_lastWaypoint == null) {
            //start at subway exit
            _currentRoom = _schedule.getNextRoom(0);
            System.out.println("Spawning at "+_currentRoom.getClass().getSimpleName());
            _nextRoom = _currentRoom;
            _lastWaypoint = PolygonUtils.RandomPointInside(_currentRoom.GetPolygon());
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
