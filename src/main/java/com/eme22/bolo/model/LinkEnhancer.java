package com.eme22.bolo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Generated;

@Entity(name = "embot_server_link_enhancer")
public class LinkEnhancer {
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "link_enhancer_id", nullable = false)
   private Long id;
   @Column(name = "link_enhancer_serverid")
   private Long server;
   @Column(name = "link_enhancer_link_regex")
   private String linkEnhancerLinkRegex;
   @Column(name = "link_enhancer_enhancer_regex")
   private String linkEnhancerEnhancerRegex;
   @Column(name = "link_enhancer_replacement")
   private String linkEnhancerReplacement;

   @Generated
   public Long getId() {
      return this.id;
   }

   @Generated
   public Long getServer() {
      return this.server;
   }

   @Generated
   public String getLinkEnhancerLinkRegex() {
      return this.linkEnhancerLinkRegex;
   }

   @Generated
   public String getLinkEnhancerEnhancerRegex() {
      return this.linkEnhancerEnhancerRegex;
   }

   @Generated
   public String getLinkEnhancerReplacement() {
      return this.linkEnhancerReplacement;
   }

   @Generated
   public void setId(final Long id) {
      this.id = id;
   }

   @Generated
   public void setServer(final Long server) {
      this.server = server;
   }

   @Generated
   public void setLinkEnhancerLinkRegex(final String linkEnhancerLinkRegex) {
      this.linkEnhancerLinkRegex = linkEnhancerLinkRegex;
   }

   @Generated
   public void setLinkEnhancerEnhancerRegex(final String linkEnhancerEnhancerRegex) {
      this.linkEnhancerEnhancerRegex = linkEnhancerEnhancerRegex;
   }

   @Generated
   public void setLinkEnhancerReplacement(final String linkEnhancerReplacement) {
      this.linkEnhancerReplacement = linkEnhancerReplacement;
   }

   @Generated
   @Override
   public String toString() {
      return "LinkEnhancer(id="
            + this.getId()
            + ", server="
            + this.getServer()
            + ", linkEnhancerLinkRegex="
            + this.getLinkEnhancerLinkRegex()
            + ", linkEnhancerEnhancerRegex="
            + this.getLinkEnhancerEnhancerRegex()
            + ", linkEnhancerReplacement="
            + this.getLinkEnhancerReplacement()
            + ")";
   }

   @Generated
   public LinkEnhancer() {
   }

   @Generated
   public LinkEnhancer(
         final Long id, final Long server, final String linkEnhancerLinkRegex, final String linkEnhancerEnhancerRegex,
         final String linkEnhancerReplacement) {
      this.id = id;
      this.server = server;
      this.linkEnhancerLinkRegex = linkEnhancerLinkRegex;
      this.linkEnhancerEnhancerRegex = linkEnhancerEnhancerRegex;
      this.linkEnhancerReplacement = linkEnhancerReplacement;
   }
}
