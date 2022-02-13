import java.util.HashMap;

public class Switcher {
    public HashMap<Integer, String> menu() {
        HashMap<Integer, String> menu = new HashMap<>();
        menu.put(1, "Enter \"1\" to validate the postcode");

        return menu;
    }
}
