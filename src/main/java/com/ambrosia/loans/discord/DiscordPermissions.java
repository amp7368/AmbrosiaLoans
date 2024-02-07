package com.ambrosia.loans.discord;

import java.util.List;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DiscordPermissions {

    private static DiscordPermissions instance;
    private final List<Long> employeeRole = List.of();
    private final List<Long> managerRole = List.of();
    private final List<Long> allowedServers = List.of();

    public DiscordPermissions() {
        instance = this;
    }

    public static DiscordPermissions get() {
        return instance;
    }

    public boolean isEmployee(@Nullable Member member) {
        if (member == null) return false;
        return isEmployee(member.getRoles());
    }

    public boolean isEmployee(List<Role> roles) {
        for (Role role : roles) {
            if (employeeRole.contains(role.getIdLong())) return true;
        }
        return false;
    }

    public boolean isManager(@Nullable Member member) {
        if (member == null) return false;
        return isManager(member.getRoles());
    }

    public boolean isManager(List<Role> roles) {
        for (Role role : roles) {
            if (managerRole.contains(role.getIdLong())) return true;
        }
        return false;
    }

    public boolean wrongServer(Guild guild) {
        return guild == null || !allowedServers.contains(guild.getIdLong());
    }

    public void generateWarnings() {
        if (this.employeeRole.isEmpty()) DiscordModule.get().logger().warn(warn("employeeRole"));
        if (this.managerRole.isEmpty()) DiscordModule.get().logger().warn(warn("managerRole"));
        if (this.allowedServers.isEmpty()) DiscordModule.get().logger().warn(warn("allowedServers"));
    }

    @NotNull
    public String warn(String missingField) {
        return missingField + " is not set in /Permissions.config.json";
    }
}
