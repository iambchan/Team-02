package cpsc310.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.reveregroup.gwt.facebook4gwt.Facebook;
import com.reveregroup.gwt.facebook4gwt.LoginButton;
import com.reveregroup.gwt.facebook4gwt.ShareButton;

/**
 * Main EntryPoint class. UI is built, client-side request is handled.
 */
public class Team_02 implements EntryPoint {
	private LayoutPanel mainPanel = new LayoutPanel();
	private DockLayoutPanel submainPanel = new DockLayoutPanel(Unit.PX);
	private SplitLayoutPanel mapContainerPanel = new SplitLayoutPanel();
	private FlowPanel sidePanel = new FlowPanel();
	private FlowPanel tableWrapPanel = new FlowPanel();
	private PropertyMap theMap;
	private boolean isSidePanelHidden = false;
	private boolean isTablePanelHidden = false;
	private HouseTable houseTable = HouseTable.createHouseTable();
	private HouseDataServiceAsync houseDataSvc = GWT
			.create(HouseDataService.class);
	private LoginServiceAsync loginService = GWT.create(LoginService.class);
	private LoginInfo loginInfo = null;
	private boolean isEditable = false;
	private boolean isLoginServiceAvailable = false;
	private boolean isAdvSearchPanelHidden = true;
	private Set<HouseData> selectedHouses = null;	
	final private List<String> searchCriteria = 
			Arrays.asList("Street Number", "Address", "Postal Code",
			"Current Land Value", "Current Improvement Value",
			"Assessment Year", "Previous Land Value",
			"Previous Improvement Value", "Year Built", "Big Improvement Year",
			"Price", "Realtor", "For Sale");
	
	private LatLng vancouver = LatLng.newInstance(49.264448, -123.185844);
	private List<String> addresses = new ArrayList<String>();

	/**
	 * Entry point method. Initializes login service. Upon completion of
	 * asynchronous request to login service, UI is built.
	 */
	public void onModuleLoad() {
		// Check login status using login service.
		if (loginService == null) {
			loginService = GWT.create(LoginService.class);
		}
		
		// TODO: when deploying delete "Team_02.html?gwt.codesvr=127.0.0.1:9997"
		// below.
		loginService.login(GWT.getHostPageBaseURL()
				+ "Team_02.html?gwt.codesvr=127.0.0.1:9997",
				new AsyncCallback<LoginInfo>() {
					public void onFailure(Throwable error) {
						Window.alert("Login service could not be loaded.");
						buildUI();
					}

					public void onSuccess(LoginInfo result) {
						loginInfo = result;
						isLoginServiceAvailable = true;
						buildUI();
					}
				});
	}

	/**
	 * Builds application's main UI
	 */
	private void buildUI() {

		// Initialize selection model for map and table
		initSelection();

		// Enable edit function only if login service is available AND
		// the user is logged in.
		if (isLoginServiceAvailable == true && loginInfo.isLoggedIn()) {
			enableEdit();
		}

		// Make main panel fill the browser
		mainPanel.setHeight(Window.getClientHeight() + "px");
		submainPanel.setHeight(Window.getClientHeight() + "px");

		// Make sidePanel
		buildSidePanel(sidePanel);
		submainPanel.addWest(sidePanel, 230);

		// Make tablePanel
		buildTablePanel(tableWrapPanel);
		submainPanel.addSouth(tableWrapPanel, 300);

		// Make mapContainerPanel
		buildMapPanel(mapContainerPanel);
		submainPanel.add(mapContainerPanel);

		// Add content wrapper to the main panel
		mainPanel.add(submainPanel);
		mainPanel.setWidgetLeftWidth(submainPanel, 0, Unit.PCT, 100, Unit.PCT);

		// Associate Main panel with the HTML host page
		RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
		rootLayoutPanel.add(mainPanel);
	}

