import java.util.Scanner; 
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.PatternSyntaxException;

/**
 * Restaurant is the main class used to run the Restaurant Reservation and Point of Sale System (RRPSS)
 * @author Nicole
 * @version 1.0
 * @since 2021-11-06
 */
public class Restaurant {
	/**
	 * The standard format for a date input, 
	 * given in 2 digit day of month (dd),
	 * 2 digit month of year (MM), and
	 * 4 digit year of era (YYYY). 
	 * e.g. 10/07/2004
	 */
	private static final String dateFormatString = "dd/MM/yyyy"; 
	/**
	 * The standard format for a datetime input, 
	 * given in 2 digit day of month (dd),
	 * 2 digit month of year (MM), 
	 * 4 digit year of era (YYYY),
	 * 2 digit hour of day (0-23) (HH), and
	 * minute of hour (mm). 
	 * e.g. 10/07/2004 00:30
	 */
	private static final String dateTimeFormatString = "dd/MM/yyyy HH:mm"; 
	/**
	 * The list of Staff employed by the restaurant.
	 */
	private ArrayList<Staff> staffList; 
	/**
	 * The Menu of this restaurant.
	 */
	private Menu menu;
	/**
	 * The TableManager of this restaurant.
	 */
	private TableManager tableMgr;
	/**
	 * The OrderManager of this restaurant.
	 */
	private OrderManager orderMgr;
	/**
	 * The ReservationManager of this restaurant.
	 */
	private ReservationManager resMgr;	
	/**
	 * Class constructor. Initialises staffList, menu, and tableMgr with test data.
	 */
	public Restaurant() 
	{
		// initialise staffList
		staffList = new ArrayList<Staff>();
		String path = new File("").getAbsolutePath();
		String fileName = path + "\\staffData.txt";
		System.out.printf("Reading staff data from %s ...\n", fileName);
		File file = new File(fileName);
		BufferedReader br;
        String line, data[];		
		Staff staff; Gender g; JobTitle j;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null)
			{
				data = line.split("\\|");
				// name|gender|emp|job 
				if (data[1] == "0") g = Gender.Male;
				else g = Gender.Female;
				if (data[3] == "0") j = JobTitle.waiter;
				else if (data[3] == "1") j = JobTitle.cashier;
				else j = JobTitle.manager;
				staff = new Staff(data[0], g, data[2], j);
				staffList.add(staff);
			}
		} catch (FileNotFoundException e) {
			System.out.printf("ERROR: FileNotFoundException for %s\n", fileName);
		} catch (IOException e) {
			System.out.printf("ERROR: IOException while reading %s\n", fileName);
		}
		// initialise menu
		menu = new Menu();
		// initialise tables
		tableMgr = new TableManager();
		// initialise order manager
		orderMgr = new OrderManager();
		// initialise reservation manager
		resMgr = new ReservationManager();
	}
	/**
	 * Requests user to input customer data (name, contact, membership), 
	 * validates inputs, 
	 * creates and returns Customer object.
	 * @param sc 	Scanner object to request inputs
	 * @return newly created customer object
	 */
	private Customer inputCustomer(Scanner sc)
	{
		Customer customer;
		System.out.print("Enter customer name: \n");
		String custName = sc.nextLine();
		System.out.print("Enter customer contact: \n");
		String custContact = sc.nextLine();
		char membership;
		do 
		{
			System.out.print("Is customer a member? (Y/N) \n");
			membership = sc.next().toUpperCase().charAt(0);
			if (membership == 'Y' || membership == 'N')
				break;
			else
				System.out.print("Invalid input. Please enter Y or N.\n");
		} while (true); 
		if (membership == 'Y')
			customer = new Customer(custName, custContact, true);
		else
			customer = new Customer(custName, custContact, false);
		return customer;
	}
	/**
	 * Requests user to input date based on DateFormatString
	 * validates inputs, 
	 * creates and returns LocalDate object.
	 * @param sc 	Scanner object to request inputs
	 * @return newly created LocatDate object
	 */
	private LocalDate inputDate(Scanner sc)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormatString);
		LocalDate date = null;
		String dateString;
		boolean valid = true;
		do 
		{
			System.out.printf("Enter date (%s): \n", dateFormatString);
			dateString = sc.next();
			try {
				date = LocalDate.parse(dateString, formatter);
			}
			catch (DateTimeParseException exc) {
				System.out.printf("Invalid date input: %s is not parsable!", dateString);
				valid = false;
			}
		} while (!valid);
		return date;
	}
	/**
	 * Requests user to input datetime based on DateTimeFormatString 
	 * validates inputs, 
	 * creates and returns LocalDateTime object.
	 * Input time is restricted to hour (e.g. 23:00, not 23:36).
	 * @param sc 	Scanner object to request inputs
	 * @return newly created LocatDateTime object
	 */
	private LocalDateTime inputDateTime(Scanner sc)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateTimeFormatString);
		LocalDateTime dateTime = null;
		String dateString, dateTimeString;
		int hour;
		boolean valid = true;
		do 
		{
			valid = true;
			System.out.printf("Enter date (%s): \n", dateFormatString);
			dateString = sc.next();
			System.out.print("Enter hour (24H, from 11 to 21): \n");
			hour = sc.nextInt();
			sc.nextLine();
			dateTimeString = String.format("%s %02d:00",dateString, hour);
			try {
				dateTime = LocalDateTime.parse(dateTimeString, formatter);
			}
			catch (DateTimeParseException exc) {
				System.out.printf("Invalid datetime input: %s is not parsable!\n", dateTimeString);
				valid = false;
			}
			if (valid)
				valid = resMgr.isValidDate(dateTime);
		} while (!valid);
		return dateTime;
	}
	/**
	 * Requests user to input MenuItemID 
	 * validates input, 
	 * gets and returns associated MenuItem object.
	 * @param sc 			Scanner object to request inputs
	 * @param isAlaCarte 	<code>true</code> if MenuItemID of AlaCarte objects should be accepted, 
	 * 						<code>false</code> otherwise.
	 * @param isPromo	 	<code>true</code> if MenuItemID of Promotion objects should be accepted, 
	 * 						<code>false</code> otherwise.
	 * @return associated MenuItem object
	 */
	private MenuItem inputMenuItem(Scanner sc, boolean isAlaCarte, boolean isPromo)
	{
		String menuItemID;
		MenuItem item;
		do
		{
			if (isAlaCarte && isPromo) menu.displayMenu(); 
			else if (isAlaCarte) menu.displayAlaCarte();
			else if (isPromo) menu.displayPromotion();
			System.out.print("Enter menu item ID: ");
			menuItemID = sc.next(); 
			if (menu.isValidID(menuItemID))
			{
				item = menu.getMenuItem(menuItemID);
				if (isAlaCarte && !isPromo) // only accept ala carte 
				{
					if (item.checkAlacarte()) 
						break;
					else
						System.out.print("Invalid input. Please enter ID of ala carte item.\n");
				} else if (!isAlaCarte && isPromo) // only accept promo
				{
					if (!item.checkAlacarte()) 
						break;
					else
						System.out.print("Invalid input. Please enter ID of promotion item.\n");
				}
				else // accept any menu item
					break;
			}
			else
				System.out.print("Invalid input. Please enter valid ID.\n");
		} while (true);
		return item;
	}
	/**
	 * Requests user to input number of people,
	 * validates input, and 
	 * and returns input.
	 * @param sc 	Scanner object to request inputs
	 * @return number of people (pax)
	 */
	private int inputPax(Scanner sc)
	{
		int pax; 
		do 
		{
			System.out.print("Enter number of people: \n"); 
			pax = sc.nextInt();
			sc.nextLine();
			if (pax >= 0 && pax <= 10)
				break;
			else
				System.out.print("Invalid input. Please enter positive integer.\n");
		} while (true); 
		return pax;
	}
	/**
	 * Requests user to input price,
	 * validates input, and 
	 * and returns input.
	 * @param sc 	Scanner object to request inputs
	 * @return price
	 */
	private float inputPrice(Scanner sc)
	{
		float price = 0;
		do 
		{
			System.out.print("Enter price: \n");
			try{
				price = sc.nextFloat();
			} catch(InputMismatchException e){
				System.out.println("Please input a valid price!");
				sc.nextLine();
				continue;
			}
			price = Math.round(price*100)/100;
			if (price <= 0)
				System.out.print("Invalid input. Please enter positive value.\n");
			else
				break;
		} while	(true); 
		return price;
	
	}
	/**
	 * Requests user to input quantity,
	 * validates input, and 
	 * and returns input.
	 * @param sc 	Scanner object to request inputs
	 * @return quantity 
	 */
	private int inputQuantity(Scanner sc)
	{
		int quantity;
		do 
		{
			System.out.print("Enter quantity: \n");
			quantity = sc.nextInt();
			if (quantity > 0)
				break;
			else
				System.out.print("Invalid input. Please enter positive integer.\n");
		} while (true);
		return quantity;
	}
	/**
	 * Finds and returns associated Staff object from staffList based on employee ID.
	 * @param employeeID 	employee ID to identify staff
	 * @return associated Staff object
	 */
	private Staff getStaff(String employeeID)
	{
		for (Staff staff : staffList)
		{
			if (staff.getEmployeeID().equals(employeeID))
				return staff;
		}
		return null;
	}
	/**
	 * Requests user to input an employee ID,
	 * validates input, and 
	 * and returns the associated Staff object.
	 * @param sc 	Scanner object to request inputs
	 * @return associated Staff object
	 */
	private Staff inputStaff(Scanner sc)
	{
		String employeeID; 
		Staff staff;
		do 
		{
			System.out.printf("Enter employee ID: \n");
			employeeID = sc.nextLine();
			staff = getStaff(employeeID);
			if (staff != null)
				break;
			else // id not found in dictionary
				System.out.print("Invalid input. Please enter valid employee ID.\n");
		} while (true);
		return staff;
	}
	/**
	 * Requests user to input table number,
	 * validates input, and 
	 * and returns input.
	 * @param sc 	Scanner object to request inputs
	 * @return table number 
	 */
	private int inputTableNum(Scanner sc)
	{
		int tableNum;
		do 
		{
			System.out.print("Enter table number: \n");
			tableNum = sc.nextInt();
			if (tableMgr.isValidTableNumber(tableNum))
				break;
			else
				System.out.print("Invalid input. Please enter valid table number.\n");
		} while (true);
		return tableNum;
	}
	/**
	 * Requests user to input Type of AlaCarte object,
	 * validates input, and 
	 * and returns input.
	 * @param sc 	Scanner object to request inputs
	 * @return type 
	 */
	private Type inputType(Scanner sc)
	{
		int num;
		do
		{
			System.out.print("1) Main course\n" +
							"2) Side\n" + 
							"3) Drink\n" +
							"4) Dessert\n");
			System.out.print("Enter number to indicate type:\n");
			num = sc.nextInt();
			switch (num)
			{
			case 1:
				return Type.MAIN_COURSE;
			case 2:
				return Type.SIDE;
			case 3:
				return Type.DRINK;
			case 4:
				return Type.DESSERT;
			default: 
				System.out.print("Invalid input. Please enter 1-4 to select valid type.\n");
			}
		} while (true);
	}
	/**
	 * Helper function for runRRPSS(), 
	 * which displays options to manage AlaCarte items on the Menu, 
	 * and executes the appropriate functions based on user inputs.
	 * @param sc 	Scanner object to request inputs
	 */
	private void alaCarteHelper(Scanner sc) 
	{
		AlaCarte alaCarteItem;
		int option = 0;		
		do {
			System.out.print("====== RRPSS manage ala carte items ======\n");
			System.out.print("1) Create ala carte item\n" + 
							"2) Update ala carte item\n" + 
							"3) Remove ala carte item\n" + 
							"4) Return to RRPSS application main menu\n");
			System.out.print("Enter option number: ");
			option = sc.nextInt();
			sc.nextLine();
			switch (option)
			{
			case 1:
				// create
				System.out.print("Enter name of new ala carte item: \n");
				String name = sc.nextLine(); 
				System.out.print("Enter description: \n");
				String desc = sc.nextLine();
				float price = inputPrice(sc);
				Type type = inputType(sc);				
				menu.createAlaCarte(name, desc, price, type);
				break;
			case 2:
				// update
				alaCarteItem = (AlaCarte) inputMenuItem(sc, true, false);
				System.out.printf("====== RRPSS update ala carte item %s =====\n", alaCarteItem.getName());
				System.out.printf("1) Update name of ala carte item\n" +
								"2) Update description of ala carte item\n" +
								"3) Update price of ala carte item\n" +
								"4) Update type of ala carte item\n");
				System.out.print("Enter option number (1-4): \n");
				int subOption = sc.nextInt();
				sc.nextLine();
				switch (subOption)
				{
				case 1:
					// name
					System.out.printf("Current name: %s\n", alaCarteItem.getName());
					System.out.print("Enter new name: \n");
					String itemName = sc.nextLine(); 
					alaCarteItem.setName(itemName);
					break;
				case 2:
					// desc
					System.out.printf("Current description: %s\n", alaCarteItem.getDescription());
					System.out.print("Enter new description: \n");
					String itemDesc = sc.nextLine();	
					alaCarteItem.setDescription(itemDesc);
					break;
				case 3:
					// price
					System.out.printf("Current price: %.2f\n", alaCarteItem.getPrice());
					float itemPrice = inputPrice(sc);
					alaCarteItem.setPrice(itemPrice);
					break;
				case 4:
					// type
					System.out.printf("Current type: %d\n", alaCarteItem.getType());
					Type itemType = inputType(sc);
					alaCarteItem.setType(itemType);
					break;
				default:
					System.out.print("Invalid input. Returning to RRPSS menu for managing ala carte items...\n");
				}
				break;
			case 3: 
				// remove
				alaCarteItem = (AlaCarte) inputMenuItem(sc, true, false);
				menu.removeMenuItem(alaCarteItem.getId());
				break;
			case 4:
				// back to main
				System.out.printf("Returning to RRPSS main menu...\n");
				break;
			default:
				// invalid input
				System.out.print("Invalid option. Please enter option 1-4.\n");
			}
		} while (option != 4);
	}
	/**
	 * Helper function for runRRPSS(), 
	 * which displays options to manage Promotion items on the Menu, 
	 * and executes the appropriate functions based on user inputs.
	 * @param sc 	Scanner object to request inputs
	 */
	private void promotionHelper(Scanner sc) 
	{
		Promotion promo;
		int option = 0;
		do 
		{
			System.out.print("====== RRPSS manage promotions ======\n");
			System.out.print("1) Create promotion\n" + 
							"2) Update promotion\n" + 
							"3) Remove promotion\n" +
							"4) Return to RRPSS application main menu\n");
			System.out.print("Enter option number: \n");
			option = sc.nextInt();
			sc.nextLine();
			switch (option) 
			{
			case 1: 
				// create
				System.out.print("Enter name of new promotional package: \n");
				String name = sc.nextLine(); 
				System.out.print("Enter description: \n");
				String desc = sc.nextLine();
				float price = inputPrice(sc);
				promo = menu.createPromotion(name, desc, price);
				System.out.printf("Enter number of ala carte items in promotional package: \n");
				int numItems = sc.nextInt(); 
				AlaCarte alaCarteItem;
				for (int i=0; i<numItems; i++)
				{
					alaCarteItem = (AlaCarte) inputMenuItem(sc, true, false); 
					promo.addItem(alaCarteItem);
				}
				break;
			case 2:
				// update
				promo = (Promotion) inputMenuItem(sc, false, true);
				System.out.printf("====== RRPSS update promotion %s =====\n", promo.getName());
				System.out.printf("1) Update name of promotional package\n" +
								"2) Update description of promotional package\n" +
								"3) Update price of promotional package\n" + 
								"4) Add item to promotional package\n" +
								"5) Remove item from %<s\n");
				System.out.print("Enter option number (1-5): \n");
				int subOption = sc.nextInt();
				sc.nextLine();
				switch (subOption)
				{
				case 1:
					// name
					System.out.printf("Current name: %s\n", promo.getName());
					System.out.print("Enter new name: \n");
					String promoName = sc.nextLine(); 
					promo.setName(promoName);
					break;
				case 2:
					// desc
					System.out.printf("Current description: %s\n", promo.getDescription());
					System.out.print("Enter new description: \n");
					String promoDesc = sc.nextLine();	
					promo.setDescription(promoDesc);
					break;
				case 3:
					// price
					System.out.printf("Current price: %.2f\n", promo.getPrice());
					float promoPrice = inputPrice(sc);
					promo.setPrice(promoPrice);
					break;
				case 4:
					alaCarteItem = (AlaCarte) inputMenuItem(sc, true, false);
					promo.addItem(alaCarteItem);
					break;
				case 5:
					alaCarteItem = (AlaCarte) inputMenuItem(sc, true, false);
					promo.removeItem(alaCarteItem);
					break;
				default:
					System.out.print("Invalid input. Returning to RRPSS menu for managing promotions...\n");
				}
				break;
			case 3: 
				// remove
				promo = (Promotion) inputMenuItem(sc, false, true);
				menu.removeMenuItem(promo.getId());
				break;
			case 4:
				// back to main
				System.out.printf("Returning to RRPSS main menu...\n");
				break;
			default:
				// invalid input
				System.out.print("Invalid option. Please enter option 1-4.\n");
			}
		} while (option != 4);
	}
	/**
	 * Helper function for runRRPSS(), 
	 * which displays options to manage orders using OrderManager, 
	 * and executes the appropriate functions based on user inputs.
	 * @param sc 	Scanner object to request inputs
	 */
	private void orderHelper(Scanner sc) 
	{
		int tableNum, quantity;
		Order order;
		MenuItem item;
		int option = 0;
		do {
			System.out.print("====== RRPSS manage orders ======\n");
			System.out.print("1) Create order\n" + 
								"2) View order\n" + 
								"3) Add order item\n" + 
								"4) Remove order item\n" +
								"5) Return to RRPSS application main menu\n");
			System.out.print("Enter option number: ");
			option = sc.nextInt();
			sc.nextLine();
			switch (option)
			{
			case 1:
				// create
				Staff staff = inputStaff(sc);
				LocalDateTime now = LocalDateTime.now(); 
				Customer customer = inputCustomer(sc);
				int pax = inputPax(sc);
				tableNum = tableMgr.checkCurrentAvailability(pax);
				tableMgr.setTableAvailability(tableNum, false);
				orderMgr.createOrder(staff, now, tableNum, customer);
				break;
			case 2: 
				// view
				tableNum = inputTableNum(sc);
				order = orderMgr.getOrder(tableNum); 
				if(order == null) System.out.printf("No order for table number %d!\n", tableNum);
				else order.viewOrder();
				break;
			case 3:
				// add
				tableNum = inputTableNum(sc);
				order = orderMgr.getOrder(tableNum);
				if(order == null) System.out.printf("No order for table number %d!\n", tableNum);
				else{
					item = inputMenuItem(sc, true, true);
					quantity = inputQuantity(sc);	 
					order.addItem(item, quantity);
				}
				break;
			case 4:
				// remove
				tableNum = inputTableNum(sc);
				order = orderMgr.getOrder(tableNum);
				if(order == null) System.out.printf("No order for table number %d!\n", tableNum);
				else{
					item = inputMenuItem(sc, true, true);
					quantity = inputQuantity(sc);	 
					order.removeItem(item, quantity);
				}
				break;
			case 5:
				// back to main
				System.out.printf("Returning to RRPSS main menu...\n");
				break;
			default:
				// invalid input
				System.out.print("Invalid option. Please enter option 1-5.\n");
			}
		} while (option != 5);
	}
	/**
	 * Helper function for runRRPSS(), 
	 * which displays options to manage reservations using ReservationManager, 
	 * and executes the appropriate functions based on user inputs.
	 * @param sc 	Scanner object to request inputs
	 */
	private void reservationHelper(Scanner sc) 
	{
		LocalDateTime dateTime;
		String custName;
		int option = 0;		
		do 
		{
			System.out.print("====== RRPSS manage reservations ======\n");
			System.out.print("1) Create reservation booking\n" + 
							"2) Check reservation booking\n" + 
							"3) Remove reservation booking\n" +
							"4) Return to RRPSS application main menu\n");
			System.out.print("Enter option number: \n");
			option = sc.nextInt();
			sc.nextLine();
			switch (option) 
			{
			case 1: 
				// create
				dateTime = inputDateTime(sc);
				int pax = inputPax(sc); 
				Customer customer = inputCustomer(sc);
				int tableNum = tableMgr.checkFutureAvailability(pax, dateTime, resMgr);
				resMgr.createReservation(dateTime, pax, customer, tableNum);
				break;
			case 2:
				// check
				System.out.print("Enter customer name: \n");
				custName = sc.nextLine();
				dateTime = inputDateTime(sc);
				resMgr.checkReservation(custName, dateTime);
				break;
			case 3: 
				// remove
				System.out.print("Enter customer name: \n");
				custName = sc.nextLine();
				dateTime = inputDateTime(sc);
				if (resMgr.removeReservation(custName, dateTime))
					System.out.println("SUCCESS: Reservation has been removed.");
				else
					System.out.println("FAILURE: No such reservation found.");
				break;
			case 4:
				// back to main
				System.out.printf("Returning to RRPSS main menu...\n");
				break;
			default:
				// invalid input
				System.out.print("Invalid option. Please enter option 1-4.\n");
			};
		} while (option != 4);
	}
	/**
	 * Function to run Restaurant Reservation and Point of Sale System (RRPSS). 
	 * Displays main menu options to manage restaurant, 
	 * and executes the appropriate functions based on user inputs.
	 */
	public void runRRPSS() 
	{
		Scanner sc = new Scanner(System.in); 
		int option = 0;
		do {
			// update reservations
			resMgr.deleteInvalidReservations(tableMgr);
			tableMgr.setReservedTablesOccupied(resMgr);

			// main menu and options
			System.out.print("====== RRPSS main menu ======\n");
			System.out.print("1) Manage ala carte items\n" + 
							"2) Manage promotions\n" + 
							"3) Manage orders\n" +
							"4) Manage reservation bookings\n" +
							"5) Check table availability\n" +
							"6) Print order invoice\n" +
							"7) Print sales revenue report by period\n" +
							"8) Quit RRPSS application\n");
			System.out.print("Enter option number: ");
			option = sc.nextInt();
			sc.nextLine();
			switch (option) 
			{
			case 1:
				// ala carte items
				alaCarteHelper(sc);
				break;
			case 2:
				// promotions
				promotionHelper(sc);
				break;
			case 3: 
				// orders
				orderHelper(sc);
				break;
			case 4:
				// reservation bookings
				reservationHelper(sc);
				break;
			case 5: 
				// table availability
				int subOption = 0;
				do 
				{
					System.out.print("====== RRPSS table availability ======\n");
					System.out.print("1) Check current availability \n" + 
									"2) Check future availability");
					subOption = sc.nextInt();
					sc.nextLine();
					if (subOption == 1 || subOption == 2)
						break;
					else
						System.out.println("Invalid input. Please enter 1 or 2.");
				} while (true);
				int pax = inputPax(sc);
				int table = -1;
				switch (subOption)
				{
				case 1:
					table = tableMgr.checkCurrentAvailability(pax);
					break;
				case 2:
					LocalDateTime dateTime = inputDateTime(sc);
					table = tableMgr.checkFutureAvailability(pax, dateTime, resMgr);
				}
				if (table != -1)
					System.out.printf("Table for %d AVAILABLE\n", pax);
				else
					System.out.printf("Table for %d NOT AVAILABLE\n", pax);
				break;
			case 6:
				// order invoice
				int tableNum;
				Order order; 				
				
				tableNum = inputTableNum(sc);
				order = orderMgr.getOrder(tableNum);
				if (order != null){
					order.printOrderInvoice();
					tableMgr.setTableAvailability(tableNum, true);
				}
				else
					System.out.printf("Invalid input. No existing order for table %d. \n", tableNum);
				break;
			case 7: 
				// sale revenue report
				System.out.print("REPORT PERIOD START: \n");
				LocalDate startDate = inputDate(sc);
				System.out.print("REPORT PERIOD END: \n");
				LocalDate endDate = inputDate(sc);
				orderMgr.generateSalesRevenueReport(startDate, endDate); 
				break;
			case 8:
				// quit
				System.out.printf("Closing RRPSS...\n");
				break;
			default:
				// invalid input
				System.out.print("Invalid option. Please enter option 1-8.\n");
			};
		} while (option != 8);
		sc.close();
	}
}