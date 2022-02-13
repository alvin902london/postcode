import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class Postcode {
    /*
    Time spent: approx 4 hours

    Assumptions:
    The application should be able to at least call ALL three api endpoints of interest:
    GET /postcodes/{POSTCODE}/
    GET /postcodes/{POSTCODE}/validate
    GET /postcodes/{POSTCODE}/nearest

    Improvements to make for scalability if there was more time:
    1) Make the main/start method modular
    2) Create a switcher class to store potentially growing switch cases (i.e. functionalities)
    and dynamically iterate them as well as update their corresponding menu options/validation.
    Something like:

    interface Command {
        void execute();
    }

    class switcher {
        private Map<Integer, Command>
        private Command;
        public void addCaseCommand()
        public void setDefaultCaseCommand()
        public void on()
    }

     */
    private final Scanner scanner;
    private final PrintStream printStream;

    public Postcode(InputStream inputStream, PrintStream printStream) {
        this.scanner = new Scanner(inputStream);
        this.printStream = printStream;
    }

    public static void main(String[] args) {
        Postcode postcode = new Postcode(System.in, System.out);
        postcode.start();
    }

    public void start() {
        int option;
        long status;
        int modeOn = 1;
        String postcode;
        JSONObject data;
        Switcher menu = new Switcher();
        //Console interface
        while (modeOn == 1) {
            //Loop until server returns 200
            do {

                //Get postcode input
                do {
                    printStream.println("Enter postcode:");

                    while (!scanner.hasNextLine()) {
                        scanner.nextLine();
                    }
                    postcode = scanner.nextLine();
                    //"^[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][ABD-HJLNP-UW-Z]{2}$" optional: validation for full postcode
                } while (!postcode.matches("[a-zA-Z0-9.? ]*")); // filter out special characters

                //Get option
                do {
//                    HashMap<Integer, String> choices = menu.menu();
//                    for (HashMap.Entry<Integer, String> entry : choices.entrySet()) {
//                        printStream.println(entry);
//                    }
                    printStream.println("Choose an option:");
                    printStream.println("Enter \"1\" to validate the postcode");
                    printStream.println("Enter \"2\" to lookup the postcode");
                    printStream.println("Enter \"3\" to lookup the nearest postcodes for the postcode");

                    while (!scanner.hasNextInt()) {
                        scanner.next();
                    }
                    option = scanner.nextInt();
                } while (!(option > 0 && option < 4));

                //API call
                data = query(postcode, option);
                status = (long) data.get("status");

                //Invalid postcodes/queries produce an error message
                if (status != 200L) {
                    printStream.println("Error status: " + status);
                    printStream.println(data.get("error") + ". Please try again.");
                }
                scanner.nextLine();
            } while (status != 200L);
            //Display results
            switch (option) {
                case 1:
                    validate(data);
                    break;
                case 2:
                    //Print the country and region for that postcode
                    getInfo(data);
                    break;
                case 3:
                    getNearestInfo(data);
                    break;
            }

            //Terminate the service or start new query
            do {
                printStream.println("End of Enquiry. Enter \"1\" to check another postcode, or enter \"2\" to exit");

                while (!scanner.hasNextInt()) {
                    scanner.next();
                }
                modeOn = scanner.nextInt();

            } while (modeOn != 1 && modeOn != 2);

            scanner.nextLine();
        }
    }

    public void validate(JSONObject data) {
        if ((boolean) data.get("result")) {
            printStream.println("The postcode is valid.");
        } else {
            printStream.println("The postcode is invalid.");
        }
    }

    public void getInfo(JSONObject data) {
        JSONObject result = (JSONObject) data.get("result");
        printStream.println("Here is some information regarding the postcode " + result.get("postcode") + ": ");
        printStream.println("Country: " + result.get("country"));
        printStream.println("Region: " + result.get("region"));
        printStream.println(" ");
    }

    public void getNearestInfo(JSONObject data) {
        JSONArray results = (JSONArray) data.get("result");

        for (int i = 0; i < results.size(); ++i) {
            JSONObject result = (JSONObject) results.get(i);
            if (i == 0) {
                printStream.println("The nearest postcodes for the postcode " + result.get("postcode") + " is: ");
            } else {
                printStream.println("===========");
                printStream.println("Postcode: " + result.get("postcode"));
                printStream.println("Country: " + result.get("country"));
                printStream.println("Region: " + result.get("region"));
            }
        }
    }

    //Utilities
    public JSONObject query(String postcode, int option) {
        String apiAddress = "https://api.postcodes.io";
        String urlAddress;
        switch (option) {
            case 1:
                urlAddress = apiAddress + "/postcodes/" + postcode + "/validate";
                break;
            case 2:
                urlAddress = apiAddress + "/postcodes/" + postcode;
                break;
            case 3:
                urlAddress = apiAddress + "/postcodes/" + postcode + "/nearest";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + option);
        }
        return apiConn(urlAddress);
    }

    public JSONObject apiConn(String urlAddress) {
        BufferedReader reader;
        String line;
        //Use StringBuffer as the app scales up to make sure it's thread safe
        StringBuilder responseContent = new StringBuilder();
        HttpURLConnection conn;
        try {
            URL url = new URL(urlAddress);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.connect();
            //Check if connect is made
            int responseCode = conn.getResponseCode();

            if (responseCode > 299) {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            }
            while((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();

            //JSON simple library Setup with Maven to convert strings to JSON
            JSONParser parse = new JSONParser();

            return (JSONObject) parse.parse(String.valueOf(responseContent));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
