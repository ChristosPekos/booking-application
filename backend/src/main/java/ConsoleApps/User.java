package ConsoleApps;

import Data.Booking;
import Data.Filter;
import Data.Room;
import java.io.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.net.Socket;

/**
 * Class for the console app of the User
 */
public class User {

    private ArrayList<Room> roomsRented ;
    private final ArrayList<Room> roomsReviewed;
    private ArrayList<Room> roomsToRent = new ArrayList<>();

    public User() {
        this.roomsRented = new ArrayList<>();
        this.roomsReviewed = new ArrayList<>();
    }

    public void search() {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Please filter your options:");

            System.out.println("Type the area of interest: ");
            String area = scanner.nextLine();

            LocalDate currentdate = LocalDate.now();
            System.out.println("Please choose the start date of availability");

            System.out.print("Year: ");
            int startYear = scanner.nextInt();

            System.out.print("Month: ");
            int startMonth = scanner.nextInt();

            System.out.print("Day: ");
            int startDay = scanner.nextInt();

            boolean illegalDate = false;
            boolean dateIsBeforeCurrent = false;
            LocalDate startDate = null;
            try {
                startDate = LocalDate.of(startYear, startMonth, startDay);
                if (startDate.isBefore(currentdate)) {
                    dateIsBeforeCurrent = true;
                }
            } catch (DateTimeException | IllegalArgumentException e) {
                illegalDate = true;
            }

            System.out.println("Please choose the last date of availability");

            System.out.print("Year: ");
            int endYear = scanner.nextInt();


            System.out.print("Month: ");
            int endMonth = scanner.nextInt();

            System.out.print("Day: ");
            int endDay = scanner.nextInt();

            LocalDate endDate = null;
            try {
                endDate = LocalDate.of(endYear, endMonth, endDay);
                if (endDate.isBefore(currentdate)) {
                    dateIsBeforeCurrent = true;
                }
            } catch (DateTimeException | IllegalArgumentException e) {
                illegalDate = true;
            }

            while (startDate.isAfter(endDate) || dateIsBeforeCurrent || illegalDate) {
                System.out.println("Invalid date(s). Please enter the correct details again");
                System.out.println("Please choose the date of arrival");

                System.out.print("Year: ");
                startYear = scanner.nextInt();

                System.out.print("Month: ");
                startMonth = scanner.nextInt();

                System.out.print("Day: ");
                startDay = scanner.nextInt();

                illegalDate = false;
                dateIsBeforeCurrent = false;
                try {
                    startDate = LocalDate.of(startYear, startMonth, startDay);
                    if (startDate.isBefore(currentdate)) {
                        dateIsBeforeCurrent = true;
                    }
                } catch (DateTimeException | IllegalArgumentException e) {
                    illegalDate = true;
                }

                System.out.println("Please choose the date of departure");

                System.out.print("Year: ");
                endYear = scanner.nextInt();

                System.out.print("Month: ");
                endMonth = scanner.nextInt();

                System.out.print("Day: ");
                endDay = scanner.nextInt();

                try {
                    endDate = LocalDate.of(endYear, endMonth, endDay);
                    if (endDate.isBefore(currentdate)) {
                        dateIsBeforeCurrent = true;
                    }
                } catch (DateTimeException | IllegalArgumentException e) {
                    illegalDate = true;
                }
            }

            System.out.println("Please type the number of people staying. If the number of people staying isn't a necessary filter, press 0");
            int noOfPeople = scanner.nextInt();

            scanner.nextLine();
            System.out.println("Do you want to filter by price? Y/N");
            String filterByPrice = scanner.nextLine();

            while (!filterByPrice.equalsIgnoreCase("Y") && !filterByPrice.equalsIgnoreCase("N")) {
                System.out.println("Answer should be Y or N for Yes or No");
                System.out.println("Do you want to filter by price? Y/N");
                filterByPrice = scanner.nextLine();
            }
            int minPrice ;
            int maxPrice;
            if (filterByPrice.equalsIgnoreCase("Y")) {
                System.out.println("Please type the minimum price limit.");
                minPrice = scanner.nextInt();

                System.out.println("Please type the maximum price limit.");
                maxPrice = scanner.nextInt();
            } else {
                minPrice = 0;
                maxPrice = Integer.MAX_VALUE;
            }

            System.out.println("Please type the minimum stars. If you want no limit, please press 0");
            float stars = scanner.nextFloat();

            Filter filters = new Filter(area, startDate, endDate, noOfPeople, minPrice, maxPrice, stars);

            // get the ip and port of master from config file
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            String masterIP = appProps.getProperty("MASTER_IP");
            int masterPort = Integer.parseInt(appProps.getProperty("MASTER_PORT"));


            Socket socket = new Socket(masterIP, masterPort);

            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

            outputStream.writeUTF("ROOM_SEARCH");
            outputStream.flush();


            outputStream.writeObject(filters);
            outputStream.flush();

            this.roomsToRent = (ArrayList<Room>) inputStream.readObject();

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();

            socket.close();
            outputStream.close();
            inputStream.close();


            if (roomsToRent.isEmpty()) {
                System.out.println("No results for these filters");
            } else {
                System.out.println("Here are the available rooms");
                for (int i = 0; i <= roomsToRent.size() - 1; i++) {
                    System.out.println("----------------------------------------------");
                    System.out.println("Room " + i + 1);
                    System.out.println("----------------------------------------------");
                    System.out.println(roomsToRent.get(i).toString());
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }


    public void book() throws IOException {
        Socket socket = null;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;

        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println('\n' +"Please type the number of the room that you wish to rent :");
            int choice = scanner.nextInt();
            choice--;

            Room roomChosen = roomsToRent.get(choice);

            System.out.println("Please filter your options:");

            LocalDate currentdate = LocalDate.now();
            System.out.println("Please choose the date of arrival");

            System.out.print("Year: ");
            int startYear = scanner.nextInt();

            System.out.print("Month: ");
            int startMonth = scanner.nextInt();

            System.out.print("Day: ");
            int startDay = scanner.nextInt();

            boolean illegalDate = false;
            boolean dateIsBeforeCurrent = false;
            LocalDate startDate = null;
            try {
                startDate = LocalDate.of(startYear, startMonth, startDay);
                if (startDate.isBefore(currentdate)) {
                    dateIsBeforeCurrent = true;
                }
            } catch (DateTimeException | IllegalArgumentException e) {
                illegalDate = true;
            }

            System.out.println("Please choose the date of departure");

            System.out.print("Year: ");
            int endYear = scanner.nextInt();


            System.out.print("Month: ");
            int endMonth = scanner.nextInt();

            System.out.print("Day: ");
            int endDay = scanner.nextInt();

            LocalDate endDate = null;
            try {
                endDate = LocalDate.of(endYear, endMonth, endDay);
                if (endDate.isBefore(currentdate)) {
                    dateIsBeforeCurrent = true;
                }
            } catch (DateTimeException | IllegalArgumentException e) {
                illegalDate = true;
            }

            while (startDate.isAfter(endDate) || dateIsBeforeCurrent || illegalDate) {
                System.out.println("Invalid date(s). Please enter the correct details again");
                System.out.println("Please choose the date of arrival");

                System.out.print("Year: ");
                startYear = scanner.nextInt();

                System.out.print("Month: ");
                startMonth = scanner.nextInt();

                System.out.print("Day: ");
                startDay = scanner.nextInt();

                illegalDate = false;
                dateIsBeforeCurrent = false;
                try {
                    startDate = LocalDate.of(startYear, startMonth, startDay);
                    if (startDate.isBefore(currentdate)) {
                        dateIsBeforeCurrent = true;
                    }
                } catch (DateTimeException | IllegalArgumentException e) {
                    illegalDate = true;
                }

                System.out.println("Please choose the date of departure");

                System.out.print("Year: ");
                endYear = scanner.nextInt();

                System.out.print("Month: ");
                endMonth = scanner.nextInt();

                System.out.print("Day: ");
                endDay = scanner.nextInt();

                try {
                    endDate = LocalDate.of(endYear, endMonth, endDay);
                    if (endDate.isBefore(currentdate)) {
                        dateIsBeforeCurrent = true;
                    }
                } catch (DateTimeException | IllegalArgumentException e) {
                    illegalDate = true;
                }
            }

            Booking bookingDates = new Booking(startDate, endDate, roomChosen);

            // get the ip and port of master from config file
            Properties appProps = new Properties();
            appProps.load(new FileInputStream("././././config.properties"));
            String masterIP = appProps.getProperty("MASTER_IP");
            int masterPort = Integer.parseInt(appProps.getProperty("MASTER_PORT"));

            socket = new Socket(masterIP, masterPort);
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            outputStream.writeUTF("BOOK_ROOM");
            outputStream.flush();

            outputStream.writeObject(bookingDates);
            outputStream.flush();

            String confirmation = inputStream.readUTF();

            // Send acknowledgment back to the sender
            outputStream.writeUTF("ACK");
            outputStream.flush();

            if(confirmation.equals("The booking was successful."))
                this.roomsRented.add(roomChosen);

            System.out.println(confirmation);

        } catch (IOException e) {

            e.printStackTrace();

        } finally {
            try {
                socket.close();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public static void main(String[] args){

        User user = new User();

        System.out.println("Welcome to the User console app. Here are the functionalities");
        do{
            System.out.println("----------------------------------------------");
            System.out.println("1. Show rooms based on filters");
            System.out.println("2. Book a room");
            System.out.println("3. Review a room");
            System.out.println("4. Exit");
            System.out.println("----------------------------------------------");
            System.out.println("Select a functionality: ");

            Scanner scanner = new Scanner(System.in);
            int answer = scanner.nextInt();

            // Check answer validity
            while (answer < 1 || answer > 4) {
                System.out.println("Wrong input. The answer should be between 1 and 4. Enter again: ");
                answer = scanner.nextInt();
            }

            if(answer == 1) {
                user.search();
                user.roomsToRent.clear();
            } else if(answer == 2) {
                user.search();
                if(user.roomsToRent.isEmpty()){
                    System.out.println("No available rooms to book");
                }else {
                    try {
                        user.book();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    user.roomsToRent.clear();
                }

            }else if(answer==3){
                try {
                    // get the ip and port of master from config file
                    Properties appProps = new Properties();
                    appProps.load(new FileInputStream("././././config.properties"));
                    String masterIP = appProps.getProperty("MASTER_IP");
                    int masterPort = Integer.parseInt(appProps.getProperty("MASTER_PORT"));

                    Socket socket = new Socket(masterIP, masterPort);

                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                    if(user.roomsRented.size() == 0){
                        System.out.println("There is no room that you've booked to make a review for");
                        continue;
                    }
                    int i = 1;
                    for(Room room: user.roomsRented){
                        System.out.println("----------------------------------------------");
                        System.out.println("Room "+ i);
                        System.out.println("----------------------------------------------");
                        System.out.println(room.toString());
                        i++;
                    }
                    System.out.println('\n' + "Select a room to review: ");

                    answer = scanner.nextInt();

                    // Check answer validity
                    while (answer < 1 || answer > user.roomsRented.size()) {
                        System.out.println("Wrong input. The answer should be between 1 and 5. Enter again: ");
                        answer = scanner.nextInt();
                    }

                    answer = answer - 1;
                    Room selectedRoom = user.roomsRented.get(answer);

                    if(user.roomsReviewed.contains(selectedRoom)){
                        System.out.println("Room has already been reviewed");
                        continue;
                    }


                    System.out.println("Enter a review from 1 to 5: ");
                    int review = scanner.nextInt();

                    // Check answer validity
                    while (review < 1 || review > 5) {
                        System.out.println("Wrong input. The answer should be between 1 and 5. Enter again: ");
                        review = scanner.nextInt();
                    }

                    outputStream.writeUTF("ADD_REVIEW");
                    outputStream.writeObject(selectedRoom);
                    outputStream.flush();
                    outputStream.writeInt(review);
                    outputStream.flush();

                    // Wait for acknowledgment from worker
                    String acknowledgment = inputStream.readUTF();

                    user.roomsReviewed.add(selectedRoom);
                    System.out.println("Added review!");

                    socket.close();
                    outputStream.close();
                    inputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.exit(0);
                break;
            }
        }while (true);
    }
}
