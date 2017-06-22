package cecs.pkg323.java.term.project;
import java.sql.*;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * The Java Database program
 * CECS 323, Section 01
 * @author Caitlin Rubia and Crystal Chun
 */
public class CECS323JavaTermProject 
{ 
    //Database credentials
    static String USER;
    static String PASS;
    static String DBNAME;

    //JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.apache.derby.jdbc.ClientDriver";
    static String DB_URL = "jdbc:derby://localhost:1527/";
    
    /**
     * Takes the input string and outputs "N/A" if the string is empty or null.
     * @param input The string to be mapped.
     * @return  Either the input string or "N/A" as appropriate.
    */
    public static String dispNull (String input) 
    {
        //because of short circuiting, if it's null, it never checks the length.
        if (input == null || input.length() == 0)
            return "N/A";
        else
            return input;
    }
    
    public static void main(String[] args) 
    {
        Scanner in = new Scanner(System.in);

        Connection conn = null; //initialize the connection
        PreparedStatement statement = null;  //initialize the statement that we're using
        
        boolean notConnected = true;
        boolean inProgram = false;
        do
        {
            try 
            {
                //Prompt the user for the database name, and the credentials.
                System.out.print("Name of the database (not the user account): ");
                DBNAME = in.nextLine();
                
                //Gets credentials 
                System.out.print("Database user name: ");
                USER = in.nextLine();
                System.out.print("Database password: ");
                PASS = in.nextLine();
                
                //Constructing the database URL connection string
                DB_URL = "jdbc:derby://localhost:1527/";
                DB_URL = DB_URL + DBNAME + ";user=" + USER +";password=" + PASS;

                //STEP 2: Register JDBC driver
                Class.forName("org.apache.derby.jdbc.ClientDriver");

                //STEP 3: Open a connection
                System.out.println("Connecting to database...\r\n");
                conn = DriverManager.getConnection(DB_URL);

                notConnected = false;
                
                System.out.println("Connected to database!\r\n");
                
                //Tests if there are all three tables in database
                statement = conn.prepareStatement("SELECT * FROM WritingGroup NATURAL JOIN book NATURAL JOIN publisher");
                statement.executeQuery();
                
                inProgram = true;
                //Loop while the user is still using the program
                while(inProgram)
                {
                    //Display Menu
                    menu();

                    //Handle user input
                    int menuChoice = inputInt(1, 10);
                    
                    //Tests if user wants to quit
                    if(menuChoice == 10)
                    {
                        System.out.println("Exiting . . .");
                        inProgram = false;
                    }
                    else
                    {
                        //User is in program and chose a menu option
                        statement = null;
                        getQuery(menuChoice, statement, conn);
                    } 
                }
            } 
            catch (SQLException se) 
            {
                if(notConnected && !se.getMessage().contains("Userid"))
                {
                    //Handle errors for JDBC connection (database with the wrong name)
                    System.out.println("Error!! Could not connect to database because the database " 
                        + DBNAME +  " does not exist!"
                        + "\r\n\r\nPlease try entering the database name again. \r\n *HINT: Database name is case sensitive.* \r\n\r\n");
                }
                else if(notConnected && se.getMessage().contains("Userid"))
                {
                    //Handle errors for invalid credentials (either username or password is wrong)
                    System.out.println("Error! Either the username or the password was invalid. Please try entering your credentials again. "
                            + "\r\n *HINT: Both username and password are case sensitive.* \r\n\r\n");            
                }
                else if(!inProgram && se.getMessage().contains("does not exist."))
                {
                    //Handles when there are no tables in the database
                    System.out.println("ERROR: There are no tables in the database.");
                }
                else if(inProgram)
                {
                    se.printStackTrace();
                }
                //se.printStackTrace();
            }
            catch (Exception e) 
            {
                //Handle errors for Class.forName
                //Not part of connecting to database error
                e.printStackTrace();
            }
            finally 
            {
                //finally block used to close resources
                try 
                {
                    if (statement != null) 
                    {
                        statement.close();
                    }
                }
                catch (SQLException se2) 
                {
                    
                }// nothing we can do
                try 
                {
                    if (conn != null) 
                    {
                        conn.close();
                    }
                }
                catch (SQLException se) 
                {
                    
                    se.printStackTrace();
                }
            }
        } while(notConnected);
        System.out.println("Goodbye!");
    }
    
