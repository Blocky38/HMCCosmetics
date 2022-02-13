import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import io.github.fisher2911.nms.DestroyPacket;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.IntArrayList;

public class DestroyPacket_1_16_R1 implements DestroyPacket {

    @Override
    public PacketContainer get(final int entityId) {
        final PacketContainer destroyPacket = new PacketContainer(
                PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getModifier().write(0, new IntArrayList(new int[]{entityId}));

        return destroyPacket;
    }
}
