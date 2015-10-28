import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.Serializable;

public class Commit implements Serializable {

    int id;
    String message;
    String time;
    String branch;

    public Commit(String input, int inputid, String inputbranch) {
        id = inputid;
        message = input;
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        time = dateFormat.format(today);
        branch = inputbranch;
    }

}