    /**
     * Displays the main menu for the user to choose what to do in the database.
     */
    public static void menu()
    {
        System.out.println("What would you like to do?");
        System.out.println(" 1. List all writing groups"
                        + "\r\n 2. List all data for a specific group"
                        + "\r\n 3. List all publishers"
                        + "\r\n 4. List all the data for a publisher"
                        + "\r\n 5. List all book titles"
                        + "\r\n 6. List all the data for a book"
                        + "\r\n 7. Insert a new book "
                        + "\r\n 8. Insert a new publisher"
                        + "\r\n 9. Remove a book"
                        + "\r\n 10. Exit");
    }
    
    /**
     * Gets the specified query.
     * @param choice The menu choice
     * @param statement The statement 
     * @param conn The connection to the database system
     * @return The prepared statement from the database
     * @throws SQLException Something went wrong when executing a query
     */
    public static PreparedStatement getQuery(int choice, PreparedStatement statement, Connection conn) throws SQLException
    {
        //Creates prepared statement and executes functions based on what the user chose to do
        switch(choice)
        {
            //List all writing group names
            case 1:     statement = conn.prepareStatement("SELECT groupName as \"Group Name\" FROM writingGroup");
                        executeQuery(statement);
                        break;
            //List all the data for a specific group that's specified by the user
            case 2:
                        //Executes first statement to show all the groups
                        statement = conn.prepareStatement("SELECT groupName as \"Group Name\" FROM writingGroup");
                        
                        ArrayList <String> writing_group_results = executeQuery(statement);
                        
                        //Checks to see if there's actually anything to choose from
                        if(writing_group_results.size() < 1)
                        {
                            System.out.println("No groups to select from.\r\n");
                        }
                        else
                        {
                            //Ask user which group they want to see
                            System.out.print("Which group's data would you like to see? ");
                            int userChoice = inputInt(1, writing_group_results.size());
                            System.out.println();
                            
                            //Creates the query statement and executes it
                            String group = writing_group_results.get(userChoice - 1);
                            group = fixString(group);
                            statement = conn.prepareStatement("SELECT groupName as \"Group Name\", headWriter as \"Head Writer\", yearFormed as \"Year Formed\", subject as \"Subject\" "
                                    + "FROM writingGroup WHERE groupName = " + group);
                            
                            executeQuery(statement);
                        }
                        break;
            //List all publishers
            case 3:
                        statement = conn.prepareStatement("SELECT publisherName as \"Publisher Name\" FROM publisher");
                        executeQuery(statement);
                        break;
            //List all the data for a publisher specified by a user
            case 4:     
                        //Lists all publishers for the user to see
                        statement = conn.prepareStatement("SELECT publisherName as \"Publisher Name\" FROM publisher");
                        ArrayList <String> publisherResults = executeQuery(statement);
               
                        //Checks to see if there's actually publishers to choose from
                        if(publisherResults.size() < 1)
                        {
                            System.out.println("No publishers to choose from. \r\n");
                            
                        }
                        else
                        {
                            //Asks user which publisher's information they want to see
                            System.out.print("Which publisher's data would you like to see? ");
                            int publisherChoice = inputInt(1, publisherResults.size());
                            System.out.println();
                            
                            //Creates the query statement and executes it
                            String publisher = publisherResults.get(publisherChoice - 1);
                            publisher = fixString(publisher);
                            statement = conn.prepareStatement("SELECT publisherName as \"Publisher Name\", publisherAddress as \"Address\", publisherPhone as \"Phone Number\","
                                    + " publisherEmail as \"Email\" FROM publisher WHERE publisherName = " + publisher);
                            executeQuery(statement);
                        }
                        break;
            //List all book titles
            case 5:
                        statement = conn.prepareStatement("SELECT bookTitle as \"Title\", groupName as \"Writing Group\" FROM book");
                        executeQuery(statement);
                        break;
            //List all the data for a book specified by the user
            case 6:
                        //Lists all books for the user to see
                        statement = conn.prepareStatement("SELECT bookTitle as \"Title\", groupName as \"Writing Group\" FROM book");
                        ArrayList <String> bookResults = executeQuery(statement);
                        
                        //Check if there are actually books to choose from
                        if(bookResults.size() < 1)
                        {
                            System.out.println("There are no books to choose from. Returning to main menu . . .\r\n");
                        }
                        else
                        {
                            //Asks user which book's information they want to see
                            System.out.print("Which book's data would you like to see? ");
                            int bookChoice = inputInt(1, bookResults.size());
                            System.out.println();
                            
                            //Creates the query statement and executes it
                            String book = bookResults.get((bookChoice * 2) - 2);
                            book = fixString(book);
                            String writer = bookResults.get((bookChoice * 2) - 1);
                            writer = fixString(writer);
                         
                            try
                            {
                               statement = conn.prepareStatement("SELECT bookTitle as \"Title\", yearPublished as \"Year Published\", numberPages as \"Number of Pages\", subject as \"Subject\", "
                                    + "groupName as \"Writing Group\", headWriter as \"Head Writer\", yearFormed as \"Year Writing Group Formed\", "
                                    + "publisherName as \"Publisher\", publisherAddress as \"Publisher Address\", publisherPhone as \"Phone\", publisherEmail as \"Publisher Email\""
                                    + "FROM book INNER JOIN writingGroup USING (groupName) INNER JOIN publisher USING (publisherName) WHERE bookTitle = " + book + " and groupName = " + writer);
                                executeQuery(statement); 
                            }
                            catch(SQLException e)
                            {
                               System.out.println("Something went wrong with retrieving the book's information. Returning back to main menu.");
                            }     
                        }
                        break;
            //Insert a new book            
            case 7:
                        getBookInfo(statement, conn); 
                        break;
            //Insert a new publisher and update a publisher to this new publisher         
            case 8:
                        //Adding a publisher and updating another publisher to this new one
                        addPublisher(statement, conn);
                        break;
            //Remove a book
            case 9:
                        //Lists all books for user to choose from
                        statement = conn.prepareStatement("SELECT bookTitle as \"Book Title\", groupName as \"Writing Group\" FROM book");
                        ArrayList <String> allBooks = executeQuery(statement);
                        
                        if(allBooks.size() < 1)
                        {
                            System.out.println("Sorry, there are no books to remove.\r\n");
                        }
                        else
                        {
                            //Asks the user which book they want to remove
                            System.out.print("Which book would you like to remove? ");
                            int removeBookChoice = inputInt(1, allBooks.size());
                            System.out.println();
                            
                            //Gets the name of the book and the writing group to remove it
                            String removeBook = allBooks.get((removeBookChoice * 2) - 2);
                            String writingGroup = allBooks.get((removeBookChoice * 2) - 1);
                            String fixed_removeBook = fixString(removeBook);
                            writingGroup = fixString(writingGroup);
                            
                            //Executes the statement to delte the book from the database
                            statement = conn.prepareStatement("DELETE FROM book WHERE bookTitle = " + fixed_removeBook + " and groupName = " + writingGroup);
                            statement.executeUpdate();
                            System.out.println(removeBook + " has been successfully removed!\r\n");
                        }
                        break;
        }
        return statement;
    }
    