	/**
	 * Create MultiSelectionModel and attach to map and table. Attachment of
	 * selection model enables display of selected houses in map, and editing of
	 * houses in table.
	 */
	private void initSelection() {

		// Create selection model
		final MultiSelectionModel<HouseData> selectionModel = new MultiSelectionModel<HouseData>(
				HouseData.KEY_PROVIDER);
		
		// Handle selection event. Upon selection selected houses get displayed
		// on map.
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						selectedHouses = selectionModel.getSelectedSet();
						
						if (selectedHouses.isEmpty()) {
							theMap.clearMarkers();
							return;
						}
						// clear map markers before proceeding to add new point
						theMap.clearMarkers();
						// add marker onto map
						for (HouseData house : selectedHouses)
							theMap.findLocation(house, true);
					}
				});

		// Attach selection model to table to enable synchronous selection
		// between map and table.
		houseTable.enableSelection(selectionModel);
	}

	/**
	 * Helper to buildUI(). Assemble map container panel.
	 * 
	 * @param mapContainerPanel
	 *            SplitLayoutPanel to hold the map
	 */
	private void buildMapPanel(SplitLayoutPanel mapContainerPanel) {
		// Open a map centered on Vancouver
		theMap = new PropertyMap(vancouver);

		// Assemble map panel
		mapContainerPanel.addWest(theMap.getStreetViewMap(), 500);
		mapContainerPanel.add(theMap.getMap());
		mapContainerPanel.setStyleName("mapContainerPanel");
	}

	/**
	 * Helper to buildUI() Assembles table panel.
	 * 
	 * @param tableWrapPanel
	 *            - flow panel to hold table related elements
	 */
	private void buildTablePanel(FlowPanel tableWrapPanel) {
		Button hideShowTablePanelButton = new Button("-");
		SimplePager simplePager = new SimplePager();

		buildTablePanelButton(hideShowTablePanelButton);

		// Create Cell Table & attach pager to table
		simplePager.setDisplay(houseTable.getHouseTable());
		simplePager.setStylePrimaryName("pager");

		// Assemble table panel
		tableWrapPanel.add(hideShowTablePanelButton);
		tableWrapPanel.add(houseTable.getHouseTable());
		tableWrapPanel.add(simplePager);
		tableWrapPanel.setStyleName("tableWrapPanel");

	}

	/**
	 * Helper to buildTablePanel() Create hide/show behavior to table panel
	 * button
	 * 
	 * @param hideShowTablePanelButton
	 *            - button to behave lie hide/show button
	 */
	private void buildTablePanelButton(final Button hideShowTablePanelButton) {
		hideShowTablePanelButton.setStyleName("hideShowButton");
		hideShowTablePanelButton.addStyleDependentName("horizontal");

		hideShowTablePanelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!isTablePanelHidden) {
					isTablePanelHidden = true;
					hideShowTablePanelButton.setText("+");
					submainPanel.setWidgetSize(tableWrapPanel, 20);
					submainPanel.animate(300);
				} else {
					isTablePanelHidden = false;
					hideShowTablePanelButton.setText("-");
					submainPanel.setWidgetSize(tableWrapPanel, 300);
					submainPanel.animate(300);
				}
			}
		});
	}

	/**
	 * Helper to buildUI() Assemble side panel.
	 * 
	 * @param sidePanel
	 *            flow panel to hold sidepanel contents
	 */
	private void buildSidePanel(FlowPanel sidePanel) {
		Button hideShowSidePanelButton = new Button("-");
		TabPanel sidebarTabPanel = new TabPanel();

		// Create hide/show ability into the button
		buildSidePanelButton(hideShowSidePanelButton);

		// Assemble GWT widgets to occupy side panel
		buildSidePanelWidgets(sidebarTabPanel);

		// Assemble side panel
		sidePanel.add(new HTML(
				"<div id ='header'><h1>iVan</br>Homes</br>Prices</h1></div>"));
		sidePanel.add(hideShowSidePanelButton);
		sidePanel.add(sidebarTabPanel);
		sidePanel
				.add(new HTML(
						"<div id ='footer'><span>iVanHomesPrices.<br/>Created by Team XD. 2012.</span></div>"));
	}

	/**
	 * Helper to buildSidePanel(). Creates hide/show button behavior.
	 * 
	 * @param hideShowSidePanelButton
	 *            - button to behave like hide/show button
	 */
	private void buildSidePanelButton(final Button hideShowSidePanelButton) {
		hideShowSidePanelButton.setStyleName("hideShowButton");
		hideShowSidePanelButton.addStyleDependentName("vertical");

		hideShowSidePanelButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!isSidePanelHidden) {
					isSidePanelHidden = true;
					hideShowSidePanelButton.setText("+");
					submainPanel.setWidgetSize(sidePanel, 20);
					submainPanel.animate(300);
				} else {
					isSidePanelHidden = false;
					hideShowSidePanelButton.setText("-");
					submainPanel.setWidgetSize(sidePanel, 230);
					submainPanel.animate(300);
				}
			}
		});
	}

	/**
	 * Helper to buildSidePanel() Assembles GWT widgets that needs to be
	 * included in the sidePanel.
	 * 
	 * @param sidebarTabPanel
	 *            - flow panel to wrap the widgets
	 */
	private void buildSidePanelWidgets(TabPanel sidebarTabPanel) {
		FlowPanel loginPanel = new FlowPanel();
		FlowPanel searchPanel = new FlowPanel();
		

		// Richard Added
		HorizontalPanel docPanel = new HorizontalPanel();
		Button helpBtn = new Button("Help");
		Button termsBtn = new Button("Terms of Use");
		DrawToolButton drawBtn = new DrawToolButton();
		drawBtn.setDrawImage();
		drawBtn.setVisible(true);
		docPanel.add(helpBtn);
		docPanel.add(termsBtn);
		docPanel.add(drawBtn);

		// Richard Added
		FlowPanel faceBookTemp = new FlowPanel();
		Facebook.init("257432264338889");
		LoginButton faceBookBtn = new LoginButton(true);
		ShareButton shareBtn = new ShareButton(GWT.getHostPageBaseURL(),"Check out this house!!!");
		faceBookTemp.add(faceBookBtn);
		faceBookTemp.add(shareBtn);
		faceBookTemp.add(new HTML("<iframe src=\"//www.facebook.com/plugins/like.php?href=http%3A%2F%2Frmar3a01.appspot.com%2F&amp;send=false&amp;layout=button_count&amp;width=450&amp;show_faces=false&amp;action=like&amp;colorscheme=light&amp;font&amp;height=21&amp;appId=257432264338889\" scrolling=\"no\" frameborder=\"0\" style=\"border:none; overflow:hidden; width:450px; height:21px;\" allowTransparency=\"true\"></iframe>"));
		docPanel.add(faceBookTemp);

		// Assemble login panel
		buildLoginPanel(loginPanel);

		// Assemble search panel
		buildSearchPanel(searchPanel);

		// Assemble widgets to go into the side panel
/*Commented Out		sidebarTabPanel.add(new HTML("<br />"));
		sidebarTabPanel.add(new HTML("<br />")); */
		// Richard Added
		sidebarTabPanel.add(docPanel, "Documents");
//Commented Out		sidebarTabPanel.add(new HTML("<br />"));
		
		sidebarTabPanel.add(searchPanel, "Search");
		sidebarTabPanel.add(loginPanel, "User Info");

		// Set style
		sidebarTabPanel.setStyleName("sidePanelContentWrap");
	}

	/**
	 * Helper to buildSidePanelWidgets(). Assembles login panel which holds
	 * login/logout buttons. TODO: add user info
	 */
	private void buildLoginPanel(FlowPanel loginPanel) {
		Button loginBtn = new Button("Login");
		Button logoutBtn = new Button("Log out");

		// Enable login/logout only if the login service is available.
		if (isLoginServiceAvailable == true) {
			// Set Login Panel
			loginPanel.add(loginBtn);
			loginPanel.add(logoutBtn);

			// Load the login/logout button depending on login/logout status
			if (loginInfo.isLoggedIn()) {
				loginBtn.setVisible(false);
				loginBtn.setEnabled(false);
				isEditable = true;
			} else {
				logoutBtn.setVisible(false);
				logoutBtn.setVisible(false);
			}

			// Listen for mouse events on Login
			loginBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Window.Location.assign(loginInfo.getLoginUrl());
				}
			});

			// Listen for mouse events on Logout
			logoutBtn.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					Window.Location.assign(loginInfo.getLogoutUrl());
				}
			});
		}

		// Set style
		loginPanel.setStyleName("loginPanel");
	}

	/**
	 * Helper to buildSidePanelWidgets(). Build search panel.
	 * searchSettingsPanel contains all the text boxes and labels needed for
	 * search field. searchPanel wraps the searchSettingsPanel and searchBtn.
	 */
	private void buildSearchPanel(FlowPanel searchPanel) {
		final FlowPanel searchSettingPanel = new FlowPanel();
		FlowPanel advancedSettingPanel = new FlowPanel();
		final PopupPanel advancedSettingPopup = new PopupPanel(false);
		Button searchBtn = new Button("Search");
		final Button advancedSearchBtn = new Button ("Advanced Search"); 
		final List<TextBox> searchValues = new ArrayList<TextBox>();
		final List<RadioButton> forSale = new ArrayList<RadioButton>(3);
		final ListBox addressDropDown = new ListBox(true);
/*Commented Out		final List<String> advancedSearchCriteria = searchCriteria.subList(4, 9);
		advancedSearchCriteria.add("Postal Code");*/
		final List<String> advancedSearchCriteria = new ArrayList<String>();
		advancedSearchCriteria.add("Postal Code");
		advancedSearchCriteria.add("Current Improvement Value");
		advancedSearchCriteria.add("Assessment Year");
		advancedSearchCriteria.add("Previous Land Value");
		advancedSearchCriteria.add("Previous Improvement Value");
		advancedSearchCriteria.add("Year Built");
		advancedSearchCriteria.add("Big Improvement Year");
		
		final List<String> basicSearchCriteria = new ArrayList<String>();
		basicSearchCriteria.add("Street Number");
		basicSearchCriteria.add("Address");
		basicSearchCriteria.add("Current Land Value");
		basicSearchCriteria.add("Price");
		basicSearchCriteria.add("Realtor");
		basicSearchCriteria.add("For Sale");
		
/*Commented Out		final List<String> basicSearchCriteria = searchCriteria.subList(0, searchCriteria.size() - 1);
		basicSearchCriteria.removeAll(advancedSearchCriteria);*/
		

		
		// Append style
		searchPanel.setStyleName("searchPanel");
		searchSettingPanel.setStyleName("searchSettingPanel");
		advancedSettingPopup.setStyleName("advancedSettingPopup");

		// Build searchSettingPanel
		searchSettingPanel.add(new HTML("<div class='border'></div>"));
		buildSearchFields(searchSettingPanel, basicSearchCriteria, 
				searchValues, forSale, addressDropDown);
		buildSearchFields(advancedSettingPanel, advancedSearchCriteria, 
				searchValues, forSale, addressDropDown);
		advancedSettingPopup.setAnimationEnabled(true);
		advancedSettingPopup.setWidget(advancedSettingPanel);	
		
		// Add polygon selection
		buildPolygonSelection(searchSettingPanel);

		// Add searchSettingPanel and searchBtn to the searchPanel
		searchPanel.add(searchSettingPanel);
		searchPanel.add(advancedSearchBtn);
		searchPanel.add(new HTML("<br />"));
		searchPanel.add(searchBtn);

		// Listen for mouse events on Search Button
		searchBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				searchHouse(addressDropDown, searchValues, forSale);
			}
		});
		
		//Listen for mouse events on Advanced Search Button
		advancedSearchBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (isAdvSearchPanelHidden == true) {
					advancedSettingPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							int left = searchSettingPanel.getAbsoluteLeft()
									+ searchSettingPanel.getOffsetWidth();
							int top = searchSettingPanel.getAbsoluteTop();
							advancedSettingPopup.setPopupPosition(left, top);
						}
					});
					isAdvSearchPanelHidden = false;
				}
				else {
					advancedSettingPopup.hide();
					isAdvSearchPanelHidden = true;
				}
			}
		});
	}

	/**
	 * Helper to buildSearchPanel().
	 * Adds search fields to given search setting panels.
	 * @param searchSettingPanel - panel to add in fields
	 * @param basicSearchCriteria - search criteria to add
	 * @param searchValues - list of search field text box
	 * @param forSale - list of 'for sale' radio buttons
	 * @param addressDropDown - Drop Down list of address
	 */
	private void buildSearchFields(FlowPanel searchSettingPanel,
			List<String> basicSearchCriteria, List<TextBox> searchValues, 
			List<RadioButton> forSale, ListBox addressDropDown) {
		
		for (String criterion : basicSearchCriteria) {
			searchSettingPanel.add(new Label(criterion));

			if (criterion.endsWith("Value") || criterion.endsWith("Price")
					|| criterion.endsWith("Year")
					|| criterion.startsWith("Year")) {
				buildRangeBoxes(searchValues, searchSettingPanel);
			} 
			else if (criterion.endsWith("Sale")) {
				buildForSale(forSale, searchSettingPanel);
			} 
			else if (criterion.equals("Address")) {
				buildAddressDropMenu(addressDropDown, searchSettingPanel);
			}
			else {
				buildRegularBoxes(searchValues, searchSettingPanel);
			}
		}
	}
	
	/**
	 * Helper to buildSearchField(). Adds address drop down menu.
	 * @param addressDropDown - address drop down to be constructed
	 * @param searchSettingPanel - panel to add address drop down
	 */
	private void buildAddressDropMenu(final ListBox addressDropDown,
			FlowPanel searchSettingPanel) {
		addresses.add("");
		// If address list is empty, fetch from server
		if (addresses.size() == 1) {
			if (houseDataSvc == null) {
				houseDataSvc = GWT.create(HouseDataService.class);
			}
			
			// Fetch address list from server
			AsyncCallback<List<String>> callback = new AsyncCallback<List<String>>() {
				public void onFailure(Throwable caught) {
					Window.alert(caught.getMessage());
				}
	
				public void onSuccess(List<String> result) {
					addresses = result;
					for (int i = 0; i < result.size(); i++) {
					      addressDropDown.addItem(result.get(i));				
					}
				}
			};
			houseDataSvc.getStreetNames(callback);
		}
		// Otherwise, build list from local store of addresses
		else {
			for (int i = 0; i < addresses.size(); i++) {
			      addressDropDown.addItem(addresses.get(i));				
			}
		}
		searchSettingPanel.add(addressDropDown);
	}

	/**
	 * Helper to buildSearchPanel(). Adds polygon selection tools.
	 * 
	 * @param searchSettingPanel
	 *            - panel to hold selection tool
	 */
	private void buildPolygonSelection(FlowPanel searchSettingPanel) {
		final DrawToolButton specifyRegionBtn = new DrawToolButton();
		final DrawToolButton clearPolygonBtn = new DrawToolButton();
		final Button editPolygonBtn = new Button();

		// Polygon settings
		specifyRegionBtn.setDrawImage();
		clearPolygonBtn.setEraseImage();
		editPolygonBtn.setText("Edit specified region");
		clearPolygonBtn.setEnabled(false);
		editPolygonBtn.setEnabled(false);

		// Listen for mouse events on specify region Button
		specifyRegionBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				theMap.setSpecifyingRegion(true);
				clearPolygonBtn.setEnabled(true);
				specifyRegionBtn.setEnabled(false);
				editPolygonBtn.setEnabled(true);
				// prompt user to click on a region on the map
				InfoWindowContent content;
				HTML htmlWidget = new HTML(
						"<p> Click on the map to specify region.</br> Drag corners to edit</p>");
				content = new InfoWindowContent(htmlWidget);
				theMap.getMap().getInfoWindow().open(vancouver, content);

			}
		});

		// Listen for mouse events on clear polygon Button
		clearPolygonBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// theMap.clearMap();
				theMap.clearSpecifiedRegion();
				specifyRegionBtn.setEnabled(true);
				clearPolygonBtn.setEnabled(false);
				editPolygonBtn.setEnabled(false);
			}
		});

		// Listen for mouse events on editPolygon Button
		editPolygonBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				theMap.editPolygon();
			}
		});

		// Add to setting panel
		searchSettingPanel.add(specifyRegionBtn);
		searchSettingPanel.add(clearPolygonBtn);
		searchSettingPanel.add(editPolygonBtn);
	}

	/**
	 * Helper to buildSearchPanel(). Builds and adds text boxes that represent a
	 * range of numbers. boxes by default get predefined labels "min" and "max"
	 * in their field.
	 * 
	 * @param searchValues
	 *            list of text boxes representing search field
	 * @param searchSettingPanel
	 *            FlowPanel that holds all the search boxes.
	 */
	private void buildRangeBoxes(List<TextBox> searchValues,
			FlowPanel searchSettingPanel) {
		TextBox[] rangeBox = { new TextBox(), new TextBox() };
		String[] labels = { "min", "max" };
		int i = 0;

		for (final TextBox box : rangeBox) {
			// add default style, add to panel and text box list
			box.addStyleDependentName("shorter");
			searchSettingPanel.add(box);
			searchValues.add(box);

			// add predefined text "min" and "max" colored in gray font color
			box.setText(labels[i]);
			box.addStyleDependentName("before");

			// when user clicks the text goes away and gray font color is
			// removed
			box.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					box.setText("");
					box.removeStyleDependentName("before");
				}
			});

			// To prevent array out of bounds error
			if (i < labels.length)
				i++;
		}
	}

	/**
	 * Helper to buildSearchPanel(). Creates non-range boxes and adds the box to
	 * the list of search boxes, and the flow panel that holds all the search
	 * fields.
	 * 
	 * @param searchValues
	 *            list of text boxes representing search field
	 * @param searchSettingPanel
	 *            FlowPanel that holds all the search boxes.
	 */
	private void buildRegularBoxes(List<TextBox> searchValues,
			FlowPanel searchSettingPanel) {
		TextBox tb = new TextBox();
		tb.addStyleDependentName("longer");
		searchValues.add(tb);
		searchSettingPanel.add(tb);
	}

	/**
	 * Helper to buildSearchPanel(). Creates radio buttons that specify the
	 * search criterion "For Sale", and adds the radio buttons to forSale list
	 * so that it will be passed to the search method. "All" criterion is
	 * selected by default.
	 * 
	 * @param forSale
	 *            list of Radio Buttons that define "for sale" criterion
	 * @param searchSettingPanel
	 *            FlowPanel that holds all the search boxes.
	 */
	private void buildForSale(List<RadioButton> forSale,
			FlowPanel searchSettingPanel) {
		// Labels that go next to the button
		String[] isSelling = { "Yes", "No", "All" };

		// Build the buttons
		for (String value : isSelling) {
			RadioButton rdBtn = new RadioButton("isSelling", value);
			searchSettingPanel.add(rdBtn);
			searchSettingPanel.add(new InlineHTML("&nbsp;&nbsp;"));
			forSale.add(rdBtn);
		}
		// All is selected by default
		forSale.get(isSelling.length - 1).setValue(true);
	}

	/**
	 * Gets user input from search tab, validates user input, makes asynchronous
	 * call to server-side search, stores search result into local store, and
	 * updates table with the search result.
	 */
	private void searchHouse(ListBox addressDropDown, List<TextBox> searchValues,
			List<RadioButton> forSale) {
		// Get user input into search boxes
		String[] userSearchInput = getUserSearchInput(addressDropDown, searchValues);

		// Validate user input
		if (!validateUserSearchInput(userSearchInput))
			return;

		// Get radio button (For Sale) response
		int isSelling = convertRadioBtnSearch(forSale);

		// Initialize the service proxy
		if (houseDataSvc == null) {
			houseDataSvc = GWT.create(HouseDataService.class);
		}

		// Set up the callback object
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			public void onSuccess(Void result) {
				houseTable.refreshTableFromBeginning();
			}
		};
		// Make the call to the house data service to search for data in the
		// server
		houseDataSvc.searchHouses(userSearchInput, isSelling, callback);
	}

	/**
	 * Helper to searchHouse(). Grabs the user's input into the search boxes.
	 * 
	 * @param addressDropDown - address drop-down list
	 * @param searchValues - list of search boxes
	 * @return array of user's search input into text boxes
	 */
	private String[] getUserSearchInput(ListBox addressDropDown, List<TextBox> searchValues) {
		// + 1 for adding address
		String[] userInput = new String[searchValues.size() + 1];	
		
		// Add civic number
		userInput[0] = searchValues.get(0).getText().trim();
		
		// index 1 is reserved for address
		int selectedAddrIndex = addressDropDown.getSelectedIndex();
		userInput[1] = addressDropDown.getValue(selectedAddrIndex);
		
		// Because civic number(street number) is already added, begin adding from index 1
		for (int i = 0; i < searchValues.size(); i++) {
			String temp = searchValues.get(i).getText().trim();		
			
			// if user left min/max labels, then the criterion is empty.
			if (temp.equals("min") || temp.equals("max"))
				temp = "";
			// Because 0 and 1 is reserved for civic number and address, begin from 2
			userInput[i+2] = temp;
		}

		return userInput;
	}

	/**
	 * Helper to searchHouse(). Validates user's input into search boxes. If
	 * invalid, notifies the user
	 * 
	 * @param userSearchInput
	 *            list of user's input into search boxes
	 * @return boolean value representing if the inputs were all valid
	 */
	private boolean validateUserSearchInput(String[] userSearchInput) {
		boolean isOK = false;
		String numericAlert = "must be numbers only. No decimal is allowed.\n";
		String postalCodeAlert = "is not a valid postal code.\n";
		String invalidMsg = "";
		int i = 0;

		for (String criterion : searchCriteria) {
			if (criterion.endsWith("Value") || criterion.endsWith("Price")) {
				if (!userSearchInput[i].matches("\\d*")
						|| !userSearchInput[i + 1].matches("\\d*")) {
					invalidMsg = invalidMsg + criterion + numericAlert;
					isOK = false;
					i += 2;
				}
			}

			else if (criterion.equals("Postal Code")) {
				if (!userSearchInput[i]
						.matches("|[A-Z][0-9][A-Z][ ][0-9][A-Z][0-9]")) {
					invalidMsg = invalidMsg + criterion + postalCodeAlert;
					isOK = false;
					i++;
				}
			}

			else {
				isOK = true;
				i++;
			}
		}

		if (isOK == false) {
			Window.alert(invalidMsg);
		}

		return isOK;
	}

	/**
	 * Helper to searchHouse(). Converts user's "For Sale" criterion response
	 * into integer. 1 = yes; 0 = no; -1 = all; Assumption is that the given
	 * list of radio button has always 3 buttons.
	 * 
	 * @param forSale
	 *            list of radio buttons for "For Sale" criteria
	 * @return integer of response
	 */
	private int convertRadioBtnSearch(List<RadioButton> forSale) {
		int isSelling = -1;

		if (forSale.get(0).getValue() == true) {
			isSelling = 1;
		} else if (forSale.get(1).getValue() == true) {
			isSelling = 0;
		} else
			isSelling = -1;

		return isSelling;
	}
	
	
	/**
	 * Enables editing of a house data.
	 * Adds edit button to the table panel,
	 * builds dialog box where user can specify price and for-sale indicator.
	 */
	private void enableEdit() {
		final DialogBox editDialog = new DialogBox();
		Button editBtn = new Button("Edit");
		editDialog.setStyleName("editDialog");
		
		editBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (checkOnlyOneSelected()) {
					buildEditPanel(editDialog);
					editDialog.center();
					editDialog.show();
				}
				else
					Window.alert("Please select one house");
			}
		});
		tableWrapPanel.add(editBtn);
	}
	
	private boolean checkOnlyOneSelected() {
		return (selectedHouses != null && selectedHouses.size() == 1);
	}

	/**
	 * Helper to enableEdit().
	 * Builds contents of edit dialog.
	 * @param editDialog - dialog to add the contents in
	 */
	private void buildEditPanel(final DialogBox editDialog) {	
		FlowPanel editPanel = new FlowPanel();			
		Button okBtn = new Button("OK");
		Button cancelBtn = new Button("Cancel");
		final TextBox priceBox = new TextBox();
		final RadioButton yesSell = new RadioButton("editSell", "Yes");
		RadioButton noSell = new RadioButton("editSell", "No");
		noSell.setValue(true);
		
		editPanel.setStyleName("editPanel");
		priceBox.addStyleDependentName("shorter");
		
		for (HouseData house : selectedHouses) {
			editPanel.add(new Label("House to edit: " + house.getAddress()));
		}
		
		editPanel.add(new InlineHTML("<br /> Price: "));
		editPanel.add(priceBox);
		editPanel.add(new InlineHTML("<br /><br /> For Sale: "));
		editPanel.add(yesSell);
		editPanel.add(new InlineHTML("&nbsp;&nbsp;"));
		editPanel.add(noSell);
		editPanel.add(new HTML("<br /><br /><br />"));
		editPanel.add(cancelBtn);
		editPanel.add(new InlineHTML("&nbsp;&nbsp;"));
		editPanel.add(okBtn);
		
		okBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editHouse(priceBox.getValue(), yesSell.getValue());
				editDialog.clear();
				editDialog.hide();
			}
		});
		
		cancelBtn.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editDialog.clear();
				editDialog.hide();
			}
		});
		editDialog.setText("Edit a house");
		editDialog.setGlassEnabled(true);
		editDialog.setAnimationEnabled(true);		
		editDialog.setWidget(editPanel);
	}

	/**
	 * Actual editing function.
	 * Sends editing data to the server by an asynchronous call.
	 * After edit was successful, refreshes table.
	 * @param price - price of house that user specified.
	 * @param yesSelling - for-sale indicator that user specified.
	 */
	private void editHouse(String price, Boolean yesSelling) {
		if (selectedHouses.size() == 1) {
			// Assemble edit field			
			HouseData house = null;
			int housePrice = 0;
			String owner = loginInfo.getEmailAddress();
			
			for (HouseData h : selectedHouses) {
				house = h;
			}
			
			//TODO REPLACE with getLat/getLong FROM MAP method once
			Double[] ll = theMap.getLL(house);
			
			
			if (!price.isEmpty()) 
				housePrice = Integer.parseInt(price);
						
			// Initialize the service proxy
			if (houseDataSvc == null) {
				houseDataSvc = GWT.create(HouseDataService.class);
			}
	
			// Set up the callback object
			AsyncCallback<Void> callback = new AsyncCallback<Void>() {
				public void onFailure(Throwable caught) {
					Window.alert(caught.getMessage());
				}
	
				public void onSuccess(Void result) {
					houseTable.refreshTableCurrentView();
				}
			};
			houseDataSvc.updateHouse(owner, housePrice, yesSelling, 
					house.getHouseID(), ll[0], ll[1], house.getPostalCode(), callback);
		}
	}

}
