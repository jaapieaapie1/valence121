package rs.valence.extractor.extractors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import rs.valence.extractor.Main;

/**
 * Walks every protocol template registered with {@link ProtocolInfoBuilder} and
 * dumps the (phase, side, name, id) tuple of every packet so that
 * {@code valence_generated} can rebuild its {@code packet_id::*} constants.
 *
 * <p>Replaces the pre-26.1 implementation that iterated the old yarn
 * {@code NetworkState} / {@code PlayStateFactories} API. The Mojang-mapped
 * 26.1 sources expose this through {@link ProtocolInfo.Details#listPackets}.
 */
public class Packets implements Main.Extractor {

    @Override
    public String fileName() {
        return "packets.json";
    }

    @Override
    public JsonElement extract() {
        // Both SimpleUnboundProtocol and UnboundProtocol extend
        // ProtocolInfo.DetailsProvider, so we can treat them uniformly.
        List<ProtocolInfo.DetailsProvider> templates = List.of(
            HandshakeProtocols.SERVERBOUND_TEMPLATE,
            StatusProtocols.SERVERBOUND_TEMPLATE,
            StatusProtocols.CLIENTBOUND_TEMPLATE,
            LoginProtocols.SERVERBOUND_TEMPLATE,
            LoginProtocols.CLIENTBOUND_TEMPLATE,
            ConfigurationProtocols.SERVERBOUND_TEMPLATE,
            ConfigurationProtocols.CLIENTBOUND_TEMPLATE,
            GameProtocols.SERVERBOUND_TEMPLATE,
            GameProtocols.CLIENTBOUND_TEMPLATE
        );

        JsonArray out = new JsonArray();

        for (ProtocolInfo.DetailsProvider provider : templates) {
            ProtocolInfo.Details details = provider.details();
            // ConnectionProtocol#id() returns the lowercase phase string
            // ("handshake", "status", "login", "configuration", "play").
            String phase = details.id().id();
            PacketFlow flow = details.flow();
            String side =
                flow == PacketFlow.SERVERBOUND
                    ? "serverbound"
                    : "clientbound";

            details.listPackets((type, id) -> {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", type.id().getPath());
                obj.addProperty("phase", phase);
                obj.addProperty("side", side);
                obj.addProperty("id", id);
                out.add(obj);
            });
        }

        return out;
    }
}
