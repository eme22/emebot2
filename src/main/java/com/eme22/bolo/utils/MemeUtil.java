package com.eme22.bolo.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

public class MemeUtil {
   private final Font font;
   private static final int width = 1000;
   private static final int height = 500;
   private final BufferedImage memeBaseImage;
   private final BufferedImage memeImage;
   private final Graphics2D g2d;

   public MemeUtil(BufferedImage image) {
      this.memeBaseImage = image;
      this.memeImage = new BufferedImage(1000, 500, 2);
      this.g2d = this.memeImage.createGraphics();
      this.font = new Font("Impact", 1, 50);
   }

   public BufferedImage generateMeme(String textoSuperior, String textoInferior) {
      this.g2d.drawImage(Scalr.resize(this.memeBaseImage, Method.AUTOMATIC, 0, 500, new BufferedImageOp[0]), 0, 0, null);
      this.g2d.setFont(this.font);
      this.g2d.setColor(Color.WHITE);
      int anchoTextoSuperior = this.g2d.getFontMetrics().stringWidth(textoSuperior);
      this.g2d.drawString(textoSuperior, (1000 - anchoTextoSuperior) / 2, 50);
      int anchoTextoInferior = this.g2d.getFontMetrics().stringWidth(textoInferior);
      this.g2d.drawString(textoInferior, (1000 - anchoTextoInferior) / 2, 460);
      return this.memeImage;
   }
}
