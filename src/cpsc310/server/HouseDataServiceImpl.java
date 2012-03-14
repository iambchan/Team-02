package cpsc310.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dev.util.collect.HashMap;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

import cpsc310.client.DataCatalogueObserver;
import cpsc310.client.DataCatalogueObserverAsync;
import cpsc310.client.HouseDataService;
import cpsc310.client.HouseData;
//import com.googlecode.objectify.Key;

/**
 * Server side methods to fetch house data
 */
public class HouseDataServiceImpl extends RemoteServiceServlet implements
		HouseDataService {
	
	//objectify objects
//	ObjectifyService.register(DataBaseIndexer.class);
//    ObjectifyService.register(HouseDataPoint.class);
	
	// Currently we store whole file on module load.
	private DataCatalogueObserverImpl observerService = new DataCatalogueObserverImpl();
	private List<String> rawData = observerService.downloadFile("http://www.ugrad.cs.ubc.ca/~y0c7/property_tax_report3.csv");
	private List<HouseDataPoint> store = new ArrayList<HouseDataPoint> (rawData.size());
	
	/**
	 * This method will be refactored into HouseDataBase.
	 * At the moment, this builds HouseDataPoint Store 
	 * by the request module load.
	 */
	public void buildHouseDataPointStore() {
		// Parse raw data
		FileParser parser = new FileParser();
		Iterator<HouseDataPoint> houserItr = parser.parseData(rawData).iterator();
				
		// Convert HouseDataPoint into HouseData
		for (int i = 0; (i < rawData.size()) && (houserItr.hasNext()); i++) {
			store.add(houserItr.next());
		}
	}

	/**
	 * Get house data for initial drawing of table. Returning list must be
	 * ArrayList because of Google RPC's Serialization policy.
	 * @param start
	 * @param range
	 * @return list of HouseData within specified range TODO: Implement JDO
	 *         Storage, Current implementation just retrieves data from server
	 */
	@Override
	public List<HouseData> getHouses(int start, int range) {
		buildHouseDataPointStore();
		
		List<HouseData> grab = null;
		int newRange = range;
		int end = start + range;

		// Check for end condition to prevent accessing out-of-array.
		if (end > getHouseDatabaseLength()) {
			end = getHouseDatabaseLength();
			newRange = end - start;
		}
		
		// Set the size of returning array.
		grab = new ArrayList<HouseData>(newRange);

		// TODO Get HouseDataPoint objects from database for the specified
		// range. Currently get it from the store.
		List<HouseDataPoint> fetch = new ArrayList<HouseDataPoint> (store.subList(start, end));
		Iterator<HouseDataPoint> houserItr = fetch.iterator();
				
		// Convert HouseDataPoint into HouseData
		for (int i = start; (i < end) && (houserItr.hasNext()); i++) {
			grab.add(convertToHouseData(houserItr.next()));
		}

		// Change below to return grab
		return grab;
	}
	

	/**
	 * Get house data within specified criteria. If user did not specify
	 * coordinate range or land value range, lowerCoord, upperCoord, lowerVal,
	 * upperVal will be -1. In Sprint 2, this function will be modified to
	 * include more criteria, and range cases.
	 * 
	 * @param lowerCoord - lower range for coordinate of house
	 * @param upperCoord - upper range for coordinate of house
	 * @param lowerVal - lower asking price of house
	 * @param upperVal - upper asking price of house
	 * @param owner - the realtor that is in charge of the house.
	 * @return list of House data within specified coordinates
	 */
	@Override
	public List<HouseData> getSearchedHouses(int lowerCoord, int upperCoord,
			double lowerLandVal, double upperLandVal, String owner) {
		//@TODO rework method to work with new houseDataPoints
//		List<HouseData> result = new ArrayList <HouseData> (store.size());
//		boolean searchCoord = false;
//		boolean searchLandVal = false;
//		boolean searchOwner = false;
//
//		// If user did not specify coordinate range or land value range,
//		// lowerCoord, upperCoord, lowerVal, upperVal will be -1!!
//		if (lowerCoord != -1 || upperCoord != -1) {
//			searchCoord = true;
//		}
//		if (lowerLandVal != -1 || upperLandVal != -1) {
//			searchLandVal = true;
//		}
//		if (owner != null) {
//			searchOwner = true;
//		}
//
//		// TODO Search database and grab necessary data.
//		Iterator<HouseDataPoint> houserItr = store.iterator();
//		HouseDataPoint check = null;
//		
//		// Convert HouseDataPoint into HouseData
//		for (int i = 0; (i < store.size()) && (houserItr.hasNext()); i++) {
//			check = houserItr.next();
//
//			if (searchLandVal == true) {
//				if ((check.getLandValue() > lowerLandVal) && 
//							(check.getLandValue() < upperLandVal)) {
//					result.add(convertToHouseData(check));
//				}		
//			}
//			
//			if (searchOwner == true) {
//				if (check.getOwner() != null) {
//					if (check.getOwner().equals(owner)) {
//						result.add(convertToHouseData(check));
//					}
//				}
//			}	
//		}
//		
//		if (result.isEmpty())
//			return null;
//			
//		return result;
		return null;
	}

	/**
	 * Helper to table drawing to figure out how many rows need to exist.
	 * @return size of database
	 */
	@Override
	public int getHouseDatabaseLength() {
		// TODO get database length
		int databaseLength = rawData.size();

		return databaseLength;
	}

	/**
	 * For Sprint 2. server-side sorting
	 */
	@Override
	public void sortHouses() {
		// TODO sort database

	}

	/**
	 * Add/update user specified information about the specified data
	 * in the database.
	 * @param Owner - name of realtor
	 * @param price - price of house
	 * @param isSelling - for-sale indicator
	 * @param houses - set of houses to update
	 * @param switchValue - 0 for updating owner; 1 for updating price; 2 for updating isSelling
	 */
	@Override
	public void updateHouses(String Owner, double price, boolean isSelling,
			Set<HouseData> houses, int switchValue) {
		
		// TODO Update HousePointData data in database
//		for (HouseData house : houses) {
//			Iterator<HouseDataPoint> houserItr = store.iterator();
//			HouseDataPoint next = null;
//			for (int i = 0; (i < rawData.size()) && (houserItr.hasNext()); i++) {
//				next = houserItr.next();
//				if (house.getPID().equals(next.getPID())) {
//					switch (switchValue) {
//					case 0:
//						next.setOwner(Owner);
//						break;
//					case 1:
//						next.setPrice(price);
//						break;
//					case 2:
//						next.setIsSelling(isSelling);
//						break;
//					default:
//						break;
//					}
//				}
//			}
//		}
	}

	/**
	 * Helper to convert HouseDataPoint into HouseDataPoint (data transfer
	 * object).
	 * @param house - HouseDataPoint to convert into HouseData
	 * @return the converted HouseDataPoint (returned as a HouseData object)
	 */
	private HouseData convertToHouseData(HouseDataPoint house) {
		HouseData converted = new HouseData();
		
		//@TODO fix to reflect new HouseDataPoints
		
		converted.setPID("FILLER");
		converted.setAddress("FILLER");
		converted.setPostalCode(house.getPostalCode());
		converted.setCoordinate(1234);
		converted.setLandValue(house.getCurrentLandValue());
		converted.setOwner(house.getOwner());
		converted.setIsSelling(house.getIsSelling());
		converted.setPrice(house.getPrice());

		return converted;
	}
	
	/*
	 * initilizes DataStore Objects
	 */
	public void initilizeDataStorage() {
		// Get Data
		DataCatalogueObserverImpl observerService = new DataCatalogueObserverImpl();		
		// try to register
		try{
			ObjectifyService.register(HouseDataPoint.class);
		}
		catch(Exception e){
			// already registered
		}
	}
}
