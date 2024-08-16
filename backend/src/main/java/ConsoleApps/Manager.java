package ConsoleApps;

import Data.Booking;
import Data.Room;
import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

/**
 * Class for the console app of the Manager
 */
public class Manager {

    public static void main(String[] args){

        System.out.println("Welcome to the Manager console app. Here are the functionalities");
        do {
            System.out.println("----------------------------------------------");
            System.out.println("1. Add a room");
            System.out.println("2. Show bookings");
            System.out.println("3. Show my rooms");
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

            if (answer == 1) {

                System.out.println("The available rooms are the following:");
                File[] jsonFiles = new File("././././rooms").listFiles();

                for (int i = 0; i < jsonFiles.length; i++) {
                    System.out.println(i + 1 + ". " + jsonFiles[i].getName());
                }

                System.out.println("Select a room to add: ");

                answer = scanner.nextInt();

                // Check answer validity
                while (answer < 1 || answer > jsonFiles.length ) {
                    System.out.printf("Wrong input. The answer should be between %d and %d. Enter again: ", 1, jsonFiles.length );
                    answer = scanner.nextInt();
                }

                // selected json
                File roomJson = jsonFiles[answer - 1];

                System.out.println("Here are the available images:");
                File[] imageFiles = new File("././././images").listFiles();

                for (int i = 0; i < jsonFiles.length ; i++) {
                    System.out.println(i + 1 + ". " + imageFiles[i].getName());
                }
                System.out.println("Select image: ");

                answer = scanner.nextInt();
                scanner.nextLine();

                // Check answer validity
                while (answer < 1 || answer > imageFiles.length) {
                    System.out.printf("Wrong input. The answer should be between %d and %d. Enter again: ", 1, imageFiles.length );
                    answer = scanner.nextInt();
                }

                // selected image
                File imageFile = imageFiles[answer - 1];

                // send json object and image
                try {
                    // Read JSON file
                    BufferedReader reader = new BufferedReader(new FileReader(roomJson));
                    StringBuilder jsonString = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonString.append(line);
                    }
                    reader.close();

                    // Convert JSON string to JSONObject
                    JSONObject jsonObject = new JSONObject(jsonString.toString());

                    LocalDate currentDate = LocalDate.now();
                    System.out.println("Please choose the starting date of availability");

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
                        if (startDate.isBefore(currentDate)) {
                            dateIsBeforeCurrent = true;
                        }
                    } catch (DateTimeException | IllegalArgumentException e) {
                        illegalDate = true;
                    }


                    System.out.println("Please choose the ending date of availability");

                    System.out.print("Year: ");
                    int endYear = scanner.nextInt();


                    System.out.print("Month: ");
                    int endMonth = scanner.nextInt();

                    System.out.print("Day: ");
                    int endDay = scanner.nextInt();


                    LocalDate endDate = null;
                    try {
                        endDate = LocalDate.of(endYear, endMonth, endDay);
                        if (endDate.isBefore(currentDate)) {
                            dateIsBeforeCurrent = true;
                        }
                    } catch (DateTimeException | IllegalArgumentException e) {
                        illegalDate = true;
                    }

                    while (startDate.isAfter(endDate) || dateIsBeforeCurrent || illegalDate) {
                        System.out.println("Invalid date(s). Please enter the correct details again");
                        System.out.println("Please choose the starting date of availability");

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
                            if (startDate.isBefore(currentDate)) {
                                dateIsBeforeCurrent = true;
                            }
                        } catch (DateTimeException | IllegalArgumentException e) {
                            illegalDate = true;
                        }


                        System.out.println("Please choose the ending date of availability");

                        System.out.print("Year: ");
                        endYear = scanner.nextInt();


                        System.out.print("Month: ");
                        endMonth = scanner.nextInt();

                        System.out.print("Day: ");
                        endDay = scanner.nextInt();

                        try {
                            endDate = LocalDate.of(endYear, endMonth, endDay);
                            if (endDate.isBefore(currentDate)) {
                                dateIsBeforeCurrent = true;
                            }
                        } catch (DateTimeException | IllegalArgumentException e) {
                            illegalDate = true;
                        }
                    }

                    jsonObject.put("startDate", startDate);
                    jsonObject.put("endDate", endDate);


                    // get the ip of master from config file
                    Properties appProps = new Properties();
                    appProps.load(new FileInputStream("././././config.properties"));
                    String masterIP = appProps.getProperty("MASTER_IP");
                    int masterPort = Integer.parseInt(appProps.getProperty("MASTER_PORT"));

                    Socket socket = new Socket(masterIP, masterPort);
                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                    // Send JSONObject over the socket
                    outputStream.writeUTF("MANAGER/ADD_ROOM");
                    outputStream.flush();
                    outputStream.writeObject(jsonObject.toString());
                    outputStream.flush();

                    System.out.println("Room has been sent");

                    // Send room image
                    FileInputStream imageFileInputStream = new FileInputStream(imageFile.getAbsolutePath());

                    String imageFileName = imageFile.getName();
                    byte[] imageNameBytes = imageFileName.getBytes();

                    byte[] imageData = new byte[(int) imageFile.length()];
                    imageFileInputStream.read(imageData);

                    // send image name bytes number
                    outputStream.writeInt(imageFileName.length());
                    outputStream.flush();
                    // send image name string in bytes
                    outputStream.write(imageNameBytes);
                    outputStream.flush();

                    // send image bytes number
                    outputStream.writeInt(imageData.length);
                    outputStream.flush();
                    // send image in bytes
                    outputStream.write(imageData);
                    outputStream.flush();

                    System.out.println("Image has been sent");

                    socket.close();
                    outputStream.close();
                    inputStream.close();


                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            } else if (answer == 2) {
                try {

                    System.out.println("Would you like to see the bookings on a specific date?");
                    System.out.println("----------------------------------------------");
                    System.out.println("1.Yes");
                    System.out.println("2.No");
                    System.out.println("----------------------------------------------");

                    int filterDate = scanner.nextInt();


                    System.out.println("Would you like to see the bookings on a specific area?");
                    System.out.println("----------------------------------------------");
                    System.out.println("1.Yes");
                    System.out.println("2.No");
                    System.out.println("----------------------------------------------");

                    int filterArea = scanner.nextInt();


                    // get the ip and port of master from config file
                    Properties appProps = new Properties();
                    appProps.load(new FileInputStream("././././config.properties"));
                    String masterIP = appProps.getProperty("MASTER_IP");
                    int masterPort = Integer.parseInt(appProps.getProperty("MASTER_PORT"));

                    Socket socket = new Socket(masterIP, masterPort);

                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

                    outputStream.writeUTF("MANAGER/SHOW_BOOKINGS");
                    outputStream.flush();


                    LocalDate firstDate = null;
                    LocalDate lastDate = null;
                    String area = "";


                    if(filterDate == 1) {
                        System.out.println("Please type the first date: ");
                        System.out.print("Year: ");
                        int startYear = scanner.nextInt();

                        System.out.print("Month: ");
                        int startMonth = scanner.nextInt();

                        System.out.print("Day: ");
                        int startDay = scanner.nextInt();

                        System.out.println("Please type the last date: ");
                        System.out.print("Year: ");
                        int endYear = scanner.nextInt();

                        System.out.print("Month: ");
                        int endMonth = scanner.nextInt();

                        System.out.print("Day: ");
                        int endDay = scanner.nextInt();

                        firstDate = LocalDate.of(startYear, startMonth, startDay);

                        lastDate = LocalDate.of(endYear, endMonth, endDay);
                    }

                    if(filterArea == 1){
                        System.out.print("Please type that the area of interest: ");
                        scanner.nextLine();
                        area = scanner.nextLine();
                     }

                    outputStream.writeObject(firstDate);
                    outputStream.flush();

                    outputStream.writeObject(lastDate);
                    outputStream.flush();

                    outputStream.writeUTF(area);
                    outputStream.flush();

                    System.out.println("Waiting for results.." + '\n');
                    ArrayList<Booking> bookings = (ArrayList<Booking>) inputStream.readObject();

                    // Send acknowledgment back to the sender
                    outputStream.writeUTF("ACK");
                    outputStream.flush();

                    socket.close();
                    outputStream.close();
                    inputStream.close();

                    if(bookings.isEmpty()){
                        System.out.println("No bookings available at this time");
                        continue;
                    }

                    System.out.println("Here are the bookings: ");
                    int i = 1;


                    for (Booking booking: bookings){
                        System.out.println("----------------------------------------------");
                        System.out.println("Booking " + i);
                        System.out.println("----------------------------------------------");
                        System.out.println(booking.toString());
                        i++;
                    }



                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
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

                    outputStream.writeUTF("MANAGER/SHOW_ROOMS");
                    outputStream.flush();

                    System.out.println("Waiting for results.."+ '\n');
                    ArrayList<Room> rooms = (ArrayList<Room>) inputStream.readObject();

                    // Send acknowledgment back to the sender
                    outputStream.writeUTF("ACK");
                    outputStream.flush();

                    socket.close();
                    outputStream.close();
                    inputStream.close();

                    if(rooms.isEmpty()){
                        System.out.println("No available rooms");
                    }else {
                        System.out.println("Here are the available rooms: ");
                        int i = 1;
                        for (Room room: rooms){
                            System.out.println("----------------------------------------------");
                            System.out.println("Room " + i);
                            System.out.println("----------------------------------------------");
                            System.out.println(room.toString());
                            i++;
                        }
                    }


                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                System.exit(0);
                break;
            }
        }while (true);
    }

}
