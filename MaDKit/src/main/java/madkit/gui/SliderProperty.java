/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.gui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.controlsfx.control.PropertySheet;

/**
 * Annotation to specify which fields, among those annotated with {@link UIProperty},
 * should be displayed as sliders when using {@link PropertySheetFactory} methods to
 * create a {@link PropertySheet}
 * 
 * Sliders from JavaFX only work with double values, so the annotated field must be of
 * type double.
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SliderProperty {
	/**
	 * The minimum value of the slider
	 * 
	 * @return the minimum value
	 */
	public double min() default Double.MIN_VALUE;

	/**
	 * The maximum value of the slider
	 * 
	 * @return the maximum value
	 */
	public double max() default Double.MAX_VALUE;

	/**
	 * The precision of the slider when using the mouse wheel. Default is 1 unit.
	 * 
	 * @return the precision of the scrolling
	 */
	public double scrollPrecision() default 1;

	/**
	 * Specifies the display name for this slider. If not specified, the field name will be
	 * used.
	 * 
	 * @return the display name
	 */
	String displayName() default "";

	/**
	 * Specifies the category of the parameter.
	 * 
	 * @return the category of the parameter
	 */
	String category() default "";

}