    /**
     * Executes a full query and displays the results.
     * @param statement The statement to be executed
     * @return An array list of all of the rows (tuples) of the query
     * @throws SQLException Executing a query went wrong
     */
    public static ArrayList <String> executeQuery(PreparedStatement statement) throws SQLException
    {
        ArrayList <String> row = new ArrayList <String>();
        
        //Gets result set which is all of the rows
        ResultSet rs = statement.executeQuery();
        
        //Gets result set meta data which is all of the columns
        ResultSetMetaData rsmd = statement.getMetaData();
        
        int number_columns = rsmd.getColumnCount();
        
        String [] columns = new String [number_columns];
        String [] columnFormat = new String [number_columns];
   
        if(rs.next())
        {
            //Gets columns and displays column names
            for(int i = 1; i <= number_columns; i++)
            {
                String columnName = rsmd.getColumnName(i);
                columns[i-1] = columnName;

                //Formats the output for each column
                if(i == 1)
                {
                    //The first column to be displayed, has extra padding to make sure there's room for the number that appears by the rows
                    columnFormat[i-1] = " %-" + (rsmd.getColumnDisplaySize(i)) + "s";
                    System.out.printf("%-3s" + columnFormat[i-1], "", columnName); 
                }
                else
                {
                    if(rsmd.getColumnTypeName(i).contains("INT"))
                    {
                        //The column is an integer data type, which requires extra padding for the column
                        columnFormat[i-1] = "%-" + (columnName.length() + 5) + "s";
                    }
                    else
                    {
                        //Gets the columns max width
                        columnFormat[i-1] = "%-" + (rsmd.getColumnDisplaySize(i)) + "s";
                    }

                    System.out.printf(columnFormat[i-1], columnName);
                }    
            }
            System.out.println();  
            
            //Gets rows and displays them
            int rowNum = 1;
            
            do
            {   
                System.out.print(rowNum + ".");

                //Goes through each column and displays the value that's in that column in this row
                for(int i = 0; i < columns.length; i++)
                {
                    String data = rs.getString(columns[i]);
                    row.add(data);

                    //Formats output 
                    if(rowNum > 9)
                    {
                        System.out.printf(columnFormat[i], dispNull(data));
                    }
                    else
                    {
                        System.out.printf(columnFormat[i], " " + dispNull(data));
                    }
                }
                System.out.println();
                rowNum ++;
            } while(rs.next());
        
            System.out.println();
        }
        else
        {
           System.out.println("There's no data in this table to be displayed.\r\n");
        }
        rs.close();
        return row;
    }
    
