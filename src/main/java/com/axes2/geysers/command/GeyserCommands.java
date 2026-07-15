package com.axes2.geysers.command;

import java.util.Arrays;

import com.axes2.geysers.Geysers;
import com.axes2.geysers.block.entity.GeyserVentBlockEntity;
import com.axes2.geysers.eruption.GeyserPhase;
import com.axes2.geysers.eruption.GeyserStyles;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Op-only debug driver for eruptions. Without worldgen or physics this is how a vent is
 * summoned into motion and tuned by hand. Operates on the nearest vent to the command
 * source (within {@link #SEARCH_RADIUS} blocks).
 */
@EventBusSubscriber(modid = Geysers.MODID)
public final class GeyserCommands {
    private static final int SEARCH_RADIUS = 8;

    private GeyserCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(build());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("geyser").requires(source -> source.hasPermission(2))
                .then(Commands.literal("start").executes(ctx -> withVent(ctx, be -> {
                    be.trigger(gameTime(ctx));
                    success(ctx, "Eruption triggered.");
                })))
                .then(Commands.literal("reset").executes(ctx -> withVent(ctx, be -> {
                    be.reset(gameTime(ctx));
                    success(ctx, "Reset to dormant.");
                })))
                .then(Commands.literal("info").executes(ctx -> withVent(ctx, be -> {
                    long gt = gameTime(ctx);
                    success(ctx, String.format("phase=%s intensity=%.3f style=%s%s",
                            be.phase().getSerializedName(), be.intensity(gt), be.styleId(),
                            be.isManual() ? " (manual)" : ""));
                })))
                .then(Commands.literal("intensity")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 1f))
                                .executes(ctx -> withVent(ctx, be -> {
                                    be.setManualIntensity(FloatArgumentType.getFloat(ctx, "value"));
                                    success(ctx, "Manual intensity pinned.");
                                }))))
                .then(Commands.literal("auto").executes(ctx -> withVent(ctx, be -> {
                    be.clearManual(gameTime(ctx));
                    success(ctx, "Manual override cleared; cycle resumed.");
                })))
                .then(Commands.literal("phase")
                        .then(Commands.argument("phase", StringArgumentType.word())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(GeyserPhase.values()).map(GeyserPhase::getSerializedName), b))
                                .executes(ctx -> withVent(ctx, be -> {
                                    GeyserPhase phase = GeyserPhase.byName(StringArgumentType.getString(ctx, "phase"));
                                    if (phase == null) {
                                        ctx.getSource().sendFailure(Component.literal("Unknown phase."));
                                        return;
                                    }
                                    be.forcePhase(phase, gameTime(ctx));
                                    success(ctx, "Forced phase " + phase.getSerializedName() + ".");
                                }))))
                .then(Commands.literal("style")
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .suggests((c, b) -> SharedSuggestionProvider.suggestResource(GeyserStyles.ids(), b))
                                .executes(ctx -> withVent(ctx, be -> {
                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                    if (be.setStyle(id)) {
                                        success(ctx, "Style set to " + id + ".");
                                    } else {
                                        ctx.getSource().sendFailure(Component.literal("Unknown style: " + id));
                                    }
                                }))));
    }

    private interface VentAction {
        void run(GeyserVentBlockEntity vent);
    }

    private static int withVent(CommandContext<CommandSourceStack> ctx, VentAction action) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        GeyserVentBlockEntity vent = findNearestVent(level, BlockPos.containing(pos));
        if (vent == null) {
            source.sendFailure(Component.literal("No geyser vent within " + SEARCH_RADIUS + " blocks."));
            return 0;
        }
        action.run(vent);
        return 1;
    }

    private static long gameTime(CommandContext<CommandSourceStack> ctx) {
        return ctx.getSource().getLevel().getGameTime();
    }

    private static void success(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSuccess(() -> Component.literal("[Geyser] " + message), false);
    }

    private static GeyserVentBlockEntity findNearestVent(ServerLevel level, BlockPos center) {
        GeyserVentBlockEntity best = null;
        double bestDistSqr = Double.MAX_VALUE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
            for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
                for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockEntity be = level.getBlockEntity(cursor);
                    if (be instanceof GeyserVentBlockEntity vent) {
                        double distSqr = center.distSqr(cursor);
                        if (distSqr < bestDistSqr) {
                            bestDistSqr = distSqr;
                            best = vent;
                        }
                    }
                }
            }
        }
        return best;
    }
}
