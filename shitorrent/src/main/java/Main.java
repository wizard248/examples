import service.FrontService;
import service.ResourceDataService;
import service.ResourceMetaDataService;
import service.Server;
import service.WorkerSchedulingService;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by vojta on 12/01/16.
 */
public class Main {
    public static void main(String[] args) {
        int count = 2;

        for (int i = 0; i < count; i++) {
            final int j = 7890 + i;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ResourceDataService resourceDataService = new ResourceDataService();
                        final ResourceMetaDataService resourceMetaDataService = new ResourceMetaDataService();
                        final WorkerSchedulingService workerSchedulingService = new WorkerSchedulingService();
                        final FrontService frontService = new FrontService(resourceDataService, resourceMetaDataService, workerSchedulingService);
                        if (j == 7890) {
                            frontService.seed(Paths.get("/Users/vojta/Downloads/inno.jpg"));
                        } else {
                            frontService.seed(Paths.get("/Users/vojta/Downloads/inno2.jpg"));
                        }
                        new Server(j, frontService).start();

                    } catch (IOException e) {
                        // TODO
                    }
                }
            }).start();
        }
    }
}
