package com.eme22.bolo.commands;

import jakarta.transaction.Transactional;
import jakarta.enterprise.context.control.ActivateRequestContext;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
@Transactional
@ActivateRequestContext
public abstract class BaseCommand extends SlashCommand {



}






