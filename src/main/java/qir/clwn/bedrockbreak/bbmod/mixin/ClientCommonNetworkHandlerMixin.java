package qir.clwn.bedrockbreak.bbmod.mixin;
import qir.clwn.bedrockbreak.bbmod.Bedrockbreak;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void delayPackets(Packet<?> packet, CallbackInfo ci) {
        if (Bedrockbreak.isDelayingPackets() && Arrays.stream(Bedrockbreak.blockedPackets).anyMatch(c -> c.isInstance(packet))) {
            Bedrockbreak.delayPacket(packet);
            ci.cancel();
        }
    }

}