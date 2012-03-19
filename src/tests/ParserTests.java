package tests;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import cpsc310.server.FileParser;
import cpsc310.server.HouseDataPoint;

public class ParserTests {

	// tests to ensure that the file parser works correctly

	private FileParser fileParser;
	private HashMap<String, HouseDataPoint> houses;
	private List<String> testList;
	private HouseDataPoint house;

	@Before
	public void setUp() throws Exception {
		fileParser = new FileParser();
		testList = new ArrayList<String>();
	}

	@Test(expected = NullPointerException.class)
	public void testNullEntry() {
		fileParser.parseData(null);
	}

	@Test(expected = NoSuchElementException.class)
	public void testEmptyList() {
		houses = fileParser.parseData(new ArrayList<String>());
	}

	@Test
	// should be size of 0 since first line is used for titles of columns
	public void testOneEntryList() {
		testList.add("PID,LEGAL_TYPE,FOLIO,LAND_COORDINATE,LOT,BLOCK,PLAN,DISTRICT_LOT,FROM_CIVIC_NUMBER,TO_CIVIC_NUMBER,STREET_PREFIX_DIRECTION,STREET_NAME,PROPERTY_POSTAL_CODE,RECORD_STATUS_CODE,NARRATIVE_LEGAL_LINE1,NARRATIVE_LEGAL_LINE2,NARRATIVE_LEGAL_LINE3,NARRATIVE_LEGAL_LINE4,NARRATIVE_LEGAL_LINE5,CURRENT_LAND_VALUE,CURRENT_IMPROVEMENT_VALUE,ASSESSMENT_YEAR,PREVIOUS_LAND_VALUE,PREVIOUS_IMPROVEMENT_VALUE,YEAR_BUILT,BIG_IMPROVEMENT_YEAR");
		houses = fileParser.parseData(testList);
		assertEquals(0, houses.size());
	}

	@Test
	// should be size of 1 since first line is used for titles of columns
	public void testTwoEntryList() {
		testList.add("PID,LEGAL_TYPE,FOLIO,LAND_COORDINATE,LOT,BLOCK,PLAN,DISTRICT_LOT,FROM_CIVIC_NUMBER,TO_CIVIC_NUMBER,STREET_PREFIX_DIRECTION,STREET_NAME,PROPERTY_POSTAL_CODE,RECORD_STATUS_CODE,NARRATIVE_LEGAL_LINE1,NARRATIVE_LEGAL_LINE2,NARRATIVE_LEGAL_LINE3,NARRATIVE_LEGAL_LINE4,NARRATIVE_LEGAL_LINE5,CURRENT_LAND_VALUE,CURRENT_IMPROVEMENT_VALUE,ASSESSMENT_YEAR,PREVIOUS_LAND_VALUE,PREVIOUS_IMPROVEMENT_VALUE,YEAR_BUILT,BIG_IMPROVEMENT_YEAR");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,888,,HOMER ST,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,223000,225000,2012,,,,");
		houses = fileParser.parseData(testList);
		assertTrue(houses.size() == 1);
	}

