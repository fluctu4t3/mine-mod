package com.example.teammod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod("teammod")
public class TeamMod {
    public static final String MOD_ID = "teammod";

    private static final Map<String, Set<PlayerEntity>> teams = new HashMap<>();
    private static final Map<String, Integer> scores = new HashMap<>();

    public TeamMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        event.getServer().getCommandManager().getDispatcher().register(
            net.minecraft.command.Commands.literal("team")
                .then(net.minecraft.command.Commands.literal("create")
                    .then(net.minecraft.command.Commands.argument("teamName", net.minecraft.command.arguments.StringArgumentType.string())
                        .executes(context -> {
                            String teamName = net.minecraft.command.arguments.StringArgumentType.getString(context, "teamName");
                            if (!teams.containsKey(teamName)) {
                                teams.put(teamName, new HashSet<>());
                                scores.put(teamName, 0);
                                context.getSource().sendFeedback(new StringTextComponent("Team " + teamName + " created!"), true);
                            } else {
                                context.getSource().sendFeedback(new StringTextComponent("Team already exists!"), false);
                            }
                            return 1;
                        })
                    )
                )
                .then(net.minecraft.command.Commands.literal("add")
                    .then(net.minecraft.command.Commands.argument("teamName", net.minecraft.command.arguments.StringArgumentType.string())
                        .then(net.minecraft.command.Commands.argument("playerName", net.minecraft.command.arguments.EntityArgument.player())
                            .executes(context -> {
                                String teamName = net.minecraft.command.arguments.StringArgumentType.getString(context, "teamName");
                                PlayerEntity player = net.minecraft.command.arguments.EntityArgument.getPlayer(context, "playerName");
                                if (teams.containsKey(teamName)) {
                                    teams.get(teamName).add(player);
                                    context.getSource().sendFeedback(new StringTextComponent("Added " + player.getName().getString() + " to " + teamName), true);
                                } else {
                                    context.getSource().sendFeedback(new StringTextComponent("Team does not exist!"), false);
                                }
                                return 1;
                            })
                        )
                    )
                )
                .then(net.minecraft.command.Commands.literal("score")
                    .then(net.minecraft.command.Commands.argument("teamName", net.minecraft.command.arguments.StringArgumentType.string())
                        .executes(context -> {
                            String teamName = net.minecraft.command.arguments.StringArgumentType.getString(context, "teamName");
                            if (scores.containsKey(teamName)) {
                                int score = scores.get(teamName);
                                context.getSource().sendFeedback(new StringTextComponent("Team " + teamName + " has a score of: " + score), true);
                            } else {
                                context.getSource().sendFeedback(new StringTextComponent("Team does not exist!"), false);
                            }
                            return 1;
                        })
                    )
                )
        );
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        PlayerEntity player = event.getPlayer();
        String teamName = getTeamOfPlayer(player);
        if (teamName != null) {
            boolean allDead = teams.get(teamName).stream().allMatch(p -> !p.isAlive());
            if (allDead) {
                scores.put(teamName, scores.getOrDefault(teamName, 0) + 1);
                player.getServer().getPlayerList().sendMessage(new StringTextComponent("Team " + teamName + " gained a point!"));
            }
        }
    }

    private String getTeamOfPlayer(PlayerEntity player) {
        for (Map.Entry<String, Set<PlayerEntity>> entry : teams.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return null;
    }
                                  }
