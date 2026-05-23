package com.eme22.bolo.controller;

import com.eme22.bolo.Bot;
import com.eme22.bolo.model.ServerStats;
import com.eme22.bolo.model.Stats;
import com.eme22.bolo.model.ServerStat;
import com.eme22.bolo.stats.StatsService;
import com.eme22.bolo.repository.ServerStatRepository;
import com.eme22.bolo.repository.StatsRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {

    private final StatsService statsService;
    private final ServerStatRepository serverStatRepository;
    private final StatsRepository statsRepository;
    private final Bot bot;

    @Inject
    public StatsResource(StatsService statsService, ServerStatRepository serverStatRepository, StatsRepository statsRepository, Bot bot) {
        this.statsService = statsService;
        this.serverStatRepository = serverStatRepository;
        this.statsRepository = statsRepository;
        this.bot = bot;
    }

    @GET
    @Path("/server/{id}")
    public ServerStats getServerStats(@PathParam("id") Long id) {
        return statsService.getServerStat(id).orElse(null);
    }

    @GET
    @Path("/server/all")
    public List<ServerStats> getAllServersStats() {
        List<ServerStat> allStats = serverStatRepository.listAll();
        Map<Long, ServerStats> map = new HashMap<>();
        for (ServerStat stat : allStats) {
            ServerStats dto = map.computeIfAbsent(stat.getServerId(), id -> {
                ServerStats s = new ServerStats();
                s.setId(id);
                return s;
            });
            String name = stat.getStatName();
            long value = stat.getValue();
            switch (name) {
                case "COMMANDS_USED" -> dto.setCommandsUsed(value);
                case "IMAGES_SEND" -> dto.setImagesSend(value);
                case "MEMES_SEND" -> dto.setMemesSend(value);
                case "SONGS_PLAYED" -> dto.setSongsPlayed(value);
                case "ANAL" -> dto.setAnal(value);
                case "KISS" -> dto.setKisses(value);
                case "SLAPS" -> dto.setSlaps(value);
                case "POKES" -> dto.setPoke(value);
                case "BITES" -> dto.setBite(value);
                case "LICKS" -> dto.setLick(value);
                case "FUCKS" -> dto.setFuck(value);
                case "CUMS" -> dto.setCum(value);
            }
        }

        // Merge active buffered stats
        for (Long guildId : statsService.getServerBuffer().keySet()) {
            ServerStats dto = map.computeIfAbsent(guildId, id -> {
                ServerStats s = new ServerStats();
                s.setId(id);
                return s;
            });
            ConcurrentHashMap<String, LongAdder> increments = statsService.getServerBuffer().get(guildId);
            if (increments != null) {
                dto.setCommandsUsed(dto.getCommandsUsed() + getBufferedValue(increments, "COMMANDS_USED"));
                dto.setImagesSend(dto.getImagesSend() + getBufferedValue(increments, "IMAGES_SEND"));
                dto.setMemesSend(dto.getMemesSend() + getBufferedValue(increments, "MEMES_SEND"));
                dto.setSongsPlayed(dto.getSongsPlayed() + getBufferedValue(increments, "SONGS_PLAYED"));
                dto.setAnal(dto.getAnal() + getBufferedValue(increments, "ANAL"));
                dto.setKisses(dto.getKisses() + getBufferedValue(increments, "KISS"));
                dto.setSlaps(dto.getSlaps() + getBufferedValue(increments, "SLAPS"));
                dto.setPoke(dto.getPoke() + getBufferedValue(increments, "POKES"));
                dto.setBite(dto.getBite() + getBufferedValue(increments, "BITES"));
                dto.setLick(dto.getLick() + getBufferedValue(increments, "LICKS"));
                dto.setFuck(dto.getFuck() + getBufferedValue(increments, "FUCKS"));
                dto.setCum(dto.getCum() + getBufferedValue(increments, "CUMS"));
            }
        }

        return new ArrayList<>(map.values());
    }

    private long getBufferedValue(ConcurrentHashMap<String, LongAdder> increments, String key) {
        LongAdder adder = increments.get(key);
        return adder != null ? adder.sum() : 0L;
    }

    @GET
    @Path("/global/all")
    public List<Stats> getGlobalStats() {
        List<Stats> dbStats = statsService.getGlobalStats();
        
        long guildsCount = 0;
        long usersCount = 0;
        long commandsCount = 0;

        if (bot != null && bot.getJDA() != null) {
            try {
                guildsCount = bot.getJDA().getGuilds().size();
                usersCount = bot.getJDA().getGuilds().stream().mapToLong(Guild::getMemberCount).sum();
            } catch (Exception e) {
                // Ignore JDA errors
            }
        }

        // Find COMMANDS_USED value in database
        for (Stats s : dbStats) {
            if ("COMMANDS_USED".equalsIgnoreCase(s.getName())) {
                commandsCount = s.getValue() != null ? s.getValue() : 0;
                break;
            }
        }

        List<Stats> result = new ArrayList<>();
        
        // Add dynamic stats expected by the Angular frontend
        result.add(new Stats("guilds", guildsCount));
        result.add(new Stats("users", usersCount));
        result.add(new Stats("commands", commandsCount));

        // Add all database stats as well
        result.addAll(dbStats);

        return result;
    }

    @GET
    @Path("/global/{name}")
    public Stats getGlobalStatsByName(@PathParam("name") String name) {
        return statsRepository.findByIdOptional(name).orElse(null);
    }
}
