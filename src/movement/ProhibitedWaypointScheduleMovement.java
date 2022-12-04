package movement;

import core.Coord;
import core.Debug;
import core.Settings;
import core.SimError;
import input.WKTMapReader;
import movement.map.MapNode;
import movement.map.SimMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Random Waypoint Movement with a prohibited region where nodes may not move
 * into. The polygon is defined by a *closed* (same point as first and
 * last) path, represented as a list of {@code Coord}s.
 *
 * @author teemuk
 */
public class ProhibitedWaypointScheduleMovement
        extends MovementModel implements SwitchableMovement{

    //==========================================================================//
    // Settings
    //==========================================================================//
    /** {@code true} to confine nodes inside the polygon */
    public static final String INVERT_SETTING = "rwpInvert";
    public static final boolean INVERT_DEFAULT = false;

    /** map cache -- in case last mm read the same map, use it without loading*/
    private static SimMap cachedMap = null;
    /** names of the previously cached map's files (for hit comparison) */
    private static List<String> cachedMapFiles = null;

    public static final String PPWSM_MOVEMENT_NS = "ProhibitedWaypointScheduleMovement";
    /** number of map files -setting id ({@value})*/
    public static final String NROF_FILES_S = "nrofMapFiles";
    /** map file -setting id ({@value})*/
    public static final String FILE_S = "mapFile";
    //==========================================================================//


    //==========================================================================//
    // Instance vars
    //==========================================================================//

    private List<Coord> polygon = null;

    /** sim map for the model */
    private SimMap map = null;

    /** how many map files are read */
    private int nrofMapFilesRead = 0;

    private Coord lastWaypoint;
    /** Inverted, i.e., only allow nodes to move inside the polygon. */
    private final boolean invert;

    protected int maxPathLength = 100;
    /**  min nrof map nodes to travel/path */
    protected int minPathLength = 10;

    private double maxX = 0;
    private double maxY = 0;


    //==========================================================================//



    //==========================================================================//
    // Implementation
    //==========================================================================//
    @Override
    public Path getPath() {
        // Creates a new path from the previous waypoint to a new one.
        final Path p;
        p = new Path( super.generateSpeed() / 10);
        p.addWaypoint( this.lastWaypoint.clone() );



        // whoever wrote this and actually considered calling it prohibited polygon movement needs to have any right to write code revoked
        // this is utter and absolute garbage. it just makes sure you dont walk through a prohibited area, not providing a path towards any point in the polygon
        /*
        Coord c;
        do {
            c = this.randomCoord();
            System.out.println(c);
        } while ( pathIntersects( this.polygon, this.lastWaypoint, c ) );
        p.addWaypoint( c );
*/
        Coord c = randomCoord();
        while( this.invert? isInside(polygon, c) : isOutside(polygon, c)) {
            c = randomCoord();
        }
        p.addWaypoint(c);
        return p;
    }

    @Override
    public Coord getInitialLocation() {

        this.lastWaypoint = new Coord(150, 74);
        System.out.println(isInside(this.polygon, this.lastWaypoint));
        return this.lastWaypoint;
        /*
        do {
            this.lastWaypoint = this.randomCoord();
        } while ( ( this.invert ) ?
                isOutside( polygon, this.lastWaypoint ) :
                isInside( this.polygon, this.lastWaypoint ) );
        return this.lastWaypoint;
        */

    }

    @Override
    public MovementModel replicate() {
        return new ProhibitedWaypointScheduleMovement( this );
    }

    private Coord randomCoord() {
        return new Coord(
                rng.nextDouble() * this.maxX,
                rng.nextDouble() * this.maxY );
    }
    //==========================================================================//


    //==========================================================================//
    // API
    //==========================================================================//
    public ProhibitedWaypointScheduleMovement( final Settings settings ) {
        super( settings );
        // Read the invert setting
        this.invert = settings.getBoolean( INVERT_SETTING, INVERT_DEFAULT );

        this.map = readMap();
        this.polygon = map.getNodes().stream().map(mapNode -> mapNode.getLocation()).collect(Collectors.toList());

        setBounds();
    }


    /**
     * Creates a new ProhibitedWaypointScheduleMovement based on a Settings object's settings
     * but with different SimMap
     * @param settings The Settings object where the settings are read from
     * @param simMap The SimMap to use
     * @param nrOfMaps How many map "files" are in the map
     */
    public ProhibitedWaypointScheduleMovement( final Settings settings, SimMap simMap, int nrOfMaps ) {
        super( settings );
        this.map = simMap;

        // Read the invert setting
        this.invert = settings.getBoolean( INVERT_SETTING, INVERT_DEFAULT );
        this.nrofMapFilesRead = nrOfMaps;
        polygon = map.getNodes().stream().map(mapNode -> mapNode.getLocation()).collect(Collectors.toList());
        setBounds();
    }

    public ProhibitedWaypointScheduleMovement( final ProhibitedWaypointScheduleMovement other ) {
        // Copy constructor will be used when settings up nodes. Only one
        // prototype node instance in a group is created using the Settings
        // passing constructor, the rest are replicated from the prototype.
        super( other );
        // Remember to copy any state defined in this class.
        this.invert = other.invert;
        this.map = other.map;
        this.nrofMapFilesRead = other.nrofMapFilesRead;
        this.polygon = other.polygon;
        setBounds();
    }
    //==========================================================================//


    private void setBounds() {
        double x = 0;
        double y = 0;
        for (Coord coord : this.polygon) {
            if(coord.getX() > x)
                x = coord.getX();
            if(coord.getY() > y)
                y = coord.getY();
        }

        this.maxX = x;
        this.maxY = y;
    }


    //==========================================================================//
    // Private - geometry
    //==========================================================================//
    private static boolean pathIntersects(
            final List <Coord> polygon,
            final Coord start,
            final Coord end ) {
        final int count = countIntersectedEdges( polygon, start, end );
        return ( count > 0 );
    }

    private static boolean isInside(
            final List <Coord> polygon,
            final Coord point ) {
        final int count = countIntersectedEdges( polygon, point,
                new Coord( -10,0 ) );
        return ( ( count % 2 ) != 0 );
    }

    private static boolean isOutside(
            final List <Coord> polygon,
            final Coord point ) {
        return !isInside( polygon, point );
    }

    private static int countIntersectedEdges(
            final List <Coord> polygon,
            final Coord start,
            final Coord end ) {
        int count = 0;
        for ( int i = 0; i < polygon.size() - 1; i++ ) {
            final Coord polyP1 = polygon.get( i );
            final Coord polyP2 = polygon.get( i + 1 );

            final Coord intersection = intersection( start, end, polyP1, polyP2 );
            if ( intersection == null ) continue;

            if ( isOnSegment( polyP1, polyP2, intersection )
                    && isOnSegment( start, end, intersection ) ) {
                count++;
            }
        }
        return count;
    }

    private static boolean isOnSegment(
            final Coord L0,
            final Coord L1,
            final Coord point ) {
        final double crossProduct
                = ( point.getY() - L0.getY() ) * ( L1.getX() - L0.getX() )
                - ( point.getX() - L0.getX() ) * ( L1.getY() - L0.getY() );
        if ( Math.abs( crossProduct ) > 0.0000001 ) return false;

        final double dotProduct
                = ( point.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
                + ( point.getY() - L0.getY() ) * ( L1.getY() - L0.getY() );
        if ( dotProduct < 0 ) return false;

        final double squaredLength
                = ( L1.getX() - L0.getX() ) * ( L1.getX() - L0.getX() )
                + (L1.getY() - L0.getY() ) * (L1.getY() - L0.getY() );
        if ( dotProduct > squaredLength ) return false;

        return true;
    }

    private static Coord intersection(
            final Coord L0_p0,
            final Coord L0_p1,
            final Coord L1_p0,
            final Coord L1_p1 ) {
        final double[] p0 = getParams( L0_p0, L0_p1 );
        final double[] p1 = getParams( L1_p0, L1_p1 );
        final double D = p0[ 1 ] * p1[ 0 ] - p0[ 0 ] * p1[ 1 ];
        if ( D == 0.0 ) return null;

        final double x = ( p0[ 2 ] * p1[ 1 ] - p0[ 1 ] * p1[ 2 ] ) / D;
        final double y = ( p0[ 2 ] * p1[ 0 ] - p0[ 0 ] * p1[ 2 ] ) / D;

        return new Coord( x, y );
    }

    private static double[] getParams(
            final Coord c0,
            final Coord c1 ) {
        final double A = c0.getY() - c1.getY();
        final double B = c0.getX() - c1.getX();
        final double C = c0.getX() * c1.getY() - c0.getY() * c1.getX();
        return new double[] { A, B, C };
    }
    //==========================================================================//



    /**
     * Returns the SimMap this movement model uses
     * @return The SimMap this movement model uses
     */
    public SimMap getMap() {
        return map;
    }

    /**
     * Reads a sim map from location set to the settings, mirrors the map and
     * moves its upper left corner to origo.
     * @return A new SimMap based on the settings
     */
    private SimMap readMap() {
        SimMap simMap;
        Settings settings = new Settings(PPWSM_MOVEMENT_NS);
        WKTMapReader r = new WKTMapReader(true);

        if (cachedMap == null) {
            cachedMapFiles = new ArrayList<String>(); // no cache present
        }
        else { // something in cache
            // check out if previously asked map was asked again
            SimMap cached = checkCache(settings);
            if (cached != null) {
                nrofMapFilesRead = cachedMapFiles.size();
                return cached; // we had right map cached -> return it
            }
            else { // no hit -> reset cache
                cachedMapFiles = new ArrayList<String>();
                cachedMap = null;
            }
        }

        try {
            int nrofMapFiles = settings.getInt(NROF_FILES_S);

            for (int i = 1; i <= nrofMapFiles; i++ ) {
                String pathFile = settings.getSetting(FILE_S + i);
                cachedMapFiles.add(pathFile);
                r.addPaths(new File(pathFile), i);
            }

            nrofMapFilesRead = nrofMapFiles;
        } catch (IOException e) {
            throw new SimError(e.toString(),e);
        }

        simMap = r.getMap();
        // mirrors the map (y' = -y) and moves its upper left corner to origo
        simMap.mirror();
        Coord offset = simMap.getMinBound().clone();
        simMap.translate(-offset.getX(), -offset.getY());


        cachedMap = simMap;
        return simMap;
    }

    /**
     * Checks map cache if the requested map file(s) match to the cached
     * sim map
     * @param settings The Settings where map file names are found
     * @return A cached map or null if the cached map didn't match
     */
    private SimMap checkCache(Settings settings) {
        int nrofMapFiles = settings.getInt(NROF_FILES_S);

        if (nrofMapFiles != cachedMapFiles.size() || cachedMap == null) {
            return null; // wrong number of files
        }

        for (int i = 1; i <= nrofMapFiles; i++ ) {
            String pathFile = settings.getSetting(FILE_S + i);
            if (!pathFile.equals(cachedMapFiles.get(i-1))) {
                return null;	// found wrong file name
            }
        }

        // all files matched -> return cached map
        return cachedMap;
    }

    @Override
    public void setLocation(Coord lastWaypoint) {
        boolean isValid = ( ( this.invert ) ?
                isInside( this.polygon, lastWaypoint ) :
                isOutside( this.polygon, lastWaypoint ) );
        if(isValid) {
            this.lastWaypoint = lastWaypoint;
        } else {
            //TODO atm just randomly put at valid position if pos is not valid. Finding edge of polygon would be better...
            this.lastWaypoint = getInitialLocation();
        }
    }

    @Override
    public Coord getLastLocation() {
        if(this.lastWaypoint != null) {
            return this.lastWaypoint;
        }

        return null;
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