    /**
     * Gets all the information for a book and inserts into the database
     * @param statement The statement to insert into the database
     * @param conn The connection to the database
     * @throws SQLException The exception that happens if executing a statement goes wrong
     */
    public static void getBookInfo(PreparedStatement statement, Connection conn) throws SQLException
    {
        
        //Asks for book's title, publish year, and number of pages
        System.out.print("What is the book's title? ");
        String bookTitle = stringInput(1, 50);
        bookTitle = fixUserInput(bookTitle);
        String fixed_bookTitle = fixString(bookTitle);
        System.out.println();
        
        //Checks if book already exists in the database
        statement = conn.prepareStatement("SELECT bookTitle, groupName, publisherName FROM Book WHERE bookTitle = " + fixed_bookTitle);
        ResultSet results = statement.executeQuery();
        
        ArrayList <String> existingGroups = new ArrayList <String> ();
        ArrayList <String> existingPublishers = new ArrayList <String> ();
        
        while(results.next())
        {
            //Book exists in database and has a writing group associated with it so grabs the writing group and adds to list
            existingGroups.add(results.getString("groupName"));
            existingPublishers.add(results.getString("publisherName"));
        }
        
        //Gets the year the book was published and the number of pages the book has
        System.out.print("What year was this book published? ");
        int year = inputInt(0, 3000);
        System.out.println();
        
        System.out.print("How many pages does this book have? ");
        int pages = inputInt(0, 2147483647);
        System.out.println();
        
        
        String publisher = "";
        String group = "";
        
        //Choosing a writing group for this book
        boolean selectingGroup = true;
        while(selectingGroup)
        {
            //Executes first statement to show all the writing groups
            statement = conn.prepareStatement("SELECT groupName as \"Writing Group\" FROM writingGroup");
            
            ArrayList <String> writing_group_results = executeQuery(statement);
            
            //Checks to see if there's actually anything to choose from
            if(writing_group_results.size() < 1)
            {
                System.out.println("There are no groups to select from, and a book cannot be placed into the database without an existing writing group.");
                System.out.println("Going back to main menu. . .");
                selectingGroup = false;
            }
            else
            {
                //Ask user which group they want to select
                System.out.print("Which writing group wrote this book? ");
                int userChoice = inputInt(1, writing_group_results.size());
                System.out.println();
                
                if(existingGroups.size() < 1)
                {
                    //Gets the name of the writing group chosen by the user since this book isn't already in the database
                    group = writing_group_results.get(userChoice - 1);
                    group = fixString(group);
                    selectingGroup = false;
                }
                else
                {
                    boolean violatesConstraint = false;
                    group = writing_group_results.get(userChoice - 1);
 
                    //Runs through all of the groups and checks to see if the group chosen matches an existing one
                    for(String prev_group: existingGroups)
                    {
                        if(group.equals(prev_group))
                        {
                            violatesConstraint = true;
                            break;
                        }
                    }
                    
                    if(violatesConstraint)
                    {
                        //User is violating primary key constraint where two books with the same title have the same writing group
                        System.out.println("Error! " + bookTitle
                                + " has already been written by " + group + "."
                                + "\r\n\r\nWould you like to quit adding a book or choose another writing group?"
                                + "\r\n 1. Quit inserting book"
                                + "\r\n 2. Choose another writing group");
                        int leaving = inputInt(1,2);
                        
                        //User wants to quit inserting a book
                        if(leaving == 1)
                        {
                            selectingGroup = false;
                            group = "";
                        }
                    }
                    else
                    {
                        //The book being added has a different writing group from the one already in there
                        group = fixString(group);
                        selectingGroup = false;
                    }
                } 
            }
        }

        //Checks to see if the user chose a group first
        if(!group.isEmpty())
        {
            boolean selectingPublisher = true;
            while(selectingPublisher)
            {
                //Lists all publishers for the user to see
                statement = conn.prepareStatement("SELECT publisherName as \"Publisher\" FROM publisher");
                ArrayList <String> publisherResults = executeQuery(statement);

                //Checks to see if there's actually publishers to choose from
                if(publisherResults.size() < 1)
                {
                    System.out.println("There are no publishers to choose from, and a book cannot be inserted without an existing publisher.");
                    System.out.println("Going back to main menu . . .\r\n");
                    selectingPublisher = false;
                }
                else
                {
                    //Asks user which publisher they'd like to use for this book
                    System.out.print("Who was the publisher of this book? ");
                    int publisherChoice = inputInt(1, publisherResults.size());
                    System.out.println();
                    
                    //Gets publisher name 
                    publisher = publisherResults.get(publisherChoice - 1);
                    
                    if(existingPublishers.size() < 1)
                    {
                        //Fixes the string and exits the loop
                        publisher = fixString(publisher);
                        selectingPublisher = false;
                    }
                    else
                    {
                        boolean violatesConstraint = false;
                        
                        //Runs through the existing group of publishers to see if chosen publisher matches existing one
                        for(String publisherName : existingPublishers)
                        {
                            if(publisherName.equals(publisher))
                            {
                                violatesConstraint = true;
                                break;
                            }
                        }
                        
                        if(violatesConstraint)
                        {
                            //This insert violates uniqueness constraint on publisher and book title
                            System.out.println("Error! " + bookTitle
                                + " has already been published by " + publisher + "."
                                + "\r\n\r\nWould you like to quit adding a book or choose publisher?"
                                + "\r\n 1. Quit inserting book"
                                + "\r\n 2. Choose another publisher");
                            int leaving = inputInt(1,2);

                            //User wants to quit inserting a book
                            if(leaving == 1)
                            {
                                selectingPublisher = false;
                                publisher = "";
                            }
                            
                        }
                        else
                        {
                            //The user chose a publisher that doesn't have this book title 
                            publisher = fixString(publisher);
                            selectingPublisher = false;
                        }
                    }    
                }
            }   
        }
        
        //Checks to make sure that there is a publisher and a group entered
        if(!group.isEmpty() && !publisher.isEmpty())
        {
            try
            {
                //Creates statement and executes 
                statement = conn.prepareStatement("INSERT INTO Book "
                        + "(BookTitle, YearPublished, NumberPages, GroupName, PublisherName)"
                        + "VALUES (" + fixed_bookTitle + ", " + year + ", " + pages + ", "  + group + ", " + publisher + ")");

                statement.executeUpdate();
            }
            catch(SQLException e)
            {
                System.out.println("There was something wrong with inserting " + bookTitle + " into the database. Returning to main menu. ");
                e.printStackTrace();
            }
            System.out.println(bookTitle + " was inserted successfully into the database! \r\n");   
        }
    }

