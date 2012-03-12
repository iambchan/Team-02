package cpsc310.server;

import java.util.HashMap;
import java.util.regex.*;

import javax.persistence.Entity;
import javax.persistence.Id;

/*
 * A single data point value representing a house
 */
@Entity
public class HouseDataPoint {

	// variables
	// tentatively stable; may be removing or adding additional ones often
	// early on
	// Variables to be set by house data
	@Id
	private String pid;
	private String address;
	private String postalCode;
	private double landValue;
	
	//@add additional interesting information like house built date

	// User specified data
	private String owner;
	private boolean isSelling;
	private double price;

	/**
	 * Constructor
	 * @pre houseRow != null;
	 * @post true;
	 * @param houseRow - the HashMap containing the information for a house
	 */
	public HouseDataPoint(HashMap<String, String> houseRow) {
		// Variables to be set by house data
		pid = houseRow.get("PID");
		pid = pid.replaceAll("-", "");
		String tempCivicNumber = houseRow.get("TO_CIVIC_NUMBER");
		tempCivicNumber = tempCivicNumber.replaceAll("\\.\\d*$", "");
		address =tempCivicNumber + " "
				+ houseRow.get("STREET_NAME");
		postalCode = houseRow.get("PROPERTY_POSTAL_CODE");
		if (!houseRow.get("CURRENT_LAND_VALUE").isEmpty()) {
			landValue = Double.parseDouble(houseRow.get("CURRENT_LAND_VALUE"));
		} else {
			landValue = 0;
		}

		// User specified data
		owner = null;
		isSelling = false;
		price = 0;
	}

	// getters
	public String getPID() {
		return pid;
	}

	public String getAddress() {
		return address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public double getLandValue() {
		return landValue;
	}

	public String getOwner() {
		return owner;
	}

	public boolean getIsSelling() {
		return isSelling;
	}

	public double getPrice() {
		return price;
	}

	// setters
	public void setOwner(String newOwner) {
		owner = newOwner;
	}

	public void setIsSelling(boolean sell) {
		isSelling = sell;
	}

	public void setPrice(double salePrice) {
		price = salePrice;
	}
}
