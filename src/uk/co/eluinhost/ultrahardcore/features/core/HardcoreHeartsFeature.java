package uk.co.eluinhost.ultrahardcore.features.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.events.PacketAdapter;
import uk.co.eluinhost.ultrahardcore.UltraHardcore;
import uk.co.eluinhost.ultrahardcore.features.UHCFeature;

public class HardcoreHeartsFeature extends UHCFeature {

    private final ProtocolManager m_manager = ProtocolLibrary.getProtocolManager();
    private final PacketListener m_packetAdapter = new HardcoreHeartsListener();

    public HardcoreHeartsFeature() {
        super("HardcoreHearts");
        setDescription("Shows the hardcore hearts instead");
    }

    private static class HardcoreHeartsListener extends PacketAdapter {

        HardcoreHeartsListener(){
            //listen for login packets on the normal priority
            super(UltraHardcore.getInstance(),ListenerPriority.NORMAL,PacketType.Play.Server.LOGIN);
        }

        @Override
        public void onPacketSending(PacketEvent event) {
            //if its a login packet write the first boolean to true (hardcore flag)
            if (event.getPacketType().equals(PacketType.Play.Server.LOGIN)) {
                event.getPacket().getBooleans().write(0, true);
            }
        }
    }
}
