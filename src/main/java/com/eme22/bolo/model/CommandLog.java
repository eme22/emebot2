package com.eme22.bolo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import lombok.*;

@Entity(name = "embot_server_command_log")
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
@Table
public class CommandLog {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "command_log_id", nullable = false)
   private Long id;
   @Column(name = "command_log_command")
   private String command;
   @Column(name = "command_log_arguments", length = 1500)
   private String arguments;
   @Column(name = "command_log_user")
   private String user;
   @Column(name = "command_log_client")
   private String client;
   @Column(name = "command_log_server")
   private String server;
   @Column(name = "command_log_channel")
   private String channel;
   @Column(name = "command_log_time")
   private LocalDateTime time;
}
