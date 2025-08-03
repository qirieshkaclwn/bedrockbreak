package qir.clwn.bedrockbreak.bbmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Bedrockbreak implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("easy-bedrock-breaker");

    private static final ArrayList<Packet<?>> delayedPackets = new ArrayList<>();

    private static KeyBinding activateKey;

    private static boolean delayingPackets = false;
    private static boolean wasKeyPressed = false;

    public static final Class<?>[] blockedPackets = {
            PlayerActionC2SPacket.class,
            PlayerInputC2SPacket.class,
            PlayerInteractBlockC2SPacket.class,
            PlayerInteractItemC2SPacket.class,
            UpdateSelectedSlotC2SPacket.class
    };

    @Override
    public void onInitializeClient() {
        activateKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.qirieshka.delayBlockPackets",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "category.qirieshka.a4fun"
        ));

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (activateKey.isPressed()) {
                if (!wasKeyPressed) {
                    delayingPackets = !delayingPackets;
                    //LOGGER.info("Delaying packets: " + delayingPackets);
                }
                wasKeyPressed = true;
            } else {
                wasKeyPressed = false;
            }

            if (!delayingPackets) {
                releasePackets();
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (isDelayingPackets()) {
                MinecraftClient client = MinecraftClient.getInstance();
                TextRenderer textRenderer = client.textRenderer;
                String text = "Delaying packets (TOGGLE ON)";
                int x = 4;
                int y = drawContext.getScaledWindowHeight() - 4 - textRenderer.fontHeight;

                drawContext.drawText(textRenderer, text, x, y, 0xFFFF5555, true); // Красный цвет
            }
        });

        //LOGGER.info("easy bedrock breaker initialized");
    }

    public static boolean isDelayingPackets() {
        return delayingPackets;
    }

    public static void delayPacket(Packet<?> p) {
        delayedPackets.add(p);
    }

    public static void clearPackets() {
        delayedPackets.clear();
    }

    private void releasePackets() {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            for (Packet<?> packet : delayedPackets) {
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);
            }
        }
        delayedPackets.clear();
    }
}
