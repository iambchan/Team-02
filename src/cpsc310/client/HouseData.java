package cpsc310.client;

import java.io.Serializable;

import com.google.gwt.view.client.ProvidesKey;

public class HouseData implements Serializable{

	private int pid;
	private int coordinate;
	private String address;
	private String postalCode;
	private int landValue;
	private String owner;
	private boolean isSelling;
	private double price;

	
	public HouseData() {
	}
	
	/*
	 * Constructor
	 */
	public HouseData(int pid, String address, String postalCode, int coordinate, 
			int landValue, String owner, boolean isSelling, double price) {
		this.pid = pid;
		this.coordinate = coordinate;
		this.address = address;
		this.postalCode = postalCode;
		this.landValue = landValue;

		this.owner = owner;
		this.isSelling = isSelling;
		this.price = price;
	}

	
	//
	public static final ProvidesKey<HouseData> KEY_PROVIDER = new ProvidesKey<HouseData>() {
		public Object getKey (HouseData house) {
			return house == null ? null : house.getPID(); 
		}
	};
	
	//getters
	public int getPID() {
		return pid;
	}

	public int getCoordinate() {
		return coordinate;
	}

	public String getAddress() {
		return address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public int getLandValue() {
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
}