import service.FrontService;
import service.ResourceDataService;
import service.ResourceMetaDataService;
import service.Server;

import java.io.IOException;

/**
 * Created by vojta on 12/01/16.
 */
public class Main {
    public static void main(String[] args) {
        int count = 1;

        for (int i = 0; i < count; i++) {
            final int j = 7890 + i;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new Server(j, new FrontService(new ResourceDataService(), new ResourceMetaDataService())).start();
                    } catch (IOException e) {
                        // TODO
                    }
                }
            }).start();
        }
    }
}
