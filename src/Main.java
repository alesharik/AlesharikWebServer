import com.alesharik.webserver.logger.Logger;
import com.alesharik.webserver.plugin.PluginCore;

public class Main extends PluginCore {
    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void run() {
        Logger.log("e");
    }
}
