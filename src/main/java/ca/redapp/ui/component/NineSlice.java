/***********************************************************************
 * REDapp - NineSlice.java
 * Copyright (C) 2015-2019 The REDapp Development Team
 * Homepage: http://redapp.org
 * 
 * REDapp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * REDapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REDapp. If not see <http://www.gnu.org/licenses/>. 
 **********************************************************************/

package ca.redapp.ui.component;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * A nine-slice image. Scaling only happens within a predefined region. Can also be used
 * with images that have no nine-slice information available.
 * 
 * In order to use the nine-slice capabilities you must specify an amd file instead of the
 * image file itself. The file can contain comments on lines starting with '#'. A line
 * for the image file name must start with 'source:' followed by the filename and a line
 * for the margins must start with 'slicemargins:' followed by the margins in the format
 * left right top bottom. An example is provided.
 * 
 * {@code
 * # this line is just a comment.
 * source: picture.png
 * slicemargins: 10 10 5 5
 * }
 * 
 * @author Travis Redpath
 */
public class NineSlice implements Icon {
	private BufferedImage image;
	private int l, r, t, b;
	private int width, height;

	/**
	 * Construct a new nine-slice image. <code>resource</code> can be either
	 * the name of the image file itself (no nine-slice scaling will be applied)
	 * or an amd file that specifies the nine-slice margins.
	 * 
	 * @param resource A path to either an image or amd file.
	 */
	public NineSlice(String resource) {
		BufferedReader br = null;
		try {
			if (resource.endsWith(".amd")) {
				br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(resource)));
				String line;
				while ((line = br.readLine()) != null) {
					if (line.startsWith("#"))
						continue;
					else if (line.toLowerCase().startsWith("source:")) {
						String split[] = line.split(":");
						int ind = resource.lastIndexOf('/');
						String path = resource.substring(0, ind + 1);
						path += split[1].replace('"', ' ').trim();
						image = ImageIO.read(getClass().getResourceAsStream(path));
					}
					else if (line.toLowerCase().startsWith("slicemargins:")) {
						String split[] = line.split(":");
						String split2[] = split[1].trim().split(" ");
						l = Integer.parseInt(split2[0]);
						r = Integer.parseInt(split2[1]);
						t = Integer.parseInt(split2[2]);
						b = Integer.parseInt(split2[3]);
					}
				}
				br.close();
			}
			else {
				image = ImageIO.read(getClass().getResourceAsStream(resource));
			}
		}
		catch (IOException e) {
			if (br != null) {
				try {
					br.close();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Returns the icon's width.
	 * 
	 * @return An int specifying the width of the icon.
	 */
	@Override
	public int getIconWidth() {
		return width;
	}

	/**
	 * Returns the icon's height.
	 * 
	 * @return An int specifying the height of the icon.
	 */
	@Override
	public int getIconHeight() {
		return height;
	}

	/**
	 * Paints the icon. The top-left corner of the icon is drawn at the point <code>(x, y)</code> in
	 * the coordinate space of the graphics context g.
	 */
	@Override
	public void paintIcon(Component cmp, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Insets i = ((JComponent)cmp).getInsets();

		int iw = image.getWidth(cmp);
		int ih = image.getHeight(cmp);
		width = cmp.getWidth() - i.left - i.right;
		height = cmp.getHeight() - i.top - i.bottom;

		g2.drawImage(image.getSubimage(l, t, iw - l - r, ih - t - b), l, t, width - l - r, height - t - b, cmp);

		if (l > 0 && r > 0 && t > 0 && b > 0) {
			g2.drawImage(image.getSubimage(l, 0, iw - l - r, t), l, 0, width - l - r, t, cmp);
			g2.drawImage(image.getSubimage(l, ih - b, iw - l - r, b), l, height - b, width - l - r, b, cmp);
			g2.drawImage(image.getSubimage(0, t, l, ih - t - b), 0, t, l, height - t - b, cmp);
			g2.drawImage(image.getSubimage(iw - r, t, r, ih - t - b), width - r, t, r, height - t - b, cmp);

			g2.drawImage(image.getSubimage(0, 0, l, t), 0, 0, cmp);
			g2.drawImage(image.getSubimage(iw - r, 0, r, t), width - r, 0, cmp);
			g2.drawImage(image.getSubimage(0, ih - b, l, b), 0, height - b, cmp);
			g2.drawImage(image.getSubimage(iw - r, ih - b, r, b), width - r, height - b, cmp);
		}
		g2.dispose();
	}
}
