package pl.skidam.automodpack_velocity.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import pl.skidam.automodpack_velocity.Constants;

import java.util.ArrayList;
import java.util.List;

public class VelocityHelper {
    /**
     * Given a hostname, determine the list of backend servers Velocity will attempt to connect them to, in order.
     * <a href="https://github.com/PaperMC/Velocity/blob/dev/3.0.0/proxy/src/main/java/com/velocitypowered/proxy/connection/util/ServerListPingHandler.java#L174">...</a>
     */
    public static List<String> getServersToTry(String hostname) {
        List<String> serversToTry = Constants.proxyServer.getConfiguration().getForcedHosts()
                .getOrDefault(hostname, Constants.proxyServer.getConfiguration().getAttemptConnectionOrder());

        // Return a copy
        return new ArrayList<>(serversToTry);
    }

    /**
     * Whether a DataPacket JSON comes from AutoModpack 5 or newer, which names its
     * fields {@code endpointHost}, {@code endpointPort}, and {@code connectionMode}
     * instead of {@code address}, {@code port}, and {@code requiresMagic}.
     */
    public static boolean isV5DataPacket(JsonObject obj) {
        return obj.has("connectionMode");
    }

    /**
     * Rewrites the endpoint fields of a DataPacket JSON so the client connects to
     * the Velocity AM proxy instead of directly to the backend, and makes the
     * client send the AMMH handshake, which the proxy reads for hostname-based routing.
     */
    public static String rewriteDataPacket(JsonObject obj, String address, int port) {
        String host = port != -1 ? address : "";
        if (isV5DataPacket(obj)) {
            obj.addProperty("endpointHost", host);
            obj.addProperty("endpointPort", port);
            obj.addProperty("connectionMode", "MAGIC");
        } else {
            obj.addProperty("address", host);
            obj.addProperty("port", port);
            obj.addProperty("requiresMagic", true);
        }

        return new Gson().toJson(obj);
    }
}