    /**
     * Method for adding a publisher and updating one publisher to this new publisher
     * @param statement The statement for executing 
     * @param conn The connection to the database
     * @throws SQLException Something went wrong with executing the query or executing an update/insert
     */
    public static void addPublisher(PreparedStatement statement, Connection conn) throws SQLException
    {
        boolean gettingPublisherName = true;
        String publisherName = "";
        String fixed_publisherName = "";
        int addDiff = 0;
        while(gettingPublisherName)
        {
            addDiff = 0;
            
            //Gets the name for a publisher
            System.out.print("What is the name of this publisher? ");
            publisherName = stringInput(1, 30);
            publisherName = fixUserInput(publisherName);
            fixed_publisherName = fixString(publisherName);
            System.out.println();
            
            //Checks if publisher already exists in table
            statement = conn.prepareStatement("SELECT publisherName FROM publisher WHERE publisherName = " + fixed_publisherName);
            ResultSet results = statement.executeQuery();
            
            if(!results.next())
            {
                //If this publisher doesn't exist, exits loop and continues adding the new publisher
                gettingPublisherName = false;
            }
            else
            {
                //Tells user the publisher already exists in the system and prompts the user if they want to continue
                System.out.println("Sorry, " + publisherName + " already exists in the table. "
                        + "\r\n\r\nWould you like to add a different publisher?"
                        + "\r\n1. Yes"
                        + "\r\n2. No");
                addDiff = inputInt(1,2);
                
                if(addDiff == 2)
                {
                    //Exit loop
                    gettingPublisherName = false;
                }
                
            }
        }
        
        if(addDiff != 2)
        {
            //The user selected entered a valid publisher name, prompts for the rest of the publisher's info
            System.out.print("What is this publisher's address? ");
            String publisherAddress = stringInput(1,50);
            publisherAddress = fixString(publisherAddress);
            System.out.println();
        
            //Probably want a way to verify this information is correct
            System.out.print("What is this publisher's phone number? ");
            String publisherNumber = stringInput(0, 15);
            publisherNumber = fixString(publisherNumber);
            System.out.println();
        
            System.out.print("What is this publisher's email address? ");
            String publisherEmail = stringInput(0, 40);
            publisherEmail = fixString(publisherEmail);
            System.out.println();

            //Creates and executes the statement to insert a new publisher
            statement = conn.prepareStatement("INSERT INTO Publisher "
                    + "(publisherName, publisherAddress, publisherPhone, publisherEmail)"
                    + "VALUES (" + fixed_publisherName + ", " + publisherAddress + ", " 
                    + publisherNumber + ", " + publisherEmail + ")" );

            statement.executeUpdate();
            System.out.println(publisherName+ " has been inserted successfully into the database!\r\n");

            //Updating publisher
            //Lists all publishers for the user to see
            statement = conn.prepareStatement("SELECT publisherName as \"Publisher\" FROM publisher");

            ArrayList <String> publisherResults = executeQuery(statement);
            String publisher = ""; 
            String publisherUpdate = "";
            //Checks to see if there's actually publishers to choose from
            if(publisherResults.size() <= 1)
            {
                System.out.println("There are no publishers to choose from.");
                System.out.println("Going back to main menu . . .");            
            }
            else
            {
                //Loops through while user hasn't chosen a valid publisher to update to this new publisher
                boolean choosingPublisher = true;
                while(choosingPublisher)
                {
                   //Asks user which publisher they'd like to update to this new publisher
                    System.out.println("Which publisher would you like to update to this new publisher?");
                    int publisherChoice = inputInt(1, publisherResults.size());

                    //Gets publisher name
                    publisher = publisherResults.get(publisherChoice - 1);
                    publisherUpdate = fixString(publisher); 

                    if(fixed_publisherName.equals(publisherUpdate))
                    {
                        //This is the publisher the user just inserted
                        System.out.println("Sorry, you can't update a publisher that you just inserted! Choose a different publisher.\r\n");
                    }
                    else
                    {
                        choosingPublisher = false;
                    }
                }
            }
            
            //Updates books to the new publisher
            statement = conn.prepareStatement("UPDATE Book SET publisherName = " + fixed_publisherName + " WHERE publisherName = " + publisherUpdate);
            statement.executeUpdate();
            
            System.out.println("Update successful! " + publisherName
                    + " is now the publisher for all of the books that were published by " + publisher + ".\r\n" );
        }
    }
    