	@Test
	// Should only have 1 instance same StreetName and CivicNumber
	public void testDuplicateHouseIDs() {
		testList.add("PID,LEGAL_TYPE,FOLIO,LAND_COORDINATE,LOT,BLOCK,PLAN,DISTRICT_LOT,FROM_CIVIC_NUMBER,TO_CIVIC_NUMBER,STREET_PREFIX_DIRECTION,STREET_NAME,PROPERTY_POSTAL_CODE,RECORD_STATUS_CODE,NARRATIVE_LEGAL_LINE1,NARRATIVE_LEGAL_LINE2,NARRATIVE_LEGAL_LINE3,NARRATIVE_LEGAL_LINE4,NARRATIVE_LEGAL_LINE5,CURRENT_LAND_VALUE,CURRENT_IMPROVEMENT_VALUE,ASSESSMENT_YEAR,PREVIOUS_LAND_VALUE,PREVIOUS_IMPROVEMENT_VALUE,YEAR_BUILT,BIG_IMPROVEMENT_YEAR");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,888,,HOMER ST,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,223000,225000,2012,,,,");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,888,,HOMER ST,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,223000,225000,2012,,,,");
		houses = fileParser.parseData(testList);
		houses.get(0);
		assertEquals(1, houses.size());
	}
	
	@Test
	// Should select house with higher house value
	public void testCorrectDuplicateRow() {
		testList.add("PID,LEGAL_TYPE,FOLIO,LAND_COORDINATE,LOT,BLOCK,PLAN,DISTRICT_LOT,FROM_CIVIC_NUMBER,TO_CIVIC_NUMBER,STREET_PREFIX_DIRECTION,STREET_NAME,PROPERTY_POSTAL_CODE,RECORD_STATUS_CODE,NARRATIVE_LEGAL_LINE1,NARRATIVE_LEGAL_LINE2,NARRATIVE_LEGAL_LINE3,NARRATIVE_LEGAL_LINE4,NARRATIVE_LEGAL_LINE5,CURRENT_LAND_VALUE,CURRENT_IMPROVEMENT_VALUE,ASSESSMENT_YEAR,PREVIOUS_LAND_VALUE,PREVIOUS_IMPROVEMENT_VALUE,YEAR_BUILT,BIG_IMPROVEMENT_YEAR");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,888,,HOMER ST,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,223000,225000,2012,,,,");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,888,,HOMER ST,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,999999,225000,2012,,,,");
		houses = fileParser.parseData(testList);
		assertEquals(999999, houses.get("888 HOMER ST").getCurrentLandValue());
	}

	@Test
	// Should not add if CivicNumber is empty
	public void testEmptyCivicNumber() {
		testList.add("PID,LEGAL_TYPE,FOLIO,LAND_COORDINATE,LOT,BLOCK,PLAN,DISTRICT_LOT,FROM_CIVIC_NUMBER,TO_CIVIC_NUMBER,STREET_PREFIX_DIRECTION,STREET_NAME,PROPERTY_POSTAL_CODE,RECORD_STATUS_CODE,NARRATIVE_LEGAL_LINE1,NARRATIVE_LEGAL_LINE2,NARRATIVE_LEGAL_LINE3,NARRATIVE_LEGAL_LINE4,NARRATIVE_LEGAL_LINE5,CURRENT_LAND_VALUE,CURRENT_IMPROVEMENT_VALUE,ASSESSMENT_YEAR,PREVIOUS_LAND_VALUE,PREVIOUS_IMPROVEMENT_VALUE,YEAR_BUILT,BIG_IMPROVEMENT_YEAR");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,,,HOMER ST,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,223000,225000,2012,,,,");
		houses = fileParser.parseData(testList);
		assertEquals(0, houses.size());
	}
	
	@Test
	// Should not add if Street name is empty
	public void testEmptyStreetName() {
		testList.add("PID,LEGAL_TYPE,FOLIO,LAND_COORDINATE,LOT,BLOCK,PLAN,DISTRICT_LOT,FROM_CIVIC_NUMBER,TO_CIVIC_NUMBER,STREET_PREFIX_DIRECTION,STREET_NAME,PROPERTY_POSTAL_CODE,RECORD_STATUS_CODE,NARRATIVE_LEGAL_LINE1,NARRATIVE_LEGAL_LINE2,NARRATIVE_LEGAL_LINE3,NARRATIVE_LEGAL_LINE4,NARRATIVE_LEGAL_LINE5,CURRENT_LAND_VALUE,CURRENT_IMPROVEMENT_VALUE,ASSESSMENT_YEAR,PREVIOUS_LAND_VALUE,PREVIOUS_IMPROVEMENT_VALUE,YEAR_BUILT,BIG_IMPROVEMENT_YEAR");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,888,,,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,223000,225000,2012,,,,");
		houses = fileParser.parseData(testList);
		assertEquals(0, houses.size());
	}
	
	@Test
	// Should not add if Current land value is empty
	public void testEmptyCurrentLandValue() {
		testList.add("PID,LEGAL_TYPE,FOLIO,LAND_COORDINATE,LOT,BLOCK,PLAN,DISTRICT_LOT,FROM_CIVIC_NUMBER,TO_CIVIC_NUMBER,STREET_PREFIX_DIRECTION,STREET_NAME,PROPERTY_POSTAL_CODE,RECORD_STATUS_CODE,NARRATIVE_LEGAL_LINE1,NARRATIVE_LEGAL_LINE2,NARRATIVE_LEGAL_LINE3,NARRATIVE_LEGAL_LINE4,NARRATIVE_LEGAL_LINE5,CURRENT_LAND_VALUE,CURRENT_IMPROVEMENT_VALUE,ASSESSMENT_YEAR,PREVIOUS_LAND_VALUE,PREVIOUS_IMPROVEMENT_VALUE,YEAR_BUILT,BIG_IMPROVEMENT_YEAR");
		testList.add("028-687-761,STRATA,1.48603E+11,14860284,3,,BCS4249,541,503,888,,HOMER ST,,,LOT 3  PLAN BCS4249  DISTRICT LOT 5,\"41  NWD GROUP 1, TOGETHER WITH AN I\",NTEREST IN THE COMMON PROPERTY IN P,ROPORTION TO THE UNIT ENTITLEMENT O,F THE STRATA LOT AS SHOWN ON FORM 1,,225000,2012,,,,");
		houses = fileParser.parseData(testList);
		assertEquals(0, houses.size());
	}
}
