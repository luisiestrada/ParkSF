package com.csc413.sfsu.csc413_parking;

import com.csc413.sfsu.sfpark_simplified.SFParkQuery;
import com.csc413.sfsu.sfpark_simplified.SFParkXMLResponse;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * The SFParkLocationFactory class is responsible for retrieving and storing location data from the
 * SFParkSimplified API.
 *
 * @Important Currently the SFParkLocationFactory should be the only point of entry into the
 * LocationDatabaseHandler class. This is due to the fact that if the timesSearched field of
 * ParkingLocation objects are set manually and added to the database manually, the database will
 * not properly remove the least searched locations.
 */
public class SFParkLocationFactory
{
    private LocationDatabaseHandler db;


    SFParkLocationFactory(MainActivity context){
        this.db=new LocationDatabaseHandler(context);
    }

    /**
     * Retrieves all parking locations from SFPark within the specified radius of the origin as a
     * list of discrete ParkingLocation objects.
     * Note: This method will also add or update all locations found to an internal database.
     * @param origin Center of search for parking locations.
     * @param radius radius to search for parking locations.
     * @return list of ParkingLocation objects within the radius of the origin.
     */
    public List<ParkingLocation> getParkingLocations(LatLng origin, double radius){
        SFParkQuery query = new SFParkQuery();
        query.setLatitude(origin.latitude);
        query.setLongitude(origin.longitude);
        query.setRadius(radius);
        query.setUnitOfMeasurement("MILE");

        SFParkXMLResponse response = new SFParkXMLResponse();
        boolean success = response.populate(query);
        List <ParkingLocation> locationList=new ArrayList<ParkingLocation>();

        String status = response.status();
        if (success) {
            String message = response.message();
            int numRecords = response.numRecords();
            for (int i = 0; i < numRecords; i++) {
                LatLng coords=new LatLng(
                        response.avl(i).loc().latitude(0), response.avl(i).loc().longitude(0));
                String name=response.avl(i).name();
                boolean hasOnStreetParking=(response.avl(i).type().equals("ON")) ? true : false;
                String desc=response.avl(i).desc();
                int ospid=response.avl(i).ospid();
                int bfid=response.avl(i).bfid();
                boolean isFavorite=false;
                int timesSearched=1;

                ParkingLocation loc=new ParkingLocation(origin, radius, hasOnStreetParking, name,
                        desc, ospid, bfid, coords, isFavorite, timesSearched);

                this.db.addLocation(loc);

                if(loc.hasOnStreetParking()){
                    locationList.add(db.getLocationFromBFID(bfid));
                }
                else{
                    locationList.add(db.getLocationFromOSPID(ospid));
                }


            }



        }
        return locationList;
    }


    public void printAllDB(){

        System.out.println("---------------Current Database Contents---------------");
        List<ParkingLocation> list=db.getAllLocations();
        if (db.getLocationsCount()==0){
            System.out.println("    The parking location database is empty.");
        }
        else{
            System.out.println("    There are "+db.getLocationsCount()+" entries in the database.");
        }
        for(int i=0; i<db.getLocationsCount(); i++){
            System.out.println("    Entry: "+(i+1)+": "+list.get(i).toString());
        }
    }



}