    /**
     * Gets an integer input from the user within a specified boundary.
     * @param lowerBound The lowest value the user may enter
     * @param upperBound The highest value the user may enter
     * @return A valid integer input
     */
    public static int inputInt(int lowerBound, int upperBound)
    {
        Scanner in = new Scanner(System.in);
        int input = 0;
        boolean gettingInput = true;

        //Loops through while user doesn't enter the correct input
        while(gettingInput)
        {
            if(in.hasNextInt())
            {
                //Correct input type
                input = in.nextInt();

                if(input < lowerBound | input > upperBound)
                {
                    //Input is out of range
                    System.out.println("This is not a valid choice, try again.");
                }
                else
                {
                    //Input is in the correct range, exits loop
                    gettingInput = false;
                }
            }
            else
            {
                //Invalid input entered
                System.out.println("Invalid input type, try again.");
                in.next();
            }
        }
        return input;
    }

    /**
     * Makes sure the string entered by the user is correct
     * @param minLength The minimum length the string is allowed to be, 1 if mandatory
     * @param maxLength The maximum length the string is allowed to be
     * @return A correct input from the user
     */
    public static String stringInput(int minLength, int maxLength)
    {
        Scanner in = new Scanner(System.in);
        String input = "";
        boolean gettingInput = true;

        while(gettingInput)
        {
            input = in.nextLine(); 
            input = input.trim();
            
            //If the user entered an input that's the correct length, exits the loop
            if(input.length() >= minLength && input.length() <= maxLength)
            {
                gettingInput = false;
            }
            else if(input.length() < minLength)
            {
                System.out.println("Error! This field must have a value! Please enter a value.");
                input = "";
            }
            else
            {
                System.out.println("Error! You've exceeded the maximum length for this attribute, which is " 
                        + maxLength + " characters long. Please try again.");
                input = "";
            }
        }

        return input;
    }

