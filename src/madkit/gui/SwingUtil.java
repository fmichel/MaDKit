/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;


/**
 * This class provides some utilities for building
 * swing components.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.16
 * @version 0.9
 * 
 */
public class SwingUtil {
	
	/**
	 * Creates a labeled panel containing a slider 
	 * 
	 * @param slider
	 * @param label
	 * @return a panel for the slider
	 */
	public static JPanel createSliderPanel(final JSlider slider, String label) {
		final JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(new TitledBorder(label));
		p.setPreferredSize(new Dimension(170, 60));
		p.add(slider);
		return p;
	}

	/**
	 * Creates a labeled panel containing a slider built
	 * using an existing {@link DefaultBoundedRangeModel}
	 * 
	 * @param model
	 * @param label
	 * @return a panel for this model
	 */
	public static JPanel createSliderPanel(final DefaultBoundedRangeModel model, String label) {
		return createSliderPanel(createJSlider(model), label);
	}

	/**
	 * Creates a JSlider built
	 * using a {@link DefaultBoundedRangeModel} and 
	 * containing a {@link MouseWheelListener} 
	 * and some usual default settings
	 * 
	 * @param model
	 * @return the corresponding {@link JSlider}
	 */
	public static JSlider createJSlider(final DefaultBoundedRangeModel model) {
		final JSlider slider = new JSlider(model);
		slider.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				slider.setValue(-e.getWheelRotation() + slider.getValue());
			}
		});
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setMajorTickSpacing(model.getMaximum()/2);
		slider.setMinorTickSpacing(model.getExtent());
		slider.setSnapToTicks(true);
		return slider;
	}

}