    /**
     * Fixes the string entered by checking if the string contains an apostrophe, and then adding an apostrophe to surround 
     * the whole string for SQL statements.
     * @param value The string to be fixed
     * @return The fixed string
     */
    public static String fixString(String value)
    {
        //Tests if the string contains an apostrophe
        if(value.contains("\'"))
        {
            //If an apostrophe exists in the string, gets where it is and adds another apostrophe
            int index = value.indexOf('\'');

            value = value.substring(0,index) + "\'" + value.substring(index);                
        }
        
        //Adds apostrophes around the value for the SQL statement
        value = "\'" + value + "\'";
        return value;
    }

    /**
     * Fixes the input the user enters to match the way the strings in the database are stored
     * @param input The input the user entered
     * @return The corrected string
     */
    public static String fixUserInput(String input)
    {
        char [] array = input.toCharArray();

        //Capitalize first letter
        input = input.substring(0,1).toUpperCase() + input.substring(1).toLowerCase();

        //Goes through all the characters in the string
        for(int i = 0; i < array.length; i++)
        {
            //If there is a white space, capitalizes the letter right after the white space
            if(array[i] == ' ' && (i + 1) != array.length)
            {
                input = input.substring(0,i) + input.substring(i, i + 2).toUpperCase() + input.substring(i + 2).toLowerCase();
            }
        }

        //Fixes the excess spaces on a string
        input = input.trim().replaceAll(" +", " ");

        return input;
    }
}

